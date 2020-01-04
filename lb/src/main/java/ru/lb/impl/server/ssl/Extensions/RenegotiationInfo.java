package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.TLSv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenegotiationInfo extends AExtension {

    private byte renegotiationInfoLen;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        renegotiationInfoLen = buffer.get();

    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.put(renegotiationInfoLen);
    }

    public byte getRenegotiationInfoLen() {
        return renegotiationInfoLen;
    }

}
