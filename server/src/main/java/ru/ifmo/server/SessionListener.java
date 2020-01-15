package ru.ifmo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class SessionListener implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (Map.Entry<String, Session> entry : Server.getSessions().entrySet()) {
                    LocalDateTime ltnow = LocalDateTime.now();
                    Thread.sleep(1000);
                    if (entry.getValue().getExpire() != null && ltnow.isAfter(entry.getValue().getExpire())) {
                        // done debug ++
                        LOG.debug("Deleting session '" + entry.getKey() + "'. Goodbye " + entry.getValue().getParam(
                                "name") + " " + entry.getValue().getParam("surname"));
                        entry.getValue().setExpired(true);
                        Server.removeSession(entry.getKey());
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
