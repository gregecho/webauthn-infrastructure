package com.greg.webauthn.services;

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
import com.greg.webauthn.services.impl.WebAuthnServiceImpl;
import com.greg.webauthn.utils.WebAuthnChallengeRequestTypeEnum;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAuthnServiceImplTest {

    @Mock private RelyingParty           relyingParty;
    @Mock private ICredentialStoreService store;

    @InjectMocks
    private WebAuthnServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "requestIdSalt", "test-salt");
    }

    // ── startRegistration ─────────────────────────────────────────────────────

    @Test
    void startRegistration_savesChallenge_andReturnsResponse() throws Exception {
        RegistrationStartRequest request = new RegistrationStartRequest();
        request.setMemberId("member1");
        request.setChannel("WEB");

        PublicKeyCredentialCreationOptions options = mock(PublicKeyCredentialCreationOptions.class);
        when(relyingParty.startRegistration(any(StartRegistrationOptions.class))).thenReturn(options);

        RegistrationStartResponse response = service.startRegistration(request);

        assertThat(response.getMemberId()).isEqualTo("member1");
        assertThat(response.getRequestId()).isNotNull();
        assertThat(response.getPublicKeyCredentialCreationOptions()).isEqualTo(options);

        ArgumentCaptor<WebAuthnChallengeRequest> captor =
                ArgumentCaptor.forClass(WebAuthnChallengeRequest.class);
        verify(store).saveChallenge(captor.capture());
        assertThat(captor.getValue().getRequestType()).isEqualTo(WebAuthnChallengeRequestTypeEnum.REGISTRATION);
        assertThat(captor.getValue().getMemberId()).isEqualTo("member1");
    }

    // ── finishRegistration ────────────────────────────────────────────────────

    @Test
    void finishRegistration_throwsWhenChallengeNotFound() {
        RegistrationCompleteServiceRequest request = new RegistrationCompleteServiceRequest();
        request.setRequestId("req-id");
        when(store.getChallengeByRequestId("req-id", null)).thenReturn(null);

        assertThatThrownBy(() -> service.finishRegistration(request))
                .isInstanceOf(com.greg.webauthn.exception.WebAuthnException.class);
    }

    @Test
    void finishRegistration_deletesChallenge_evenOnFailure() throws Exception {
        var regRequest = mock(RegistrationCompleteServiceRequest.class);
        when(regRequest.getRequestId()).thenReturn("req-id");
        when(regRequest.getMemberId()).thenReturn("member1");

        WebAuthnChallengeRequest challenge = WebAuthnChallengeRequest.builder()
                .requestId("req-id").memberId("member1")
                .requestType(WebAuthnChallengeRequestTypeEnum.REGISTRATION)
                .publicKeyCredentialCreationOptions(mock(PublicKeyCredentialCreationOptions.class))
                .build();
        when(store.getChallengeByRequestId("req-id", "member1")).thenReturn(challenge);

        var credential = mock(com.yubico.webauthn.data.PublicKeyCredential.class);
        when(regRequest.getCredential()).thenReturn(credential);

        when(relyingParty.finishRegistration(any(FinishRegistrationOptions.class)))
                .thenThrow(new RegistrationFailedException(
                        new IllegalArgumentException("test")));

        assertThatThrownBy(() -> service.finishRegistration(regRequest))
                .isInstanceOf(com.greg.webauthn.exception.WebAuthnException.class);

        // Challenge must be deleted even when registration fails
        verify(store).deleteChallengeByRequestId("req-id");
    }

    // ── startAssertion ────────────────────────────────────────────────────────

    @Test
    void startAssertion_throwsWhenNoCredentialsForMemberId() {
        AssertionStartRequest request = new AssertionStartRequest();
        request.setMemberId("member1");
        request.setChannel("WEB");
        when(store.getCredentialsByMemberId("member1")).thenReturn(List.of());

        assertThatThrownBy(() -> service.startAssertion(request))
                .isInstanceOf(com.greg.webauthn.exception.WebAuthnException.class);
    }

    @Test
    void startAssertion_withNullMemberId_doesNotCheckCredentials() throws Exception {
        AssertionStartRequest request = new AssertionStartRequest();
        request.setChannel("WEB");

        AssertionRequest assertionReq = mock(AssertionRequest.class);
        PublicKeyCredentialRequestOptions opts = mock(PublicKeyCredentialRequestOptions.class);
        when(assertionReq.getPublicKeyCredentialRequestOptions()).thenReturn(opts);
        when(relyingParty.startAssertion(any(StartAssertionOptions.class))).thenReturn(assertionReq);

        AssertionStartResponse response = service.startAssertion(request);

        verify(store, never()).getCredentialsByMemberId(any());
        assertThat(response.getRequestId()).isNotNull();
    }

    // ── getWebAuthnInfoByMemberId ──────────────────────────────────────────────

    @Test
    void getWebAuthnInfoByMemberId_returnsInfoList() {
        when(store.getCredentialsByMemberId("member1"))
                .thenReturn(List.of(
                        WebAuthnCredentials.builder()
                                .credentialId("cred1")
                                .authenticatorName("iPhone")
                                .build()));

        List<WebAuthnInfoResponse> result = service.getWebAuthnInfoByMemberId("member1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCredId()).isEqualTo("cred1");
        assertThat(result.get(0).getAuthenticatorName()).isEqualTo("iPhone");
    }

    @Test
    void getWebAuthnInfoByMemberId_throwsWhenEmpty() {
        when(store.getCredentialsByMemberId("member1")).thenReturn(List.of());

        assertThatThrownBy(() -> service.getWebAuthnInfoByMemberId("member1"))
                .isInstanceOf(com.greg.webauthn.exception.WebAuthnException.class);
    }
}
