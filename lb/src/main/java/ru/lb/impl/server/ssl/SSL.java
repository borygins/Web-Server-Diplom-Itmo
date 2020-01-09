package ru.lb.impl.server.ssl;

import ru.lb.design.server.ssl.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SSL implements ISSL {

    private ContentType contentType;
    private TLSv tlsV;
    private short len;
    private List<IHandshake> handshakes = new ArrayList<>();

    private static SSL getFabric(HandshakeType typeProtokol){
        SSL ssl = null;
        switch (typeProtokol){
            case ServerHello:
                ssl = new SSL();
                ssl.contentType = ContentType.Handshake;
                ssl.tlsV = TLSv.TLS1_2;
                break;
        }

        return ssl;
    }


    @Override
    public void setSSL(ByteBuffer buffer) {
        byte tempByte;
        short tempShort;
        buffer.flip();
        tempByte = buffer.get();
        contentType = Arrays.stream(ContentType.values()).filter((q1) -> (q1.getType() == tempByte)).findFirst().orElse(null);
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

    public ContentType getContentType() {
        return contentType;
    }

    public TLSv getTlsV() {
        return tlsV;
    }

    public short getLen() {
        return len;
    }

    public List<IHandshake> getHandshakes() {
        return handshakes;
    }

    @Override
    public byte[] toByte() {
        ByteBuffer buf = ByteBuffer.allocate(8192);
        buf.put(contentType.getType());
        buf.putShort(tlsV.getType());
        buf.putShort(len);

        for(IHandshake handshake : handshakes){
            buf.put(handshake.toByte());
        }


        return new byte[0];
    }


}
