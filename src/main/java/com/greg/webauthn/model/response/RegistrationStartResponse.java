package com.greg.webauthn.model.response;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RegistrationStartResponse {

    @Schema(description = "Request ID — echo back in /registration/complete", type = "string")
    private ByteArray requestId;

    @Schema(description = "Member ID", type = "string")
    private String memberId;

    @Schema(description = "PublicKeyCredentialCreationOptions for the browser WebAuthn API", type = "object")
    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;
}
