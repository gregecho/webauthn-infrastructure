package com.greg.webauthn.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssertionCompleteResponse {

    @Schema(description = "Login status", type = "boolean")
    private boolean success;

    @Schema(description = "Credential ID used for this authentication", type = "string")
    private String credentialId;

    @Schema(description = "Member ID of the authenticated user", type = "string")
    private String memberId;
}
