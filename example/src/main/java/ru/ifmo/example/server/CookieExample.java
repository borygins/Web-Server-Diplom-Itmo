package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CookieExample {
    public static void main(String[] args) throws URISyntaxException, IOException {
        ServerConfig config = new ServerConfig()
                .addHandler("/login",
                        CookieExample::handle)
                .addHandler("/page1",
                        CookieExample::handle2)
                .addHandler("/page2",
                        CookieExample::handle3)
                .addHandler("/logout",
                        CookieExample::handle4);
        Server.start(config);

        String loginPage = "http://localhost:" + ServerConfig.DFLT_PORT + "/login";

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(loginPage));
        }
        else
            System.out.println(">>> Open " + loginPage);
    }

    private static void handle(Request request, Response response) {
        request.getSession();
        request.getArguments().get("login");
        response.setHeader("Working", "Cookies!");
        response.setCookie(new Cookie("tasty", "beer", 1));
        response.setCookie(new Cookie("yummy", "cheese", 1));
        String name = request.getArguments().get("name");

        response.getWriter().write("<!DOCTYPE html>");
        response.getWriter().write("<html><body>");
        response.getWriter().write("<head><title>Cookies & sessions example</title>");
        response.getWriter().write("</title></head>");
        response.getWriter().write("<p><b>Login page</b></p><br>");
        response.getWriter().write("CookieExample! (Look cookie value on webdev tools, seems like tasty beer and " +
                "yummy cheese)" + "\n");
        response.getWriter().write("<p>JSESSIONID=" + request.getSession().getId() + "</p>");
        if (name == null) {
            response.getWriter().write("<p>Hello new user!</p>");
        } else {
            response.getWriter().write("<p>Hello " + name + "</p>");
        }
        if (name == null) {
            response.getWriter().write("<form method=\"GET\" action=\"" + request.getPath() + "\">");
            response.getWriter().write("Name: <input type=\"text\" name=\"name\"></input><br><br>");
            response.getWriter().write("<input type=\"submit\" value=\"submit\"></input>");
            response.getWriter().write("</form>");
        } else {
            Session session = request.getSession();
            session.setParam("name", name);


            response.getWriter().write("<p><a href=\"./page1\">to Page 1</a>&nbsp;");
            response.getWriter().write("<a href=\"./page2\">to Page 2</a></p>");
        }
        response.getWriter().write("</body></html>");
    }

    private static void handle2(Request request, Response response) {
        request.getSession();
        Session session = request.getSession();
        request.getArguments().get("login");
        response.setHeader("Working", "Cookies!");
        response.setCookie(new Cookie("tasty", "beer", 1));
        response.setCookie(new Cookie("yummy", "cheese", 1));
        String name = request.getArguments().get("name");

        response.getWriter().write("<!DOCTYPE html>");
        response.getWriter().write("<html><body>");
        response.getWriter().write("<head><title>Cookies & sessions example</title>");
        response.getWriter().write("</title></head>");
        response.getWriter().write("<p><b>Page 1</b></p><br>");
        response.getWriter().write("CookieExample! (Look cookie value on webdev tools, seems like tasty beer and " +
                "yummy cheese)" + "\n");
        response.getWriter().write("<p>JSESSIONID=" + request.getSession().getId() + "</p>");
        response.getWriter().write("<p>session: " + session.getId() + "</p>");
        response.getWriter().write("<p>Your name from session: " + request.getSession().getParam("name") + "</p>");
        response.getWriter().write("<a href=\"./page2\">to Page 2</a></p>");
        response.getWriter().write("<a href=\"./logout\">Exit</a></p>");
        response.getWriter().write("</body></html>");
    }

    private static void handle3(Request request, Response response) {
        request.getSession();
        Session session = request.getSession();
        request.getArguments().get("login");
        response.setHeader("Working", "Cookies!");
        response.setCookie(new Cookie("tasty", "beer", 10));
        response.setCookie(new Cookie("yummy", "cheese", 10));

        response.getWriter().write("<!DOCTYPE html>");
        response.getWriter().write("<html><body>");
        response.getWriter().write("<head><title>Cookies & sessions example</title>");
        response.getWriter().write("</title></head>");
        response.getWriter().write("<p><b>Page 2</b></p><br>");
        response.getWriter().write("CookieExample! (Look cookie value on webdev tools, seems like tasty beer and " +
                "yummy cheese)" + "\n");
        response.getWriter().write("<p>JSESSIONID=" + request.getSession().getId() + "</p>");
        response.getWriter().write("<p>session: " + session.getId() + "</p>");
        response.getWriter().write("<p>Your name from session: " + request.getSession().getParam("name") + "</p>");
        response.getWriter().write("<a href=\"./page1\">to Page 1</a></p>");
        response.getWriter().write("<a href=\"./logout\">Exit</a></p>");
        response.getWriter().write("</body></html>");
    }

    private static void handle4(Request request, Response response) {
        request.getSession();
        Session session = request.getSession();
        session.invalidate();

        request.getArguments().get("login");
        response.setHeader("Working", "Cookies!");
        response.setCookie(new Cookie("tasty", "beer", 1));
        response.setCookie(new Cookie("yummy", "cheese", 1));

        response.getWriter().write("<!DOCTYPE html>");
        response.getWriter().write("<html><body>");
        response.getWriter().write("<head><title>Cookies & sessions example</title>");
        response.getWriter().write("</title></head>");
        response.getWriter().write("<p><b>Logout page</b></p><br>");
        response.getWriter().write("CookieExample! (Look cookie value on webdev tools, seems like tasty beer and " +
                "yummy cheese)" + "\n");
        response.getWriter().write("<p>JSESSIONID=" + request.getSession().getId() + "</p>");
        response.getWriter().write("<p>session: " + session.getId() + "</p>");
        response.getWriter().write("<p>Your name from session: " + request.getSession().getParam("name") + "</p>");
        response.getWriter().write("<a href=\"./login\">Login</a></p>");
        response.getWriter().write("</body></html>");
    }
}


