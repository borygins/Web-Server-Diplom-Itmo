package ru.ifmo.server;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {

    /**
     * Aviable symbols and lenght to generate jsessionid
     */
    private static final String JSID_SYMBOLS = "abcdefghijklmnopqrstuvwxyz123456789";
    private static final int JSID_LENGTH = 32;

    /**
     * Session name & livetime in minutes
     */
    public static int SESSION_LIVETIME = 1;
    public static String SESSION_COOKIENAME = "JSESSIONID";

    private Map<String, Object> sessionData;

    private String id;
    private LocalDateTime expire;
    volatile boolean expired;

    public Session() {
        this.id = generateSID();
        this.setExpire(SESSION_LIVETIME);
        this.expired = false;
    }

    public LocalDateTime getExpire() {
        return expire;
    }

    public synchronized void setExpire(int minutes) {
        this.expire = LocalDateTime.now().plusMinutes(minutes);
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getId() {
        return id;
    }

    public void invalidate() {
        expired = true;
        Server.removeSession(id);
    }

    public <T> void setParam(String key, T value) throws SessionException {
        if (!expired) {
            if (sessionData == null) {
                synchronized (this) {
                    if (sessionData == null) {
                        sessionData = new ConcurrentHashMap<>();
                    }
                }
            }
            sessionData.put(key, value);
        } else throw new SessionException("Session is expired!");
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(String key) {
        return sessionData == null ? null : (T) sessionData.get(key);
    }

    public static String generateSID() {
        String symbols = JSID_SYMBOLS;

        StringBuilder randString = new StringBuilder();
        for (int i = 0; i < JSID_LENGTH; i++) {
            randString.append(symbols.charAt((int) (Math.random() * symbols.length())));
        }
        return randString.toString();
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionData=" + sessionData + ", " +
                "expires=" + expire +
                "expired=" + expired +
                '}';
    }
}
