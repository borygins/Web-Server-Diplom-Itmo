import design.config.IConfig;
import design.server.IServer;
import impl.config.Config;
import impl.server.Server;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {

        IConfig config = new Config();
        config.addIPserver("127.0.0.1:8080", new InetSocketAddress("195.208.1.160", 80));
        config.setIPserver(new InetSocketAddress("localhost", 9000));
        IServer server = new Server(true,1024, config);
        server.start();
    }
}
