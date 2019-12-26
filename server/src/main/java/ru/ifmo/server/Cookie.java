package ru.ifmo.server;

public class Cookie {

    String name;
    String value;
    String maxage;
    String domain;
    String path;
    String comment;


    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(String name, String value, String maxage) {
        this.name = name;
        this.value = value;
        this.maxage = maxage;
    }

    public Cookie(String name, String value, String maxage, String domain, String path, String comment) {

        this.name = name;
        this.value = value;
        this.maxage = maxage;
        this.domain = domain;
        this.path = path;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Cookie{" +
                "NAME='" + name + '\'' +
                ", VALUE='" + value + '\'' +
                ", MAX AGE='" + maxage + '\'' +
                ", DOMAIN='" + domain + '\'' +
                ", PATH='" + path + '\'' +
                ", COMMENT='" + comment + '\'' +
                '}';
    }
}
