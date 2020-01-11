package ru.ifmo.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides {@link java.io.OutputStream} ro respond to client.
 */
public class Response {
    final Socket socket;
    List<Cookie> setCookies;
    PrintWriter printWriter;
    ByteArrayOutputStream bufferOutputStream;

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
     * Returns a buffered OutputStream suitable for writing binary data in the response. Need send responseto client exec method FlushBuffer
     * @return buffered ByteArrayOutputStream
     */
    public ByteArrayOutputStream getOutputStreamBuffer() {
        if (bufferOutputStream==null)
            bufferOutputStream = new ByteArrayOutputStream();

        return bufferOutputStream;
    }

    /**
     * Returns a PrintWriter object that can send character text to the client.
     flush() calling automatically on flushBuffer()
     * @return {@link PrintWriter}
     * @throws ServerException  if an output exception occurred
     */
    public PrintWriter getWriter() {
        if (printWriter==null)
            printWriter = new PrintWriter(getOutputStreamBuffer());
        return printWriter;
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
