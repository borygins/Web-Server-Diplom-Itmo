package impl.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import design.config.IConfig;
import impl.config.Config;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
class ServerTestHttp {

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
        config.setIPserver(new InetSocketAddress("localhost", 80));

        lbServer = new Thread(new Runnable() {
            @Override
            public void run() {
                (new Server(true, 1024, config)).start();
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
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(10);
        lbServer.interrupt();
    }
}
