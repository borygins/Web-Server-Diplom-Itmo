package impl.server;

import design.config.IConfig;
import design.server.IHistoryQuery;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryQuery implements IHistoryQuery {
    private final Map<InetSocketAddress, Map<String, InetSocketAddress>> mapTable;
    private IConfig config;
    private Object monitor = new Object();

    public HistoryQuery() {
        mapTable = new HashMap<>();
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    @Override
    public  InetSocketAddress find(InetSocketAddress address, String host) {
        InetSocketAddress out = null;
        Map<String, InetSocketAddress> hostMap = null;
        if(mapTable.containsKey(address)){
            hostMap = mapTable.get(address);
            if(hostMap.containsKey(host)){
                out =  hostMap.get(host);
            } else {
                synchronized (monitor) {
                    hostMap = new HashMap<>();
                    out = config.getRandomIPserver(host);
                    hostMap.put(host, out);
                }
            }
        } else {
            synchronized (monitor) {
                hostMap = new HashMap<>();
                out = config.getRandomIPserver(host);
                hostMap.put(host, out);
                mapTable.put(address, hostMap);
            }
        }
        return out;
    }
}
