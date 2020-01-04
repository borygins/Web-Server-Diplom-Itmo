package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ExtensionSuportedGroups;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationLayerProtocolNegotiation extends AExtension {

    private short alpnLen;
    private List<String> alpnNextProtocols;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        alpnLen = buffer.getShort();
        int position = buffer.position();
        if(alpnLen > 0){
            alpnNextProtocols = new ArrayList<>();

            while (alpnLen + position < buffer.position()){
                byte[] arrTemp = new byte[buffer.get()];
                buffer.get(arrTemp);
                alpnNextProtocols.add(String.valueOf(arrTemp));
            }
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.putShort(alpnLen);
        for (String alpnNextProtocol: alpnNextProtocols) {
            byteBuffer.put((byte) alpnNextProtocol.length());
            byteBuffer.put(alpnNextProtocol.getBytes());
        }
    }

    public short getAlpnLen() {
        return alpnLen;
    }

    public List<String> getAlpnNextProtocols() {
        return alpnNextProtocols;
    }
}
