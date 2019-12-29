package design.server;

import design.config.IConfig;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public interface IHistoryQuery {

    void setConfig(IConfig config);
    InetSocketAddress find(InetSocketAddress address, String host);

}
