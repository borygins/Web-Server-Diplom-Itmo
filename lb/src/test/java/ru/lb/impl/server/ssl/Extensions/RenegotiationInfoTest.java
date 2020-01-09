package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class RenegotiationInfoTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("ff01000100");
        byte[] arr = SSLUtils.hexStringToByteArray("000100");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        RenegotiationInfo renegotiationInfo = new RenegotiationInfo();
        renegotiationInfo.setSSL(buf, ExtensionType.RenegotiationInfo, ExtensionType.RenegotiationInfo.getType());

        assertArrayEquals(arrEq, renegotiationInfo.toByte());
    }

    @Test
    void getRenegotiationInfoLen() {
    }
}