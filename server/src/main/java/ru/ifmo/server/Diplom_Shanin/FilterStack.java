package ru.ifmo.server.Diplom_Shanin;

// стэк

public interface FilterStack {

    // добавить фильтр в начало списка
    void  push(Object filter);

    // удалить фильтр из начала списка
    Object pop();
}
