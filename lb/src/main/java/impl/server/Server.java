package impl.server;

import design.config.IConfig;
import design.server.IIdConnect;
import design.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server implements IServer {

    private IConfig config;
    private final int BUFFER_SIZE;
    private Selector selector;
    private ServerSocket serverSocket;
    private final boolean startServer;
    private Long id;
    private ArrayList<ByteBuffer> buf;
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server(boolean startServer, int BUFFER_SIZE, IConfig config) {
        this.startServer = startServer;
        this.BUFFER_SIZE = BUFFER_SIZE;
        this.createBuf(512, this.BUFFER_SIZE);
        this.config = config;

        try {
            if (!startServer) {
                this.selector = Selector.open();
            } else {
                this.selector = Selector.open();
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
                    } else if (!selectionKey.isValid()) {
                        valid(selectionKey);
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

    @Override
    public void acceptable(SelectionKey key) {
        Socket socket = null;
        SocketChannel client = null;

        try {
            socket = this.serverSocket.accept();

            if (LOG.isInfoEnabled())
                LOG.info("Connection from: " + socket);

            client = socket.getChannel();
            client.configureBlocking(false);
            client.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to use channel");
                e.printStackTrace();
            }
            key.cancel();
        }

    }

    @Override
    public void readable(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();

        if (idConnect == null) {
            idConnect = new IdConnect();
            idConnect.setInverseConnect(new IdConnect());
            idConnect.getInverseConnect().setInverseConnect(idConnect);
            idConnect.setSelectionKey(key);
            key.attach(idConnect);
        }

        ByteBuffer sharedBuffer = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(this.BUFFER_SIZE);

        int bytes = -1;
        int countBuf = 3;
        try {

            while (true) {

                bytes = socketChannel.read(sharedBuffer);

                boolean writeData = (countBuf == 0) | (sharedBuffer.position() != 0 & bytes < 1 & sharedBuffer.position() != sharedBuffer.capacity());
                boolean closeSelectionKey = (bytes == -1);

                if (writeData) {
                    idConnect.getInverseConnect().addBuf(sharedBuffer);

                    if(idConnect.getInverseConnect().getSelectionKey() == null) {
                        SocketChannel writer = SocketChannel.open();
                        writer.configureBlocking(false);
                        writer.connect(config.getRandomIPserver("127.0.0.1:8080"));
                        SelectionKey keyWriter = writer.register(this.selector, socketChannel.validOps() & ~SelectionKey.OP_READ, idConnect.getInverseConnect());
                        idConnect.getInverseConnect().setSelectionKey(keyWriter);
                    } else {
                        idConnect.getInverseConnect().getSelectionKey().interestOps(socketChannel.validOps() & ~SelectionKey.OP_READ);
                    }
                    if(!closeSelectionKey)
                    break;
                }

                if (closeSelectionKey) {
                    key.cancel();
                    idConnect.getInverseConnect().setStopConnect(true);
                    break;
                }


                if (sharedBuffer.position() == sharedBuffer.capacity()) {
                    idConnect.getInverseConnect().addBuf(sharedBuffer);
                    sharedBuffer = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(this.BUFFER_SIZE);
                }

                countBuf--;

            }


        } catch (IOException | RuntimeException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error writing back bytes");
                e.printStackTrace();
            }
            key.cancel();
        }
    }

    @Override
    public void writable(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();
        while (true) {
            ByteBuffer sharedBuffer = idConnect.getAndRemoveBuf();

            if(sharedBuffer == null){
                key.interestOps(socketChannel.validOps() & ~SelectionKey.OP_WRITE);
                break;
            }

            try {
                sharedBuffer.flip();

                while (sharedBuffer.hasRemaining()) {
//                    byte[] b = new byte[1024];
//                    this.sharedBuffer.get(b, 0, bytes);
                    socketChannel.write(sharedBuffer);

                }

                sharedBuffer.clear();
                this.buf.add(sharedBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (idConnect.isStopConnect()) {
            key.cancel();
            idConnect.getInverseConnect().setStopConnect(true);
        }
    }

    @Override
    public void connectable(SelectionKey key)  {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Err connect..." + key.channel().toString());
                e.printStackTrace();
            }
            key.cancel();

            SocketChannel writer = null;
            try {
                writer = SocketChannel.open();
                writer.configureBlocking(false);
                writer.connect(config.getRandomIPserver("127.0.0.1:8080"));
                writer.register(this.selector, writer.validOps(), key.attachment());
            }catch (IOException ex){
                if (LOG.isErrorEnabled()) {
                    LOG.error("Err connect..." + key.channel().toString());
                    e.printStackTrace();
                }
            }

            return;
        }

        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    @Override
    public void valid(SelectionKey key) {
        try {
            ((SocketChannel) key.channel()).close();
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Err closing..." + key.channel().toString());
                e.printStackTrace();
            }
            key.cancel();
        }
    }

    @Override
    public void addConfig(IConfig config) {
        this.config = config;
    }


    @Override
    public void createBuf(int count, int bufSize) {
        this.buf = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            this.buf.add(ByteBuffer.allocate(bufSize));
        }
    }

    private void increment() {
        if (id == Long.MAX_VALUE) {
            id = 0l;
        }
        id++;
    }

    @Override
    public void run() {
        start();
    }
}