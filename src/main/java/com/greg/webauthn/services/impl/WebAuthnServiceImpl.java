package com.greg.webauthn.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.webauthn.model.domain.WebAuthnChallengeRequest;
import com.greg.webauthn.model.domain.WebAuthnCredentials;
import com.greg.webauthn.model.request.AssertionCompleteServiceRequest;
import com.greg.webauthn.model.request.AssertionStartRequest;
import com.greg.webauthn.model.request.RegistrationCompleteServiceRequest;
import com.greg.webauthn.model.request.RegistrationStartRequest;
import com.greg.webauthn.model.response.AssertionCompleteResponse;
import com.greg.webauthn.model.response.AssertionStartResponse;
import com.greg.webauthn.model.response.RegistrationStartResponse;
import com.greg.webauthn.model.response.WebAuthnInfoResponse;
import com.greg.webauthn.services.ICredentialStoreService;
import com.greg.webauthn.services.IWebAuthnService;
import com.greg.webauthn.utils.RequestIdUtils;
import com.greg.webauthn.utils.WebAuthnChallengeRequestTypeEnum;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.greg.webauthn.exception.WebAuthnErrorsEnum.*;

/**
 * Core WebAuthn Relying Party logic.
 *
 * <p>Ported from {@code com.hyatt.webauthn.services.impl.WebAuthnServiceImpl}.
 * Changes from the reference service:
 * <ul>
 *   <li>Uses {@link ICredentialStoreService} (JPA) instead of svc-pai-idp REST calls.</li>
 *   <li>Uses SLF4J instead of {@code HyattLogger} / {@code StructuredLogger}.</li>
 *   <li>Removed {@code @HyattEvent} monitoring annotations.</li>
 *   <li>Removed svc-telegraph email calls (no email side-effects in this service).</li>
 * </ul>
 */
@Service
public class WebAuthnServiceImpl implements IWebAuthnService {

    private static final Logger LOG = LoggerFactory.getLogger(WebAuthnServiceImpl.class);

    @Value("${request.id.hash.salt}")
    private String requestIdSalt;

    @Autowired private RelyingParty relyingParty;
    @Autowired private ICredentialStoreService store;

    // ── Registration ─────────────────────────────────────────────────────────

    @Override
    public RegistrationStartResponse startRegistration(RegistrationStartRequest request)
            throws JsonProcessingException {

        UserIdentity userIdentity = UserIdentity.builder()
                .name(request.getMemberId())
                .displayName(request.getMemberId())
                .id(new ByteArray(request.getMemberId().getBytes()))
                .build();

        PublicKeyCredentialCreationOptions creationOptions = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(userIdentity)
                        .authenticatorSelection(
                                AuthenticatorSelectionCriteria.builder()
                                        .residentKey(ResidentKeyRequirement.PREFERRED)
                                        .build())
                        .build());

        ByteArray requestId = RequestIdUtils.generateRequestId(requestIdSalt);

        WebAuthnChallengeRequest challenge = WebAuthnChallengeRequest.builder()
                .requestId(requestId.getBase64Url())
                .channel(request.getChannel())
                .memberId(request.getMemberId())
                .requestType(WebAuthnChallengeRequestTypeEnum.REGISTRATION)
                .publicKeyCredentialCreationOptions(creationOptions)
                .build();

        store.saveChallenge(challenge);

