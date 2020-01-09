package ru.lb.impl.server.ssl.Extensions;

import ru.lb.design.server.ssl.AExtension;

import java.nio.ByteBuffer;

public class StatusRequest extends AExtension {

    private byte certificateStatusType;
    private short responderIDListLen;
    private short requestExtensionsLen;
    @Override
    protected void readBuf(ByteBuffer buffer) {
        certificateStatusType = buffer.get();
        responderIDListLen = buffer.getShort();
        if(responderIDListLen > 0){

        }

        requestExtensionsLen = buffer.getShort();
        if(requestExtensionsLen > 0){

        }
    }

    @Override
    protected void getByte(ByteBuffer byteBuffer) {
        byteBuffer.put(certificateStatusType);
        byteBuffer.putShort(responderIDListLen);
        byteBuffer.putShort(requestExtensionsLen);
    }

    public byte getCertificateStatusType() {
        return certificateStatusType;
    }

    public short getResponderIDListLen() {
        return responderIDListLen;
    }

    public short getRequestExtensionsLen() {
        return requestExtensionsLen;
    }

    public void setCertificateStatusType(byte certificateStatusType) {
        this.certificateStatusType = certificateStatusType;
    }

    public void setResponderIDListLen(short responderIDListLen) {
        this.responderIDListLen = responderIDListLen;
    }

    public void setRequestExtensionsLen(short requestExtensionsLen) {
        this.requestExtensionsLen = requestExtensionsLen;
    }
}
