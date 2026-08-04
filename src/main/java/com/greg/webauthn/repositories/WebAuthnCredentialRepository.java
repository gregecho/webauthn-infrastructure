package com.greg.webauthn.repositories;

import com.greg.webauthn.model.domain.WebAuthnCredentials;
import com.greg.webauthn.services.ICredentialStoreService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.Base64UrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.greg.webauthn.exception.WebAuthnErrorsEnum.*;

/**
 * Bridges the Yubico {@link CredentialRepository} interface and
 * {@link ICredentialStoreService} (JPA).
 *
 * <p>Ported from {@code com.hyatt.webauthn.repositories.WebAuthnCredentialRepository}.
 * The only change is the injected dependency: {@link ICredentialStoreService}
 * instead of {@code ISvcPaiIdpService}.
 */
@Repository
public class WebAuthnCredentialRepository implements CredentialRepository {

    private static final Logger LOG = LoggerFactory.getLogger(WebAuthnCredentialRepository.class);

    @Autowired
    private ICredentialStoreService store;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return store.getCredentialsByMemberId(username).stream()
                .map(cred -> {
                    try {
                        return PublicKeyCredentialDescriptor.builder()
                                .id(ByteArray.fromBase64Url(cred.getCredentialId()))
                                .build();
                    } catch (Base64UrlException e) {
                        LOG.error("Invalid Base64Url credentialId for memberId={}: ", username, e.getMessage());
                        ASSERTION_FAILED.throwException(e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return Optional.of(new ByteArray(username.getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.of(new String(userHandle.getBytes()));
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return lookupAll(credentialId).stream().findFirst();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        Set<RegisteredCredential> result = new HashSet<>();
        WebAuthnCredentials cred = store.getCredentialById(credentialId.getBase64Url());
        if (cred == null) {
            return result;
        }
        try {
            result.add(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url(cred.getCredentialId()))
                    .userHandle(new ByteArray(cred.getMemberId().getBytes()))
                    .publicKeyCose(ByteArray.fromBase64Url(cred.getPublicKeyCose()))
                    .signatureCount(cred.getCount())
                    .build());
        } catch (Base64UrlException e) {
            REGISTRATION_FAILED.throwException(e.getMessage());
        }
        return result;
    }
}
