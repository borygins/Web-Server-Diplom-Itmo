package ru.lb.impl.server.ssl.Extensions;

import org.junit.jupiter.api.Test;
import ru.lb.design.server.ssl.ExtensionType;
import ru.lb.impl.server.ssl.SSLUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class SessionTicketTest {

    @Test
    void readBuf() {
    }

    @Test
    void getByte() {
        byte[] arrEq = SSLUtils.hexStringToByteArray("0023010400000000c1fd3b14341995478b628b1027ecbdbcfcc09043c824181c665fb7fb21b95fb4c7fa92188a5c02c2b71ee74c54b397ce4d06dac3e14e0c4b1fb3e4de770b1a0f34e42a202e56a1905e68158223c9647efed07ad9a025fbc4f0a6c4ea925c8591c3a660ec1a9a5ed6193f07768af51b8e4bc24c76125c88794d36b7d7749970a0f50ec8e962adf1360406f5e747e37ea6de020728a34b57d68de7c6c7d79bd6b403198cf0fc9ea31f20f2c4e8c617d6fc9115cf988b955f99e193eae8c6b9484846756cca90b54a9b84ca6350da78bb3116a5f2de8adeea6b808c8cba8103de4eab29d91c120619858d04ad8351135a5d63b98d025be19452d2a5e1b8457dff18");
        byte[] arr = SSLUtils.hexStringToByteArray("010400000000c1fd3b14341995478b628b1027ecbdbcfcc09043c824181c665fb7fb21b95fb4c7fa92188a5c02c2b71ee74c54b397ce4d06dac3e14e0c4b1fb3e4de770b1a0f34e42a202e56a1905e68158223c9647efed07ad9a025fbc4f0a6c4ea925c8591c3a660ec1a9a5ed6193f07768af51b8e4bc24c76125c88794d36b7d7749970a0f50ec8e962adf1360406f5e747e37ea6de020728a34b57d68de7c6c7d79bd6b403198cf0fc9ea31f20f2c4e8c617d6fc9115cf988b955f99e193eae8c6b9484846756cca90b54a9b84ca6350da78bb3116a5f2de8adeea6b808c8cba8103de4eab29d91c120619858d04ad8351135a5d63b98d025be19452d2a5e1b8457dff18");
        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.put(arr);
        buf.flip();

        SessionTicket sessionTicket = new SessionTicket();
        sessionTicket.setSSL(buf, ExtensionType.SessionTicket, ExtensionType.SessionTicket.getType());

        assertArrayEquals(arrEq, sessionTicket.toByte());
    }

    @Test
    void getData() {
    }
}