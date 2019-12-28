package design.server;

import design.config.IConfig;

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
    void valid(SelectionKey key);
    void addConfig(IConfig config);
    void createBuf(int count, int bufSize);
}
