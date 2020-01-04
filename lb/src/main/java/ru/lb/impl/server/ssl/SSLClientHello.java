package ru.lb.impl.server.ssl;

import ru.lb.design.server.ssl.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class SSLClientHello implements IHandshake {
    private HandshakeType typeProtokol;
    private int lenHandshake;
    private TLSv tlsV;
    private int lenMsg;
    private SSLRandom sslRandom;
    byte sessionLenID;
    byte[] sessionID;
    private short cipherSuitesLen;
    private List<CipherSuites> cipherSuites;

    byte lenCompressionMethod;
    private List<CompressionMethod> compressionMethods;

    private boolean err = false;

    @Override
    public void setSSL(ByteBuffer buf, short len, HandshakeType typeProtokol) {
        int lenHandshake = buf.position();

        short tempShort;
        byte tempByte;

        tempByte = buf.get();
        this.typeProtokol = typeProtokol;
        lenMsg = SSLUtils.convertByteToInt(buf, 3);
        tempShort = buf.getShort();
        tlsV = Arrays.stream(TLSv.values()).filter((q1) -> (q1.getType() == tempShort)).findFirst().get();

        sslRandom = new SSLRandom(buf);

        sessionLenID = buf.get();
        if(sessionLenID > 0){
            sessionID = new byte[sessionLenID];
            buf.get(sessionID);
        }

        cipherSuitesLen = buf.getShort();
        if(cipherSuitesLen > 0) {
            cipherSuites = new ArrayList<>();
            for (int i = 0; i < cipherSuitesLen/2; i++) {
                cipherSuites.add(Arrays.stream(CipherSuites.values()).
                        filter((q1) -> q1.getTypeCipherSuites() == buf.getShort()).findFirst().orElse(null));
            }
            cipherSuites = cipherSuites.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
        }

        lenCompressionMethod = buf.get();
        if(lenCompressionMethod > 0){
            for (int i = 0; i < lenCompressionMethod; i++) {
                compressionMethods.add(Arrays.stream(CompressionMethod.values()).
                        filter((q1) -> q1.getType() == buf.get()).findFirst().orElse(null));
            }
            compressionMethods = compressionMethods.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
        }

        if(len + lenHandshake == buf.position())
            err = true;
    }

    @Override
    public byte[] toByte() {
        return new byte[0];
    }
}
