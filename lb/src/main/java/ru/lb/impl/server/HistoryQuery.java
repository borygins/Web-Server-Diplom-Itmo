package ru.lb.impl.server;

import ru.lb.design.config.IConfig;
import ru.lb.design.server.IHistoryQuery;
import ru.lb.impl.exception.NotHostException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class HistoryQuery implements IHistoryQuery {
    private final Map<String, Map<String, InetSocketAddress>> mapTable;
    private IConfig config;
    private Object monitor = new Object();

    public HistoryQuery() {
        mapTable = new HashMap<>();
    }

    @Override
    public void setConfig(IConfig config) {
        this.config = config;
    }

    /**
     * @return Возвращается хост из заголовков.
     */
    @Override
    public String getHostConnection(ByteBuffer buf) throws NotHostException {
        buf.flip();
        byte[] b = new byte[config.getSizeBuf()];
        buf.get(b, 0, buf.limit());
        Matcher matcher = config.getMatcher(new String(b, 0, buf.limit()));
        if (matcher.find())
        {
            return  matcher.group(1);
        }

        throw new NotHostException("Имя хоста не найдено в заголовках.");
    }

    @Override
    public  InetSocketAddress find(InetSocketAddress address, ByteBuffer buf) throws NotHostException {
        String host = getHostConnection(buf);
        InetSocketAddress out = null;
        Map<String, InetSocketAddress> hostMap = null;
        if(mapTable.containsKey(address.getAddress().getHostAddress())){
            hostMap = mapTable.get(address.getAddress().getHostAddress());
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
                mapTable.put(address.getAddress().getHostAddress(), hostMap);
            }
        }
        return out;
    }
}
