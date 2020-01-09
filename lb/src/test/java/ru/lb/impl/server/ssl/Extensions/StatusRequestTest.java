package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class StatusRequestTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {
        byte[] arrEq = SSLUtils.hexStringToByteArray("000500050100000000");
        byte[] arr = SSLUtils.hexStringToByteArray("00050100000000");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setSSL(buf, ExtensionType.StatusRequest, ExtensionType.StatusRequest.getType());

        assertArrayEquals(arrEq, statusRequest.toByte());
    }

    @Test
    void getCertificateStatusType() {
    }

    @Test
    void getResponderIDListLen() {
    }

    @Test
    void getRequestExtensionsLen() {
    }
}