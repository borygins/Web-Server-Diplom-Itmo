package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ExtensionSuportedGroups;
import ru.lb.design.server.ssl.TLSv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SupportedVersions extends AExtension {

    private byte supportedVersionsListLen;
    private List<TLSv> tlsVs;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        supportedVersionsListLen = buffer.get();

        if(supportedVersionsListLen > 0){
            tlsVs = new ArrayList<>();
            for (int i = 0; i < supportedVersionsListLen/2; i++) {
                short tempShort = buffer.getShort();
                tlsVs.add(
                    Arrays.stream(TLSv.values()).filter((q1) -> (q1.getType() == tempShort)).findFirst().orElse(null)
                );
            }
            tlsVs = tlsVs.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.put(supportedVersionsListLen);
        for (TLSv tlsV: tlsVs) {
            byteBuffer.putShort(tlsV.getType());
        }
    }

    public byte getSupportedVersionsListLen() {
        return supportedVersionsListLen;
    }

    public List<TLSv> getTlsVs() {
        return tlsVs;
    }

    public void setSupportedVersionsListLen(byte supportedVersionsListLen) {
        this.supportedVersionsListLen = supportedVersionsListLen;
    }

    public void setTlsVs(List<TLSv> tlsVs) {
        this.tlsVs = tlsVs;
    }
}
