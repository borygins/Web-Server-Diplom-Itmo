package ru.lb.impl.config;

import java.net.InetSocketAddress;

public class ConfigIPServer {
    private InetSocketAddress ipServer;
    private boolean ssl;
    private int countSelector;

    public ConfigIPServer() {
    }

    public ConfigIPServer(InetSocketAddress ipServer, boolean ssl, int countSelector) {
        this.ipServer = ipServer;
        this.ssl = ssl;
        this.countSelector = countSelector;
    }

    public InetSocketAddress getIpServer() {
        return ipServer;
    }

    public void setIpServer(InetSocketAddress ipServer) {
        this.ipServer = ipServer;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getCountSelector() {
        return countSelector;
    }

    public void setCountSelector(int countSelector) {
        this.countSelector = countSelector;
    }
}
