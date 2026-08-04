package com.greg.webauthn.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.webauthn.model.request.AssertionStartRequest;
import com.greg.webauthn.model.response.AssertionStartResponse;
import com.greg.webauthn.security.JwtUtil;
import com.greg.webauthn.services.IWebAuthnService;
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

@WebMvcTest(AssertionController.class)
class AssertionControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private IWebAuthnService webAuthnService;
    @MockBean private JwtUtil          jwtUtil;

    @Test
    @WithMockUser
    void startAssertion_returns400WhenBodyMissing() throws Exception {
        mockMvc.perform(post(AssertionController.ASSERTION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void startAssertion_returns400WhenChannelMissing() throws Exception {
        AssertionStartRequest req = new AssertionStartRequest();

        mockMvc.perform(post(AssertionController.ASSERTION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void startAssertion_returns200WithValidChannel() throws Exception {
        AssertionStartRequest req = new AssertionStartRequest();
        req.setChannel("WEB");

        when(webAuthnService.startAssertion(any()))
                .thenReturn(AssertionStartResponse.builder().build());

        mockMvc.perform(post(AssertionController.ASSERTION_START)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
