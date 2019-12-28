package impl.config;

import design.config.IConfig;

import java.net.InetSocketAddress;
import java.util.*;

public class Config implements IConfig {
    private final Map<String, String> att = new HashMap<>();
    private final Map<String, List<InetSocketAddress>> groupServer = new HashMap<>();
    private InetSocketAddress IP_SERVER;

    @Override
    public InetSocketAddress getIPserver() {
        return IP_SERVER;
    }

    @Override
    public void setIPserver(InetSocketAddress value) {
        this.IP_SERVER = value;
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

    @Override
    public void addIPserver(String group, InetSocketAddress value) {
        if(groupServer.containsKey(group)){
            groupServer.get(group).add(value);
        } else {
            List<InetSocketAddress> serv = new ArrayList<>();
            serv.add(value);
            groupServer.put(group, serv);
        }
    }
}
