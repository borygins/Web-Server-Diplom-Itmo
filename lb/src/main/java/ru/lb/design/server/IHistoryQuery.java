package ru.lb.design.server;

import ru.lb.design.config.IConfig;
import ru.lb.impl.exception.NotHostException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface IHistoryQuery {

    void setConfig(IConfig config);
    InetSocketAddress find(InetSocketAddress address, ByteBuffer buf) throws NotHostException;
    String getHostConnection(ByteBuffer buf) throws NotHostException;
}
