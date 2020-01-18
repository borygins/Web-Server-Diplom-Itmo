package ru.ifmo.server;

import ru.ifmo.server.annotation.RequestType;

import java.util.*;

/**
 * Holds server configs: local port, handler mappings, etc.
 */
public class ServerConfig {
    /**
     * Default local port.
     */
    public static final int DFLT_PORT = 8080;
    private Collection<Class<?>> classes;

    private int port = DFLT_PORT;
    private Map<String, Map<HttpMethod, Handler>> handlers;
    private int socketTimeout;

    public ServerConfig() {
        handlers = new HashMap<>();
        classes = new HashSet<>();
    }

    public ServerConfig(ServerConfig config) {
        this();

        port = config.port;
        classes = new HashSet<>(config.classes);
        handlers = new HashMap<>();
        for (Map.Entry<String, Map<HttpMethod, Handler>> handler : config.handlers.entrySet()) {
            handlers.put(handler.getKey(), handler.getValue());
        }
        socketTimeout = config.socketTimeout;
    }

    /**
     * @return Local port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Define local port.
     *
     * @param port TCP port.
     * @return Itself for chaining.
     */
    public ServerConfig setPort(int port) {
        this.port = port;

        return this;
    }

//    public ServerConfig addHandler(Handler handler) {
//        try {
//            RequestType requestType = handler.getClass().getDeclaredMethod("handle", Request.class, Response.class)
//                    .getDeclaredAnnotation(RequestType.class);
//            String path = "";
//            if (requestType != null && requestType.path().length() > 0) {
//                path = requestType.path();
//            }
//            return this.addHandler(path, handler);
//        } catch (NoSuchMethodException e) {
//            throw new ServerException("Cannot process annotation", e);
//        }
//    }

    /**
     * Add handler mapping.
     *
     * @param path    Path which will be associated with this handler.
     * @param handler Request handler.
     * @return Itself for chaining.
     */
    public ServerConfig addHandler(String path, Handler handler) {
        // todo remove code duplicates
        RequestType requestType = null;
        try {
            requestType = handler.getClass().getDeclaredMethod("handle", Request.class, Response.class)
                    .getDeclaredAnnotation(RequestType.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        HttpMethod[] methods;
        if (requestType == null) {
            methods = new HttpMethod[]{HttpMethod.ALL};
        } else {
            methods = requestType.method();
        }

        if (Arrays.stream(methods).filter((q1) -> q1 == HttpMethod.ALL).count() > 0)
            methods = HttpMethod.values();

        for (HttpMethod method : methods) {
            if (handlers.containsKey(path)) {
                handlers.get(path).put(method, handler);
            } else {
                Map<HttpMethod, Handler> temp = new HashMap<>();
                temp.put(method, handler);
                handlers.put(path, temp);
            }
        }

        return this;
    }

    /**
     * Add handler mappings.
     *
     * @param handlers Map paths to handlers.
     * @return Itself for chaining.
     */
    public ServerConfig addHandlers(Map<String, Map<HttpMethod, Handler>> handlers) {
        this.handlers.putAll(handlers);
        return this;
    }

    Handler handler(String path, HttpMethod method) {
        if (handlers.containsKey(path)) {
            if (handlers.get(path).containsKey(method)) {
                return handlers.get(path).get(method);
            }
        }
        return null;
    }

    public ServerConfig addClasses(Collection<Class<?>> classes) {
        this.classes.addAll(classes);

        return this;
    }

    public ServerConfig addClass(Class<?> cls) {
        this.classes.add(cls);
        return this;
    }

    public Collection<Class<?>> getClasses() {
        return classes;
    }


    /**
     * @return Current handler mapping.
     */
    public Map<String, Map<HttpMethod, Handler>> getHandlers() {
        return handlers;
    }

    /**
     * Set handler mappings.
     *
     * @param handlers Handler mappings.
     */
    public void setHandlers(Map<String, Map<HttpMethod, Handler>> handlers) {
        this.handlers = handlers;
    }

    /**
     * @return Socket timeout value.
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Set socket timeout. By default it's unlimited.
     *
     * @param socketTimeout Socket timeout, 0 means no timeout.
     * @return Itself for chaining.
     */
    public ServerConfig setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;

        return this;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "port=" + port +
                ", handlers=" + handlers +
                ", classes=" + classes +
                ", socketTimeout=" + socketTimeout +
                '}';
    }
}
