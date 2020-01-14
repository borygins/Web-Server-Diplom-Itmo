package ru.ifmo.example.server;

import ru.ifmo.server.HttpMethod;
import ru.ifmo.server.Request;
import ru.ifmo.server.Response;
import ru.ifmo.server.annotation.Uri;

import java.io.IOException;

public class MyController {
    @Uri(value = "/index", method = HttpMethod.POST)
    public void courses(Request request, Response res) throws IOException {
        res.getOutputStream().write("Hello!!".getBytes());
    }
}
