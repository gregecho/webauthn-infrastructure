package com.greg.webauthn.controllers;

import com.greg.webauthn.model.response.WebAuthnInfoResponse;
import com.greg.webauthn.services.ICredentialStoreService;
import com.greg.webauthn.services.IWebAuthnService;
import com.greg.webauthn.validator.EmptyCheck;
import com.greg.webauthn.validator.MemberId;
import com.greg.webauthn.validator.ValidationCheckSequence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Tag(name = "WebAuthnManagement", description = "Credential management endpoints")
@RestController
@Validated(ValidationCheckSequence.class)
public class WebAuthnManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(WebAuthnManagementController.class);

    protected static final String GET_CREDENTIALS          = "/webauthn/v1/credentials";
    protected static final String DELETE_CREDENTIALS_V1    = "/webauthn/v1/credentials";
    protected static final String DELETE_CREDENTIALS_V2    = "/webauthn/v2/credentials";

    @Autowired private IWebAuthnService       webAuthnService;
    @Autowired private ICredentialStoreService store;

    @Operation(summary = "List credentials", description = "Returns all passkeys registered for a member")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credentials returned"),
        @ApiResponse(responseCode = "400", description = "memberId missing"),
        @ApiResponse(responseCode = "404", description = "No credentials found")
    })
    @GetMapping(GET_CREDENTIALS)
    @ResponseStatus(HttpStatus.OK)
    public List<WebAuthnInfoResponse> getCredentials(
            @RequestParam(value = "memberId", required = false)
            @NotEmpty(message = "missingMemberId|Required String Parameter memberId is Not Present",
                      groups = {EmptyCheck.class})
            String memberId) {

        return webAuthnService.getWebAuthnInfoByMemberId(HtmlUtils.htmlEscape(memberId));
    }

    @Operation(summary = "Delete credentials (v1 — JWT required)",
               description = "Deletes all passkeys for a member; requires a valid JWT via AuthorizationHeaderInterceptor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credentials deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @DeleteMapping(DELETE_CREDENTIALS_V1)
    @ResponseStatus(HttpStatus.OK)
    public void deleteCredentials(
            @RequestParam(value = "memberId", required = false)
            @NotEmpty(message = "missingMemberId|Required String Parameter memberId is Not Present",
                      groups = {EmptyCheck.class})
            String memberId) {

        store.deleteCredentialsByMemberId(memberId);
        LOG.info("Deleted all credentials for memberId={}", memberId);
    }

    @Operation(summary = "Delete credentials (v2 — HTTP Basic auth)",
               description = "Deletes all passkeys for a member using HTTP Basic auth (no JWT)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credentials deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Member not found")
    })
    @DeleteMapping(DELETE_CREDENTIALS_V2)
    @ResponseStatus(HttpStatus.OK)
    public void deleteCredentialsV2(
            @RequestParam(value = "memberId", required = false)
            @NotEmpty(message = "missingMemberId|Required String Parameter memberId is Not Present",
                      groups = {EmptyCheck.class})
            @MemberId
            String memberId) {

        store.deleteCredentialsByMemberId(memberId);
        LOG.info("Deleted all credentials (v2) for memberId={}", memberId);
    }
}
