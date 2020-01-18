package ru.lb.design.server;

import ru.lb.design.config.IConfig;
import ru.lb.impl.exception.NotHostException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface IServer extends Runnable {
    void start();
    void acceptable(SelectionKey key);
    void readable(SelectionKey key);
    void writable(SelectionKey key) throws IOException;
    void connectable(SelectionKey key);
    void close(SelectionKey key, IIdConnect idConnect);
    void setConfig(IConfig config);
    void createBuf(int count, int bufSize);
    void setHistoryQuery(IHistoryQuery newHistoryQuery);
    void findHost(IIdConnect iIdConnect, ByteBuffer sharedBuffer) throws NotHostException;
    ByteBuffer getBuffer(IIdConnect idConnect);
    void addBuffer(IIdConnect idConnect, ByteBuffer byteBuffer);
}
