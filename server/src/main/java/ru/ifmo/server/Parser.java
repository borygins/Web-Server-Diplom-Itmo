package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

class Parser {

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);
    private static String dir = System.getProperty("user.dir") + "\\example\\src\\main\\java\\ru\\ifmo\\example\\server\\logs";
    private static String file = dir + "\\access.log";

    private static String hostIP; // хост/IP-адрес, с которого произведён запрос к серверу;
    private static String type; // тип запроса, его содержимое и версия;
    private static String statusCode = ""; // код состояния HTTP;
    private static String userAgent; // HTTP-заголовок, содержащий информацию о запросе (клиентское приложение, язык и т. д.);
    private static String destinationPath;  // Virtual Host, к которому идет обращение.
    private static String language = ""; //Список поддерживаемых естественных языков
    private static String typeReq = ""; //Список допустимых форматов ресурса.

    private static String getTime() {
        Date currentTime = new Date();
        SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return newDateFormat.format(currentTime);
    }

    private static File checkDir(String location) {
        File dir = new File(location);
        if (!dir.exists()) dir.mkdirs();
        return new File(location);
    }

    private static String modificator(String str) {
        try {
            return str.replace("[", "").replace("]", "")
                    .replace("{", "").replace("}", "");
        } catch (NullPointerException e) {
            LOG.warn(e.getMessage());
        }
        return null;
    }

    private static ArrayList<String> tokenizer(String string) throws NoSuchElementException {
        String[] splitter = string.split(",");
        ArrayList list = new ArrayList();
        Collections.addAll(list, splitter);
        return list;
    }

    static synchronized void requestParser(Request request, Response response) {
        String requestString = modificator(request.toString());
        String time = getTime();
        assert requestString != null;
        ArrayList<String> list = tokenizer(requestString);
        for (String tokens : list) {
            if (tokens.contains("Host")) {
                String[] getHost = tokens.substring(tokens.indexOf("Host=") + 5).split(":");
                if (getHost[0].contains("localhost")) hostIP = "127.0.0.1";
                else
                    hostIP = getHost[0];
            }
            if (tokens.contains("method")) {
                String[] getMethod = tokens.split("=");
                type = getMethod[1] + "=";
            }
            if (tokens.contains("path")) {
                String[] getMethod = tokens.split("=");
                destinationPath = getMethod[1];
            }
            int agentParseBegin = requestString.indexOf("User-Agent");
            int agentParseEnd = requestString.indexOf("Sec-Fetch");
            userAgent = requestString.substring(agentParseBegin, agentParseEnd);
            int langParseBegin = requestString.indexOf("Language");
            int langParseEnd = requestString.indexOf("Cookie");
            language = requestString.substring(langParseBegin, langParseEnd + 1);
            statusCode = String.valueOf(response.getStatusCode());
            int typeReqParseBegin = requestString.indexOf("text/html");
            int typeReqParseEnd = requestString.indexOf(", Sec-Fetch-Site");
            typeReq = requestString.substring(typeReqParseBegin, typeReqParseEnd);
        }

        fileWriter(hostIP + " - - [" + time + "] " + type + destinationPath
                + " " + statusCode + " " + " accept=" + typeReq + " " + userAgent + " " + language + "\n");
    }

    static synchronized void responseParser(String response, int statusCode) {
        String time = getTime();
        ArrayList<String> list = tokenizer(response);
        for (String tokens : list) {
            if (tokens.contains("Host")) {
                String[] getHost = tokens.substring(tokens.indexOf("Host=") + 5).split(":");
                hostIP = getHost[0];
            }
        }
        fileWriter(hostIP + " - - [" + time + "] RESPONSE - " + statusCode + " " + response + "\n");
    }

    private static void fileWriter(String parsingString) {
        checkDir(dir);
        try (FileOutputStream outFile = new FileOutputStream(file, true);
             BufferedOutputStream outBuff = new BufferedOutputStream(outFile)) {
            byte[] buffer = parsingString.getBytes();
            outBuff.write(buffer, 0, buffer.length);
            outBuff.flush();
        } catch (IOException ex) {
            LOG.warn(ex.fillInStackTrace().toString());
        } finally {
            System.gc();
        }
    }
}