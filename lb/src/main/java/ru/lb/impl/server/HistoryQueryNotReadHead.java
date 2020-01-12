package ru.lb.impl.server;

import ru.lb.design.config.IConfig;
import ru.lb.impl.exception.NotHostException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class HistoryQueryNotReadHead extends HistoryQuery {

    private IConfig config;
    private InetSocketAddress last;

    public HistoryQueryNotReadHead() {

    }

    @Override
    public String getHostConnection(ByteBuffer buf) throws NotHostException {
        return null;
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    @Override
    public synchronized InetSocketAddress find(InetSocketAddress address, String host, boolean remove)  {
        return config.getRandomIPserver();
    }
}
