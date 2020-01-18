package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.server.annotation.Uri;
import ru.ifmo.server.util.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.ifmo.server.Http.*;
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

    private static final EnumSet<HttpMethod> SUPPORTED_METHODS = EnumSet.allOf(HttpMethod.class);

    private final ServerConfig config;

    private ServerSocket socket;

    private ExecutorService acceptorPool;

    //  todo
    private static Map<String, ReflectiveHandler> reflectiveHandlers = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private Server(ServerConfig config) {
        this.config = new ServerConfig(config);
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

//      todo

            server.initReflectiveHandlers(config.getClasses());
            server.openConnection();
            server.startAcceptor();

            LOG.info("Server started on port: {}", config.getPort());
            return server;
        } catch (IOException e) {
            throw new ServerException("Cannot start server on port: " + config.getPort());
        }
    }

    private static void initReflectiveHandlers(Collection<Class<?>> classes) {
        Collection<Class<?>> classList = new ArrayList<>(classes);

        for (Class<?> c : classList) {
            try {
                String name = c.getName();
                Class<?> cls = Class.forName(name);

                for (Method method : cls.getDeclaredMethods()) {
                    Uri an = method.getAnnotation(Uri.class);
                    if (an != null) {
                        Class<?>[] params = method.getParameterTypes();
                        Class<?> methodType = method.getReturnType();

                        if (params.length == 2 && methodType.equals(void.class) && Modifier.isPublic(method.getModifiers())
                                && params[0].equals(Request.class) && params[1].equals(Response.class)) {
                            String path = an.value();

                            EnumSet<HttpMethod> set = EnumSet.copyOf(Arrays.asList(an.method()));

                            ReflectiveHandler reflectiveHandler = new ReflectiveHandler(cls.newInstance(), method, set);
                            reflectiveHandlers.put(path, reflectiveHandler);
                        } else {
                            throw new ServerException("Invalid @URL annotated method: " + c.getSimpleName() + "." + method.getName() + "(). "
                                    + "Valid method: must be public void and accept only two arguments: Request and Response." + '\n' +
                                    "Example: public void helloWorld(Request request, Response Response");
                        }

                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new ServerException("Unable initialize @URL annotated handlers. ", e);
            }
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

        Handler handler = config.handler(req.getPath(), req.method);
        Response resp = new Response(sock);

        if (handler != null) {
            try {
                handler.handle(req, resp);
            } catch (Exception e) {
                if (LOG.isDebugEnabled())
                    LOG.error("Server error:", e);

                respond(SC_SERVER_ERROR, "Server Error", htmlMessage(SC_SERVER_ERROR + " Server error"),
                        sock.getOutputStream());
            }
        }
        //todo
        else if (reflectiveHandlers.containsKey(req.getPath())) {
            final ReflectiveHandler reflectHandler = reflectiveHandlers.get(req.getPath());
            if (reflectHandler != null && reflectHandler.isApplicable(req.method))
                processReflectHandler(reflectHandler, req, resp, sock);

            /*else if (reflectiveHandlers.containsKey(req.getPath())) {
            final ReflectiveHandler hnd = reflectiveHandlers.get(req.getPath());

            if (hnd.method == req.method) {
                try {
                    hnd.reflMethod.invoke(hnd.obj, req, resp);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    // 500
                }
            } // else 404
        }*/

            else
                respond(SC_NOT_FOUND, "Not Found", htmlMessage(SC_NOT_FOUND + " Not found"),
                        sock.getOutputStream());
        }
    }

    private void processReflectHandler(ReflectiveHandler rf, Request req, Response resp, Socket sock) throws IOException {
        if (rf.method.equals(req.method))
        try {
            rf.method.invoke(rf.obj, req, resp);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (LOG.isDebugEnabled())
                LOG.error("Error invoke method:" + rf.method, e);

            respond(SC_SERVER_ERROR, "Server Error", htmlMessage(SC_SERVER_ERROR + " Server error"),
                    sock.getOutputStream());
        }
        else{
            respond(SC_NOT_FOUND, "Not Found", htmlMessage(SC_NOT_FOUND + " Not found"),
                    sock.getOutputStream());
        }
    }


    private Request parseRequest(Socket socket) throws IOException, URISyntaxException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());

        Request req = new Request(socket);
        StringBuilder sb = new StringBuilder(READER_BUF_SIZE);
        /* TODO */
        int contentLen = -1;
        while (readLine(reader, sb, contentLen) > 0) {
            if (req.method == null)
                parseRequestLine(req, sb);
            else
                parseHeader(req, sb);

            sb.setLength(0);

            if (req.getHeaders().containsKey("Content-Length")) {
                contentLen = Integer.valueOf(req.getHeaders().get("Content-Length"));
            }
        }

        if (contentLen > 0) {
            this.parseQuery(sb.toString(), req);
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
        this.parseQuery(query, req);

    }

    private void parseQuery(String query, Request req) {
        if (query != null) {
            int start = 0;

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
    }

    private int readLine(InputStreamReader in, StringBuilder sb, int contentlen) throws IOException {
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

        if (contentlen > 0 && count == 0) {
            while ((c = in.read()) >= 0) {
                sb.append((char) c);
                count++;
                if (count == contentlen) {
                    count = 0;
                    break;
                }
            }
        }

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
        return SUPPORTED_METHODS.contains(method);
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

    private static class ReflectiveHandler {
        //TODO
        private Method method;
        private Object obj;
        private EnumSet<HttpMethod> methods;

        public ReflectiveHandler(Object obj, Method method, EnumSet<HttpMethod> methods) {
            this.obj = obj;
            this.method = method;
            this.methods = methods;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public EnumSet<HttpMethod> getMethods() {
            return methods;
        }

        public void setMethods(EnumSet<HttpMethod> methods) {
            this.methods = methods;
        }

        boolean isApplicable(HttpMethod method) {
            return methods.contains(HttpMethod.ALL) || methods.contains(method);
        }
    }


}
