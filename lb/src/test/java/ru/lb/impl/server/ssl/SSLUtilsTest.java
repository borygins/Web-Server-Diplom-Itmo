package ru.lb.impl.server.ssl;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class SSLUtilsTest {


    @Test
    void convertIntToByte() {
        int temp = 329;
        assertArrayEquals(new byte[]{0,1,73}, SSLUtils.convertIntToByte(temp, 3));
    }


    @Test
    void testShort(){
//        1100 0000 0010 1100
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put((byte) 192);
        buffer.put((byte) 44);
        buffer.flip();
        System.out.println(buffer.getShort());

    }


    @Test
    void convertByteToInt(){
//        1100 0000 0010 1100
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) 0);
        buffer.put((byte) 1);
        buffer.put((byte) 213);
        buffer.flip();
        System.out.println(SSLUtils.convertByteToInt(buffer, 3));

    }
}