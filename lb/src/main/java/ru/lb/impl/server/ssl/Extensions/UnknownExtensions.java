package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.TLSv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnknownExtensions extends AExtension {

    private byte[] array;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        if(len > 0){
            array = new byte[len];
            buffer.get(array);
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.put(array);
    }

    public byte[] getArray() {
        return array;
    }

    public void setArray(byte[] array) {
        this.array = array;
    }
}
