package ru.lb.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.IIdConnect;
import ru.lb.design.server.ServerReadStatus;
import ru.lb.design.server.ServerWriteStatus;
import ru.lb.impl.config.ConfigIPServer;
import ru.lb.impl.exception.NotHostException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSSL extends Server {

    private static final Logger LOG = LoggerFactory.getLogger(ServerSSL.class);

    private SSLContext context;
    private SSLSession dummySession;
    protected LinkedList<ByteBuffer> bufNet;

    public ServerSSL(boolean startServer, IConfig config, ConfigIPServer configIPServer) {
        super(startServer, config, configIPServer, false);

        char[] pass = "changeit".toCharArray();

        try {
            context = SSLContext.getInstance("TLS");
            context.init(createKeyManagers("PKCS12", "C:\\Users\\kozlo\\IdeaProjects\\Web-Server-Diplom-Itmo\\lb\\localhost.p12", "changeit", "changeit"), null, new SecureRandom());

            dummySession = context.createSSLEngine().getSession();


            this.createBuf(config.getCountBuf(), 0);

            dummySession.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createBuf(int count, int bufSize) {
        this.buf = new LinkedList<>();
        this.bufNet = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            this.buf.add(ByteBuffer.allocate(dummySession.getApplicationBufferSize()));
            this.bufNet.add(ByteBuffer.allocate(dummySession.getPacketBufferSize()));
        }
    }

    @Override
    public ByteBuffer getBuffer(IIdConnect idConnect) {
        return getBuffer(idConnect.isClient());
    }

    public ByteBuffer getBuffer(boolean idConnect) {
        ByteBuffer buf = null;
        if (idConnect) {
//            buf = (this.bufNet.size() > 0) ? this.bufNet.remove(this.bufNet.size() - 1) : ByteBuffer.allocate(dummySession.getPacketBufferSize());

            try {
                buf = this.bufNet.removeLast();
            }catch (NoSuchElementException e){
                buf = ByteBuffer.allocate(dummySession.getPacketBufferSize());
            }
        } else {
//            buf = (this.buf.size() > 0) ? this.buf.remove(this.buf.size() - 1) : ByteBuffer.allocate(dummySession.getApplicationBufferSize());
            try {
                buf = this.buf.removeLast();
            }catch (NoSuchElementException e){
                buf = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
            }
        }
        buf.clear();
        return buf;
    }

    public void addBuffer(boolean idConnect, ByteBuffer byteBuffer) {
        if (idConnect) {
            this.bufNet.addFirst(byteBuffer);
        } else {
            this.buf.addFirst(byteBuffer);
        }
    }

    @Override
    public void addBuffer(IIdConnect idConnect, ByteBuffer byteBuffer) {
        this.addBuffer(idConnect.isClient(), byteBuffer);
    }

    @Override
    protected IIdConnect regOnSelector(SelectionKey key, SocketChannel client, Selector selectorTemp) throws IOException {
        IIdConnect idConnect = null;
        ResultCheckSSL resultCheckSSL = checkSSL(client, false);
        if (resultCheckSSL.isResult()) {
            idConnect = super.regOnSelector(key, client, selectorTemp);
            idConnect.setSSLEngine(resultCheckSSL.getEngine());
            idConnect.getInverseConnect().setSSLEngine(resultCheckSSL.getEngine());
            return idConnect;
        } else {
            return null;
        }
    }

    @Override
    protected ResultCheckSSL checkSSL(SocketChannel socketChannel, boolean typeClientMode) {
        SSLEngine engine = context.createSSLEngine();
        ResultCheckSSL resultCheckSSL = new ResultCheckSSL(false, engine);
        engine.setUseClientMode(typeClientMode);
        try {
            engine.beginHandshake();
            if (doHandshake(socketChannel, engine)) {
                resultCheckSSL.setResult(true);
                resultCheckSSL.setEngine(engine);
            } else {
                socketChannel.close();
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Соединение закрыто, сбой в TLS.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultCheckSSL;
    }

    @Override
    protected ServerWriteStatus write(SelectionKey key, ByteBuffer sharedBuffer, SocketChannel socketChannel, IIdConnect idConnect) {
        SSLEngine engine = idConnect.getSSLEngine();

        if (idConnect.isServer() && sharedBuffer != null) {

            if (sharedBuffer.position() < sharedBuffer.limit())
                sharedBuffer.position((sharedBuffer.get() == 23) ? 5 : 0);

            return super.write(key, sharedBuffer, socketChannel, idConnect);
        } else if (sharedBuffer == null || sharedBuffer.limit() == sharedBuffer.capacity()) {
            return super.write(key, sharedBuffer, socketChannel, idConnect);
        }

        try {

            // The loop has a meaning for (outgoing) messages larger than 16KB.
            // Every wrap call will remove 16KB from the original message and send it to the remote peer.
            ByteBuffer myNetData = this.getBuffer(idConnect);
            while (sharedBuffer.hasRemaining()) {
                SSLEngineResult result = null;

                result = engine.wrap(sharedBuffer, myNetData);
                switch (result.getStatus()) {
                    case OK:
                        myNetData.flip();
                        super.write(key, myNetData, socketChannel, idConnect);
                        break;
                    case BUFFER_OVERFLOW:
                        myNetData = enlargePacketBuffer(engine, myNetData);
                        break;
                    case BUFFER_UNDERFLOW:
                        throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                    case CLOSED:
                        close(key, idConnect);
                        return ServerWriteStatus.EXIT;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }

        } catch (SSLException e) {

            e.printStackTrace();
        }

        return ServerWriteStatus.EXIT;
    }

    @Override
    protected ServerReadStatus read(SelectionKey key, IIdConnect idConnect, SocketChannel socketChannel, ByteBuffer sharedBuffer, int countBuf, int bytes) throws IOException, NotHostException {

        SSLEngine engine = idConnect.getSSLEngine();
        if (bytes == 0 || idConnect.isServer()) {
            return super.read(key, idConnect, socketChannel, sharedBuffer, countBuf, bytes);
        } else if (bytes > 0) {
            sharedBuffer.flip();
            ByteBuffer peerAppData = this.getBuffer(idConnect.getInverseConnect());
            while (sharedBuffer.hasRemaining()) {
                SSLEngineResult result = engine.unwrap(sharedBuffer, peerAppData);
                switch (result.getStatus()) {
                    case OK:
                        return super.read(key, idConnect, socketChannel, peerAppData, countBuf, bytes);
                    case BUFFER_OVERFLOW:
                        peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                        return ServerReadStatus.CONTINUE;
                    case BUFFER_UNDERFLOW:
                        sharedBuffer = handleBufferUnderflow(engine, sharedBuffer);
                        return ServerReadStatus.CONTINUE;
                    case CLOSED:
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("Client wants to close connection...");
                        close(key, idConnect);
                        return ServerReadStatus.EXIT;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }

        } else if (bytes < 0) {
            if (getLogger().isErrorEnabled())
                getLogger().error("Received end of stream. Will try to close connection with client...");
            handleEndOfStream(key, idConnect, engine);
        }
        return ServerReadStatus.EXIT;
    }


    /**
     * Will be used to execute tasks that may emerge during handshake in parallel with the server's main thread.
     */
    protected ExecutorService executor = Executors.newSingleThreadExecutor();


    /**
     * Will contain this peer's encrypted data, that will be generated after {@link SSLEngine#wrap(ByteBuffer, ByteBuffer)}
     * is applied on . It should be initialized using {@link SSLSession#getPacketBufferSize()},
     * which returns the size up to which, SSL/TLS packets will be generated from the engine under a session.
     * All SSLEngine network buffers should be sized at least this large to avoid insufficient space problems when performing wrap and unwrap calls.
     */
    protected ByteBuffer myNetData;


    /**
     * Will contain the other peer's encrypted data. The SSL/TLS protocols specify that implementations should produce packets containing at most 16 KB of plaintext,
     * so a buffer sized to this value should normally cause no capacity problems. However, some implementations violate the specification and generate large records up to 32 KB.
     * If the {@link SSLEngine#unwrap(ByteBuffer, ByteBuffer)} detects large inbound packets, the buffer sizes returned by SSLSession will be updated dynamically, so the this peer
     * should check for overflow conditions and enlarge the buffer using the session's (updated) buffer size.
     */
    protected ByteBuffer peerNetData;

    /**
     * Implements the handshake protocol between two peers, required for the establishment of the SSL/TLS connection.
     * During the handshake, encryption configuration information - such as the list of available cipher suites - will be exchanged
     * and if the handshake is successful will lead to an established SSL/TLS session.
     * <p>
     * <p/>
     * A typical handshake will usually contain the following steps:
     *
     * <ul>
     *   <li>1. wrap:     ClientHello</li>
     *   <li>2. unwrap:   ServerHello/Cert/ServerHelloDone</li>
     *   <li>3. wrap:     ClientKeyExchange</li>
     *   <li>4. wrap:     ChangeCipherSpec</li>
     *   <li>5. wrap:     Finished</li>
     *   <li>6. unwrap:   ChangeCipherSpec</li>
     *   <li>7. unwrap:   Finished</li>
     * </ul>
     * <p/>
     * Handshake is also used during the end of the session, in order to properly close the connection between the two peers.
     * A proper connection close will typically include the one peer sending a CLOSE message to another, and then wait for
     * the other's CLOSE message to close the transport link. The other peer from his perspective would read a CLOSE message
     * from his peer and then enter the handshake procedure to send his own CLOSE message as well.
     *
     * @param socketChannel - the socket channel that connects the two peers.
     * @param engine        - the engine that will be used for encryption/decryption of the data exchanged with the other peer.
     * @return True if the connection handshake was successful or false if an error occurred.
     * @throws IOException - if an error occurs during read/write to the socket channel.
     */
    protected boolean doHandshake(SocketChannel socketChannel, SSLEngine engine) throws IOException {

        if (getLogger().isDebugEnabled())
            getLogger().debug("Начало проверки TLS соединения.");

        SSLEngineResult result;
        SSLEngineResult.HandshakeStatus handshakeStatus;

        // NioSslPeer's fields myAppData and peerAppData are supposed to be large enough to hold all message data the peer
        // will send and expects to receive from the other peer respectively. Since the messages to be exchanged will usually be less
        // than 16KB long the capacity of these fields should also be smaller. Here we initialize these two local buffers
        // to be used for the handshake, while keeping client's buffers at the same size.
        int appBufferSize = engine.getSession().getApplicationBufferSize();

        myNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
        peerNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());

        ByteBuffer myAppData = this.getBuffer(false);
        ByteBuffer peerAppData = this.getBuffer(false);

        handshakeStatus = engine.getHandshakeStatus();
        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    if (socketChannel.read(peerNetData) < 0) {
                        if (engine.isInboundDone() && engine.isOutboundDone()) {
                            this.addBuffer(false, myAppData);
                            this.addBuffer(false, peerAppData);
                            return false;
                        }
                        try {
                            engine.closeInbound();
                        } catch (SSLException e) {
                            getLogger().error("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
                        }
                        engine.closeOutbound();
                        // After closeOutbound the engine will be set to WRAP state, in order to try to send a close message to the client.
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    peerNetData.flip();
                    try {
                        result = engine.unwrap(peerNetData, peerAppData);
                        peerNetData.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        getLogger().error("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    switch (result.getStatus()) {
                        case OK:
                            break;
                        case BUFFER_OVERFLOW:
                            // Will occur when peerAppData's capacity is smaller than the data derived from peerNetData's unwrap.
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            // Will occur either when no data was read from the peer or when the peerNetData buffer was too small to hold all peer's data.
                            peerNetData = handleBufferUnderflow(engine, peerNetData);
                            break;
                        case CLOSED:
                            if (engine.isOutboundDone()) {
                                this.addBuffer(false, myAppData);
                                this.addBuffer(false, peerAppData);
                                return false;
                            } else {
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                break;
                            }
                        default:
                            this.addBuffer(false, myAppData);
                            this.addBuffer(false, peerAppData);
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_WRAP:
                    myNetData.clear();
                    try {
                        result = engine.wrap(myAppData, myNetData);
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        getLogger().error("A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    switch (result.getStatus()) {
                        case OK:
                            myNetData.flip();
                            while (myNetData.hasRemaining()) {
                                socketChannel.write(myNetData);
                            }
                            break;
                        case BUFFER_OVERFLOW:
                            // Will occur if there is not enough space in myNetData buffer to write all the data that would be generated by the method wrap.
                            // Since myNetData is set to session's packet size we should not get to this point because SSLEngine is supposed
                            // to produce messages smaller or equal to that, but a general handling would be the following:
                            myNetData = enlargePacketBuffer(engine, myNetData);
                            break;
                        case BUFFER_UNDERFLOW:
                            this.addBuffer(false, myAppData);
                            this.addBuffer(false, peerAppData);
                            throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                        case CLOSED:
                            try {
                                myNetData.flip();
                                while (myNetData.hasRemaining()) {
                                    socketChannel.write(myNetData);
                                }
                                // At this point the handshake status will probably be NEED_UNWRAP so we make sure that peerNetData is clear to read.
                                peerNetData.clear();
                            } catch (Exception e) {
                                getLogger().error("Failed to send server's CLOSE message due to socket channel's failure.");
                                handshakeStatus = engine.getHandshakeStatus();
                            }
                            break;
                        default:
                            this.addBuffer(false, myAppData);
                            this.addBuffer(false, peerAppData);
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        executor.execute(task);
                    }
                    handshakeStatus = engine.getHandshakeStatus();
                    break;
                case FINISHED:
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    this.addBuffer(false, myAppData);
                    this.addBuffer(false, peerAppData);
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }

        this.addBuffer(false, myAppData);
        this.addBuffer(false, peerAppData);
        return true;

    }

    protected ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    /**
     * Compares <code>sessionProposedCapacity<code> with buffer's capacity. If buffer's capacity is smaller,
     * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
     * with capacity twice the size of the initial one.
     *
     * @param buffer                  - the buffer to be enlarged.
     * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by {@link SSLSession}.
     * @return A new buffer with a larger capacity.
     */
    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    /**
     * Handles {@link SSLEngineResult.Status#BUFFER_UNDERFLOW}. Will check if the buffer is already filled, and if there is no space problem
     * will return the same buffer, so the client tries to read again. If the buffer is already filled will try to enlarge the buffer either to
     * session's proposed size or to a larger capacity. A buffer underflow can happen only after an unwrap, so the buffer will always be a
     * peerNetData buffer.
     *
     * @param buffer - will always be peerNetData buffer.
     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
     * @throws Exception
     */
    protected ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public void close(SelectionKey key, IIdConnect iIdConnect) {

        IIdConnect idConnect = (IIdConnect) key.attachment();
        if (idConnect.isClient()) {
            SSLEngine engine = idConnect.getSSLEngine();
            try {
                if (engine != null) {
                    engine.closeOutbound();
                    doHandshake((SocketChannel) key.channel(), engine);
                }
            } catch (IOException e) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Ошибка разрыва TSL соединения: " + key.channel().toString());
                    e.printStackTrace();
                }
            }
        }
        super.close(key, idConnect);
    }

    /**
     * In addition to orderly shutdowns, an unorderly shutdown may occur, when the transport link (socket channel)
     * is severed before close messages are exchanged. This may happen by getting an -1 or {@link IOException}
     * when trying to read from the socket channel, or an {@link IOException} when trying to write to it.
     * In both cases {@link SSLEngine#closeInbound()} should be called and then try to follow the standard procedure.
     *
     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    protected void handleEndOfStream(SelectionKey key, IIdConnect idConnect, SSLEngine engine) throws IOException {
        try {
            engine.closeInbound();
        } catch (Exception e) {
            getLogger().error("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
        }
        close(key, idConnect);
    }

    /**
     * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
     *
     * @param filepath         - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @param keyPassword      - the key's passsword.
     * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
     * @throws Exception
     */
    protected KeyManager[] createKeyManagers(String type, String filepath, String keystorePassword, String keyPassword) throws Exception {
        //KeyStore keyStore = KeyStore.getInstance("JKS");
        KeyStore keyStore = KeyStore.getInstance(type);
        InputStream keyStoreIS = new FileInputStream(filepath);
        try {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
     *
     * @param filepath         - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
     * @throws Exception
     */
    protected TrustManager[] createTrustManagers(String type, String filepath, String keystorePassword) throws Exception {
        //KeyStore trustStore = KeyStore.getInstance("JKS");
        KeyStore trustStore = KeyStore.getInstance(type);
        InputStream trustStoreIS = new FileInputStream(filepath);
        try {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }

}
