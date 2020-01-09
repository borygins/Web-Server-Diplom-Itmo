package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class KeyShareTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("0033002b00293a3a000100001d0020c182c23ddf06c5ac9a3e85010195026f43d7c0e26756eb90d60e527b3386c269");
        byte[] arr = SSLUtils.hexStringToByteArray("002b00293a3a000100001d0020c182c23ddf06c5ac9a3e85010195026f43d7c0e26756eb90d60e527b3386c269");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        KeyShare keyShare = new KeyShare();
        keyShare.setSSL(buf, ExtensionType.KeyShare, ExtensionType.KeyShare.getType());

        assertArrayEquals(arrEq, keyShare.toByte());
    }

    @Test
    void getKeyShareLen() {
    }

    @Test
    void getReserved() {
    }

    @Test
    void getKeyExchageLen() {
    }

    @Test
    void getKeyExchage() {
    }

    @Test
    void getExtensionSuportedGroups() {
    }

    @Test
    void getKeyExchageGroupLen() {
    }

    @Test
    void getKeyExchageGroup() {
    }
}