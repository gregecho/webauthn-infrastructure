package com.greg.webauthn.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.webauthn.model.domain.WebAuthnChallengeRequest;
import com.greg.webauthn.model.domain.WebAuthnCredentials;
import com.greg.webauthn.model.entity.WebAuthnChallengeEntity;
import com.greg.webauthn.model.entity.WebAuthnCredentialEntity;
import com.greg.webauthn.repositories.ChallengeJpaRepository;
import com.greg.webauthn.repositories.CredentialJpaRepository;
import com.greg.webauthn.services.ICredentialStoreService;
import com.greg.webauthn.utils.WebAuthnChallengeRequestTypeEnum;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA-backed implementation of {@link ICredentialStoreService}.
 *
 * <p>Replaces {@code SvcPaiIdpServiceImpl} from svc-digital-webauthn.
 * All persistence goes through {@link CredentialJpaRepository} and
 * {@link ChallengeJpaRepository} instead of REST calls to svc-pai-idp.
 */
@Service
public class CredentialStoreServiceImpl implements ICredentialStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialStoreServiceImpl.class);

    /** How long a challenge is valid before it must be completed. */
    @Value("${webauthn.challenge.ttl-seconds:300}")
    private long challengeTtlSeconds;

    @Autowired private ChallengeJpaRepository  challengeRepo;
    @Autowired private CredentialJpaRepository credentialRepo;
    @Autowired private ObjectMapper            objectMapper;

    // ── Challenges ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void saveChallenge(WebAuthnChallengeRequest challenge) throws JsonProcessingException {
        String optionsJson = serializeOptions(challenge);
        WebAuthnChallengeEntity entity = WebAuthnChallengeEntity.builder()
                .requestId(challenge.getRequestId())
                .memberId(challenge.getMemberId())
                .optionsJson(optionsJson)
                .requestType(challenge.getRequestType().name())
                .channel(challenge.getChannel())
                .expiresAt(Instant.now().plusSeconds(challengeTtlSeconds))
                .build();
        challengeRepo.save(entity);
        LOG.debug("Saved challenge requestId={} type={}", challenge.getRequestId(), challenge.getRequestType());
    }

    @Override
    @Transactional(readOnly = true)
    public WebAuthnChallengeRequest getChallengeByRequestId(String requestId, String memberId) {
        Optional<WebAuthnChallengeEntity> opt = challengeRepo.findByRequestId(requestId);
        if (opt.isEmpty()) {
            LOG.warn("Challenge not found: requestId={}", requestId);
            return null;
        }
        WebAuthnChallengeEntity entity = opt.get();
        if (entity.getExpiresAt().isBefore(Instant.now())) {
            LOG.warn("Challenge expired: requestId={}", requestId);
            return null;
        }
        try {
            return deserializeChallenge(entity);
        } catch (Exception e) {
            LOG.error("Failed to deserialize challenge requestId={}", requestId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteChallengeByRequestId(String requestId) {
        challengeRepo.deleteById(requestId);
        LOG.debug("Deleted challenge requestId={}", requestId);
    }

    // ── Credentials ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void saveCredential(WebAuthnCredentials cred) {
        String transportsJson = serializeTransports(cred.getAuthenticatorTransports());
        WebAuthnCredentialEntity entity = WebAuthnCredentialEntity.builder()
                .credentialId(cred.getCredentialId())
                .memberId(cred.getMemberId())
                .publicKeyCose(cred.getPublicKeyCose())
                .signatureCount(cred.getCount())
                .channel(cred.getChannel())
                .transports(transportsJson)
                .build();
        credentialRepo.save(entity);
        LOG.debug("Saved credential credentialId={} memberId={}", cred.getCredentialId(), cred.getMemberId());
    }

    @Override
    @Transactional(readOnly = true)
    public WebAuthnCredentials getCredentialById(String credentialId) {
        return credentialRepo.findById(credentialId)
                .map(this::toCredentials)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebAuthnCredentials> getCredentialsByMemberId(String memberId) {
        return credentialRepo.findByMemberId(memberId).stream()
                .map(this::toCredentials)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateSignatureCount(String credentialId, long count) {
        credentialRepo.updateSignatureCount(credentialId, count);
        LOG.debug("Updated signature count credentialId={} count=", credentialId, count);
    }

    @Override
    @Transactional
    public void deleteCredentialsByMemberId(String memberId) {
        credentialRepo.deleteByMemberId(memberId);
        LOG.debug("Deleted all credentials for memberId={}", memberId);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private String serializeOptions(WebAuthnChallengeRequest challenge) throws JsonProcessingException {
        if (challenge.getRequestType() == WebAuthnChallengeRequestTypeEnum.REGISTRATION) {
            return challenge.getPublicKeyCredentialCreationOptions().toJson();
        } else {
            return challenge.getAssertionRequest().toJson();
        }
    }

    private WebAuthnChallengeRequest deserializeChallenge(WebAuthnChallengeEntity entity)
            throws Exception {
        WebAuthnChallengeRequestTypeEnum type =
                WebAuthnChallengeRequestTypeEnum.valueOf(entity.getRequestType());
        WebAuthnChallengeRequest.WebAuthnChallengeRequestBuilder builder = WebAuthnChallengeRequest.builder()
                .requestId(entity.getRequestId())
                .memberId(entity.getMemberId())
                .requestType(type)
                .channel(entity.getChannel());

        if (type == WebAuthnChallengeRequestTypeEnum.REGISTRATION) {
            builder.publicKeyCredentialCreationOptions(
                    PublicKeyCredentialCreationOptions.fromJson(entity.getOptionsJson()));
        } else {
            builder.assertionRequest(
                    AssertionRequest.fromJson(entity.getOptionsJson()));
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private String serializeTransports(java.util.Set transports) {
        if (transports == null || transports.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(transports);
        } catch (JsonProcessingException e) {
            LOG.warn("Could not serialise transports", e);
            return null;
        }
    }

    private WebAuthnCredentials toCredentials(WebAuthnCredentialEntity entity) {
        return WebAuthnCredentials.builder()
                .credentialId(entity.getCredentialId())
                .memberId(entity.getMemberId())
                .publicKeyCose(entity.getPublicKeyCose())
                .count((int) entity.getSignatureCount())
                .authenticatorName(entity.getAuthenticatorName())
                .channel(entity.getChannel())
                .build();
    }
}
