package com.xaiht.kyber.crypto;

import java.security.Provider;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

public final class PostQuantumProviderRegistry {

    private static final Provider PQC_PROVIDER = new BouncyCastlePQCProvider();

    private PostQuantumProviderRegistry() {
    }

    public static Provider provider() {
        return PQC_PROVIDER;
    }
}
