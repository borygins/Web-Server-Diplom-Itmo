import design.config.IConfig;
import design.server.IServer;
import impl.config.Config;
import impl.server.HistoryQuery;
import impl.server.Server;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {

        IConfig config = new Config();
        config.addIPserver("fasie.ru", new InetSocketAddress("185.9.147.48", 80));
        config.setIPserver(new InetSocketAddress("localhost", 80));
        config.setCountBuf(512);
        config.setSizeBuf(1024);
        IServer server = new Server(true, config);
        server.setHistoryQuery(new HistoryQuery());
        server.start();
    }
}
