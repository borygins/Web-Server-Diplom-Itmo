package ru.lb.impl.server;

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

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerHttpMultiCloseConnectTest {

    private final List<HttpServer> httpServers = new ArrayList<>();
    private final List<Thread> lbServers = new ArrayList<>();
    private int portServer = 8765;
    private final String ipServer = "localhost";
    private final String URL = "http://localhost/index";
    private final IConfig config = new Config();

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        config.setCountBuf(512);
        config.setSizeBuf(1024);
        config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 80), false,0));
        config.setPatternReadHeadHost("\\r\\nHost: (.+)(:|\\r\\n)");

        for (int i = 0; i < 10; i++) {
            httpServers.add(this.startHttpServer(portServer));
            config.addIPserver("localhost", new InetSocketAddress(ipServer, portServer++));
        }

        Thread lbServer = null;
        for(ConfigIPServer ipServer : config.getIPlb()) {
            lbServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    IServer server = AServer.serverFabric(config, ipServer,true);
                    server.setHistoryQuery(new HistoryQuery());
                    server.start();
                }
            });

            lbServers.add(lbServer);
            lbServer.start();
        }

        Thread.sleep(5000);
    }

    private HttpServer startHttpServer(int portServer) throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress("127.0.0.1",portServer), 0);
        httpServer.createContext("/index", new HttpHandler() {
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
//                httpServers.remove(httpServer);
//                httpServer.stop(3000);

            }
        });
        httpServer.start();
        return httpServer;
    }

    @Test
    void testQuery() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .setHeader("ValidHead", "1245")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(true, response.body().toLowerCase().contains("validhead=1245"));

        httpServers.forEach((q1) -> q1.stop(0));

        config.getListIPserver("localhost").clear();

            httpServers.add(this.startHttpServer(portServer));
            config.addIPserver("localhost", new InetSocketAddress(ipServer, portServer));

        Thread.sleep(5000);

        request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .setHeader("ValidHead", "1241")
                .build();
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        HttpResponse<String> response2 = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode());
        assertEquals(true, response2.body().toLowerCase().contains("validhead=1241"));
    }


    @AfterEach
    void tearDown() {
        for (HttpServer httpServer : httpServers)
            httpServer.stop(0);
        for (Thread lbServer : lbServers)
            lbServer.interrupt();
    }


}
