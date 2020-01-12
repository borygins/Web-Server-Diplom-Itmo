package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CookieExample {
    static Map<String, Object> sessionData = new ConcurrentHashMap<>();

    public static void main(String[] args) throws URISyntaxException, IOException {

        Handler printHandler2 = new InfoHandler2();
        Handler printHandler3 = new InfoHandler3();
        Handler printHandler4 = new InfoHandler4();
        Handler printHandler5 = new InfoHandler5();

        Map<String, Handler> handlers = new HashMap<>();
        handlers.put("/login.html", printHandler2);
        handlers.put("/login", printHandler2);
        handlers.put("/page1.html", printHandler3);
        handlers.put("/page1", printHandler3);
        handlers.put("/page2.html", printHandler4);
        handlers.put("/page2", printHandler4);
        handlers.put("/logout.html", printHandler5);
        handlers.put("/logout", printHandler5);

        // Define config with request handlers
        ServerConfig config = new ServerConfig()
                .addHandlers(handlers);




        // Start server
        @SuppressWarnings("unused")
        Server server = Server.start(config);

        // And open it!
        String loginPage = "http://localhost:" + ServerConfig.DFLT_PORT + "/login.html";

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(loginPage));
        }
        else
            System.out.println(">>> Open " + loginPage);

    }

    private static class InfoHandler2 implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {
            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            String name = request.getArguments().get("name");
            String password = request.getArguments().get("password");

            // Set doctype
            sb.append("<!DOCTYPE html>");

            // Write some HTML
            sb.append("<html><body>");
            sb.append("<head><title>Cookie example</title>");
            sb.append("</title>");
            sb.append("<p><b>Login page</b></p><br>");


            if (name == null) {
                sb.append("<p>Hello new user!</p>");
            } else {
                sb.append("<p>Hello " + name + "</p>");
            }
            if (name == null && password == null) {

                sb.append("<form method=\"GET\" action=\"" + request.getPath() + "\">");
                sb.append("Name: <input type=\"text\" name=\"name\"></input><br><br>");
                sb.append("Password: <input type=\"password\" name=\"password\"></input><br><br>");
                sb.append("<input type=\"submit\" value=\"submit\"></input>");
                sb.append("</form>");
            } else {

                Session session = request.getSession();

                session.setParam("name", name);
                session.setParam("password", password);

                sessionData.put("name", name);
                sessionData.put("password", password);

                sb.append("<p><a href=\"./page1\">to Page 1</a>&nbsp;");
                sb.append("<a href=\"./page2\">to Page 2</a></p>");

            }

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



            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            String name = request.getArguments().get("name");
            String password = request.getArguments().get("password");

            sessionData.get("name");
            sessionData.get("password");

            // Set doctype
            sb.append("<!DOCTYPE html>");

            // Write some HTML
            sb.append("<html><body>");
            sb.append("<head><title>Cookie example</title>");
            sb.append("</title>");
            sb.append("<p><b>Page 1</b></p><br>");
            sb.append("<p>session: " + session.getId() + "</p>");
            sb.append("<p>Your name from session: " + name + "</p>");
            sb.append("<p>Your password from session: " + password + "</p>");
            sb.append("<a href=\"./page2\">to Page 2</a></p>");
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

            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            String name = request.getArguments().get("name");
            String password = request.getArguments().get("password");

            sessionData.get("name");
            sessionData.get("password");

            // Set doctype
            sb.append("<!DOCTYPE html>");

            // Write some HTML
            sb.append("<html><body>");
            sb.append("<head><title>Cookie example</title>");
            sb.append("</title>");
            sb.append("<p><b>Page 2</b></p><br>");
            sb.append("<p>session: " + session.getId() + "</p>");
            sb.append("<p>Your name from session: " + name + "</p>");
            sb.append("<p>Your password from session: " + password + "</p>");
            sb.append("<a href=\"./page2\">to Page 2</a></p>");
            sb.append("<a href=\"./logout\">Exit</a></p>");
            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }

    private static class InfoHandler5 implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {

            Session session = request.getSession();

            session.invalidate();

            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            String name = request.getArguments().get("name");
            String password = request.getArguments().get("password");

            sessionData.get("name");
            sessionData.get("password");

            // Set doctype
            sb.append("<!DOCTYPE html>");

            // Write some HTML
            sb.append("<html><body>");
            sb.append("<head><title>Cookie example</title>");
            sb.append("</title>");
            sb.append("<p><b>Logout page</b></p><br>");
            sb.append("<p>session: " + session.getId() + "</p>");
            sb.append("<p>Your name from session: " + name + "</p>");
            sb.append("<p>Your password from session: " + password + "</p>");
            sb.append("<a href=\"./login\">Login</a></p>");
            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }

}

