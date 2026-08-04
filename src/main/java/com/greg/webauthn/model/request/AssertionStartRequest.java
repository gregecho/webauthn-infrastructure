package com.greg.webauthn.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.greg.webauthn.validator.Channel;
import com.greg.webauthn.validator.EmptyCheck;
import com.greg.webauthn.validator.FormatCheck;
import com.greg.webauthn.validator.MemberId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssertionStartRequest {

    @Schema(description = "Member ID (optional — omit for discoverable-credential login)", type = "string")
    @MemberId(groups = {FormatCheck.class}, isEmpty = true)
    private String memberId;

    @Schema(description = "Channel name (WEB / MOBILE / IOS / ANDROID)", type = "string")
    @NotEmpty(message = "missingChannel|Required String Parameter channel is Not Present",
              groups = {EmptyCheck.class})
    @Channel(message = "invalidChannel|String Parameter channel is Not valid",
             groups = {FormatCheck.class})
    private String channel;
}
