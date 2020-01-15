package ru.ifmo.server;

/**
 * HTTP constants.
 */
public class Http {
    public static final int SC_CONTINUE = 100;
    public static final int SC_OK = 200;
    public static final int SC_MULTIPLE_CHOICES = 300;
    public static final int SC_REDIRECT = 301;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;

    public static final String[] codeTranslator = new String[600];

    /**
     * Translate status codes
     * */
    static {
        codeTranslator[SC_CONTINUE] = "Continue";
        codeTranslator[SC_OK] = "Ok";
        codeTranslator[SC_MULTIPLE_CHOICES] = "Multiple choises";
        codeTranslator[SC_BAD_REQUEST] = "Bad request";
        codeTranslator[SC_NOT_FOUND] = "Not found";
        codeTranslator[SC_SERVER_ERROR] = "Server Error";
        codeTranslator[SC_NOT_IMPLEMENTED] = "Not implemented";
    }

    /**
     * OK header that preceded rest response data.
     */
    public static final String OK_HEADER = "HTTP/1.0 200 OK\r\n\r\n";

    /**
     * Headers
     */
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";

}
