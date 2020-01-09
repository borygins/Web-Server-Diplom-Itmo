package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;

import java.nio.ByteBuffer;

public class ServerName extends AExtension {

    private short lenServerName;
    private byte type;
    private short lenHostName;
    private byte[] hostName;

    @Override
    protected void readBuf(ByteBuffer buffer) {
        lenServerName = buffer.getShort();
        type = buffer.get();
        lenHostName = buffer.getShort();
        if(lenHostName > 0){
            hostName = new byte[lenHostName];
            buffer.get(hostName);
        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.putShort(lenServerName);
        byteBuffer.put(type);
        byteBuffer.putShort(lenHostName);
        byteBuffer.put(hostName);
    }

    public short getLenServerName() {
        return lenServerName;
    }

    public byte getType() {
        return type;
    }

    public short getLenHostName() {
        return lenHostName;
    }

    public byte[] getHostName() {
        return hostName;
    }

    public void setLenServerName(short lenServerName) {
        this.lenServerName = lenServerName;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setLenHostName(short lenHostName) {
        this.lenHostName = lenHostName;
    }

    public void setHostName(byte[] hostName) {
        this.hostName = hostName;
    }
}
