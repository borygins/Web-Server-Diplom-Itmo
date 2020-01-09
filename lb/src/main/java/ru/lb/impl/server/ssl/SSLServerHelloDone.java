package ru.lb.impl.server.ssl;

import ru.lb.design.server.ssl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SSLServerHelloDone implements IHandshake {
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

    private short extensionsLen;
    private List<AExtension> extensions;
    private boolean err = false;

    @Override
    public void setSSL(ByteBuffer buf, short len, HandshakeType typeProtokol) {
        int lenHandshake = buf.position();

        short tempShort;
        byte tempByte;


        this.typeProtokol = typeProtokol;
        lenMsg = SSLUtils.convertByteToInt(buf, 3);
        tempShort = buf.getShort();
        tlsV = Arrays.stream(TLSv.values()).filter((q1) -> (q1.getType() == tempShort)).findFirst().get();

        sslRandom = new SSLRandom();
        sslRandom.setSSL(buf);

        sessionLenID = buf.get();
        if(sessionLenID > 0){
            sessionID = new byte[sessionLenID];
            buf.get(sessionID);
        }

        cipherSuitesLen = buf.getShort();
        if(cipherSuitesLen > 0) {
            cipherSuites = new ArrayList<>();
            for (int i = 0; i < cipherSuitesLen/2; i++) {
                short finalTempShort = buf.getShort();
                cipherSuites.add(Arrays.stream(CipherSuites.values()).
                        filter((q1) -> q1.getTypeCipherSuites() == finalTempShort).findFirst().orElse(null));
            }
            cipherSuites = cipherSuites.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
        }

        lenCompressionMethod = buf.get();
        if(lenCompressionMethod > 0){
            compressionMethods = new ArrayList<>();
            for (int i = 0; i < lenCompressionMethod; i++) {
                tempByte = buf.get();
                byte finalTempByte = tempByte;
                compressionMethods.add(Arrays.stream(CompressionMethod.values()).
                        filter((q1) -> q1.getType() == finalTempByte).findFirst().orElse(null));
            }
            compressionMethods = compressionMethods.stream().filter((q1) -> q1 != null).collect(Collectors.toList());
        }

        extensionsLen = buf.getShort();
        int endExtensionsLen = extensionsLen + buf.position();
        if(extensionsLen > 0){
            extensions = new ArrayList<>();
            while (buf.position() < endExtensionsLen){
                extensions.add(AExtension.getExtension(buf));
            }
        }

        if(len + lenHandshake == buf.position())
            err = true;
    }

    @Override
    public byte[] toByte() {
        return new byte[0];
    }
}
