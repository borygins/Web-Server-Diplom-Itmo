package ru.lb.design.server.ssl;

public enum ExtensionSuportedGroups {
    x25519(29),
    secp256r1(23),
    secp384r1(24);

    short type;

    ExtensionSuportedGroups(int type) {
        this.type = (short) type;
    }
    ExtensionSuportedGroups(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }
}
