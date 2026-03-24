package com.xaiht.kyber.crypto;

public final class VerificationResult {

    private final KyberSecurityLevel securityLevel;
    private final String verifierFingerprint;
    private final boolean verified;
    private final String computedMacBase64;

    public VerificationResult(KyberSecurityLevel securityLevel, String verifierFingerprint, boolean verified,
            String computedMacBase64) {
        this.securityLevel = securityLevel;
        this.verifierFingerprint = verifierFingerprint;
        this.verified = verified;
        this.computedMacBase64 = computedMacBase64;
    }

    public KyberSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public String getVerifierFingerprint() {
        return verifierFingerprint;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getComputedMacBase64() {
        return computedMacBase64;
    }
}
