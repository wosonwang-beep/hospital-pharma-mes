package com.hospital.mes.audit.attachment.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class AttachmentRules {
    public static final long MAX_BYTES = 10_485_760;
    private AttachmentRules() { }

    public static void validateSize(long size) {
        if (size < 1 || size > MAX_BYTES) throw new IllegalArgumentException("Attachment must contain 1..10485760 bytes");
    }

    public static String fileName(String submitted) {
        if (submitted == null || submitted.length() > 255) throw new IllegalArgumentException("Invalid attachment filename");
        String normalized = submitted.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").strip();
        if (name.isEmpty() || name.equals(".") || name.equals("..")) throw new IllegalArgumentException("Invalid attachment filename");
        return name;
    }

    public static String mediaType(String submitted) {
        if (submitted == null || submitted.length() > 127
            || !submitted.matches("[A-Za-z0-9!#$&^_.+\\-]+/[A-Za-z0-9!#$&^_.+\\-]+")) {
            return "application/octet-stream";
        }
        return submitted;
    }

    public static String digest(byte[] content) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content)); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
