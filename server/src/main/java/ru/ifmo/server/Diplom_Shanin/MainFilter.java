package ru.ifmo.server.Diplom_Shanin;

public class MainFilter {
    public static void main(String[] args) {
        LinkedListFilter linkedListFilter = new LinkedListFilter();
        linkedListFilter.add("filter1");
        linkedListFilter.add("filter2");
        linkedListFilter.add("filter3");
        linkedListFilter.add("filter4");
        linkedListFilter.add("filter5");
        linkedListFilter.add("filter6");
        linkedListFilter.add("filter7");
        linkedListFilter.add("filter8");
        linkedListFilter.add("filter9");
        linkedListFilter.add("filter10");
        linkedListFilter.add("filter11");
        linkedListFilter.add("filter12");
        linkedListFilter.push("filter0");
        linkedListFilter.add("filter13");
        linkedListFilter.pop();
        linkedListFilter.pop();
        linkedListFilter.remove(5);
        linkedListFilter.take();

        for(Object filter: linkedListFilter){
            System.out.println(filter);
        }

        Object a = linkedListFilter.get(6);
        System.out.println(a);
    }
}
