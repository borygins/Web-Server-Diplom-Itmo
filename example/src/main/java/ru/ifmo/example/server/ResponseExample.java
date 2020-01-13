package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.io.PrintWriter;

public class ResponseExample {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .addHandler("/test", (request, response) -> {
                    response.setStatusCode(200);
                    response.setContentType("text/html");
                    response.setHeader("Content-Type", "text/html");
                    response.setHeader("Content-Length", "200");

                    PrintWriter pw = new PrintWriter(response.getOutputStream());
                    pw.write(Http.OK_HEADER + response.getStatusCode());
                    pw.write(response.getOutputStream().toString());
                    pw.flush();
                    pw.close();
                });
        Server.start(config);
    }
}
