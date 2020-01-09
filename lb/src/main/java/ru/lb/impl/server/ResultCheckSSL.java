package ru.lb.impl.server;

import javax.net.ssl.SSLEngine;

public class ResultCheckSSL {
    private boolean result;
    private SSLEngine engine;

    public ResultCheckSSL(boolean result, SSLEngine engine) {
        this.result = result;
        this.engine = engine;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public SSLEngine getEngine() {
        return engine;
    }

    public void setEngine(SSLEngine engine) {
        this.engine = engine;
    }
}
