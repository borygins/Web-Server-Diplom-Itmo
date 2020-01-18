package ru.lb.design.config;

import ru.lb.impl.config.ConfigIPServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public interface IConfig {
    String getAtt(String name);
    void addAtt(String name, String value);
    InetSocketAddress getRandomIPserver(String group);
    InetSocketAddress getRandomIPserver();
    List<InetSocketAddress> getListIPserver(String group);
    void addIPserver(String group, InetSocketAddress value);
    void setIPserver(ConfigIPServer value);
    Map<String, List<InetSocketAddress>> getGroupServer();
    List<ConfigIPServer> getIPlb();

    int getCountBuf();
    void setCountBuf(int countBuf);
    int getSizeBuf();
    void setSizeBuf(int sizeBuf);

    void setPatternReadHeadHost(String pattern);
    Matcher getMatcher(String str);
}
