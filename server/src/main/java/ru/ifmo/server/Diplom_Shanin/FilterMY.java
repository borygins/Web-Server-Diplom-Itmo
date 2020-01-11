package ru.ifmo.server.Diplom_Shanin;

import ru.ifmo.server.Request;
import ru.ifmo.server.Response;

// фильтр

public class FilterMY extends FunctionFilter
{
    // фильтр
   Object filter;

   // указатель на следующий фильтр
   protected FilterMY next;

   FilterMY(Object filter){
       this.filter = filter;
   }

    // метод-обработчик
    @Override
    FunctionFilter filter(Request request, Response response) {
        return (next.filter(request,response));
    }
}
