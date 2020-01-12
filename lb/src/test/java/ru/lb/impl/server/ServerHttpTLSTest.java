package ru.lb.impl.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.AServer;
import ru.lb.design.server.IServer;
import ru.lb.impl.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.lb.impl.config.ConfigIPServer;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerHttpTLSTest {

    private final List<HttpServer> httpServers = new ArrayList<>();
    private final List<Thread> lbServers = new ArrayList<>();
    private int portServer = 8765;
    private final String ipServer = "localhost";
    private final String URL = "https://localhost/index";

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        IConfig config = new Config();
        config.setCountBuf(512);
        config.setSizeBuf(1024);
        config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 443), true));
        config.setPatternReadHeadHost("\\r\\nHost: (.+)(:|\\r\\n)");

        for (int i = 0; i < 2; i++) {
            HttpServer httpServer = HttpServer.create();
            this.startHttpServer(httpServer, portServer);
            httpServer.start();
            httpServers.add(httpServer);
            config.addIPserver("localhost", new InetSocketAddress(ipServer, portServer));
            portServer++;
        }

        Thread lbServer = null;
        for(ConfigIPServer ipServer : config.getIPlb()) {
            lbServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    IServer server = AServer.serverFabric(config, ipServer);
                    server.setHistoryQuery(new HistoryQuery());
                    server.start();
                }
            });
            lbServer.start();
        }
        lbServers.add(lbServer);


        Thread.sleep(5000);
    }

    private void startHttpServer(HttpServer httpServer, int portServer) throws IOException {
        httpServer.bind(new InetSocketAddress(portServer), 0);
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
            }
        });
    }

    @Test
    void testQuery() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(createKeyManagers("PKCS12","C:\\Users\\kozlo\\IdeaProjects\\Web-Server-Diplom-Itmo\\lb\\localhost.p12", "changeit", "changeit"),
                null, new SecureRandom());

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .sslContext(sslContext)
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

    }

    @AfterEach
    void tearDown() {
        for (HttpServer httpServer : httpServers)
            httpServer.stop(0);
        for (Thread lbServer : lbServers)
            lbServer.interrupt();
    }

    private KeyManager[] createKeyManagers(String type, String filepath, String keystorePassword, String keyPassword) throws Exception {
        //KeyStore keyStore = KeyStore.getInstance("JKS");
        KeyStore keyStore = KeyStore.getInstance(type);
        InputStream keyStoreIS = new FileInputStream(filepath);
        try {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
     * @throws Exception
     */
    private TrustManager[] createTrustManagers(String type, String filepath, String keystorePassword) throws Exception {
        //KeyStore trustStore = KeyStore.getInstance("JKS");
        KeyStore trustStore = KeyStore.getInstance(type);
        InputStream trustStoreIS = new FileInputStream(filepath);
        try {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }

}
