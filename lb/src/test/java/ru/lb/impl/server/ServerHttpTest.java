package ru.lb.impl.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.IServer;
import ru.lb.impl.config.Config;
import org.junit.jupiter.api.*;
import ru.lb.impl.config.ConfigIPServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
class ServerHttpTest {

    private HttpServer httpServer;
    private Thread lbServer;
    private final int portServer = 8765;
    private final String ipServer = "localhost";
    private final String URL = "http://localhost/index";

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(portServer), 0);
        httpServer.createContext("/index", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                StringBuilder builder = new StringBuilder();

                builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

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

        IConfig config = new Config();
        config.addIPserver("127.0.0.1:8080", new InetSocketAddress(ipServer, portServer));
        config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 80), false,0));

        lbServer = new Thread(new Runnable() {
            @Override
            public void run() {
                IConfig config = new Config();
                config.addIPserver("127.0.0.1:8080", new InetSocketAddress("185.9.147.48", 80));
                config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 80), false,0));
                config.setCountBuf(512);
                config.setSizeBuf(1024);
                IServer server = new Server(true,  config, config.getIPlb().get(0), false);
                server.setHistoryQuery(new HistoryQuery());
                server.start();
            }});
        lbServer.start();
        Thread.sleep(5000);
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
                .setHeader("ValidHead","1245")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(true, response.body().toLowerCase().contains("validhead=1245"));
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(10);
        lbServer.interrupt();
    }
}
