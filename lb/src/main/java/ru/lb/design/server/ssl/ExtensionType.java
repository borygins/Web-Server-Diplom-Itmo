package ru.lb.design.server.ssl;

public enum ExtensionType {
    ServerName(0), StatusRequest(5),
    SupportedGroups(10), EcPointFormats(11),
    SignatureAlgorithms(13), SessionTicket(35),
    TokenBinding(24),KeyShare(51),
    ExtendedMasterSecret(23),SupportedVersions(43),
    RenegotiationInfo(65281),ApplicationLayerProtocolNegotiation(16);

    short type;

    ExtensionType(int type) {
        this.type = (short) type;
    }
    ExtensionType(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }
}
