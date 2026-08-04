package com.greg.webauthn.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stateless JWT utility — verifies Bearer tokens using HMAC-256.
 *
 * <p>Replaces {@code com.hyatt.auth.util.JwtUtil} from lib-hyatt-auth.
 * To use asymmetric keys (RS256/ES256), swap {@link Algorithm#HMAC256}
 * for {@link Algorithm#RSA256} / {@link Algorithm#ECDSA256} and load
 * the public key from PEM.
 */
@Component
public class JwtUtil {

    private final JWTVerifier verifier;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    /**
     * Verifies the token signature and expiry.
     *
     * @param bearerToken the raw {@code Authorization} header value (with or without "Bearer " prefix)
     * @return the decoded and verified JWT
     * @throws JWTVerificationException if the token is invalid or expired
     */
    public DecodedJWT verify(String bearerToken) throws JWTVerificationException {
        String token = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;
        return verifier.verify(token);
    }

    /**
     * Extracts the {@code user_name} claim from an already-decoded JWT.
     * Returns {@code null} if the claim is absent.
     */
    public String extractUsername(DecodedJWT jwt) {
        var claim = jwt.getClaim("user_name");
        return claim.isNull() ? null : claim.asString();
    }
}
