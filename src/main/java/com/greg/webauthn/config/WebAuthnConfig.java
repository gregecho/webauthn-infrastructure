package com.greg.webauthn.config;

import com.greg.webauthn.repositories.WebAuthnCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.Set;

/**
 * Wires the Yubico {@link RelyingParty} bean.
 *
 * <p>Key properties (all in {@code application.properties}):
 * <ul>
 *   <li>{@code webauthn.relying.party.id}      — effective domain (no scheme/port)</li>
 *   <li>{@code webauthn.relying.party.name}     — human-readable RP name</li>
 *   <li>{@code webauthn.relying.party.origins}  — allowed origins (comma-separated)</li>
 * </ul>
 */
@Configuration
public class WebAuthnConfig {

    @Autowired
    WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Value("${webauthn.relying.party.id}")
    private String rpId;

    @Value("${webauthn.relying.party.name}")
    private String rpName;

    @Value("${webauthn.relying.party.origins}")
    private Set<String> rpOrigins;

    @Bean
    public RelyingParty relyingParty() {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(rpId)
                .name(rpName)
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(webAuthnCredentialRepository)
                .origins(rpOrigins)
                .attestationConveyancePreference(Optional.of(AttestationConveyancePreference.NONE))
                .allowOriginPort(true)
                .allowOriginSubdomain(true)
                .allowUntrustedAttestation(true)
                .validateSignatureCounter(true)
                .build();
    }
}
