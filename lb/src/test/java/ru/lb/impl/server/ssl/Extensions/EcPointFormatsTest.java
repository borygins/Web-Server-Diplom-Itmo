package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class EcPointFormatsTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("000b00020100");
        byte[] arr = SSLUtils.hexStringToByteArray("00020100");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        EcPointFormats ecPointFormats = new EcPointFormats();
        ecPointFormats.setSSL(buf, ExtensionType.EcPointFormats, ExtensionType.EcPointFormats.getType());

        assertArrayEquals(arrEq, ecPointFormats.toByte());
    }
}