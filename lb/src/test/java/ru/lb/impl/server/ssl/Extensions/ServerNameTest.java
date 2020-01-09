package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ServerNameTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {
        byte[] arrEq = SSLUtils.hexStringToByteArray("00000029002700002465766f6b652d77696e646f777373657276696365732d7461732e6d73656467652e6e6574");
        byte[] arr = SSLUtils.hexStringToByteArray("0029002700002465766f6b652d77696e646f777373657276696365732d7461732e6d73656467652e6e6574");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        ServerName serverName = new ServerName();
        serverName.setSSL(buf, ExtensionType.ServerName, ExtensionType.ServerName.getType());
        assertArrayEquals(arrEq, serverName.toByte());
    }

    @Test
    void getLenServerName() {
    }

    @Test
    void getType() {
    }

    @Test
    void getLenHostName() {
    }

    @Test
    void getHostName() {
    }
}