package design.server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;

public interface IIdConnect {

    List<ByteBuffer> getAllBuf();
    void addBuf(ByteBuffer buffer);
    ByteBuffer getAndRemoveBuf();

    void setStopConnect(boolean stopConnect);
    boolean isStopConnect();

    IIdConnect getInverseConnect();
    void setInverseConnect(IIdConnect connect);

    void setSelectionKey(SelectionKey key);
    SelectionKey getSelectionKey();
}
