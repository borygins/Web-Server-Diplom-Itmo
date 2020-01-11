package ru.ifmo.server.Diplom_Shanin;

import java.util.Iterator;

// итератор

public abstract class FilterListIterator implements Iterator {

    @Override
    public abstract boolean hasNext();

    @Override
    public abstract Object next();
}
