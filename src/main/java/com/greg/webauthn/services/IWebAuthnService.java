package com.greg.webauthn.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.webauthn.model.request.AssertionCompleteServiceRequest;
import com.greg.webauthn.model.request.AssertionStartRequest;
import com.greg.webauthn.model.request.RegistrationCompleteServiceRequest;
import com.greg.webauthn.model.request.RegistrationStartRequest;
import com.greg.webauthn.model.response.AssertionCompleteResponse;
import com.greg.webauthn.model.response.AssertionStartResponse;
import com.greg.webauthn.model.response.RegistrationStartResponse;
import com.greg.webauthn.model.response.WebAuthnInfoResponse;
import com.yubico.webauthn.RegistrationResult;

import java.util.List;

public interface IWebAuthnService {

    RegistrationStartResponse startRegistration(RegistrationStartRequest request)
            throws JsonProcessingException;

    RegistrationResult finishRegistration(RegistrationCompleteServiceRequest request)
            throws JsonProcessingException;

    AssertionStartResponse startAssertion(AssertionStartRequest request)
            throws JsonProcessingException;

    AssertionCompleteResponse finishAssertion(AssertionCompleteServiceRequest request)
            throws JsonProcessingException;

    List<WebAuthnInfoResponse> getWebAuthnInfoByMemberId(String memberId);
}
