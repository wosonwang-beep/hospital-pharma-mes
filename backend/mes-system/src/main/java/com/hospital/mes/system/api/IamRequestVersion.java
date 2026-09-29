package com.hospital.mes.system.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IamRequestVersion {
    private static final Pattern STRONG_DECIMAL_ETAG = Pattern.compile("\\\"(0|[1-9][0-9]*)\\\"");

    private IamRequestVersion() { }

    static long parse(String value) {
        if (value == null) throw new IllegalArgumentException("If-Match is required");
        Matcher matcher = STRONG_DECIMAL_ETAG.matcher(value);
        if (!matcher.matches()) throw new IllegalArgumentException("If-Match must be a quoted decimal version");
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("If-Match version is invalid", ex);
        }
    }
}
