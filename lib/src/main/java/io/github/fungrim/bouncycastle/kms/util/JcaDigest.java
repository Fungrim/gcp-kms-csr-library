package io.github.fungrim.bouncycastle.kms.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm;
import com.google.cloud.kms.v1.Digest;
import com.google.protobuf.ByteString;

public enum  JcaDigest {
    
    SHA256(() -> safeGetDigest("SHA-256"), (b) -> com.google.cloud.kms.v1.Digest.newBuilder().setSha256(ByteString.copyFrom(b)).build()),
    SHA384(() -> safeGetDigest("SHA-382"), (b) -> com.google.cloud.kms.v1.Digest.newBuilder().setSha384(ByteString.copyFrom(b)).build()),
    SHA512(() -> safeGetDigest("SHA-512"), (b) -> com.google.cloud.kms.v1.Digest.newBuilder().setSha512(ByteString.copyFrom(b)).build());

    public static JcaDigest of(CryptoKeyVersionAlgorithm algorithm) {
        String name = algorithm.name();
        if (name.endsWith("256") || name.endsWith("256K")) {
            return SHA256;
        } else if (name.endsWith("384")) {
            return SHA384;
        } else {
            return SHA512;
        }
    }

    private static MessageDigest safeGetDigest(String alg) {
        try {
            return MessageDigest.getInstance(alg);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing algorithm: " + alg, e);
        }
    }

    private final Provider<MessageDigest> digestProvider;
    private final KmsDigestBuilder kmsDigestBuilder;

    private JcaDigest(Provider<MessageDigest> digestProvider, KmsDigestBuilder kmsDigestBuilder) {
        this.digestProvider = digestProvider;
        this.kmsDigestBuilder = kmsDigestBuilder;
    }

    public byte[] digest(byte[] bytes) {
        return digestProvider.get().digest(bytes);
    }

    public Digest wrap(byte[] digestBytes) {
        return kmsDigestBuilder.build(digestBytes);
    }

    public Digest digestAndWrap(byte[] bytes) {
        return wrap(digest(bytes));
    }
}
