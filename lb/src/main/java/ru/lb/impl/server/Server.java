package ru.lb.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSOutput;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.AServer;
import ru.lb.design.server.IIdConnect;
import ru.lb.design.server.ServerReadStatus;
import ru.lb.design.server.ServerWriteStatus;
import ru.lb.impl.config.ConfigIPServer;
import ru.lb.impl.exception.NotHostException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Server extends AServer {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server(boolean startServer, IConfig config, ConfigIPServer configIPServer, boolean createBuffer) {
        super(startServer, config, configIPServer, createBuffer);
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
                    sharedBuffer = this.getBuffer(idConnect);
                }

                bytes = socketChannel.read(sharedBuffer);

                status = this.read(key, idConnect, socketChannel, sharedBuffer, countBuf--, bytes);

            }

        } catch (IOException | RuntimeException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error read bytes", e);
            }

            this.addBuffer(idConnect, sharedBuffer);
            this.close(key, idConnect);
        } catch (NotHostException e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Хост не был найден в заголовках.", e);
            }
            this.addBuffer(idConnect, sharedBuffer);
            this.close(key, idConnect);
        }
    }

    @Override
    protected ResultCheckSSL checkSSL(SocketChannel socketChannel, boolean typeClientMode) {
        return new ResultCheckSSL(false, null);
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

            writeStatus = this.write(key, sharedBuffer, socketChannel, idConnect);
        }

        if (idConnect.isStopConnect()) {
            this.close(key, idConnect);
        }
    }

}