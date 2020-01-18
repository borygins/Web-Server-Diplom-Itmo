package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.server.util.Utils;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.ifmo.server.util.Utils.htmlMessage;
import static ru.ifmo.server.Http.*;
import static ru.ifmo.server.Session.SESSION_COOKIENAME;
import static ru.ifmo.server.util.Utils.htmlMessage;

/**
 * Ifmo Web Server.
 * <p>
 * To start server use {@link #start(ServerConfig)} and register at least
 * one handler to process HTTP requests.
 * Usage example:
 * <pre>
 * {@code
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
 * To stop the server use {@link #stop()} or {@link #close()} methods.
 * </p>
 *
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
    private static HashMap<String, String> mimeMap = new HashMap<>();

    private final ServerConfig config;
    private ServerSocket socket;
    private ExecutorService acceptorPool;
    private Thread lisThread;
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();

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
        SessionListener sessionListener = new SessionListener(sessions);
        lisThread = new Thread(sessionListener);
        lisThread.start();

        // done debug++
        LOG.debug("Session listener started, deleting by timeout.");
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

            server.listenSessions();

            return server;
        } catch (IOException e) {
            throw new ServerException("Cannot start server on port: " + config.getPort());
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
        lisThread.interrupt();

        socket = null;
    }

    private void responseExecutor(Response response, Request request) throws IOException {
        // status code
        if (response.getStatusCode() == 0) {
            response.setStatusCode(Http.SC_OK);
        }

        // create HTTP response:
        if (response.writer != null) {
            response.writer.flush();
        }

        long contentLength = 0;
        // if no content length set
        //body.length - Content-Length
        if (response.bout != null) {
            contentLength = response.bout.size();
        }

        // set this header
        if (response.headers.get(CONTENT_LENGTH) == null) {
            response.setHeader(CONTENT_LENGTH, String.valueOf(contentLength));
        }

        // write all headers
        OutputStream outputStream = response.socket.getOutputStream();
        outputStream.write(("HTTP/1.0" + SPACE + response.getStatusCode() + SPACE + codeTranslator[response.getStatusCode()] + CRLF).getBytes());

        for (Map.Entry<String, String> head : response.headers.entrySet()) {
            outputStream.write((head + ":" + SPACE + head.getValue() + CRLF).getBytes());
        }

        if (request.getSession() != null) {
            response.setCookie(new Cookie(SESSION_COOKIENAME, request.getSession().getId()));
        }

        for (Map.Entry<String, Cookie> entry : response.setCookies.entrySet()) {
            StringBuilder cookieLine = new StringBuilder();
            cookieLine.append(entry.getKey()).append("=").append(entry.getValue().getValue());
            if (entry.getValue().getMaxAge() != 0) {
                cookieLine.append(";Max-Age=").append(entry.getValue().getMaxAge());
            }
            if (entry.getValue().getDomain() != null) {
                cookieLine.append(";DOMAIN=").append(entry.getValue().getDomain());
            }
            if (entry.getValue().getPath() != null) {
                cookieLine.append(";PATH=").append(entry.getValue().getPath());
            }
            outputStream.write(("Set-Cookie:" + SPACE + cookieLine.toString() + CRLF).getBytes());
        }


        outputStream.write(CRLF.getBytes());

        if (response.bout != null) {
            System.out.println(response.bout.toString());
            outputStream.write(response.bout.toByteArray());
        }

        outputStream.flush();
    }

    private void processConnection(Socket sock) throws IOException {
        if (LOG.isDebugEnabled())
            LOG.debug("Accepting connection on: {}", sock);
        Request req;
        try {
            req = parseRequest(sock);

            if (LOG.isDebugEnabled())
                LOG.debug("Parsed request: {}", req);
        } catch (URISyntaxException e) {
            if (LOG.isDebugEnabled())
                LOG.error("Malformed URL", e);

            respond(SC_BAD_REQUEST, "Malformed URL", htmlMessage(SC_BAD_REQUEST + " Malformed URL"),
                    sock.getOutputStream());
            return;
        } catch (Exception e) {
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

                //Create response
                responseExecutor(resp, req);
            } catch (Exception e) {
                if (LOG.isDebugEnabled())
                    LOG.error("Server error:", e);
                respond(SC_SERVER_ERROR, "Server Error", htmlMessage(SC_SERVER_ERROR + " Server error"),
                        sock.getOutputStream());
            }

        } else if (!tryLoadFile(req, resp)) {
            respond(SC_NOT_FOUND, "Not Found", htmlMessage(SC_NOT_FOUND + " Not found"),
                    sock.getOutputStream());
        Parser.requestParser(req, resp);
        Parser.responseParser(resp.bout.toString(), resp.getStatusCode());
    }

    private Request parseRequest(Socket socket) throws IOException, URISyntaxException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());

        Request req = new Request(socket, sessions);
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
                } else if (key != null && (query.charAt(i) == AMP || last)) {
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

        if ("Cookie".equals(key)) {
            String[] pairs = sb.substring(start, len).trim().split("; ");
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                String[] keyValue = pair.split("=");
                req.mapCookie(keyValue[0], new Cookie(keyValue[0], keyValue[1]));
            }
        }
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
        // done close session listener: +++ in line 146
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
                } catch (Exception e) {
                    if (!Thread.currentThread().isInterrupted())
                        LOG.error("Error accepting connection", e);
                }
            }
        }
    }

    /**
     * HashMap for mime types
     */
    private static void setMime() {
        mimeMap.put(".txt", MIME_TEXT_PLAIN);
        mimeMap.put(".htm", MIME_TEXT_HTML);
        mimeMap.put(".js", MIME_APPLICATION_JS);
        mimeMap.put(".gif", MIME_IMAGE_GIF);
        mimeMap.put(".png", MIME_IMAGE_PNG);
        mimeMap.put(".jpeg", MIME_IMAGE_JPEG);
        mimeMap.put(".pdf", MIME_APPLICATION_PDF);
        mimeMap.put(".docx", MIME_APPLICATION_MSWORD);
        mimeMap.put(".xls", MIME_APPLICATION_MSEXCEL);

    }

    private String mimeGetter(String extension) {
        setMime();
        try {
            for (Map.Entry<String, String> entry : mimeMap.entrySet()) {
                if (extension.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return String.valueOf(SC_BAD_REQUEST);

    }

    private String getFileExtension(String str) {
        int index = str.indexOf(".");
        return index == -1 ? null : str.substring(index);
    }

    private String findMime(File file) {

        return mimeGetter(getFileExtension(file.getName()));

    }
/**
 * Support for war directory with loading static content (.txt, .html, js, .png, etc.).
 * Set properly headers, e.g. Content-Type and Content-Length. Content-Type detect by file extension.
 */
    private boolean tryLoadFile(Request req, Response resp) throws IOException {
        final File workDirectory = config.getWorkDirectory();

        if (workDirectory != null) {
            File file = new File(workDirectory, req.getPath());

            if (!file.exists()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("File {} not found", file.getAbsolutePath());
                }

                return false;
            }

            final String mime = findMime(file);

            if (LOG.isDebugEnabled())
                LOG.debug("Loading file = {}, length = {}, mime = {}", file.getAbsolutePath(), file.length(), mime);

            final String head = "HTTP/1.0 200 OK" + CRLF +
                    CONTENT_TYPE + ": " + mime + "; charset=utf-8" + CRLF +
                    CONTENT_LENGTH + ": " + file.length() + CRLF + CRLF;

            // Will be closed on socket close.
            final OutputStream out = resp.socket.getOutputStream();

            out.write(head.getBytes(StandardCharsets.UTF_8));

            try (final InputStream in = new FileInputStream(file)) {
                final byte[] buf = new byte[1024];

                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }

                out.flush();
            }

            return true;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Working directory was not set");
        }

        return false;
    }


}
