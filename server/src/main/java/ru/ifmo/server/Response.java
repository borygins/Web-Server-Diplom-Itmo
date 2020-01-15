package ru.ifmo.server;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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
    int length;
    ByteArrayOutputStream bout;
    String location;

    Response(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return {@link OutputStream} connected to the client.
     */
    public ByteArrayOutputStream getOutputStream() {
        if (bout == null)
            bout = new ByteArrayOutputStream(1024);

        return bout;
    }

    /**
     * @return Content-Length
     * @info Возвращает длину content body in the response In HTTP servlets.
     */
    public long getContentLength() {
        return length;
    }

    /**
     * @info Задает длину тела содержимого в ответе В сервлете HTTP этот метод задает заголовок HTTP Content-Length.
     */
    public void setContentLength(int length) {
        setHeader(Http.CONTENT_LENGTH, String.valueOf(length));
        this.length = length;
    }

    /**
     * @info Задает тип содержимого ответа, отправляемого клиенту, если ответ еще не зафиксирован.
     * Данный тип контента может включать в себя спецификацию кодирования символов, например, текст/html; charset = UTF-8.
     * Кодировка символов ответа устанавливается из данного типа контента только в том случае, если этот метод вызывается до вызова getWriter.
     */
    public void setContentType(String type) {
        setHeader(Http.CONTENT_TYPE, type);
    }

    /**
     * Возвращает объект PrintWriter, который может отправлять клиенту поток.
     */
    public PrintWriter getWriter() {
        if (writer == null)
            writer = new PrintWriter(getOutputStream());
        return writer;
    }

    /**
     * Добавляет заголовок response HTTP с указанным именем и значением.
     *
     * @param name  name header
     * @param value String value header
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Возвращает хэдеры в Map (http с именем и значением)
     */
    private Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    /**
     * Переписывает заголовки http с именем и значением
     *
     * @param headers map name and value
     */
    public void setHeaders(Map<String, String> headers) {
        getHeaders().putAll(headers);
    }

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
     * Метод возвращает код состояния HTTP (current response)
     *
     * @return int http status code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public void redirect(String location) {
        this.location = location;
        getHeaders();
    }

    public String getLocation() {
        return location;
    }
}

