package com.xaiht.kyber.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import jakarta.enterprise.context.Dependent;

import org.bouncycastle.pqc.jcajce.interfaces.KyberKey;

import com.xaiht.kyber.crypto.CryptoOperationException;
import com.xaiht.kyber.crypto.KyberKeyMaterial;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.crypto.PostQuantumProviderRegistry;
import com.xaiht.kyber.crypto.ValidationException;

@Dependent
public class KyberKeyService {

    private static final String KEY_ALGORITHM = "KYBER";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public KyberKeyMaterial generateKeyPair(KyberSecurityLevel securityLevel) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM,
                    PostQuantumProviderRegistry.provider());
            keyPairGenerator.initialize(securityLevel.getParameterSpec(), SECURE_RANDOM);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            return new KyberKeyMaterial(securityLevel, encodeKey(keyPair.getPublic()), encodeKey(keyPair.getPrivate()),
                    fingerprint(keyPair.getPublic()));
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("Unable to generate a Kyber key pair.", ex);
        }
    }

    public PublicKey decodePublicKey(String encodedKey, KyberSecurityLevel expectedSecurityLevel) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PostQuantumProviderRegistry.provider());
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodePemOrBase64(encodedKey)));
            validateSecurityLevel(publicKey, expectedSecurityLevel, "public");
            return publicKey;
        } catch (GeneralSecurityException ex) {
            throw new ValidationException("Unable to parse the supplied Kyber public key.", ex);
        }
    }

    public PrivateKey decodePrivateKey(String encodedKey, KyberSecurityLevel expectedSecurityLevel) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PostQuantumProviderRegistry.provider());
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodePemOrBase64(encodedKey)));
            validateSecurityLevel(privateKey, expectedSecurityLevel, "private");
            return privateKey;
        } catch (GeneralSecurityException ex) {
            throw new ValidationException("Unable to parse the supplied Kyber private key.", ex);
        }
    }

    public String encodeKey(java.security.Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String fingerprint(PublicKey publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getEncoded());
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 12 && i < hash.length; i++) {
                builder.append(String.format("%02x", hash[i]));
            }
            return builder.toString();
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("Unable to fingerprint the Kyber public key.", ex);
        }
    }

    public String fingerprint(PrivateKey privateKey) {
        if (privateKey instanceof org.bouncycastle.pqc.jcajce.interfaces.KyberPrivateKey) {
            org.bouncycastle.pqc.jcajce.interfaces.KyberPrivateKey kyberPrivateKey =
                    (org.bouncycastle.pqc.jcajce.interfaces.KyberPrivateKey) privateKey;
            return fingerprint(kyberPrivateKey.getPublicKey());
        }

        throw new ValidationException("The supplied private key is not a Kyber private key.");
    }

    private void validateSecurityLevel(java.security.Key key, KyberSecurityLevel expectedSecurityLevel, String keyType) {
        if (!(key instanceof KyberKey)) {
            throw new ValidationException("The supplied " + keyType + " key is not a Kyber key.");
        }

        KyberSecurityLevel actualSecurityLevel = KyberSecurityLevel.fromKey((KyberKey) key);
        if (actualSecurityLevel != expectedSecurityLevel) {
            throw new ValidationException("The supplied " + keyType + " key is " + actualSecurityLevel.getDisplayName()
                    + ", but the request selected " + expectedSecurityLevel.getDisplayName() + ".");
        }
    }

    private byte[] decodePemOrBase64(String value) {
        if (value == null) {
            throw new ValidationException("Key material is required.");
        }

        String sanitized = value.replace("\r", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        if (sanitized.isEmpty()) {
            throw new ValidationException("Key material is required.");
        }

        try {
            return Base64.getDecoder().decode(sanitized.getBytes(StandardCharsets.US_ASCII));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Key material must be valid Base64 or PEM-encoded data.", ex);
        }
    }
}
