package ru.ifmo.example.server;

import ru.ifmo.server.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Simple example that shows basic usage.
 */
public class SimpleExample {
    public static void main(String[] args) throws URISyntaxException, IOException {
        Handler printHandler = new InfoHandler();

        // Define config with request handlers
        ServerConfig config = new ServerConfig()
                .addHandler("/info.html", printHandler)
                .addHandler("/info", printHandler);

        // Start server
        @SuppressWarnings("unused")
        Server server = Server.start(config);

        // And open it!
        String infoPage = "http://localhost:" + ServerConfig.DFLT_PORT + "/info.html";

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(infoPage));
        }
        else
            System.out.println(">>> Open " + infoPage);

    }

    private static class InfoHandler implements Handler {
        @Override
        public void handle(Request request, Response response) throws Exception {
            // Set correct header
            StringBuilder sb = new StringBuilder(Http.OK_HEADER);

            // Set doctype
            sb.append("<!DOCTYPE html>");

            // Write some HTML
            sb.append("<html><body>");

            sb.append("<p>Requested address: ").append(request.getPath()).append("<br>");
            sb.append("<p>Request method: ").append(request.getMethod()).append("<br>");

            Map<String, String> args = request.getArguments();

            if (!args.isEmpty()) {
                sb.append("<p><strong>Passed arguments:</strong><br>");

                for (Map.Entry<String, String> entry : args.entrySet()) {
                    sb.append("Key: ").append(entry.getKey());
                    sb.append(", Value: ").append(entry.getValue());
                    sb.append("<br>");
                }

                sb.append("</p>");
            }

            Map<String, String> headers = request.getHeaders();

            if (!headers.isEmpty()) {
                sb.append("<p><strong>Passed headers:</strong><br>");

                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    sb.append("Key: ").append(entry.getKey());
                    sb.append(", Value: ").append(entry.getValue());
                    sb.append("<br>");
                }

                sb.append("</p>");
            }

            sb.append(" <iframe width=\"420\" height=\"315\"\n" +
                    "src=\"https://www.youtube.com/embed/dQw4w9WgXcQ\">\n" +
                    "</iframe> ");

            sb.append("</body></html>");

            // Write everything to output
            response.getOutputStream().write(sb.toString().getBytes());
            response.getOutputStream().flush();
        }
    }
}
