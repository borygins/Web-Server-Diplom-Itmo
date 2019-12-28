package impl.server;

import design.server.IIdConnect;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

public class IdConnect implements IIdConnect {

    private final List<ByteBuffer> buffer = new ArrayList<>();
    private IIdConnect connect;
    private boolean stopConnect = false;
    private SelectionKey key;

    public IdConnect() {

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
        return  (this.buffer.size() > 0) ? this.buffer.remove(this.buffer.size() - 1) : null;
    }


    @Override
    public List<ByteBuffer> getAllBuf() {
        return this.buffer;
    }

}
