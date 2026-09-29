package com.hospital.mes.audit.signature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SignableObjectProviderRegistry {
    private final Map<String, SignableObjectProvider> providers;

    public SignableObjectProviderRegistry(List<SignableObjectProvider> providers) {
        Map<String, SignableObjectProvider> indexed = new HashMap<>();
        for (SignableObjectProvider provider : providers) {
            if (indexed.putIfAbsent(provider.objectType(), provider) != null) {
                throw new IllegalStateException("Duplicate signable provider: " + provider.objectType());
            }
        }
        this.providers = Map.copyOf(indexed);
    }

    public SignableObjectProvider require(String objectType, SignatureMeaning meaning) {
        SignableObjectProvider provider = providers.get(objectType);
        if (provider == null) throw new UnsupportedSignableObjectException(objectType);
        if (!provider.allowedMeanings().contains(meaning)) throw new SignatureMeaningNotAllowedException(objectType, meaning);
        return provider;
    }
}
