package ru.lb.design.config;

import java.net.InetSocketAddress;

public interface IConfig {
    String getAtt(String name);
    void addAtt(String name, String value);
    InetSocketAddress getRandomIPserver(String group);
    void addIPserver(String group, InetSocketAddress value);
    void setIPserver(InetSocketAddress value);
    InetSocketAddress getIPserver();

    int getCountBuf();
    void setCountBuf(int countBuf);
    int getSizeBuf();
    void setSizeBuf(int sizeBuf);

    void setCountSelector(int countSelector);
    int getCountSelector();
}
