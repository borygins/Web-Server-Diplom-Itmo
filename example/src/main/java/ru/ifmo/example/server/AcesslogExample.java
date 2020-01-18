package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.io.OutputStreamWriter;
import java.io.Writer;

public class AcesslogExample {

    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .addHandler("/access", (request, response) -> {
                    Writer writer = new OutputStreamWriter(response.getOutputStream());
                    writer.write(Http.OK_HEADER + "Access Log Check!");
                    writer.flush();
                });

        Server.start(config);
    }
}
