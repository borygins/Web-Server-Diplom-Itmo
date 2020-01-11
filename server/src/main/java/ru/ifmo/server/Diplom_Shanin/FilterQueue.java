package ru.ifmo.server.Diplom_Shanin;

// хвост

public interface FilterQueue {

    // добвить фильтр в конец списка
    void add (Object filter);

    // убрать фильтр из начала списка
    Object take();
}
