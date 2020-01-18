package ru.ifmo.example.server;

import ru.ifmo.server.Server;
import ru.ifmo.server.ServerConfig;

import java.util.ArrayList;

public class FiltersExample {
    public static void main(String[] args) {

        SimpleFilter simpleFilter= new SimpleFilter();

        ServerConfig serverConfig = new ServerConfig();

        serverConfig.addFilter(simpleFilter);

        Server server = new Server(serverConfig);
        server.start(serverConfig);
    }
}
