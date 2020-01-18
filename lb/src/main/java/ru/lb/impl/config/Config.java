package ru.lb.impl.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.lb.design.config.IConfig;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config implements IConfig {
    private final Map<String, String> att = new HashMap<>();
    private final Map<String, List<InetSocketAddress>> groupServer = new ConcurrentHashMap<>();
    private List<ConfigIPServer> ipServer = new ArrayList<>();
    private int sizeBuf;
    private int countBuf;
    private String patternReadHeadHost;
    private Pattern pattern;
    private String firstGroup;

    public Map<String, String> getAtt() {
        return att;
    }

    @Override
    public Map<String, List<InetSocketAddress>> getGroupServer() {
        return groupServer;
    }

    @Override
    public List<InetSocketAddress> getListIPserver(String group) {
        if(groupServer.containsKey(group)){
            return groupServer.get(group);
        } else {
            return List.of();
        }
    }

    @Override
    public List<ConfigIPServer> getIPlb() {
        return ipServer;
    }

    @Override
    public int getCountBuf() {
        return countBuf;
    }

    @Override
    public void setCountBuf(int countBuf) {
        this.countBuf = countBuf;
    }

    @Override
    public int getSizeBuf() {
        return sizeBuf;
    }

    @Override
    public void setSizeBuf(int sizeBuf) {
        this.sizeBuf = sizeBuf;
    }

    @Override
    public void setIPserver(ConfigIPServer value) {
        this.ipServer.add(value);
    }

    @Override
    public String getAtt(String name) {
        return (att.containsKey(name)) ? att.get(name) : "";
    }

    @Override
    public void addAtt(String name, String value) {
        att.put(name, value);
    }

    @Override
    public InetSocketAddress getRandomIPserver(String group) {
        Random random = new Random();
        List<InetSocketAddress> ip = groupServer.get(group);
        return ip.get(random.nextInt(ip.size()));
    }

    @JsonIgnore
    @Override
    public InetSocketAddress getRandomIPserver() {
        if(firstGroup == null)
            setFirstGroup();
        Random random = new Random();
        List<InetSocketAddress> ip = groupServer.get(firstGroup);
        return ip.get(random.nextInt(ip.size()));
    }

    private void setFirstGroup(){
        for(Map.Entry<String, List<InetSocketAddress>> map : groupServer.entrySet()){
            firstGroup = map.getKey();
            break;
        }
    }

    @Override
    public void addIPserver(String group, InetSocketAddress value) {
        if(groupServer.containsKey(group)){
            groupServer.get(group).add(value);
        } else {
            List<InetSocketAddress> serv = new CopyOnWriteArrayList<>();
            serv.add(value);
            groupServer.put(group, serv);
        }
    }

    public void updateList(){
        for(Map.Entry<String, List<InetSocketAddress>> tempGroupServer :  groupServer.entrySet()){
            List<InetSocketAddress> tempList = tempGroupServer.getValue();
            List<InetSocketAddress> cocurent = new CopyOnWriteArrayList<>();
            cocurent.addAll(tempList);
            groupServer.put(tempGroupServer.getKey(), cocurent);
        }
    }

    @Override
    public void setPatternReadHeadHost(String pattern) {
        this.patternReadHeadHost = pattern;
        this.pattern = Pattern.compile(this.patternReadHeadHost);
    }

    public String getPatternReadHeadHost() {
        return patternReadHeadHost;
    }

    @Override
    public Matcher getMatcher(String str) {
        return this.pattern.matcher(str);
    }
}
