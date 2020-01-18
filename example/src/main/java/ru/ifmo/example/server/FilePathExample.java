package ru.ifmo.example.server;

import ru.ifmo.server.Server;
import ru.ifmo.server.ServerConfig;

import java.io.File;

public class FilePathExample {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .setWorkDirectory(new File("D:/ITMO/diplom/src/main/resources"));
        Server.start(config);
    }
}
