package ru.ifmo.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds server configs: local port, handler mappings, etc.
 */
public class ServerConfig {
    /** Default local port. */
    public static final int DFLT_PORT = 8080;

    private int port = DFLT_PORT;
    private Map<String, Handler> handlers;
    private int socketTimeout;

    public ServerConfig() {
        handlers = new HashMap<>();
    }

    public ServerConfig(ServerConfig config) {
        this();

        port = config.port;
        handlers = new HashMap<>(config.handlers);
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

    /**
     * Add handler mapping.
     *
     * @param path Path which will be associated with this handler.
     * @param handler Request handler.
     * @return Itself for chaining.
     */
    public ServerConfig addHandler(String path, Handler handler) {
        handlers.put(path, handler);

        return this;
    }

    /**
     * Add handler mappings.
     *
     * @param handlers Map paths to handlers.
     * @return Itself for chaining.
     */
    public ServerConfig addHandlers(Map<String, Handler> handlers) {
        this.handlers.putAll(handlers);

        return this;
    }

    Handler handler(String path) {
        return handlers.get(path);
    }

    /**
     * @return Current handler mapping.
     */
    public Map<String, Handler> getHandlers() {
        return handlers;
    }

    /**
     * Set handler mappings.
     *
     * @param handlers Handler mappings.
     */
    public void setHandlers(Map<String, Handler> handlers) {
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
                ", socketTimeout=" + socketTimeout +
                '}';
    }
}
