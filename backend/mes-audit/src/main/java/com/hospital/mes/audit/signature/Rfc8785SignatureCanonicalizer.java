package com.hospital.mes.audit.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.TreeSet;
import org.springframework.stereotype.Component;
import org.erdtman.jcs.JsonCanonicalizer;

@Component
public class Rfc8785SignatureCanonicalizer implements SignatureCanonicalizer {
    private final ObjectMapper json;
    public Rfc8785SignatureCanonicalizer(ObjectMapper json) { this.json = json; }

    @Override public String canonicalJson(SignableObject object) {
        rejectFloatingPoint(object.canonicalRecord());
        ObjectNode envelope = json.createObjectNode();
        envelope.put("schemaVersion", "1.0"); envelope.put("objectType", object.objectType());
        envelope.put("objectId", object.objectId()); envelope.put("recordVersion", object.recordVersion());
        envelope.set("record", object.canonicalRecord());
        ArrayNode evidence = envelope.putArray("evidenceIds");
        new TreeSet<>(object.evidenceIds()).forEach(evidence::add);
        try { return new JsonCanonicalizer(json.writeValueAsBytes(envelope)).getEncodedString(); }
        catch (java.io.IOException e) { throw new IllegalArgumentException("record is not canonicalizable", e); }
    }

    @Override public String digest(SignableObject object) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(canonicalJson(object).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    private static void rejectFloatingPoint(JsonNode node) {
        if (node.isFloatingPointNumber()) throw new IllegalArgumentException("binary floating-point is forbidden in signable records");
        node.elements().forEachRemaining(Rfc8785SignatureCanonicalizer::rejectFloatingPoint);
    }
}
