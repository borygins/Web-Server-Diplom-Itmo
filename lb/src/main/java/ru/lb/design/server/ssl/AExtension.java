package ru.lb.design.server.ssl;

import ru.lb.impl.server.ssl.Extensions.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class AExtension implements IExtension {
    protected ExtensionType extensionType;
    protected short type;
    protected short len;

    @Override
    public void setSSL(ByteBuffer buffer, ExtensionType extensionType, short type) {
        if(extensionType == null){
            this.type = type;
        }else {
            this.extensionType = extensionType;
        }
        len = buffer.getShort();
        readBuf(buffer);
    }

    protected abstract void readBuf(ByteBuffer buffer);

    @Override
    public byte[] toByte() {
        ByteBuffer outByte = ByteBuffer.allocate(4 + this.len);
        outByte.putShort((extensionType != null) ? extensionType.getType() : this.type);
        outByte.putShort(len);
        if(len > 0) {
            getByte(outByte);
        }
        return outByte.array();
    }

    protected abstract void getByte(ByteBuffer byteBuffer);

    public ExtensionType getExtensionType() {
        return extensionType;
    }

    public short getLen() {
        return len;
    }

    public static AExtension getExtension(ByteBuffer byteBuffer){
        short tempShort = byteBuffer.getShort();
        ExtensionType extensionType = Arrays.stream(ExtensionType.values()).
                filter((q1) -> q1.getType() == tempShort).findFirst().orElse(null);
        AExtension out = null;
        if(extensionType == null){
            out = new UnknownExtensions();
        } else {
            switch (extensionType) {
                case KeyShare:
                    out = new KeyShare();
                    break;
                case TokenBinding:
                    out = new TokenBinding();
                    break;
                case ServerName:
                    out = new ServerName();
                    break;
                case SessionTicket:
                    out = new SessionTicket();
                    break;
                case StatusRequest:
                    out = new StatusRequest();
                    break;
                case EcPointFormats:
                    out = new EcPointFormats();
                    break;
                case SupportedGroups:
                    out = new SupportedGroups();
                    break;
                case RenegotiationInfo:
                    out = new RenegotiationInfo();
                    break;
                case SupportedVersions:
                    out = new SupportedVersions();
                    break;
                case SignatureAlgorithms:
                    out = new SignatureAlgorithms();
                    break;
                case ExtendedMasterSecret:
                    out = new ExtendedMasterSecret();
                    break;
                case ApplicationLayerProtocolNegotiation:
                    out = new ApplicationLayerProtocolNegotiation();
                    break;
                default:
                    out = new UnknownExtensions();
                    break;
            }
        }
        out.setSSL(byteBuffer, extensionType, tempShort);
        return out;
    }
}
