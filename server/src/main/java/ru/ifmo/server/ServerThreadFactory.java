package ru.ifmo.server;

import java.util.concurrent.ThreadFactory;

/**
 * Internal server thread factory.
 */
class ServerThreadFactory implements ThreadFactory {
    private final String name;
    public static final String PREFIX = "ifmo-server-";

    public ServerThreadFactory(String name) {
        this.name = name;
    }

    public Thread newThread(Runnable r) {
        return new Thread(r, PREFIX + name);
    }

    public static ServerThreadFactory factory(String threadName) {
        return new ServerThreadFactory(threadName);
    }
}
