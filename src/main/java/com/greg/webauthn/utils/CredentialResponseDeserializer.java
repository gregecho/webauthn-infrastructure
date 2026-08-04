package com.greg.webauthn.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

/**
 * Jackson deserializer that accepts the {@code credential} field as either:
 * <ul>
 *   <li>a plain JSON string (already serialised by the client), or</li>
 *   <li>a JSON object (authenticator response sent directly).</li>
 * </ul>
 * The downstream validators ({@code @RegistrationCredential}, {@code @AssertionCredential})
 * then parse the resulting string with the Yubico API.
 */
public class CredentialResponseDeserializer extends StdDeserializer<String> {

    public CredentialResponseDeserializer() {
        this(null);
    }

    protected CredentialResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node instanceof TextNode) {
            return node.textValue();
        }
        return node.toString();
    }
}
