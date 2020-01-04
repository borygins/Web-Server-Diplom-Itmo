package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.TLSv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionTicket extends AExtension {

    private byte[] data;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        if(len > 0) {
            data = new byte[len];
            buffer.get(data);
        }

    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        if(len > 0) {
            byteBuffer.put(data);
        }
    }

    public byte[] getData() {
        return data;
    }
}
