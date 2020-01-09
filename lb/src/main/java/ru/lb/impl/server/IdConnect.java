package ru.lb.impl.server;

import ru.lb.design.server.IIdConnect;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

public class IdConnect implements IIdConnect {

    private final List<ByteBuffer> buffer = new ArrayList<>();
    private IIdConnect connect;
    private boolean stopConnect = false;
    private SelectionKey key;
    private InetSocketAddress host = null;
    private boolean client = false;
    private boolean server = false;
    private SSLEngine engine;

    public IdConnect() {

    }

    @Override
    public void setSSLEngine(SSLEngine engine) {
        this.engine = engine;
    }

    @Override
    public SSLEngine getSSLEngine() {
        return engine;
    }

    @Override
    public void setServer(boolean server) {
        this.server = server;
    }

    @Override
    public void setClient(boolean client) {
        this.client = client;
    }

    @Override
    public boolean isClient() {
        return client;
    }

    @Override
    public boolean isServer() {
        return server;
    }

    @Override
    public void setSelectionKey(SelectionKey key) {
        this.key = key;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return this.key;
    }

    @Override
    public void setHostConnection(InetSocketAddress host) {
        this.host = host;
    }

    @Override
    public InetSocketAddress getHostConnection() {
        return this.host;
    }

    @Override
    public void setStopConnect(boolean stopConnect) {
        this.stopConnect = stopConnect;
    }

    @Override
    public boolean isStopConnect() {
        return this.stopConnect;
    }

    @Override
    public IIdConnect getInverseConnect() {
        return this.connect;
    }

    @Override
    public void setInverseConnect(IIdConnect connect) {
        this.connect = connect;
    }

    @Override
    public void addBuf(ByteBuffer buffer) {
        this.buffer.add(buffer);
    }


    @Override
    public ByteBuffer getAndRemoveBuf() {
        return  (this.buffer.size() > 0) ? this.buffer.remove(0) : null;
    }


    @Override
    public List<ByteBuffer> getAllBuf() {
        return this.buffer;
    }

}
