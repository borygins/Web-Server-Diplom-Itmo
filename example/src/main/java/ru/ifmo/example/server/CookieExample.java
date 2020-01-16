package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.io.Writer;

public class CookieExample {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .addHandler("/index",
                        (Request request, Response response) -> {
                            request.getSession().getId();
                            request.getArguments().get("login");
                            response.setHeader("FIRST", "HANDLER!");
                            response.setCookie(new Cookie("tasty", "strawberry", 1));
                            response.setCookie(new Cookie("yummy", "choco", 1));
                            response.getWriter().write("CookieExample!");
                        });
        Server.start(config);
    }
}


