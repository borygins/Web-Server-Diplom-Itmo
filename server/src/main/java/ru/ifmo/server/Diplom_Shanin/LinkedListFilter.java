package ru.ifmo.server.Diplom_Shanin;

import java.util.Iterator;

// Связный список из фильтров

public class LinkedListFilter implements FilterList, FilterQueue, FilterStack {

    // ссылка на головной элемент списка
    private FilterMY TailFilter;

    @Override
    public void add(Object filter) {
        if (TailFilter==null) {
            TailFilter =new FilterMY(filter);
            return;
        }
        FilterMY newFilterMY = TailFilter;
        while (newFilterMY.next !=null){
            newFilterMY = newFilterMY.next;
        }
        newFilterMY.next = new FilterMY(filter);
    }

    @Override
    public Object take() {

        if (TailFilter==null){
            return null;
        }
        Object temp = TailFilter.filter;
        TailFilter = TailFilter.next;
        return temp;
    }

    @Override
    public Object get(int i) {
        if(TailFilter==null||i<0){
            return null;
        }

        FilterMY newFilterMY = TailFilter;
        for (int j = 0; j < i ; j++) {
            newFilterMY = newFilterMY.next;
            if(newFilterMY == null){
                return null;
            }
        }

        return newFilterMY.filter;
    }

    @Override
    public Object remove(int i) {
        if(TailFilter==null||i<0){
            return null;
        }

        FilterMY deleteFilterMY = TailFilter;
        FilterMY previous = TailFilter;
        FilterMY following = TailFilter;

        if(i==0) {
            TailFilter = deleteFilterMY.next;
            return deleteFilterMY.filter;
        }

        for (int j = 0; j < i; j++) {

            previous = deleteFilterMY;
            deleteFilterMY = deleteFilterMY.next;
            following = deleteFilterMY.next;

            if(deleteFilterMY==null){
                return null;
            }
        }

        previous.next = following;
        return deleteFilterMY.filter;
    }

    @Override
    public Iterator iterator() {

        return new FilterListIterator() {
            FilterMY iterator = TailFilter;
            Object iterfilter=null;

            @Override
            public boolean hasNext() {

                return iterator!=null;
            }

            @Override
            public Object next() {
                iterfilter = iterator.filter;
                iterator = iterator.next;
                return iterfilter;
            }
        };
    }

    @Override
    public void push(Object filter) {
        FilterMY pushFilterMY = TailFilter;
        TailFilter = new FilterMY(filter);
        TailFilter.next = pushFilterMY;
    }

    @Override
    public Object pop() {

        if (TailFilter==null) {
            return null;
        }

        Object temp = TailFilter.filter;
        TailFilter = TailFilter.next;
        return temp;
    }
}
