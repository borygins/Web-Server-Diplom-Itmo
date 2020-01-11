package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.server.util.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.ifmo.server.Http.*;
import static ru.ifmo.server.Session.SESSION_COOKIENAME;
import static ru.ifmo.server.util.Utils.htmlMessage;

/**
 * Ifmo Web Server.
 * <p>
 *     To start server use {@link #start(ServerConfig)} and register at least
 *     one handler to process HTTP requests.
 *     Usage example:
 *     <pre>
 *{@code
 * ServerConfig config = new ServerConfig()
 *      .addHandler("/index", new Handler() {
 *          public void handle(Request request, Response response) throws Exception {
 *              Writer writer = new OutputStreamWriter(response.getOutputStream());
 *              writer.write(Http.OK_HEADER + "Hello World!");
 *              writer.flush();
 *          }
 *      });
 *
 * Server server = Server.start(config);
 *      }
 *     </pre>
 * </p>
 * <p>
 *     To stop the server use {@link #stop()} or {@link #close()} methods.
 * </p>
 * @see ServerConfig
 */
public class Server implements Closeable {
    private static final char LF = '\n';
    private static final char CR = '\r';
    private static final String CRLF = "" + CR + LF;
    private static final char AMP = '&';
    private static final char EQ = '=';
    private static final char HEADER_VALUE_SEPARATOR = ':';
    private static final char SPACE = ' ';
    private static final int READER_BUF_SIZE = 1024;

    private final ServerConfig config;

    private ServerSocket socket;

    private ExecutorService acceptorPool;

    private static Map<String, Session> sessions = new ConcurrentHashMap<>();
    private Thread lisThread;

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private Server(ServerConfig config) {
        this.config = new ServerConfig(config);
    }

    static Map<String, Session> getSessions() {
        return sessions;
    }

    static void setSessions(String key, Session session) {
        Server.sessions.put(key, session);
    }

    static void removeSession(String key) {
        Server.sessions.remove(key);
    }

    private void listenSessions() throws IOException {
        SessionListener sessionListener = new SessionListener();
        lisThread = new Thread(sessionListener);
        lisThread.start();

        LOG.info("Session listener started, deleting by timeout.");
    }

    /**
     * Starts server according to config. If null passed
     * defaults will be used.
     *
     * @param config Server config or null.
     * @return Server instance.
     * @see ServerConfig
     */
    public static Server start(ServerConfig config) {
        if (config == null)
            config = new ServerConfig();

        try {
            if (LOG.isDebugEnabled())
                LOG.debug("Starting server with config: {}", config);

            Server server = new Server(config);

            server.openConnection();
            server.startAcceptor();

            LOG.info("Server started on port: {}", config.getPort());
            return server;
        }
        catch (IOException e) {
            throw new ServerException("Cannot start server on port: " + config.getPort());
        }
    }

    /**
     * Forces any content in the buffer to be written to the client
     */
    private static void flushResponse(Request request, Response response) {


        try {
            OutputStream out = response.socket.getOutputStream();
            if (request.getSession() != null) {
                response.setCookie(new Cookie(SESSION_COOKIENAME, request.getSession().getId()));
            }

            if (response.setCookies != null) {

                for (Cookie cookie : response.setCookies) {

                    StringBuilder cookieline = new StringBuilder();

                    cookieline.append(cookie.name + "=" + cookie.value);
                    if (cookie.maxage != null) cookieline.append(";MAX-AGE=" + cookie.maxage);
                    if (cookie.domain != null) cookieline.append(";DOMAIN=" + cookie.domain);
                    if (cookie.path != null) cookieline.append(";PATH=" + cookie.path);

                    out.write(("Set-Cookie:" + SPACE + cookieline.toString() + CRLF).getBytes());

                }
            }

            out.write(CRLF.getBytes());
            if (response.bufferOutputStream != null)
                out.write(response.bufferOutputStream.toByteArray());

            out.flush();
        } catch (IOException e) {
            throw new ServerException("Cannot get output stream", e);
        }

    }


    private void openConnection() throws IOException {
        socket = new ServerSocket(config.getPort());
    }

    private void startAcceptor() {
        acceptorPool = Executors.newSingleThreadExecutor(new ServerThreadFactory("con-acceptor"));

        acceptorPool.submit(new ConnectionHandler());
    }

    /**
     * Stops the server.
     */
    public void stop() {
        acceptorPool.shutdownNow();
        Utils.closeQuiet(socket);

        socket = null;
    }

