package ru.lb.design.server;

import org.slf4j.Logger;
import ru.lb.design.config.IConfig;
import ru.lb.impl.config.ConfigIPServer;
import ru.lb.impl.exception.NotHostException;
import ru.lb.impl.server.IdConnect;
import ru.lb.impl.server.ResultCheckSSL;
import ru.lb.impl.server.Server;
import ru.lb.impl.server.ServerSSL;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;

public abstract class AServer implements IServer {

    protected IConfig config;
    protected Selector selector;
    protected ServerSocket serverSocket;
    protected final boolean startServer;
    protected LinkedList<ByteBuffer> buf;
    protected final static Map<String, Queue<Selector>> queSelector = new HashMap<>();
    protected final String typeClass;
    protected static IHistoryQuery historyQuery;

    public static IServer serverFabric(IConfig config, ConfigIPServer configIPServer, boolean createServ) {
        if (configIPServer.isSsl()) {
            if(createServ) {
                return new ServerSSL(true, config, configIPServer);
            } else {
                return new ServerSSL(false, config, null);
            }

        } else {
            if(createServ) {
               return new Server(true, config, configIPServer, true);
            } else {
              return  new Server(false, config, null, true);
            }
        }
    }


    /**
     * Конструктор.
     *
     * @param startServer  Если true, то при вызове метода start() будет добавлен в селектор сервер.
     * @param config       конфигурация сервера.
     * @param createBuffer условие на создание массива буферов.
     */
    public AServer(boolean startServer, IConfig config, ConfigIPServer configIPServer, boolean createBuffer) {
        this.startServer = startServer;
        this.config = config;
        this.typeClass = getClass().getTypeName();

        if (createBuffer)
            this.createBuf(config.getCountBuf(), config.getSizeBuf());

        try {
            this.selector = Selector.open();
            synchronized (queSelector) {
                if (!queSelector.containsKey(typeClass)) {
                    queSelector.put(typeClass, new ConcurrentLinkedQueue<>());
                }
                queSelector.get(typeClass).offer(this.selector);
            }
            if (startServer) {

                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                this.serverSocket = serverSocketChannel.socket();
                this.serverSocket.bind(configIPServer.getIpServer());
                serverSocketChannel.register(this.selector, serverSocketChannel.validOps());
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("Выполнен запуск и регистрация канала " + this.serverSocket.toString());
                }
            }
        } catch (IOException e) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Unable to setup environment", e);
        }

    }

    /**
     * Метод для добавления реализации объекта хренияния истории соединения.
     *
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

        if (getLogger().isInfoEnabled()) {
            if (this.startServer) {
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
                getLogger().error("Error during select()", e);
            }
        }
    }

    @Override
    public ByteBuffer getBuffer(IIdConnect idConnect) {
        ByteBuffer buf = null;
        try {
            buf = this.buf.removeLast();
        }catch (NoSuchElementException e){
            buf = ByteBuffer.allocate(config.getSizeBuf());
        }

        buf.clear();
        return buf;
    }

    @Override
    public void addBuffer(IIdConnect idConnect, ByteBuffer byteBuffer) {
        this.buf.addFirst(byteBuffer);
    }

    /**
     * Метод обработки входящих соединений.
     *
     * @param key ключ из выборки селектора
     */
    @Override
    public void acceptable(SelectionKey key) {
        Selector selectorTemp = queSelector.get(typeClass).poll();
        queSelector.get(typeClass).offer(selectorTemp);
        try {
            Socket socket = this.serverSocket.accept();

            if (getLogger().isInfoEnabled())
                getLogger().info("Установлено соединение с клиентом: " + socket + ", поток: " + Thread.currentThread().getName());

            //Принимаем входящее соединение и снимаем блокировку
            SocketChannel client = socket.getChannel();
            client.configureBlocking(false);
            regOnSelector(key, client, selectorTemp);

        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Unable to use channel", e);
            }
            this.close(key, null);
        }

    }

    /**
     * @param key
     * @param client
     * @param selectorTemp
     * @return
     * @throws ClosedChannelException
     */
    protected IIdConnect regOnSelector(SelectionKey key, SocketChannel client, Selector selectorTemp) throws IOException {

        SelectionKey selectionKeyClient = client.register(selectorTemp, SelectionKey.OP_READ);

        //Проверяем на существование идентификатора. Если его нет, создаем.
        IIdConnect idConnect = (IIdConnect) key.attachment();
        if (idConnect == null) {
            idConnect = new IdConnect();
            idConnect.setClient(true);
            idConnect.setInverseConnect(new IdConnect());
            idConnect.setClientConnection((InetSocketAddress) client.getRemoteAddress());
            idConnect.getInverseConnect().setInverseConnect(idConnect);
            idConnect.getInverseConnect().setServer(true);
            idConnect.setSelectionKey(selectionKeyClient);
            selectionKeyClient.attach(idConnect);
        }

        if (!this.selector.equals(selectorTemp)) {
            selectorTemp.wakeup();
        }

        return idConnect;
    }

    protected abstract ResultCheckSSL checkSSL(SocketChannel socketChannel, boolean typeClientMode);

    protected abstract Logger getLogger();

    @Override
    public void findHost(IIdConnect idConnect, ByteBuffer sharedBuffer) throws NotHostException {
        if (idConnect.isClient() && idConnect.getHostConnection() == null) {
            idConnect.setHostConnection(historyQuery.getHostConnection(sharedBuffer));
            idConnect.setHostConnection(historyQuery.find(idConnect.getClientConnection(), idConnect.getHostConnectionToString(), false));
        } else if (idConnect.isServer()) {
            idConnect.setHostConnection(historyQuery.find(idConnect.getInverseConnect().getHostConnection(), idConnect.getInverseConnect().getHostConnectionToString(), true));
        }
        idConnect.getInverseConnect().setHostConnection(idConnect.getHostConnection());
    }

    /**
     * Метод обработки входящих данных.
     *
     * @param key ключ из выборки селектора
     */
    @Override
    public abstract void readable(SelectionKey key);

    protected ServerReadStatus read(SelectionKey key, IIdConnect idConnect, SocketChannel socketChannel, ByteBuffer sharedBuffer, int countBuf, int bytes) throws IOException, NotHostException {

        this.findHost(idConnect, sharedBuffer);

        boolean writeData = (countBuf == 0) | (sharedBuffer.position() != 0 & bytes < 1 & sharedBuffer.position() != sharedBuffer.capacity());
        boolean closeSelectionKey = (bytes == -1);

        if (writeData) {
            idConnect.getInverseConnect().addBuf(sharedBuffer);
            sharedBuffer.flip();

            if (idConnect.getInverseConnect().getSelectionKey() == null) {
                idConnect.getInverseConnect().setSelectionKey(
                this.createConnectToServ(key.selector(), idConnect.getInverseConnect(),idConnect.getHostConnection()));
            } else {
                idConnect.setInverseInterestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            if (!closeSelectionKey)
                return ServerReadStatus.EXIT;
        }

        if (closeSelectionKey) {
            idConnect.setInverseInterestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            this.addBuffer(idConnect, sharedBuffer);
            this.close(key, idConnect);
            return ServerReadStatus.EXIT;
        }

        if (sharedBuffer.position() == sharedBuffer.capacity()) {
            idConnect.getInverseConnect().addBuf(sharedBuffer);
            sharedBuffer.flip();
            return ServerReadStatus.NEW_BUF;
        }

        return ServerReadStatus.CONTINUE;
    }

    /**
     * Метод обработки исходящих соединений, в данном случае канал для соединения с сервером.
     *
     * @param key ключ из выборки селектора
     */
    @Override
    public abstract void writable(SelectionKey key);

    protected ServerWriteStatus write(SelectionKey key, ByteBuffer sharedBuffer, SocketChannel socketChannel, IIdConnect idConnect) {
        if (sharedBuffer == null) {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            return ServerWriteStatus.EXIT;
        }

        try {
            while (sharedBuffer.hasRemaining()) {
                socketChannel.write(sharedBuffer);
            }
            sharedBuffer.rewind();
            this.addBuffer(idConnect, sharedBuffer);
        } catch (IOException e) {
            if(getLogger().isErrorEnabled())
                getLogger().error("Ошибка создания соедениения с сервером.",e);
            idConnect.addBufFirst(sharedBuffer);
            try {
                if(idConnect.isServer())
                idConnect.setSelectionKey(
                        createConnectToServ(key.selector(), idConnect, idConnect.getHostConnection()));
                return ServerWriteStatus.EXIT;
            } catch (IOException ex) {
                if(getLogger().isErrorEnabled())
                    getLogger().error("Ошибка создания повторного соедениения с сервером",e);
            }
        }

        return ServerWriteStatus.CONTINUE;
    }

    /**
     * Метод обработки исходящих соединений, в данном случае канал для соединения с сервером.
     *
     * @param key ключ из выборки селектора
     */
    @Override
    public void connectable(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();

        try {
            socketChannel.finishConnect();
            key.interestOps(SelectionKey.OP_WRITE);
            if (getLogger().isInfoEnabled())
                getLogger().info("Установлено соединение с севрером: " + socketChannel + ", селектор: " + key.selector()+ ", поток: " + Thread.currentThread().getName());
        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Err connect..." + key.channel().toString(), e);
            }
            this.close(key, idConnect);

            try {
                if (idConnect.incrementCountConnection() > 2) {
                    this.findHost(idConnect, null);
                }

                idConnect.setSelectionKey(
                        this.createConnectToServ(key.selector(), idConnect, idConnect.getHostConnection()));

            } catch (IOException | NotHostException ex) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Err connect..." + key.channel().toString(), e);
                }
            }
        }

    }

    protected SelectionKey createConnectToServ(Selector selector, IIdConnect idConnect, InetSocketAddress host) throws IOException {
        SocketChannel writer = SocketChannel.open();
        writer.configureBlocking(false);
        writer.connect(host);
       return writer.register(selector, SelectionKey.OP_CONNECT, idConnect);
    }


    /**
     * Метод закрытия соединения каналов
     *
     * @param key ключ из выборки селектора
     */
    @Override
    public void close(SelectionKey key, IIdConnect idConnect) {
        //Закрываем соединение, вроде key.cancel(); делать не надо.
        if(idConnect == null || idConnect.isClient() || idConnect.countBuff() < 1) {
            if(idConnect.getInverseConnect() != null) {
                idConnect.getInverseConnect().setStopConnect(true);
                idConnect.setInverseInterestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            try {
                key.cancel();
                SocketChannel sc = (SocketChannel) key.channel();
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Разорвано соединение с: " + sc.toString() + ", селектор: " + key.selector().toString() + ", поток: " + Thread.currentThread().getName());
                sc.close();
                idConnect.setSelectionKey(null);
            } catch (IOException e) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Ошибка разрыва соединения: " + key.channel().toString(), e);
                }
            }
        }
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    /**
     * Создание буфера буферов)))
     *
     * @param count   Количество объектов буфера
     * @param bufSize Размер буфера.
     */
    @Override
    public void createBuf(int count, int bufSize) {
        this.buf = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            this.buf.add(ByteBuffer.allocate(bufSize));
        }
    }

    @Override
    public void run() {
        start();
    }
}