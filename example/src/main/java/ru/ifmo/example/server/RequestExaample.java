package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.io.OutputStreamWriter;
import java.io.Writer;

class RequestExample {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .addHandler("/test", new Handler() {
                    @Override
                    public void handle(Request request, Response response) throws Exception {
                        Writer writer = new OutputStreamWriter(response.getOutputStream());
                        writer.write(Http.OK_HEADER + "Hello World!");
                        writer.flush();
                    }
                });

        Server.start(config);
    }
}
