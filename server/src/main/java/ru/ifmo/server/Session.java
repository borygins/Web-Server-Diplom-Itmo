package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    /**
     * Available symbols and length to generate jsessionid
     */
    private static final String JSID_SYMBOLS = "abcdefghijklmnopqrstuvwxyz123456789"; //possible chars for session id
    private static final int JSID_LENGTH = 32;                                        //max length of session id

    /**
     * Session cookie common name & session lifetime in minutes
     */
    public static int SESSION_LIVETIME = 1;
    public static String SESSION_COOKIENAME = "JSESSIONID";

    private volatile Map<String, Object> sessionData;
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
        this.expire = LocalDateTime.now().plusSeconds(minutes);
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

    public <T> void setParam(String key, T value) throws SessionException { //T value напр. корзина покупок, пока не используется
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
        LOG.debug("New session opened " + randString);
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
