package ru.lb.design.server;

import ru.lb.design.config.IConfig;

import java.net.InetSocketAddress;

public interface IHistoryQuery {

    void setConfig(IConfig config);
    InetSocketAddress find(InetSocketAddress address, String host);

}
