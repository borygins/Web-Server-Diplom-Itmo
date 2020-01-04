package ru.lb.impl.server.ssl;

import ru.lb.design.server.ssl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SSL implements ISSL {

    private ContentType contentType;
    private TLSv tlsV;
    private short len;
    private List<IHandshake> handshakes = new ArrayList<>();


    @Override
    public void setSSL(ByteBuffer buffer) {
        byte tempByte;
        short tempShort;
        contentType = Arrays.stream(ContentType.values()).filter((q1) -> (q1.getType() == buffer.get())).findFirst().orElse(null);
        tempShort = buffer.getShort();
        tlsV = Arrays.stream(TLSv.values()).filter((q1) -> (q1.getType() == tempShort)).findFirst().orElse(null);
        len = buffer.getShort();

        switch (contentType){
            case ApplicationData:
                break;
            case Handshake:
                this.handshake(buffer);
                break;
            case ChangeCipherSpec:
                break;
        }

    }

    private void handshake(ByteBuffer buffer){
        HandshakeType handshakeTypeTemp =  Arrays.stream(HandshakeType.values()).filter((q1) -> (q1.getType() == buffer.get())).findFirst().orElse(null);
        IHandshake handshake;

        switch (handshakeTypeTemp){
            case ClientHello:
                handshake = new SSLClientHello();
                handshake.setSSL(buffer, len, handshakeTypeTemp);
                handshakes.add(handshake);
                break;
            case Certificate:
                break;
            case ServerHello:
                break;
            case ServerHelloDone:
                break;
            case ServerKeyExchage:
                break;
            case CertificateStatus:
                break;
            case ClientKeyExchange:
                break;
        }
    }

    @Override
    public byte[] toByte() {
        return new byte[0];
    }
}
