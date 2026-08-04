package com.greg.webauthn.controllers;

import com.greg.webauthn.model.response.WebAuthnInfoResponse;
import com.greg.webauthn.security.JwtUtil;
import com.greg.webauthn.services.ICredentialStoreService;
import com.greg.webauthn.services.IWebAuthnService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebAuthnManagementController.class)
class WebAuthnManagementControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private IWebAuthnService       webAuthnService;
    @MockBean private ICredentialStoreService store;
    @MockBean private JwtUtil                jwtUtil;

    @Test
    @WithMockUser
    void getCredentials_returns400WhenMemberIdMissing() throws Exception {
        mockMvc.perform(get(WebAuthnManagementController.GET_CREDENTIALS))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getCredentials_returns200WithValidMemberId() throws Exception {
        when(webAuthnService.getWebAuthnInfoByMemberId("member1"))
                .thenReturn(List.of(WebAuthnInfoResponse.builder()
                        .credId("cred1").authenticatorName("iPhone").build()));

        mockMvc.perform(get(WebAuthnManagementController.GET_CREDENTIALS)
                        .param("memberId", "member1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].credId").value("cred1"));
    }

    @Test
    @WithMockUser
    void deleteCredentialsV2_returns400WhenMemberIdMissing() throws Exception {
        mockMvc.perform(delete(WebAuthnManagementController.DELETE_CREDENTIALS_V2)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteCredentialsV2_returns200WithValidMemberId() throws Exception {
        mockMvc.perform(delete(WebAuthnManagementController.DELETE_CREDENTIALS_V2)
                        .param("memberId", "member1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(store).deleteCredentialsByMemberId("member1");
    }
}
