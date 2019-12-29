package impl.server;

import design.config.IConfig;
import design.server.IServer;
import impl.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Disabled
class ServerImplTest {

    Thread server;

    @BeforeEach
    void setUp() {
        server = new Thread(new Runnable() {
            @Override
            public void run() {
                IConfig config = new Config();
                config.addIPserver("127.0.0.1:8080", new InetSocketAddress("185.9.147.48", 80));
                config.setIPserver(new InetSocketAddress("localhost", 80));
                config.setCountBuf(512);
                config.setSizeBuf(1024);

                IServer server = new Server(true,  config);
                server.setHistoryQuery(new HistoryQuery());
                server.start();
            }});
        server.start();
    }

    @Test
    public void testCloseServ() throws InterruptedException {
        Thread.sleep(5000);
        assertEquals(Thread.State.RUNNABLE, server.getState());
        server.interrupt();
        Thread.sleep(5000);
        assertEquals(Thread.State.TERMINATED, server.getState());
    }

    @Test
    @Disabled
    public void testArrayList() throws InterruptedException {
        List<String> arr= new ArrayList<>();
        arr.add("str1");
        int id = 0;
        assertEquals("str1", arr.remove(id));
        assertNull((arr.size() > id) ? arr.remove(id) : null);
    }



    @AfterEach
    void tearDown() {
        server.interrupt();
    }
}