package ru.ifmo.example.server;

import ru.ifmo.server.*;

public class ResponseExample {
    public static void main(String[] args) {
        ServerConfig config;
        config = new ServerConfig()
                .addHandler("/index",
                        (request, response) -> response.getWriter().write("Hello!"));
        Server.start(config);
    }
}