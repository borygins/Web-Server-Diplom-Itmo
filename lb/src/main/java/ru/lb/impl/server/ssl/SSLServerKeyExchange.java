package ru.lb.impl.server.ssl;

import ru.lb.design.server.ssl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SSLServerKeyExchange implements IHandshake {
    private short lenHandshakeAll;
    private HandshakeType typeProtokol;
    private int lenMsg;
    private CurveType named_curve;
    private ExtensionSuportedGroups extensionSuportedGroups;
    private byte pubKeyLen;
    private byte[] pubKey;
    private SignatureAlgorithm signatureAlgorithm;
    private short lenKey;
    private byte[] key;

    private boolean err = false;

    @Override
    public void setSSL(ByteBuffer buf, short len, HandshakeType typeProtokol) {
        short lenHandshakeAll = len;
        int lenHandshake = buf.position();

        short tempShort;
        byte tempByte;

        tempByte = buf.get();
        this.typeProtokol = typeProtokol;
        lenMsg = SSLUtils.convertByteToInt(buf, 3);

        named_curve = Arrays.stream(CurveType.values()).filter((q1) -> (q1.getType() == buf.get())).findFirst().get();
        extensionSuportedGroups = Arrays.stream(ExtensionSuportedGroups.values()).filter((q1) -> (q1.getType() == buf.getShort())).findFirst().get();

        pubKeyLen = buf.get();
        if(pubKeyLen > 0){
            buf.get(pubKey);
        }

        signatureAlgorithm = Arrays.stream(SignatureAlgorithm.values()).filter((q1) -> (q1.getType() == buf.getShort())).findFirst().get();

        lenKey = buf.getShort();
        if(lenKey > 0){
            key = new byte[lenKey];
            buf.get(key);
        }

        if(len + lenHandshake == buf.position())
            err = true;
    }

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(lenHandshakeAll);
        buffer.put(typeProtokol.getType());
        buffer.put(typeProtokol.getType());

        return new byte[0];
    }
}
