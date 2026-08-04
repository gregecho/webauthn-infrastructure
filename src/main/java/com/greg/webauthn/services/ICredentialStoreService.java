package com.greg.webauthn.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.webauthn.model.domain.WebAuthnCredentials;
import com.greg.webauthn.model.domain.WebAuthnChallengeRequest;

import java.util.List;

/**
 * Abstraction over the credential and challenge persistence layer.
 * All reads and writes go through implementations of this interface;
 * no persistence concern leaks into the service layer.
 */
public interface ICredentialStoreService {

    void saveChallenge(WebAuthnChallengeRequest challenge) throws JsonProcessingException;

    WebAuthnChallengeRequest getChallengeByRequestId(String requestId, String memberId);

    void deleteChallengeByRequestId(String requestId);

    void saveCredential(WebAuthnCredentials credentials);

    WebAuthnCredentials getCredentialById(String credentialId);

    List<WebAuthnCredentials> getCredentialsByMemberId(String memberId);

    void updateSignatureCount(String credentialId, long count);

    void deleteCredentialsByMemberId(String memberId);
}
