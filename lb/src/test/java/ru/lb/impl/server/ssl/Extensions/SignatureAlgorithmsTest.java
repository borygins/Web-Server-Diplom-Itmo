package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SignatureAlgorithmsTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("000d00140012040105010201040305030203020206010603");
        byte[] arr = SSLUtils.hexStringToByteArray("00140012040105010201040305030203020206010603");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        SignatureAlgorithms signatureAlgorithms = new SignatureAlgorithms();
        signatureAlgorithms.setSSL(buf, ExtensionType.SignatureAlgorithms, ExtensionType.SignatureAlgorithms.getType());

        assertArrayEquals(arrEq, signatureAlgorithms.toByte());
    }

    @Test
    void getSignatureAlgorithmLen() {
    }

    @Test
    void getSignatureAlgorithms() {
    }
}