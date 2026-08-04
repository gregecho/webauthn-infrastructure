package com.greg.webauthn.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.webauthn.model.request.RegistrationCompleteRequest;
import com.greg.webauthn.model.request.RegistrationStartRequest;
import com.greg.webauthn.model.response.RegistrationStartResponse;
import com.greg.webauthn.services.IWebAuthnService;
import com.greg.webauthn.validator.ValidationCheckSequence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.greg.webauthn.exception.WebAuthnErrorsEnum.REGISTRATION_FAILED;

@Tag(name = "Registration", description = "Passkey registration endpoints")
@RestController
public class RegistrationController {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationController.class);

    public static final String REGISTRATION_START    = "/webauthn/v1/registration/start";
    public static final String REGISTRATION_COMPLETE = "/webauthn/v1/registration/complete";

    @Autowired
    private IWebAuthnService webAuthnService;

    @Operation(summary = "Registration start", description = "Creates a challenge and returns PublicKeyCredentialCreationOptions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Challenge created"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping(value = REGISTRATION_START, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RegistrationStartResponse startRegistration(
            @RequestBody
            @Validated(ValidationCheckSequence.class) RegistrationStartRequest request) {

        RegistrationStartResponse response;
        try {
            response = webAuthnService.startRegistration(request);
        } catch (JsonProcessingException e) {
            REGISTRATION_FAILED.throwException(e.getMessage());
            return null; // unreachable — satisfy compiler
        }
        // Re-wrap to prevent potential XSS from untrusted field values
        return new RegistrationStartResponse(
                response.getRequestId(),
                response.getMemberId(),
                response.getPublicKeyCredentialCreationOptions());
    }

    @Operation(summary = "Registration complete", description = "Verifies the authenticator response and persists the credential")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credential registered"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Registration failed")
    })
    @PostMapping(value = REGISTRATION_COMPLETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void finishRegistration(
            @RequestBody(required = false)
            @Validated(ValidationCheckSequence.class) RegistrationCompleteRequest request) {

        try {
            webAuthnService.finishRegistration(request.toServiceRequest());
        } catch (JsonProcessingException e) {
            REGISTRATION_FAILED.throwException(e.getMessage());
        }
    }
}
