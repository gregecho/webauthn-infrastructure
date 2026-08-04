package com.greg.webauthn.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(builder = WebAuthnInfoResponse.WebAuthnInfoResponseBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebAuthnInfoResponse {

    @Schema(description = "Credential ID for the member", type = "string")
    private String credId;

    @Schema(description = "Name of authenticator", type = "string")
    private String authenticatorName;
}
