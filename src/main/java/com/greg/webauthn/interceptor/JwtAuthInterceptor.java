package com.greg.webauthn.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.webauthn.controllers.RegistrationController;
import com.greg.webauthn.exception.ErrorJson;
import com.greg.webauthn.model.request.RegistrationCompleteRequest;
import com.greg.webauthn.model.request.RegistrationStartRequest;
import com.greg.webauthn.security.JwtUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Pre-handle interceptor that verifies the JWT Bearer token for mutation endpoints.
 *
 * <p>Applied to:
 * <ul>
 *   <li>{@code /webauthn/v1/registration/**} (POST)</li>
 *   <li>{@code DELETE /webauthn/v1/credentials}</li>
 * </ul>
 *
 * <p>Replaces {@code com.hyatt.webauthn.interceptor.AuthorizationHeaderInterceptor}.
 * The deep-check / token-refresh logic from lib-hyatt-auth has been removed;
 * the token is verified with HMAC-256 via {@link JwtUtil}.
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private static final String TOKEN_MISSING_MSG  = "Authorization header is null";
    private static final String TOKEN_INVALID_MSG  = "Authorization header verification failed";
    private static final String MEMBER_ID_PARAM    = "memberId";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // GET endpoints on the same URL pattern are exempt
        if (HttpMethod.GET.matches(request.getMethod())) {
            return true;
        }

        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null) {
            LOG.error("{} — requestURL: {}", TOKEN_MISSING_MSG, request.getRequestURL());
            reject(response, HttpStatus.UNAUTHORIZED, TOKEN_MISSING_MSG);
            return false;
        }

        DecodedJWT jwt;
        try {
            jwt = jwtUtil.verify(bearerToken);
        } catch (Exception ex) {
            LOG.info("JWT verification failed: {}", ex.getMessage());
            reject(response, HttpStatus.FORBIDDEN, ex.getMessage());
            return false;
        }

        if (!verifyMemberId(request, jwt)) {
            reject(response, HttpStatus.FORBIDDEN, TOKEN_INVALID_MSG);
            return false;
        }

        return true;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Checks that the memberId in the request body or query param matches
     * the {@code user_name} claim in the token.
     */
    private boolean verifyMemberId(HttpServletRequest request, DecodedJWT jwt) {
        try {
            String userFromToken = jwtUtil.extractUsername(jwt);
            String memberId      = extractMemberIdFromRequest(request);
            boolean match        = Objects.equals(memberId, userFromToken);
            LOG.info("memberId match: requestMemberId={} userFromToken={} result={}", memberId, userFromToken, match);
            return match;
        } catch (IOException e) {
            // Body-parse failure — treat as mismatch
            LOG.warn("Could not extract memberId from request body", e);
            return false;
        }
    }

    private String extractMemberIdFromRequest(HttpServletRequest request) throws IOException {
        if (HttpMethod.DELETE.matches(request.getMethod())) {
            return StringUtils.defaultString(request.getParameter(MEMBER_ID_PARAM));
        }
        return extractMemberIdFromBody(request);
    }

    private String extractMemberIdFromBody(HttpServletRequest request) throws IOException {
        BufferedServletRequestWrapper wrapper = new BufferedServletRequestWrapper(request);
        try {
            if (request.getRequestURI().equals(RegistrationController.REGISTRATION_START)) {
                RegistrationStartRequest req =
                        objectMapper.readValue(wrapper.getBody(), RegistrationStartRequest.class);
                return ObjectUtils.isEmpty(req) ? StringUtils.EMPTY : req.getMemberId();
            }
            if (request.getRequestURI().equals(RegistrationController.REGISTRATION_COMPLETE)) {
                RegistrationCompleteRequest req =
                        objectMapper.readValue(wrapper.getBody(), RegistrationCompleteRequest.class);
                return ObjectUtils.isEmpty(req) ? StringUtils.EMPTY : req.getMemberId();
            }
        } catch (IOException e) {
            throw new HttpMessageConversionException(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    private static void reject(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        ErrorJson body = ErrorJson.newBuilder()
                .setStatus(status.value())
                .setMessage(message)
                .done();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        try (PrintWriter writer = response.getWriter()) {
            writer.print(new ObjectMapper().writeValueAsString(body));
        }
        response.flushBuffer();
    }
}
