package ru.lb.design.server.ssl;

public enum CurveType {
    NamedCurve(3);

    byte type;

    CurveType(int type) {
        this.type = (byte) type;
    }
    CurveType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
