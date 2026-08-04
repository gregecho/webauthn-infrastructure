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
public class RegistrationCompleteRequest {

    @Schema(description = "Request ID for passkey creation", type = "string")
    @NotEmpty(message = "missingRequestId|Required String Parameter requestId is Not Present",
              groups = {EmptyCheck.class})
    @RequestId(groups = {FormatCheck.class})
    private String requestId;

    @Schema(description = "Credential body for passkey creation", type = "string")
    @CredentialNotEmpty(groups = {EmptyCheck.class})
    @RegistrationCredential(message = "invalidCredential|Missing fields or malformed JSON in fields",
                            groups = {FormatCheck.class})
    @JsonDeserialize(using = CredentialResponseDeserializer.class)
    private String credential;

    @Schema(description = "Channel name", type = "string")
    @NotEmpty(message = "missingChannel|Required String Parameter channel is Not Present",
              groups = {EmptyCheck.class})
    @Channel(message = "invalidChannel|String Parameter channel is Not valid",
             groups = {FormatCheck.class})
    private String channel;

    @Schema(description = "Member ID", type = "string")
    @NotEmpty(message = "missingMemberId|Required String Parameter memberId is Not Present",
              groups = {EmptyCheck.class})
    private String memberId;

    @Schema(description = "Type of device", type = "string")
    private String deviceType;

    @Schema(description = "Name of web browser", type = "string")
    private String webBrowser;

    @Schema(hidden = true)
    @JsonIgnore
    public RegistrationCompleteServiceRequest toServiceRequest() {
        try {
            return new RegistrationCompleteServiceRequest(
                    requestId,
                    com.yubico.webauthn.data.PublicKeyCredential.parseRegistrationResponseJson(credential),
                    channel, memberId, deviceType, webBrowser);
        } catch (IOException e) {
            return new RegistrationCompleteServiceRequest();
        }
    }
}
