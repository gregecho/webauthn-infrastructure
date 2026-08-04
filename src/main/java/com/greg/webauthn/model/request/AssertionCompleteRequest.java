package com.greg.webauthn.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.greg.webauthn.utils.CredentialResponseDeserializer;
import com.greg.webauthn.validator.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.IOException;

@Data
public class AssertionCompleteRequest {

    @Schema(description = "Request ID for the login", type = "string")
    @NotEmpty(message = "missingRequestId|Required String Parameter requestId is Not Present",
              groups = {EmptyCheck.class})
    @RequestId(groups = {FormatCheck.class})
    private String requestId;

    @Schema(description = "Verified credential details for login", type = "string")
    @CredentialNotEmpty(groups = {EmptyCheck.class})
    @AssertionCredential(message = "invalidCredential|Missing fields or malformed JSON in fields",
                         groups = {FormatCheck.class})
    @JsonDeserialize(using = CredentialResponseDeserializer.class)
    private String credential;

    @Schema(hidden = true)
    @JsonIgnore
    public AssertionCompleteServiceRequest toServiceRequest() {
        try {
            return new AssertionCompleteServiceRequest(
                    requestId,
                    com.yubico.webauthn.data.PublicKeyCredential.parseAssertionResponseJson(credential));
        } catch (IOException e) {
            return new AssertionCompleteServiceRequest();
        }
    }
}
