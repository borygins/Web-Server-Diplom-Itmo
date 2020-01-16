package ru.ifmo.server;

class DispatcherTest implements Dispatcher{
    static final String FOR_DISPATCH_URL = "/for_dispatch";
    static final String DISPATCHED_URL = "/dispatched";

    @Override
    public String dispatch(Request request, Response response) {
        if (request.getPath().equals(FOR_DISPATCH_URL))
            return DISPATCHED_URL;

        return request.getPath();
    }
}