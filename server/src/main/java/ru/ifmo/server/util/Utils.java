package ru.ifmo.server.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Some util methods.
 */
public final class Utils {
    private Utils() {
    }

    public static void closeQuiet(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static String htmlMessage(String msg) {
        return "<html><body>" + msg + "<hr><i>Powered by IFMO HTTP Server</i></body></html>";
    }
}
