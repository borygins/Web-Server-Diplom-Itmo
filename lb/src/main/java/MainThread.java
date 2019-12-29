import design.config.IConfig;
import design.server.IServer;
import impl.config.Config;
import impl.server.HistoryQuery;
import impl.server.HistoryQueryRandom;
import impl.server.Server;

import java.net.InetSocketAddress;

public class MainThread {
    public static void main(String[] args) {

        IConfig config = new Config();
        config.addIPserver("fasie.ru", new InetSocketAddress("185.9.147.48", 80));
        config.setIPserver(new InetSocketAddress("localhost", 80));
        config.setCountBuf(512);
        config.setSizeBuf(1024);


        Thread lbServer = new Thread(new Runnable() {
            @Override
            public void run() {
                IServer server = new Server(true, config);
                server.setHistoryQuery(new HistoryQuery());
                server.start();
            }
        });

        for (int i = 0; i < 1; i++) {
            lbServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    IServer server = new Server(false, config);
                    server.setHistoryQuery(new HistoryQuery());
                    server.start();
                }
            });

            lbServer.start();
        }

    }
}
