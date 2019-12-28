package impl.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerImplTest {

    Thread server;

    @BeforeEach
    void setUp() {
        server = new Thread(new Runnable() {
            @Override
            public void run() {
                    (new Server(true, 1024, null)).start();
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