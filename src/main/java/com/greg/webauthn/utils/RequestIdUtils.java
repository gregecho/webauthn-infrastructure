package com.greg.webauthn.utils;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.exception.Base64UrlException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Generates and validates the Base64URL request-ID used to correlate
 * /start and /complete calls.
 *
 * The ID is SHA-256(UUID + salt), URL-safe Base64-encoded (no padding → 43 chars).
 * The salt is injected via {@code request.id.hash.salt} in application.properties.
 */
public class RequestIdUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RequestIdUtils.class);

    private RequestIdUtils() { /* static utility class */ }

    public static ByteArray generateRequestId(String salt) {
        try {
            String uuid      = UUID.randomUUID().toString();
            String requestId = urlEncodeOneWayHash(uuid + salt);
            return ByteArray.fromBase64Url(requestId);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Could not create SHA-256 digest for requestId", e);
            return null;
        } catch (Base64UrlException e) {
            LOG.error("Could not generate requestId", e);
            return null;
        }
    }

    /** Returns true if the string is valid Base64 and exactly 43 characters long. */
    public static boolean validateRequestIdFormat(String requestId) {
        return Base64.isBase64(requestId) && requestId.length() == 43;
    }

    private static String urlEncodeOneWayHash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest());
    }
}
