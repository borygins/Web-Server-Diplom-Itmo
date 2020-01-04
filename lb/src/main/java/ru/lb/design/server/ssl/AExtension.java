package ru.lb.design.server.ssl;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class AExtension implements IExtension {
    protected ExtensionType extensionType;
    protected short len;

    @Override
    public void setSSL(ByteBuffer buffer) {
        extensionType = Arrays.stream(ExtensionType.values()).filter((q1) -> (q1.getType() == buffer.getShort())).findFirst().get();
        len = buffer.getShort();
        readBuf(buffer);
    }

    protected abstract void readBuf(ByteBuffer buffer);

    @Override
    public byte[] toByte() {
        ByteBuffer outByte = ByteBuffer.allocate(1 + this.len);
        outByte.putShort(extensionType.getType());
        outByte.putShort(len);
        if(len > 0) {
            getByte(outByte);
        }
        return outByte.array();
    }

    protected abstract void getByte(ByteBuffer byteBuffer);

    public ExtensionType getExtensionType() {
        return extensionType;
    }

    public short getLen() {
        return len;
    }
}
