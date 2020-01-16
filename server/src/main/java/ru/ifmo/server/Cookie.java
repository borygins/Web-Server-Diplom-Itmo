package ru.ifmo.server;

public class Cookie {

    private String key;
    private String value;
    private int maxAge;
    private String domain;
    private String path;
    private String comment;


    public Cookie(String name, String value) {
        this.key = name;
        this.value = value;
    }

    public Cookie(String name, String value, int time) {
        this.key = name;
        this.value = value;
        this.maxAge = time;
    }

    public Cookie(String name, String value, int time, String domain, String path, String comment) {

        this.key = name;
        this.value = value;
        this.maxAge = time;
        this.domain = domain;
        this.path = path;
        this.comment = comment;
    }

    public void setMaxAge(int time) {
        maxAge = time;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Cookie{" +
                "NAME='" + key + '\'' +
                ", VALUE='" + value + '\'' +
                ", MAX AGE='" + maxAge + '\'' +
                ", DOMAIN='" + domain + '\'' +
                ", PATH='" + path + '\'' +
                ", COMMENT='" + comment + '\'' +
                '}';
    }
}
