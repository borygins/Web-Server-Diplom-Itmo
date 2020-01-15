package ru.ifmo.server;

public interface Dispatcher {
    String dispatch(Request request, Response response);
}
