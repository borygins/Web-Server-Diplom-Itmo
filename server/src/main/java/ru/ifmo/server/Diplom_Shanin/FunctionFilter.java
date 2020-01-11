package ru.ifmo.server.Diplom_Shanin;

// абстрактный метод-обработчик

import ru.ifmo.server.Request;
import ru.ifmo.server.Response;

public abstract class FunctionFilter {

    abstract FunctionFilter filter(Request request, Response response);
}
