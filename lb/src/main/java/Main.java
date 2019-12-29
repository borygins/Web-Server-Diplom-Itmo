import design.config.IConfig;
import design.server.IServer;
import impl.config.Config;
import impl.server.Server;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {

        IConfig config = new Config();
        config.addIPserver("127.0.0.1:8080", new InetSocketAddress("185.9.147.48", 80));
        config.setIPserver(new InetSocketAddress("localhost", 80));
        IServer server = new Server(true,1024, config);
        server.start();
    }
}
