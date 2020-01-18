package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class SessionListener implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private final Map<String, Session> sessions;

    public SessionListener(Map<String, Session> sessions) {
        this.sessions = sessions;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                    LocalDateTime curntTime = LocalDateTime.now();
                    Thread.sleep(1000);
                    if (entry.getValue().getExpire() != null && curntTime.isAfter(entry.getValue().getExpire())) {
                        LOG.debug("Deleting session '" + entry.getKey() + "'. Goodbye " );
                        entry.getValue().setExpired(true);
                        sessions.remove(entry.getKey());
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
