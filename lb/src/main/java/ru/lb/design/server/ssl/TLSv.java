package ru.lb.design.server.ssl;

public enum TLSv {
    Unknown(35466),TLS1_0(7669),TLS1_1(770),TLS1_2(771),TLS1_3(772);

    short type;

    TLSv(short type) {
        this.type = type;
    }

    TLSv(int i) {
        this.type = (short) i;
    }

    public short getType() {
        return type;
    }
}
