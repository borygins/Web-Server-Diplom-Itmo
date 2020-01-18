package ru.ifmo.example.server;

import ru.ifmo.server.FunctionFilter;
import ru.ifmo.server.Request;
import ru.ifmo.server.Response;

public class SimpleFilter extends FunctionFilter {
    @Override
    protected void filter(Request request, Response response) throws Exception {

        Request req = request;
        String path = req.getPath();
        System.out.println("#INFO " + " - ServletPath :" + path + ", Argument =" + req.getArguments());

    }
}
