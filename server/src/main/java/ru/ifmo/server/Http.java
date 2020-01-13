package ru.ifmo.server;

/**
 * HTTP constants.
 */
public class Http {
    public static final int SC_CONTINUE = 100;
    public static final int SC_OK = 200;
    public static final int SC_MULTIPLE_CHOICES = 300;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;

    public static String[] codeTranslator = new String[600];

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
    //todo Map with MineType (replace from Server str 310)
    //getMimeType, список типов
    //octet-stream - двоичный файл без указания формата (https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_MIME-%D1%82%D0%B8%D0%BF%D0%BE%D0%B2)
    public static final String MIME_BINARY = "application/octet-stream";
    public static final String MIME_TEXT_PLAIN = "text/txt";
    public static final String MIME_TEXT_HTML = "text/html";
    public static final String MIME_APPLICATION_JS = "application/javascript";
    public static final String MIME_IMAGE_GIF = "image/gif";
    public static final String MIME_IMAGE_PNG = "text/png";
    public static final String MIME_IMAGE_JPEG = "image/jpeg";
    public static final String MIME_APPLICATION_PDF = "application/pdf";
    public static final String MIME_APPLICATION_MSWORD = "application/msword";
    public static final String MIME_APPLICATION_MSEXCEL = "application/msexcel";
    /**
     * Headers
     */
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
}