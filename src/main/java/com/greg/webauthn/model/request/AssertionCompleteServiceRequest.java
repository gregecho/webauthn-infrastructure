package com.greg.webauthn.model.request;

import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Internal service request carrying the parsed Yubico assertion credential. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssertionCompleteServiceRequest {

    private String requestId;
    private PublicKeyCredential<AuthenticatorAssertionResponse,
                                ClientAssertionExtensionOutputs> credential;
}
