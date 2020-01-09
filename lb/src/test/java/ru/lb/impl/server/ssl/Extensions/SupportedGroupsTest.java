package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SupportedGroupsTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {

        byte[] arrEq = SSLUtils.hexStringToByteArray("000a00080006001d00170018");
        byte[] arr = SSLUtils.hexStringToByteArray("00080006001d00170018");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        SupportedGroups supportedGroups = new SupportedGroups();
        supportedGroups.setSSL(buf, ExtensionType.SupportedGroups, ExtensionType.SupportedGroups.getType());

        assertArrayEquals(arrEq, supportedGroups.toByte());
    }

    @Test
    void getSupportedGroupsListLen() {
    }

    @Test
    void getSupportedGroups() {
    }
}