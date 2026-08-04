package com.greg.webauthn.model.response;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssertionStartResponse {

    @Schema(description = "Request ID — echo back in /assertion/complete", type = "string")
    private ByteArray requestId;

    @Schema(description = "PublicKeyCredentialRequestOptions for the browser WebAuthn API", type = "object")
    private PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions;
}
