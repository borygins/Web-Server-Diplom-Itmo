package ru.ifmo.server.annotation;

import ru.ifmo.server.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Uri {
    String value();
    HttpMethod method() default HttpMethod.ALL;
}
