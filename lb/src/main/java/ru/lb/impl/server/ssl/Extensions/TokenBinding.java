package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;

import java.nio.ByteBuffer;

public class TokenBinding extends AExtension {

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

    public void setData(byte[] data) {
        this.data = data;
    }
}
