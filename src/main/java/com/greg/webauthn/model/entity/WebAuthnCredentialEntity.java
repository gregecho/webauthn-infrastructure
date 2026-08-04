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
 * Persisted WebAuthn credential (one row per registered passkey).
 * credentialId is the Yubico Base64URL-encoded credential ID — used as the
 * natural primary key since it is globally unique per the WebAuthn spec.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webauthn_credentials")
public class WebAuthnCredentialEntity {

    /** Base64URL-encoded credential ID (from the authenticator). */
    @Id
    @Column(name = "credential_id", nullable = false)
    private String credentialId;

    /** Owner's member/user ID. */
    @Column(name = "member_id", nullable = false)
    private String memberId;

    /** COSE-encoded public key, stored as Base64URL. */
    @Column(name = "public_key_cose", nullable = false, length = 2048)
    private String publicKeyCose;

    /** Monotonically-increasing signature counter — used to detect cloned authenticators. */
    @Column(name = "signature_count", nullable = false)
    private long signatureCount;

    /** Human-readable name set by the user at registration time (optional). */
    @Column(name = "authenticator_name")
    private String authenticatorName;

    /** The channel from which the credential was registered (e.g. WEB, MOBILE). */
    @Column(name = "channel")
    private String channel;

    /** JSON-serialised set of AuthenticatorTransport values (e.g. ["internal","hybrid"]). */
    @Column(name = "transports", length = 512)
    private String transports;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
