package ru.lb.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.AServer;
import ru.lb.design.server.IIdConnect;
import ru.lb.design.server.ServerReadStatus;
import ru.lb.design.server.ServerWriteStatus;
import ru.lb.impl.exception.NotHostException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Server extends AServer {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server(boolean startServer, IConfig config) {
        super(startServer, config);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Метод обработки входящих данных.
     * @param key ключ из выборки селектора
     */
    @Override
    public void readable(SelectionKey key) {
        //Получаем канал и его идентификатор
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();

        ByteBuffer sharedBuffer = null;
        int bytes = -1;
        int countBuf = 3;
        ServerReadStatus status = ServerReadStatus.NEW_BUF;

        try {

            while (status != ServerReadStatus.EXIT) {
                if(status == ServerReadStatus.NEW_BUF) {
                    sharedBuffer = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(config.getSizeBuf());
                    sharedBuffer.clear();
                }

                bytes = socketChannel.read(sharedBuffer);

                status = this.read(key, idConnect, socketChannel,sharedBuffer, countBuf--, bytes);

            }

        } catch (IOException | RuntimeException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error writing back bytes");
                e.printStackTrace();
            }
            this.buf.add(sharedBuffer);
            sharedBuffer.clear();
            this.close(key);
        } catch ( NotHostException e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Хост не был найден в заголовках.");
                e.printStackTrace();
            }
            this.buf.add(sharedBuffer);
            sharedBuffer.clear();
            this.close(key);
        }
    }

    /**
     * Метод обработки исходящих соединений, в данном случае канал для соединения с сервером.
     * @param key ключ из выборки селектора
     */
    @Override
    public void writable(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();
        ServerWriteStatus writeStatus = ServerWriteStatus.CONTINUE;
        while (writeStatus != ServerWriteStatus.EXIT) {
            ByteBuffer sharedBuffer = idConnect.getAndRemoveBuf();

            if(sharedBuffer != null)
                System.out.println(new String(sharedBuffer.array(), 0, sharedBuffer.limit()));

            writeStatus = this.write(key, sharedBuffer, socketChannel);
        }

        if (idConnect.isStopConnect()) {
            idConnect.getInverseConnect().setStopConnect(true);
            this.close(key);
        }
    }

}