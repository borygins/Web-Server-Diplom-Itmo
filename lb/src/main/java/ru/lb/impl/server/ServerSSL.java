package ru.lb.impl.server;

import ru.lb.design.config.IConfig;
import ru.lb.design.server.IIdConnect;
import ru.lb.design.server.ServerReadStatus;
import ru.lb.impl.exception.NotHostException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ServerSSL extends Server {
    public ServerSSL(boolean startServer, IConfig config) {
        super(startServer, config);
    }

    @Override
    protected ServerReadStatus read(SelectionKey key, IIdConnect idConnect, SocketChannel socketChannel, ByteBuffer sharedBuffer, int countBuf, int bytes) throws IOException, NotHostException {
        sharedBuffer.flip();

        return super.read(key, idConnect, socketChannel, sharedBuffer, countBuf, bytes);
    }
}
