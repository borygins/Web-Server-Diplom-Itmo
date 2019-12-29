package design.server;

import design.config.IConfig;
import impl.config.NotHostException;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface IServer extends Runnable {
    void start();
    void acceptable(SelectionKey key);
    void readable(SelectionKey key);
    void writable(SelectionKey key) throws IOException;
    void connectable(SelectionKey key);
    void close(SelectionKey key);
    void addConfig(IConfig config);
    void newConnectToServer();
    void createBuf(int count, int bufSize);
    void setHistoryQuery(IHistoryQuery newHistoryQuery);
    String getHostConnection(ByteBuffer buf) throws NotHostException;
}
