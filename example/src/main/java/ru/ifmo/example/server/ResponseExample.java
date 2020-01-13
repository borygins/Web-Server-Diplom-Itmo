package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.io.Writer;

public class ResponseExample {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .addHandler("/test", new Handler() {
                    @Override
                    public void handle(Request request, Response response) throws Exception {
                        Writer writer = response.getWriter();
                        writer.write("Hello World!");
                    }
                });
        Server.start(config);
    }
}