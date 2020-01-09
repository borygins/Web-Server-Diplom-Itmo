package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationLayerProtocolNegotiationTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("0010000e000c02683208687474702f312e31");
        byte[] arr = SSLUtils.hexStringToByteArray("000e000c02683208687474702f312e31");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        ApplicationLayerProtocolNegotiation applicationLayerProtocolNegotiation = new ApplicationLayerProtocolNegotiation();
        applicationLayerProtocolNegotiation.setSSL(buf, ExtensionType.ApplicationLayerProtocolNegotiation, ExtensionType.ApplicationLayerProtocolNegotiation.getType());

        assertArrayEquals(arrEq, applicationLayerProtocolNegotiation.toByte());
    }

    @Test
    void getAlpnLen() {
    }

    @Test
    void getAlpnNextProtocols() {
    }
}