package com.greg.webauthn.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Short-lived WebAuthn challenge record (registration or assertion).
 * Deleted after the corresponding /complete call succeeds or expires.
 *
 * requestId is the Base64URL-encoded SHA-256(UUID + salt) value returned
 * to the caller in the /start response and echoed back in /complete.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webauthn_challenges")
public class WebAuthnChallengeEntity {

    @Id
    @Column(name = "request_id", nullable = false)
    private String requestId;

    /**
     * Owner member ID. Nullable for assertion flows where memberId is optional
     * (discoverable-credential / usernameless login).
     */
    @Column(name = "member_id")
    private String memberId;

    /**
     * Serialised PublicKeyCredentialCreationOptions or PublicKeyCredentialRequestOptions.
     * Stored as a JSON string; deserialised back to the Yubico object on /complete.
     */
    @Column(name = "options_json", nullable = false, length = 8192)
    private String optionsJson;

    /**
     * REGISTRATION or ASSERTION — determines which Yubico deserialization path to use.
     */
    @Column(name = "request_type", nullable = false)
    private String requestType;

    /** Channel from the /start request (e.g. WEB, MOBILE). */
    @Column(name = "channel")
    private String channel;

    /** When this challenge was created. Used for TTL-based cleanup. */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /** Challenges should expire after a short window (e.g. 5 minutes). */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
