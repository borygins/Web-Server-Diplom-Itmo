package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;
import ru.lb.design.server.ssl.ExtensionSuportedGroups;
import ru.lb.design.server.ssl.TLSv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyShare extends AExtension {

    private short keyShareLen;
    private short reserved;
    private short keyExchageLen;
    private byte[] keyExchage;
    private ExtensionSuportedGroups extensionSuportedGroups;
    private short keyExchageGroupLen;
    private byte[] keyExchageGroup;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        keyShareLen = buffer.getShort();
        reserved = buffer.getShort();
        keyExchageLen = buffer.getShort();
        if(keyExchageLen > 0) {
            keyExchage = new byte[keyExchageLen];
            buffer.get(keyExchage);
        }

        extensionSuportedGroups = Arrays.stream(ExtensionSuportedGroups.values()).filter((q1) -> (q1.getType() == buffer.getShort())).findFirst().get();
        keyExchageGroupLen = buffer.getShort();

        if(keyExchageGroupLen > 0){
            keyExchageGroup = new byte[keyExchageGroupLen];
            buffer.get(keyExchageGroup);
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.putShort(keyShareLen);
        byteBuffer.putShort(reserved);
        byteBuffer.putShort(keyExchageLen);
        if(keyExchageLen > 0) {
            byteBuffer.put(keyExchage);
        }
        byteBuffer.putShort(extensionSuportedGroups.getType());
        byteBuffer.putShort(keyExchageGroupLen);
        if(keyExchageGroupLen > 0){
            byteBuffer.put(keyExchageGroup);
        }
    }

    public short getKeyShareLen() {
        return keyShareLen;
    }

    public short getReserved() {
        return reserved;
    }

    public short getKeyExchageLen() {
        return keyExchageLen;
    }

    public byte[] getKeyExchage() {
        return keyExchage;
    }

    public ExtensionSuportedGroups getExtensionSuportedGroups() {
        return extensionSuportedGroups;
    }

    public short getKeyExchageGroupLen() {
        return keyExchageGroupLen;
    }

    public byte[] getKeyExchageGroup() {
        return keyExchageGroup;
    }
}
