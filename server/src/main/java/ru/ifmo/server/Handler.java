package ru.ifmo.server;

import java.util.Map;

/**
 * Define your implementations of this interface and register with
 * {@link ServerConfig#addHandler(String, Handler)}
 * or {@link ServerConfig#addHandlers(Map)}.
 * Method {@link #handle(Request, Response)} will be invoked on each
 * request according to mapping in {@link ServerConfig}.
 * @see ServerConfig
 * @see Server
 */
public interface Handler {
    /**
     * Invoked on each request according to mapping in
     * {@link ServerConfig#addHandler(String, Handler)}
     * or {@link ServerConfig#addHandlers(Map)}.
     *
     * @param request Request object keeps request information: method, headers, params
     *                and provides {@link java.io.InputStream} to get additional data
     *                from client.
     * @param response Response object provides {@link java.io.OutputStream} ro respond to
     *                 client.
     * @throws Exception If thrown 500 error code will be sent to client.
     */
    void handle(Request request, Response response) throws Exception;
}
