package impl.server;

import design.config.IConfig;
import design.server.IHistoryQuery;
import design.server.IIdConnect;
import design.server.IServer;
import impl.config.NotHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements IServer {

    private IConfig config;
    private Selector selector;
    private ServerSocket serverSocket;
    private final boolean startServer;
    private Long id;
    private ArrayList<ByteBuffer> buf;
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final Queue<Selector> queSelector = new ConcurrentLinkedQueue<>();
    private static IHistoryQuery historyQuery;

    /**
     * Конструктор.
     * @param startServer Если true, то при вызове метода start() будет добавлен в селектор сервер.
     * @param config конфигурация сервера.
     */
    public Server(boolean startServer, IConfig config) {
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
                if(LOG.isInfoEnabled()){
                    LOG.info("Выполнен запуск и регистрация канала " + this.serverSocket.toString());
                }
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled())
                LOG.debug("Unable to setup environment");
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

        if(LOG.isInfoEnabled()){
            if(this.startServer) {
                LOG.info("Запуск главного потока");
            } else {
                LOG.info("Запуск следующего потока");
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
            if (LOG.isErrorEnabled()) {
                LOG.error("Error during select()");
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

            if (LOG.isInfoEnabled())
                LOG.info("Установлено соединение с клиентом: " + socket);

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
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to use channel");
                e.printStackTrace();
            }
            this.close(key);
        }

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


        ByteBuffer sharedBuffer = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(config.getSizeBuf());
        sharedBuffer.clear();
        int bytes = -1;
        int countBuf = 3;
        try {

            while (true) {

                bytes = socketChannel.read(sharedBuffer);

                if(idConnect.isClient() && idConnect.getHostConnection() == null) {
                    idConnect.setHostConnection(historyQuery.find((InetSocketAddress) socketChannel.getRemoteAddress(),this.getHostConnection(sharedBuffer)));
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
                    break;
                }

                if (closeSelectionKey) {
                    idConnect.getInverseConnect().setStopConnect(true);
                    idConnect.getInverseConnect().getSelectionKey().interestOps(socketChannel.validOps());
                    sharedBuffer.clear();
                    this.buf.add(sharedBuffer);
                    this.close(key);
                    break;
                }


                if (sharedBuffer.position() == sharedBuffer.capacity()) {
                    idConnect.getInverseConnect().addBuf(sharedBuffer);
                    sharedBuffer.flip();
                    sharedBuffer = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(config.getSizeBuf());
                }

                countBuf--;

            }


        } catch (IOException | RuntimeException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error writing back bytes");
                e.printStackTrace();
            }
            this.close(key);
        } catch ( NotHostException e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Хост не был найден в заголовках.");
                e.printStackTrace();
            }
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
        StringBuilder out = new StringBuilder();
        while (true) {
            ByteBuffer sharedBuffer = idConnect.getAndRemoveBuf();

            if(sharedBuffer == null){
                key.interestOps(socketChannel.validOps() & ~SelectionKey.OP_WRITE);
                break;
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

        }

        if (idConnect.isStopConnect()) {
            idConnect.getInverseConnect().setStopConnect(true);
            this.close(key);
        }
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
            if (LOG.isInfoEnabled())
                LOG.info("Установлено соединение с севрером: " + socketChannel);
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Err connect..." + key.channel().toString());
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
                if (LOG.isErrorEnabled()) {
                    LOG.error("Err connect..." + key.channel().toString());
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
            if (LOG.isDebugEnabled())
                LOG.debug("Разорвано соединение с: " + sc.toString() + ", селектор: " + key.selector().toString() + ", поток: "+ Thread.currentThread().getName());
            sc.close();
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Ошибка разрыва соединения: " + key.channel().toString());
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

    /**
     * @return Возвращается хост из заголовков.
     */
    @Override
    public String getHostConnection(ByteBuffer buf) throws NotHostException {
        buf.flip();
        byte[] b = new byte[config.getSizeBuf()];
        buf.get(b, 0, buf.limit());
        Pattern pattern = Pattern.compile("\\r\\nHost: (.+)(:|\\r\\n)");
        Matcher matcher = pattern.matcher(new String(b, 0, buf.limit()));
        if (matcher.find())
        {
            return  matcher.group(1);
        }

        throw new NotHostException("Имя хоста не найдено в заголовках.");
    }

    @Override
    public void run() {
        start();
    }
}