package ru.lb.design.server;

import org.slf4j.Logger;
import ru.lb.design.config.IConfig;
import ru.lb.impl.exception.NotHostException;
import ru.lb.impl.server.IdConnect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AServer implements IServer {

    protected IConfig config;
    protected Selector selector;
    protected ServerSocket serverSocket;
    protected final boolean startServer;
    protected ArrayList<ByteBuffer> buf;
    protected static final Queue<Selector> queSelector = new ConcurrentLinkedQueue<>();
    protected static IHistoryQuery historyQuery;
    

    /**
     * Конструктор.
     * @param startServer Если true, то при вызове метода start() будет добавлен в селектор сервер.
     * @param config конфигурация сервера.
     */
    public AServer(boolean startServer, IConfig config) {
        this.startServer = startServer;
        this.createBuf(config.getCountBuf(), config.getSizeBuf());
        this.config = config;

        try {
            if (!startServer) {
                this.selector = Selector.open();
                queSelector.offer(this.selector);
            } else {
                this.selector = Selector.open();
                queSelector.offer(this.selector);
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                this.serverSocket = serverSocketChannel.socket();
                this.serverSocket.bind(this.config.getIPserver());
                serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
                if(getLogger().isInfoEnabled()){
                    getLogger().info("Выполнен запуск и регистрация канала " + this.serverSocket.toString());
                }
            }
        } catch (IOException e) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Unable to setup environment");
        }

    }

    /**
     * Метод для добавления реализации объекта хренияния истории соединения.
     * @param newHistoryQuery реализация хранения истории соединий для подбора ip-адреса сервера для передачи данных.
     */
    @Override
    public void setHistoryQuery(IHistoryQuery newHistoryQuery) {
        historyQuery = newHistoryQuery;
        historyQuery.setConfig(config);
    }

    /**
     * Метод создания селектора для обратотки каналов.
     */
    @Override
    public void start() {

        if(getLogger().isInfoEnabled()){
            if(this.startServer) {
                getLogger().info("Запуск главного потока");
            } else {
                getLogger().info("Запуск следующего потока");
            }
        }

        try {
            while (!Thread.currentThread().isInterrupted()) {
                int count = this.selector.select();
                // нечего обрабатывать
                if (count == 0) {
                    continue;
                }

                Set keySet = this.selector.selectedKeys();
                Iterator itor = keySet.iterator();

                while (itor.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) itor.next();
                    itor.remove();

                    if (startServer && selectionKey.isAcceptable()) {
                        acceptable(selectionKey);
                    } else if (selectionKey.isConnectable()) {
                        connectable(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        readable(selectionKey);
                    } else if (selectionKey.isValid() && selectionKey.isWritable()) {
                        writable(selectionKey);
                    }
                }
            }
        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Error during select()");
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод обработки входящих соединений.
     * @param key ключ из выборки селектора
     */
    @Override
    public void acceptable(SelectionKey key) {
        Selector selectorTemp = queSelector.poll();
        queSelector.offer(selectorTemp);
        try {
            Socket socket = this.serverSocket.accept();

            if (getLogger().isInfoEnabled())
                getLogger().info("Установлено соединение с клиентом: " + socket + ", поток: " + Thread.currentThread().getName());

            //Принимаем входящее соединение и снимаем блокировку
            SocketChannel client = socket.getChannel();
            client.configureBlocking(false);
            SelectionKey selectionKeyClient = client.register(selectorTemp, client.validOps() & ~SelectionKey.OP_WRITE);

            //Проверяем на существование идентификатора. Если его нет, создаем.
            IIdConnect idConnect = (IIdConnect) key.attachment();
            if (idConnect == null) {
                idConnect = new IdConnect();
                idConnect.setClient(true);
                idConnect.setInverseConnect(new IdConnect());
                idConnect.getInverseConnect().setInverseConnect(idConnect);
                idConnect.getInverseConnect().setServer(true);
                idConnect.setSelectionKey(selectionKeyClient);
                selectionKeyClient.attach(idConnect);
            }

            if(!this.selector.equals(selectorTemp)){
                selectorTemp.wakeup();
            }
        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Unable to use channel");
                e.printStackTrace();
            }
            this.close(key);
        }

    }

    protected abstract Logger getLogger();

    /**
     * Метод обработки входящих данных.
     * @param key ключ из выборки селектора
     */
    @Override
    public abstract void readable(SelectionKey key);

    protected ServerReadStatus read(SelectionKey key, IIdConnect idConnect, SocketChannel socketChannel, ByteBuffer sharedBuffer, int countBuf, int bytes) throws IOException, NotHostException {
        if(idConnect.isClient() && idConnect.getHostConnection() == null) {
            idConnect.setHostConnection(historyQuery.find((InetSocketAddress) socketChannel.getRemoteAddress(), sharedBuffer));
            idConnect.getInverseConnect().setHostConnection(idConnect.getHostConnection());
        }

        boolean writeData = (countBuf == 0) | (sharedBuffer.position() != 0 & bytes < 1 & sharedBuffer.position() != sharedBuffer.capacity());
        boolean closeSelectionKey = (bytes == -1);

        if (writeData) {
            idConnect.getInverseConnect().addBuf(sharedBuffer);
            sharedBuffer.flip();

            if(idConnect.getInverseConnect().getSelectionKey() == null) {
                SocketChannel writer = SocketChannel.open();
                writer.configureBlocking(false);
                writer.connect(idConnect.getHostConnection());
                SelectionKey keyWriter = writer.register(key.selector(), socketChannel.validOps(), idConnect.getInverseConnect());
                idConnect.getInverseConnect().setSelectionKey(keyWriter);
            } else {
                idConnect.getInverseConnect().getSelectionKey().interestOps(socketChannel.validOps() & ~SelectionKey.OP_READ);
            }
            if(!closeSelectionKey)
                return ServerReadStatus.EXIT;
        }

        if (closeSelectionKey) {
            idConnect.getInverseConnect().setStopConnect(true);
            idConnect.getInverseConnect().getSelectionKey().interestOps(socketChannel.validOps());
            sharedBuffer.clear();
            this.buf.add(sharedBuffer);
            this.close(key);
            return ServerReadStatus.EXIT;
        }


        if (sharedBuffer.position() == sharedBuffer.capacity()) {
            idConnect.getInverseConnect().addBuf(sharedBuffer);
            sharedBuffer.flip();
            sharedBuffer = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(config.getSizeBuf());
        }

        countBuf--;

        return ServerReadStatus.CONTINUE;
    }

    /**
     * Метод обработки исходящих соединений, в данном случае канал для соединения с сервером.
     * @param key ключ из выборки селектора
     */
    @Override
    public abstract void writable(SelectionKey key);

    protected ServerWriteStatus write(SelectionKey key, IIdConnect idConnect, SocketChannel socketChannel){
        ByteBuffer sharedBuffer = idConnect.getAndRemoveBuf();
        if(sharedBuffer == null){
            key.interestOps(socketChannel.validOps() & ~SelectionKey.OP_WRITE);
            return ServerWriteStatus.EXIT;
        }

        try {
            while (sharedBuffer.hasRemaining()) {
                socketChannel.write(sharedBuffer);
            }
            sharedBuffer.rewind();
            this.buf.add(sharedBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ServerWriteStatus.CONTINUE;
    }

    /**
     * Метод обработки исходящих соединений, в данном случае канал для соединения с сервером.
     * @param key ключ из выборки селектора
     */
    @Override
    public void connectable(SelectionKey key)  {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();

        try {
            socketChannel.finishConnect();
            key.interestOps(socketChannel.validOps());
            if (getLogger().isInfoEnabled())
                getLogger().info("Установлено соединение с севрером: " + socketChannel);
        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Err connect..." + key.channel().toString());
                e.printStackTrace();
            }
            this.close(key);

            SocketChannel writer = null;
            try {
                writer = SocketChannel.open();
                writer.configureBlocking(false);
                writer.connect(idConnect.getHostConnection());
                writer.register(key.selector(), writer.validOps(), idConnect);
            }catch (IOException ex){
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Err connect..." + key.channel().toString());
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Метод закрытия соединения каналов
     * @param key ключ из выборки селектора
     */
    @Override
    public void close(SelectionKey key) {
        //Закрываем соединение, вроде key.cancel(); делать не надо.
        try {
            SocketChannel sc = (SocketChannel) key.channel();
            if (getLogger().isDebugEnabled())
                getLogger().debug("Разорвано соединение с: " + sc.toString() + ", селектор: " + key.selector().toString() + ", поток: "+ Thread.currentThread().getName());
            sc.close();
        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Ошибка разрыва соединения: " + key.channel().toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    /**
     * Создание буфера буферов)))
     * @param count Количество объектов буфера
     * @param bufSize Размер буфера.
     */
    @Override
    public void createBuf(int count, int bufSize) {
        this.buf = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            this.buf.add(ByteBuffer.allocate(bufSize));
        }
    }

    @Override
    public void run() {
        start();
    }
}