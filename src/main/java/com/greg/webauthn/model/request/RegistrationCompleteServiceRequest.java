package com.greg.webauthn.model.request;

import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Internal service request carrying the parsed Yubico credential object. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationCompleteServiceRequest {

    private String requestId;
    private PublicKeyCredential<AuthenticatorAttestationResponse,
                                ClientRegistrationExtensionOutputs> credential;
    private String channel;
    private String memberId;
    private String deviceType;
    private String webBrowser;
}
