package com.greg.webauthn.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.webauthn.model.request.AssertionCompleteRequest;
import com.greg.webauthn.model.request.AssertionStartRequest;
import com.greg.webauthn.model.response.AssertionCompleteResponse;
import com.greg.webauthn.model.response.AssertionStartResponse;
import com.greg.webauthn.services.IWebAuthnService;
import com.greg.webauthn.validator.ValidationCheckSequence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import static com.greg.webauthn.exception.WebAuthnErrorsEnum.ASSERTION_FAILED;
import static com.greg.webauthn.exception.WebAuthnErrorsEnum.BAD_INPUT;

@Tag(name = "Assertion", description = "Passkey login (assertion) endpoints")
@RestController
public class AssertionController {

    private static final Logger LOG = LoggerFactory.getLogger(AssertionController.class);

    protected static final String ASSERTION_START    = "/webauthn/v1/assertion/start";
    protected static final String ASSERTION_COMPLETE = "/webauthn/v1/assertion/complete";

    @Autowired
    private IWebAuthnService webAuthnService;

    @Operation(summary = "Assertion start", description = "Creates a challenge and returns PublicKeyCredentialRequestOptions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Challenge created"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping(value = ASSERTION_START, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AssertionStartResponse startAssertion(
            @RequestBody(required = false)
            @Validated(ValidationCheckSequence.class) AssertionStartRequest request) {

        if (request == null) {
            BAD_INPUT.throwException("Required request body is missing");
        }
        // XSS-escape user-controlled fields before further processing
        request.setChannel(HtmlUtils.htmlEscape(request.getChannel()));
        if (StringUtils.isNotEmpty(request.getMemberId())) {
            request.setMemberId(HtmlUtils.htmlEscape(request.getMemberId()));
        }

        AssertionStartResponse response;
        try {
            response = webAuthnService.startAssertion(request);
        } catch (JsonProcessingException e) {
            LOG.error("startAssertion JSON error: {}", e.getMessage());
            ASSERTION_FAILED.throwException(e.getMessage());
            return null;
        }
        return new AssertionStartResponse(
                response.getRequestId(),
                response.getPublicKeyCredentialRequestOptions());
    }

    @Operation(summary = "Assertion complete", description = "Verifies the authenticator response and returns login result")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Assertion failed")
    })
    @PostMapping(value = ASSERTION_COMPLETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AssertionCompleteResponse finishAssertion(
            @RequestBody(required = false)
            @Validated(ValidationCheckSequence.class) AssertionCompleteRequest request) {

        if (request == null) {
            BAD_INPUT.throwException("Required request body is missing");
        }

        AssertionCompleteResponse response;
        try {
            response = webAuthnService.finishAssertion(request.toServiceRequest());
        } catch (JsonProcessingException e) {
            LOG.error("finishAssertion JSON error: {}", e.getMessage());
            ASSERTION_FAILED.throwException(e.getMessage());
            return null;
        }
        return new AssertionCompleteResponse(
                response.isSuccess(),
                response.getCredentialId(),
                response.getMemberId());
    }
}
