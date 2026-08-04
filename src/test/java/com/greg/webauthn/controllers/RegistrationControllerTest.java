package com.greg.webauthn.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.webauthn.interceptor.JwtAuthInterceptor;
import com.greg.webauthn.model.request.RegistrationStartRequest;
import com.greg.webauthn.model.response.RegistrationStartResponse;
import com.greg.webauthn.security.JwtUtil;
import com.greg.webauthn.services.IWebAuthnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired private MockMvc     mockMvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private IWebAuthnService    webAuthnService;
    @MockBean private JwtUtil             jwtUtil;
    @MockBean private JwtAuthInterceptor  jwtAuthInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // Let all requests pass through the interceptor to reach controller validation
        when(jwtAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser
    void startRegistration_returns400WhenBodyMissing() throws Exception {
        mockMvc.perform(post(RegistrationController.REGISTRATION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void startRegistration_returns400WhenMemberIdMissing() throws Exception {
        RegistrationStartRequest req = new RegistrationStartRequest();
        req.setChannel("WEB");

        mockMvc.perform(post(RegistrationController.REGISTRATION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void startRegistration_returns400WhenChannelInvalid() throws Exception {
        RegistrationStartRequest req = new RegistrationStartRequest();
        req.setMemberId("member1");
        req.setChannel("INVALID_CHANNEL");

        mockMvc.perform(post(RegistrationController.REGISTRATION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void startRegistration_returns200OnValidRequest() throws Exception {
        RegistrationStartRequest req = new RegistrationStartRequest();
        req.setMemberId("member1");
        req.setChannel("WEB");

        when(webAuthnService.startRegistration(any())).thenReturn(
                RegistrationStartResponse.builder().memberId("member1").build());

        mockMvc.perform(post(RegistrationController.REGISTRATION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
