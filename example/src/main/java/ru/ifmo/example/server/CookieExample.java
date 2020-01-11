package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CookieExample {

    public static void main(String[] args) throws URISyntaxException, IOException {
        Handler printHandler1 = new CookieExample.InfoHandler1();
        Handler printHandler2 = new CookieExample.InfoHandler2();
        Handler printHandler3 = new CookieExample.InfoHandler3();
        Handler printHandler4 = new CookieExample.InfoHandler3();

        Map<String, Handler> handlers = new HashMap<>();

        handlers.put("/login.html", printHandler1);
        handlers.put("/login", printHandler1);
        handlers.put("/page1.html", printHandler2);
        handlers.put("/page1", printHandler2);
        handlers.put("/page2.html", printHandler3);
        handlers.put("/page2", printHandler3);
        handlers.put("/logout.html", printHandler4);
        handlers.put("/logout", printHandler4);


        // Define config with request handlers
        ServerConfig config = new ServerConfig()
                .addHandlers(handlers);


        // Start server
        @SuppressWarnings("unused")
        Server server = Server.start(config);

        // And open it!
        String infoPage = "http://localhost:" + ServerConfig.DFLT_PORT + "/login.html";

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(infoPage));
        }
        else
            System.out.println(">>> Open " + infoPage);

    }

    private static class InfoHandler1 implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {
            String name = request.getArguments().get("name");
            String surname = request.getArguments().get("surname");
            String password = request.getArguments().get("password");

            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            // Set doctype
            sb.append("<!DOCTYPE html>");
            sb.append("<html><body>");
            sb.append("<head><title>Java ITMO project</title>");
            sb.append("</title>");
            sb.append("<p><b>Login page</b></p><br>");

            if (name == null) {
                sb.append("<p>Hello new user!</p>");
            } else {
                sb.append("<p>Hello " + name + "</p>");
            }
            if (name == null && surname == null && password == null) {

                sb.append("<form method=\"POST\" action=\"" + request.getPath() + "\">");
                sb.append("Name: <input type=\"text\" name=\"name\"></input><br><br>");
                sb.append("Surname: <input type=\"text\" name=\"surname\"></input><br><br>");
                sb.append("Password: <input type=\"password\" name=\"password\"></input><br><br>");
                sb.append("<input type=\"submit\" value=\"submit\"></input>");
                sb.append("</form>");
            } else {

                Session session = request.getSession();

                session.setParam("name", name);
                session.setParam("surname", surname);
                session.setParam("password", password);

                sb.append("<p><a href=\"./page1\">to Page 1</a>&nbsp;");
                sb.append("<a href=\"./page2\">to Page 2</a></p>");

            }

            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }

    private static class InfoHandler2 implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {
            Session session = request.getSession();

            String name = session.getParam("name");
            String surname = session.getParam("surname");
            String password = session.getParam("password");

            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            // Set doctype
            sb.append("<!DOCTYPE html>");
            sb.append("<html><body>");
            sb.append("<head><title>Java ITMO project</title>");
            sb.append("</title>");
            sb.append("<p><b>Page 1</b></p><br>");
            sb.append("<p>session: " + session.getId() + "</p>");
            sb.append("<p>Your name from session: " + name + "</p>");
            sb.append("<p>Your surname from session: " + surname + "</p>");
            sb.append("<p>Your password from session: " + password + "</p>");
            sb.append("<a href=\"./page2\">to Page 2</a></p>");
            sb.append("<a href=\"./logout\">Exit</a></p>");
            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }

    private static class InfoHandler3 implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {
            Session session = request.getSession();

            String name = session.getParam("name");
            String surname = session.getParam("surname");
            String password = session.getParam("password");

            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            // Set doctype
            sb.append("<!DOCTYPE html>");
            sb.append("<html><body>");
            sb.append("<head><title>Java kanban project</title>");
            sb.append("</title>");
            sb.append("<p><b>Page 2</b></p><br>");
            sb.append("<p>session: " + session.getId() + "</p>");
            sb.append("<p>Your name from session: " + name + "</p>");
            sb.append("<p>Your surname from session: " + surname + "</p>");
            sb.append("<p>Your password from session: " + password + "</p>");
            sb.append("<a href=\"./page1\">to Page 1</a></p>");
            sb.append("<a href=\"./logout\">Exit</a></p>");
            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }

    private static class InfoHandler4 implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {
            Session session = request.getSession();

            String name = session.getParam("name");
            String surname = session.getParam("surname");
            String password = session.getParam("password");

            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            // Set doctype
            sb.append("<!DOCTYPE html>");
            sb.append("<html><body>");
            sb.append("<head><title>Java kanban project</title>");
            sb.append("</title>");
            sb.append("<p><b>Logout page</b></p><br>");
            sb.append("<p>session: " + session.getId() + "</p>");
            sb.append("<p>Your name from session: " + name + "</p>");
            sb.append("<p>Your surname from session: " + surname + "</p>");
            sb.append("<p>Your password from session: " + password + "</p>");
            sb.append("<a href=\"./login\">Login</a></p>");
            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }

}
