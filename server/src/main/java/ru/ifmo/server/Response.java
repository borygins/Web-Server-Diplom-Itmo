package ru.ifmo.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides {@link java.io.OutputStream} ro respond to client.
 */
public class Response {
    final Socket socket;
    List<Cookie> setCookies;

    Response(Socket socket) {
        this.socket = socket;
    }

    /**
     *Установка Cookie
     */
    public void setCookie(Cookie cookie) {

        if (setCookies == null) {
            setCookies = new ArrayList<>();
        }
        setCookies.add(cookie);
    }

    /**
     *Сброс Cookie
     */

    public void resetCookie(Cookie cookie) {

        if (setCookies == null) {
            setCookies = new ArrayList<>();
        }

        cookie.value = " ";

        setCookies.add(cookie);
    }

    /**
     * @return {@link OutputStream} connected to the client.
     */
    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        }
        catch (IOException e) {
            throw new ServerException("Cannot get output stream", e);
        }
    }
}
