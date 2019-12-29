package ru.lb.impl.server;

import ru.lb.design.config.IConfig;
import ru.lb.design.server.IHistoryQuery;

import java.net.InetSocketAddress;

public class HistoryQueryRandom implements IHistoryQuery {

    private IConfig config;
    private InetSocketAddress last;

    public HistoryQueryRandom() {

    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    @Override
    public synchronized InetSocketAddress find(InetSocketAddress address, String host) {
        InetSocketAddress out;
        while (true) {
            out = config.getRandomIPserver(host);
            if(!out.equals(last))
                break;
        }
        last = out;
        return out;
    }
}
