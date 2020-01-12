package ru.lb.impl.server;

import ru.lb.design.config.IConfig;
import ru.lb.design.server.IHistoryQuery;
import ru.lb.impl.exception.NotHostException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class HistoryQueryRandom extends HistoryQuery {

    private IConfig config;
    private InetSocketAddress last;

    public HistoryQueryRandom() {

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
    public synchronized InetSocketAddress find(InetSocketAddress address, String host, boolean remove) {
        InetSocketAddress out;
        while (true) {
            out = config.getRandomIPserver();
            if(!out.equals(last))
                break;
        }
        last = out;
        return out;
    }
}