        return RegistrationStartResponse.builder()
                .memberId(request.getMemberId())
                .publicKeyCredentialCreationOptions(creationOptions)
                .requestId(requestId)
                .build();
    }

    @Override
    public RegistrationResult finishRegistration(RegistrationCompleteServiceRequest request)
            throws JsonProcessingException {

        WebAuthnChallengeRequest challenge =
                store.getChallengeByRequestId(request.getRequestId(), request.getMemberId());

        if (challenge == null) {
            LOG.error("Registration challenge not found: requestId={}", request.getRequestId());
            REQUEST_NOT_FOUND.throwException("Cannot find registration challenge request");
        }

        RegistrationResult result = null;
        try {
            result = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(challenge.getPublicKeyCredentialCreationOptions())
                            .response(request.getCredential())
                            .build());

            Set transports = request.getCredential().getResponse().getTransports();
            WebAuthnCredentials credentials = WebAuthnCredentials.builder()
                    .credentialId(result.getKeyId().getId().getBase64Url())
                    .channel(request.getChannel())
                    .publicKeyCose(result.getPublicKeyCose().getBase64Url())
                    .count(Math.toIntExact(result.getSignatureCount()))
                    .authenticatorTransports(transports)
                    .memberId(challenge.getMemberId())
                    .build();

            store.saveCredential(credentials);

        } catch (RegistrationFailedException e) {
            LOG.error("Registration failed: {}", e.getMessage());
            REGISTRATION_FAILED.throwException(e.getMessage());
        } finally {
            store.deleteChallengeByRequestId(request.getRequestId());
        }

        return result;
    }

    // ── Assertion ─────────────────────────────────────────────────────────────

    @Override
    public AssertionStartResponse startAssertion(AssertionStartRequest request)
            throws JsonProcessingException {

        boolean hasMemberId = StringUtils.isNotEmpty(request.getMemberId());
        if (hasMemberId) {
            List<WebAuthnCredentials> creds = store.getCredentialsByMemberId(request.getMemberId());
            if (creds.isEmpty()) {
                LOG.error("No credentials found for memberId={}", request.getMemberId());
                REQUEST_NOT_FOUND.throwException("Not found credentials by MemberId");
            }
        }

        AssertionRequest assertionRequest = relyingParty.startAssertion(
                StartAssertionOptions.builder()
                        .username(request.getMemberId())
                        .build());

        ByteArray requestId = RequestIdUtils.generateRequestId(requestIdSalt);

        WebAuthnChallengeRequest challenge = WebAuthnChallengeRequest.builder()
                .requestId(requestId.getBase64Url())
                .channel(request.getChannel())
                .memberId(request.getMemberId())
                .requestType(WebAuthnChallengeRequestTypeEnum.ASSERTION)
                .assertionRequest(assertionRequest)
                .build();

        store.saveChallenge(challenge);

        return AssertionStartResponse.builder()
                .publicKeyCredentialRequestOptions(
                        assertionRequest.getPublicKeyCredentialRequestOptions())
                .requestId(requestId)
                .build();
    }

    @Override
    public AssertionCompleteResponse finishAssertion(AssertionCompleteServiceRequest request)
            throws JsonProcessingException {

        WebAuthnChallengeRequest challenge =
                store.getChallengeByRequestId(request.getRequestId(), null);

        if (challenge == null) {
            LOG.error("Assertion challenge not found: requestId={}", request.getRequestId());
            REQUEST_NOT_FOUND.throwException("Cannot find assertion challenge request");
        }

        AssertionCompleteResponse response = null;
        try {
            AssertionResult result = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                            .request(challenge.getAssertionRequest())
                            .response(request.getCredential())
                            .build());

            if (result.isSuccess()) {
                store.updateSignatureCount(
                        result.getCredentialId().getBase64Url(),
                        result.getSignatureCount());

                response = AssertionCompleteResponse.builder()
                        .success(true)
                        .credentialId(result.getCredential().getCredentialId().getBase64Url())
                        .memberId(result.getUsername())
                        .build();
            } else {
                LOG.error("Assertion failed: invalid assertion");
                ASSERTION_FAILED.throwException("Invalid assertion");
            }

        } catch (Exception e) {
            LOG.error("Assertion error: {}", e.getMessage());
            ASSERTION_FAILED.throwException(e.getMessage());
        } finally {
            store.deleteChallengeByRequestId(request.getRequestId());
        }

        return response;
    }

    // ── Management ────────────────────────────────────────────────────────────

    @Override
    public List<WebAuthnInfoResponse> getWebAuthnInfoByMemberId(String memberId) {
        List<WebAuthnCredentials> creds = store.getCredentialsByMemberId(memberId);
        if (creds == null || creds.isEmpty()) {
            LOG.error("No credentials found for memberId={}", memberId);
            REQUEST_NOT_FOUND.throwException("Cannot find webAuthn credentials by MemberId");
        }

        List<WebAuthnInfoResponse> result = new ArrayList<>();
        for (WebAuthnCredentials cred : creds) {
            result.add(WebAuthnInfoResponse.builder()
                    .credId(cred.getCredentialId())
                    .authenticatorName(cred.getAuthenticatorName())
                    .build());
        }
        return result;
    }
}
