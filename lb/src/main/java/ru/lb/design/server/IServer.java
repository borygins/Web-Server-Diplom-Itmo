package ru.lb.design.server;

import ru.lb.design.config.IConfig;
import ru.lb.impl.config.NotHostException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface IServer extends Runnable {
    void start();
    void acceptable(SelectionKey key);
    void readable(SelectionKey key);
    void writable(SelectionKey key) throws IOException;
    void connectable(SelectionKey key);
    void close(SelectionKey key);
    void setConfig(IConfig config);
    void createBuf(int count, int bufSize);
    void setHistoryQuery(IHistoryQuery newHistoryQuery);
    String getHostConnection(ByteBuffer buf) throws NotHostException;
}
