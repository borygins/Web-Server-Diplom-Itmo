package ru.lb.impl.config;

import java.net.InetSocketAddress;

public class ConfigIPServer {
    private InetSocketAddress ipServer;
    private boolean ssl;

    public ConfigIPServer(InetSocketAddress ipServer, boolean ssl) {
        this.ipServer = ipServer;
        this.ssl = ssl;
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
}
