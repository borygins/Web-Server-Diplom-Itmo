package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ECPointFormatType;
import ru.lb.design.server.ssl.TLSv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcPointFormats extends AExtension {

    private byte ecPointFormatLen;
    private List<ECPointFormatType> ecPointFormatTypes;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        ecPointFormatLen = buffer.get();

        if(ecPointFormatLen > 0){
            ecPointFormatTypes = new ArrayList<>();
            for (int i = 0; i < ecPointFormatLen; i++) {
                ecPointFormatTypes.add(
                        Arrays.stream(ECPointFormatType.values()).filter((q1) -> (q1.getType() == buffer.get())).findFirst().get()
                );
            }
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {

        byteBuffer.put(ecPointFormatLen);
        for (ECPointFormatType ecPointFormatType: ecPointFormatTypes) {
            byteBuffer.putShort(ecPointFormatType.getType());
        }
    }


}
