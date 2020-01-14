package ru.ifmo.server;

// абстрактный метод-обработчик

public abstract class FunctionFilter {
    protected FunctionFilter next;

    abstract void filter(Request request, Response response) throws Exception;
}
