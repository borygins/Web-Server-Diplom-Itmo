package ru.lb.impl.server.ssl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SSLUtils {
    public static int convertByteToInt(ByteBuffer byteBuffer, int count){
        if(count > 4)
            throw new NumberFormatException("Для получения Integer, необходимо не больше 4 байт");
        int tepm = 0;
        byte switchByte = (byte) ((count - 1) * 8);
        byte[] arr = new byte[count];
        byteBuffer.get(arr);

        if(byteBuffer.order() == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < count; i++) {
                tepm = tepm | (arr[i] & 255) << switchByte;
                switchByte -= 8;
            }
        } else {
            for (int i = count - 1; i >= 0; i--) {
                tepm = tepm | (arr[i] & 255) << switchByte;
                switchByte -= 8;
            }
        }
        return tepm;
    }

    public static byte[] convertIntToByte(int value, int count) {
        if(count > 4)
            throw new NumberFormatException("Для получения Integer, необходимо не больше 4 байт");

        byte[] result = new byte[count];
        for(int i = count - 1; i >= 0; i--) {
            result[i] = (byte)(value & 0xFF);
            value >>>= 8;
        }
        return result;
    }
}