    private void processConnection(Socket sock) throws IOException {
        if (LOG.isDebugEnabled())
            LOG.debug("Accepting connection on: {}", sock);

        Request req;

        try {
            req = parseRequest(sock);

            if (LOG.isDebugEnabled())
                LOG.debug("Parsed request: {}", req);
        }
        catch (URISyntaxException e) {
            if (LOG.isDebugEnabled())
                LOG.error("Malformed URL", e);

            respond(SC_BAD_REQUEST, "Malformed URL", htmlMessage(SC_BAD_REQUEST + " Malformed URL"),
                    sock.getOutputStream());

            return;
        }
        catch (Exception e) {
            LOG.error("Error parsing request", e);

            respond(SC_SERVER_ERROR, "Server Error", htmlMessage(SC_SERVER_ERROR + " Server error"),
                    sock.getOutputStream());

            return;
        }

        if (!isMethodSupported(req.method)) {
            respond(SC_NOT_IMPLEMENTED, "Not Implemented", htmlMessage(SC_NOT_IMPLEMENTED + " Method \""
                    + req.method + "\" is not supported"), sock.getOutputStream());

            return;
        }

        Handler handler = config.handler(req.getPath());
        Response resp = new Response(sock);

        if (handler != null) {
            try {
                handler.handle(req, resp);
            }
            catch (Exception e) {
                if (LOG.isDebugEnabled())
                    LOG.error("Server error:", e);

                respond(SC_SERVER_ERROR, "Server Error", htmlMessage(SC_SERVER_ERROR + " Server error"),
                        sock.getOutputStream());
            }
        }
        else
            respond(SC_NOT_FOUND, "Not Found", htmlMessage(SC_NOT_FOUND + " Not found"),
                    sock.getOutputStream());
    }

    private Request parseRequest(Socket socket) throws IOException, URISyntaxException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());

        Request req = new Request(socket);
        StringBuilder sb = new StringBuilder(READER_BUF_SIZE); // TODO

        while (readLine(reader, sb) > 0) {
            if (req.method == null)
                parseRequestLine(req, sb);
            else
                parseHeader(req, sb);

            sb.setLength(0);
        }

        return req;
    }

    private void parseRequestLine(Request req, StringBuilder sb) throws URISyntaxException {
        int start = 0;
        int len = sb.length();

        for (int i = 0; i < len; i++) {
            if (sb.charAt(i) == SPACE) {
                if (req.method == null)
                    req.method = HttpMethod.valueOf(sb.substring(start, i));
                else if (req.path == null) {
                    req.path = new URI(sb.substring(start, i));

                    break; // Ignore protocol for now
                }

                start = i + 1;
            }
        }

        assert req.method != null : "Request method can't be null";
        assert req.path != null : "Request path can't be null";

        String query = req.path.getQuery();

        if (query != null) {
            start = 0;

            String key = null;

            for (int i = 0; i < query.length(); i++) {
                boolean last = i == query.length() - 1;

                if (key == null && query.charAt(i) == EQ) {
                    key = query.substring(start, i);

                    start = i + 1;
                }
                else if (key != null && (query.charAt(i) == AMP || last)) {
                    req.addArgument(key, query.substring(start, last ? i + 1 : i));

                    key = null;
                    start = i + 1;
                }
            }

            if (key != null)
                req.addArgument(key, null);
        }
    }

    private void parseHeader(Request req, StringBuilder sb) {
        String key = null;

        int len = sb.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (sb.charAt(i) == HEADER_VALUE_SEPARATOR) {
                key = sb.substring(start, i).trim();

                start = i + 1;

                break;
            }
        }

        req.addHeader(key, sb.substring(start, len).trim());
    }

    private int readLine(InputStreamReader in, StringBuilder sb) throws IOException {
        int c;
        int count = 0;

        while ((c = in.read()) >= 0) {
            if (c == LF)
                break;

            sb.append((char) c);

            count++;
        }

        if (count > 0 && sb.charAt(count - 1) == CR)
            sb.setLength(--count);

        if (LOG.isTraceEnabled())
            LOG.trace("Read line: {}", sb.toString());

        return count;
    }

    private void respond(int code, String statusMsg, String content, OutputStream out) throws IOException {
        out.write(("HTTP/1.0" + SPACE + code + SPACE + statusMsg + CRLF + CRLF + content).getBytes());
        out.flush();
    }

    /**
     * Invokes {@link #stop()}. Usable in try-with-resources.
     *
     * @throws IOException Should be never thrown.
     */
    public void close() throws IOException {
        stop();
    }

    private boolean isMethodSupported(HttpMethod method) {
        return method == HttpMethod.GET;
    }

    private class ConnectionHandler implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket sock = socket.accept()) {
                    sock.setSoTimeout(config.getSocketTimeout());

                    processConnection(sock);
                }
                catch (Exception e) {
                    if (!Thread.currentThread().isInterrupted())
                        LOG.error("Error accepting connection", e);
                }
            }
        }
    }
}
