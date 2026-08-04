package com.greg.webauthn.model.request;

import com.greg.webauthn.validator.Channel;
import com.greg.webauthn.validator.EmptyCheck;
import com.greg.webauthn.validator.FormatCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegistrationStartRequest {

    @Schema(description = "Member ID", type = "string")
    @NotEmpty(message = "missingMemberId|Required String Parameter memberId is Not Present",
              groups = {EmptyCheck.class})
    private String memberId;

    @Schema(description = "Channel name (WEB / MOBILE / IOS / ANDROID)", type = "string")
    @NotEmpty(message = "missingChannel|Required String Parameter channel is Not Present",
              groups = {EmptyCheck.class})
    @Channel(message = "invalidChannel|String Parameter channel is Not valid",
             groups = {FormatCheck.class})
    private String channel;
}
