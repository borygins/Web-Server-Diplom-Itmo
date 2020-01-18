package ru.ifmo.server;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides {@link java.io.OutputStream} ro respond to client.
 */
public class Response {
    final Socket socket;
    int statusCode;
    Map<String, String> headers = new HashMap<>();
    PrintWriter writer;
    ByteArrayOutputStream bout;
    Map<String, Cookie> setCookies;


    Response(Socket socket) {
        this.socket = socket;
    }

    public void setHeaders(Map<String, String> headers) {
        getHeaders().putAll(headers);
    }
    /**
     * Возвращает хэдеры в Map (http с именем и значением)
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    /**
     * Метод возвращает код состояния HTTP (current response)
     *     * @return int http status code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public void setCookie(Cookie cookie) {
        if (setCookies == null) {
            setCookies = new HashMap<>();
        }
        setCookies.put(cookie.getKey(), cookie);
    }

    /**
     * Добавляет заголовок response HTTP с указанным именем и значением.
     *
     * @param key  name header
     * @param val String value header
     */
    public void setHeader(String key, String val) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, val);
    }

    public void resetCookie(Cookie cookie) {
        if (setCookies == null) {
            setCookies = new HashMap<>();
        }
        setCookies.put(" ", cookie);
    }

    /**
     * Переписывает заголовки http с именем и значением
     *
     * @param headers map name and value
     */

    /**
     * Этот метод задает код состояния HTTP
     *
     * @param code method takes an int (the status code) as an argument.
     */
    public void setStatusCode(int code) {
        if (code < Http.SC_CONTINUE || code > Http.SC_NOT_IMPLEMENTED) {
            throw new ServerException("Invalid Status Code " + code);
        }
        statusCode = code;
    }
    /**
     * @return {@link OutputStream} connected to the client.
     */
    public ByteArrayOutputStream getOutputStream() {
        if (bout == null) {
            bout = new ByteArrayOutputStream(1024);
        }
        return bout;
    }
    /**
     * Возвращает объект PrintWriter, который может отправлять клиенту поток.
     */
    public PrintWriter getWriter() {
        if (writer == null) {
            writer = new PrintWriter(getOutputStream());
        }
        return writer;
    }
}
