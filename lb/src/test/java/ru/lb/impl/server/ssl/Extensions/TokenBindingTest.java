package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class TokenBindingTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("00180006001003020100");
        byte[] arr = SSLUtils.hexStringToByteArray("0006001003020100");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        TokenBinding tokenBinding = new TokenBinding();
        tokenBinding.setSSL(buf, ExtensionType.TokenBinding, ExtensionType.TokenBinding.getType());

        assertArrayEquals(arrEq, tokenBinding.toByte());
    }

    @Test
    void getData() {
    }
}