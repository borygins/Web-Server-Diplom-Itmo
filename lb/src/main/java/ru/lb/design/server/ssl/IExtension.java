package ru.lb.design.server.ssl;

import java.nio.ByteBuffer;

public interface IExtension {
    void setSSL(ByteBuffer buffer, ExtensionType extensionType, short type);
    byte[] toByte();

}
