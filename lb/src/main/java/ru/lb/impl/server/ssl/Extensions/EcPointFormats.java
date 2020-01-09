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
                byte tempByte = buffer.get();
                ecPointFormatTypes.add(
                        Arrays.stream(ECPointFormatType.values()).filter((q1) -> (q1.getType() == tempByte)).findFirst().get()
                );
            }
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {

        byteBuffer.put(ecPointFormatLen);
        for (ECPointFormatType ecPointFormatType: ecPointFormatTypes) {
            byteBuffer.put(ecPointFormatType.getType());
        }
    }

    public byte getEcPointFormatLen() {
        return ecPointFormatLen;
    }

    public void setEcPointFormatLen(byte ecPointFormatLen) {
        this.ecPointFormatLen = ecPointFormatLen;
    }

    public List<ECPointFormatType> getEcPointFormatTypes() {
        return ecPointFormatTypes;
    }

    public void setEcPointFormatTypes(List<ECPointFormatType> ecPointFormatTypes) {
        this.ecPointFormatTypes = ecPointFormatTypes;
    }
}
