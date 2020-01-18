package ru.lb.impl.service;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.AServer;
import ru.lb.design.server.IServer;
import ru.lb.impl.config.Config;
import ru.lb.impl.config.ConfigIPServer;
import ru.lb.impl.server.HistoryQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    private final List<HttpServer> httpServers = new ArrayList<>();
    private Map<String, InetSocketAddress> ip = new HashMap<>();
    private int portServer = 8765;
    private final String ipServer = "localhost";
    private final IConfig config = new Config();

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        config.setCountBuf(512);
        config.setSizeBuf(1024);
        config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 80), false,0));
        config.setPatternReadHeadHost("\\r\\nHost: (.+)(:|\\r\\n)");

        for (int i = 0; i < 4; i++) {
            if(i % 2 == 0) {
                httpServers.add(this.startHttpServer(portServer));
            } else {
                ip.put("localhost:" + portServer, new InetSocketAddress(ipServer, portServer));
            }
            config.addIPserver("localhost:" + portServer, new InetSocketAddress(ipServer, portServer++));
        }

        Thread.sleep(5000);

        ((Config) config).updateList();
    }

    private HttpServer startHttpServer(int portServer) throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress("127.0.0.1",portServer), 0);
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                StringBuilder builder = new StringBuilder();
                builder.append("ServerName: ").append(portServer);
                builder.append("<br><h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

                Headers headers = exchange.getRequestHeaders();
                for (String header : headers.keySet()) {
                    builder.append("<p>").append(header).append("=")
                            .append(headers.getFirst(header)).append("</p>");
                }

                byte[] bytes = builder.toString().getBytes();
                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        });
        httpServer.start();
        return httpServer;
    }

    @Test
    void run() throws InterruptedException, IOException {
        Service service = new Service(config);
        new Thread(service).start();
        Thread.sleep(20000);
        assertArrayEquals(service.getTempIP().entrySet().stream().map((q1) -> q1.getValue()).flatMap(List::stream).collect(Collectors.toList()).toArray(Object[]::new)
                , ip.entrySet().stream().map((q1) -> q1.getValue()).collect(Collectors.toList()).toArray(Object[]::new));

        httpServers.add(this.startHttpServer(--portServer));
        ip.remove("localhost:" + portServer);
        Thread.sleep(20000);
        assertArrayEquals(service.getTempIP().entrySet().stream().map((q1) -> q1.getValue()).flatMap(List::stream).collect(Collectors.toList()).toArray(Object[]::new)
                , ip.entrySet().stream().map((q1) -> q1.getValue()).collect(Collectors.toList()).toArray(Object[]::new));
    }

    @AfterEach
    void tearDown() {
        for (HttpServer httpServer : httpServers)
            httpServer.stop(0);
    }
}