package ru.ifmo.server.Diplom_Shanin;

// лист

    public interface FilterList extends Iterable {

        //  Добавляет значение в конец списка
        void add(Object val);

        // Извлекает значение из списка по индексу
        Object get(int i);

        // Удаляет значение по индексу и возвращает удаленный элемент
        Object remove(int i);
    }
