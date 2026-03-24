package com.xaiht.kyber.crypto;

import java.util.Locale;

import org.bouncycastle.pqc.jcajce.interfaces.KyberKey;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

public enum KyberSecurityLevel {
    KYBER_512("512", "Kyber512", KyberParameterSpec.kyber512),
    KYBER_768("768", "Kyber768", KyberParameterSpec.kyber768),
    KYBER_1024("1024", "Kyber1024", KyberParameterSpec.kyber1024);

    private final String formValue;
    private final String displayName;
    private final KyberParameterSpec parameterSpec;

    KyberSecurityLevel(String formValue, String displayName, KyberParameterSpec parameterSpec) {
        this.formValue = formValue;
        this.displayName = displayName;
        this.parameterSpec = parameterSpec;
    }

    public String getFormValue() {
        return formValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public KyberParameterSpec getParameterSpec() {
        return parameterSpec;
    }

    public static KyberSecurityLevel fromFormValue(String value) {
        if (value == null) {
            throw new ValidationException("Kyber security level is required.");
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("512".equals(normalized) || "kyber512".equals(normalized)) {
            return KYBER_512;
        }
        if ("768".equals(normalized) || "kyber768".equals(normalized)) {
            return KYBER_768;
        }
        if ("1024".equals(normalized) || "kyber1024".equals(normalized)) {
            return KYBER_1024;
        }

        throw new ValidationException("Unsupported Kyber security level: " + value);
    }

    public static KyberSecurityLevel fromKey(KyberKey key) {
        return fromFormValue(key.getParameterSpec().getName());
    }
}
