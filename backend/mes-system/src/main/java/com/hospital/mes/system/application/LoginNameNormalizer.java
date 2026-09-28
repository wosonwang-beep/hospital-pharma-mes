package com.hospital.mes.system.application;

import java.text.Normalizer;
import java.util.Locale;

public final class LoginNameNormalizer {
    private LoginNameNormalizer() {
    }

    public static String normalize(String loginName) {
        return Normalizer.normalize(loginName.strip(), Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
    }
}
