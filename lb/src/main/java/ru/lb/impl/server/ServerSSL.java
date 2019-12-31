package ru.lb.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.AServer;
import ru.lb.design.server.IIdConnect;
import ru.lb.design.server.ServerReadStatus;
import ru.lb.design.server.ServerWriteStatus;
import ru.lb.impl.exception.NotHostException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.cert.CertificateException;

public class ServerSSL extends AServer {
    private static final Logger LOG = LoggerFactory.getLogger(ServerSSL.class);
    private SSLEngine engine;

    public ServerSSL(boolean startServer, IConfig config) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        super(startServer, config);
        // Create/initialize the SSLContext with key material

        char[] passphrase = "passphrase".toCharArray();

// First initialize the key and trust material.
        KeyStore ksKeys = KeyStore.getInstance("JKS");
        ksKeys.load(new FileInputStream("testKeys"), passphrase);
        KeyStore ksTrust = KeyStore.getInstance("JKS");
        ksTrust.load(new FileInputStream("testTrust"), passphrase);

// KeyManager's decide which key material to use.
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance("SunX509");
        kmf.init(ksKeys, passphrase);

// TrustManager's decide whether to allow connections.
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance("SunX509");
        tmf.init(ksTrust);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                kmf.getKeyManagers(), tmf.getTrustManagers(), null);

// We're ready for the engine.
        SSLEngine engine = sslContext.createSSLEngine(config.getIPserver().getHostName(), config.getIPserver().getPort());

// Use as client
        engine.setUseClientMode(true);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Метод обработки входящих данных.
     *
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
        ServerReadStatus status = ServerReadStatus.CONTINUE;
        try {

            while (status != ServerReadStatus.EXIT) {
                bytes = socketChannel.read(sharedBuffer);
                status = this.read(key, idConnect, socketChannel, sharedBuffer, countBuf, bytes);
            }

        } catch (IOException | RuntimeException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error writing back bytes");
                e.printStackTrace();
            }
            this.close(key);
        } catch (NotHostException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Хост не был найден в заголовках.");
                e.printStackTrace();
            }
            this.close(key);
        }
    }

    /**
     * Метод обработки исходящих соединений, в данном случае канал для соединения с сервером.
     *
     * @param key ключ из выборки селектора
     */
    @Override
    public void writable(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        IIdConnect idConnect = (IIdConnect) key.attachment();
        ServerWriteStatus writeStatus = ServerWriteStatus.CONTINUE;

        SSLSession session = engine.getSession();
        ByteBuffer myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        ByteBuffer peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());

// Do initial handshake
        try {
            this.doHandshake(socketChannel, engine, myNetData, peerNetData);

            while (writeStatus != ServerWriteStatus.EXIT) {
                writeStatus = this.write(key, idConnect.getAndRemoveBuf(), socketChannel);
            }

            if (idConnect.isStopConnect()) {
                idConnect.getInverseConnect().setStopConnect(true);
                this.close(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doHandshake(SocketChannel socketChannel, SSLEngine engine,
                             ByteBuffer myNetData, ByteBuffer peerNetData) throws Exception {

        // Create byte buffers to use for holding application data
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);

        // Begin handshake
        engine.beginHandshake();
        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();

        // Process handshaking message
        while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
                hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            switch (hs) {

                case NEED_UNWRAP:
                    // Receive handshaking data from peer
                    if (socketChannel.read(peerNetData) < 0) {
                        // Handle closed channel
                    }

                    // Process incoming handshaking data
                    peerNetData.flip();
                    SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK:
                            // Handle OK status
                            break;

                        // Handle other status: BUFFER_UNDERFLOW, BUFFER_OVERFLOW, CLOSED

                    }
                    break;

                case NEED_WRAP:
                    // Empty the local network packet buffer.
                    myNetData.clear();

                    // Generate handshaking data
                    res = engine.wrap(myAppData, myNetData);
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK:
                            myNetData.flip();

                            // Send the handshaking data to peer
                            while (myNetData.hasRemaining()) {
                                if (socketChannel.write(myNetData) < 0) {
                                    // Handle closed channel
                                }
                            }
                            break;

                        // Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED

                    }
                    break;

                case NEED_TASK:
                    // Handle blocking tasks
                    break;

                // Handle other status:  // FINISHED or NOT_HANDSHAKING

            }
        }

        // Processes after handshaking

    }

}