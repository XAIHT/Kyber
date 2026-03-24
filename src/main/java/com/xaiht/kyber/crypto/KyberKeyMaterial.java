package com.xaiht.kyber.crypto;

public final class KyberKeyMaterial {

    private final KyberSecurityLevel securityLevel;
    private final String publicKeyBase64;
    private final String privateKeyBase64;
    private final String keyFingerprint;

    public KyberKeyMaterial(KyberSecurityLevel securityLevel, String publicKeyBase64, String privateKeyBase64,
            String keyFingerprint) {
        this.securityLevel = securityLevel;
        this.publicKeyBase64 = publicKeyBase64;
        this.privateKeyBase64 = privateKeyBase64;
        this.keyFingerprint = keyFingerprint;
    }

    public KyberSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    public String getKeyFingerprint() {
        return keyFingerprint;
    }
}
