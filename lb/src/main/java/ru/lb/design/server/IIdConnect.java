package ru.lb.design.server;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;

public interface IIdConnect {

    List<ByteBuffer> getAllBuf();
    void addBuf(ByteBuffer buffer);
    void addBufFirst(ByteBuffer buffer);
    int countBuff();
    ByteBuffer getAndRemoveBuf();

    void setStopConnect(boolean stopConnect);
    boolean isStopConnect();

    IIdConnect getInverseConnect();
    void setInverseConnect(IIdConnect connect);

    void setSelectionKey(SelectionKey key);
    SelectionKey getSelectionKey();

    void setHostConnection(String host);
    String getHostConnectionToString();

    void setHostConnection(InetSocketAddress host);
    InetSocketAddress getHostConnection();

    void setClientConnection(InetSocketAddress client);
    InetSocketAddress getClientConnection();

    void setServer(boolean server);
    void setClient(boolean client);

    boolean isClient();
    boolean isServer();

    int incrementCountConnection();
    void resetCountConnection();

    void setSSLEngine(SSLEngine engine);
    SSLEngine getSSLEngine();

    void setMyInterestOps(int interestOps);
    void setInverseInterestOps(int interestOps);
}
