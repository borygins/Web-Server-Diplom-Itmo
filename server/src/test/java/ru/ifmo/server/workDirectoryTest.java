package ru.ifmo.server;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;
import static ru.ifmo.server.TestUtils.assertStatusCode;

public class workDirectoryTest {
    private static final HttpHost host = new HttpHost("localhost", ServerConfig.DFLT_PORT);
    private static Server server;
    private CloseableHttpClient client;
    private static ServerConfig cfg;

    @BeforeClass
    public void init() throws IOException {
        ServerConfig cfg = new ServerConfig();
        File tmp = File.createTempFile("test", ".xml");
        Files.copy(getClass().getClassLoader().getResourceAsStream("web-server.xml"), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        cfg.setWorkDirectory(new File("C:\\Users\\Alex Korn\\Desktop\\itmo\\Web-Server-Diplom-Itmo\\server\\example\\src\\main\\resources"));
        server = Server.start(cfg);
        client = HttpClients.createDefault();
    }

    @AfterClass
    public void stop() {
        IOUtils.closeQuietly(server);
        IOUtils.closeQuietly(client);

        server = null;
        client = null;
    }

    @Test
    public void testFindFile() throws IOException, URISyntaxException {
        URI uri = new URI("/web-server.xml");
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(host, get);

        assertStatusCode(HttpStatus.SC_OK, response);
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                        "<config>\n" +
                        " <port>8080</port>\n" +
                        " <socketTimeout>3000</socketTimeout>\n" +
                        " <handlers>\n" +
                        " <handler url=\"/success\">ru.ifmo.server.Http.OK_HEADER</handler>\n" +
                        " </handlers>\n" +
                        "</config>",
                EntityUtils.toString(response.getEntity()));
    }
}
