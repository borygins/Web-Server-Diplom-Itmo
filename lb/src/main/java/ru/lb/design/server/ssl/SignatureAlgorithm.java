package ru.lb.design.server.ssl;

public enum SignatureAlgorithm {
    ecdsa_secp256r1_sha256 (1027),
    rsa_pss_rsae_sha256 (2052),
    rsa_pkcs1_sha256 (1025),
    ecdsa_secp384r1_sha384 (1283),
    rsa_pss_rsae_sha384 (2053),
    rsa_pkcs1_sha384 (1281),
    rsa_pss_rsae_sha512 (2054),
    rsa_pkcs1_sha512 (1537),
    rsa_pkcs1_sha1 (513);

    short type;

    SignatureAlgorithm(int type) {
        this.type = (short) type;
    }
    SignatureAlgorithm(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }
}
