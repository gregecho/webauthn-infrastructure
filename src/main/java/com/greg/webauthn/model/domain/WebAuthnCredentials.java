package com.greg.webauthn.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Domain model for a registered passkey credential.
 * This is the internal representation passed between the service and repository layers;
 * it does not expose JPA annotations to the rest of the application.
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class WebAuthnCredentials {

    private String memberId;

    private String credentialId;

    private String publicKeyCose;

    private String authenticatorName;

    @SuppressWarnings("rawtypes")
    private Set authenticatorTransports;

    private String channel;

    private int count;
}
