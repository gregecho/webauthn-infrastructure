package com.greg.webauthn.model.domain;

import com.greg.webauthn.utils.WebAuthnChallengeRequestTypeEnum;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Domain model for a pending WebAuthn challenge (registration or assertion).
 * Persisted via {@link com.greg.webauthn.repositories.ChallengeJpaRepository}.
 */
@Data
@ToString
@AllArgsConstructor
@Builder
public class WebAuthnChallengeRequest {

    private String requestId;

    private String memberId;

    /** Populated for REGISTRATION challenges; null for ASSERTION. */
    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

    /** Populated for ASSERTION challenges; null for REGISTRATION. */
    private AssertionRequest assertionRequest;

    private WebAuthnChallengeRequestTypeEnum requestType;

    private String channel;
}
