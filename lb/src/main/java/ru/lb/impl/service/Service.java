package ru.lb.impl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lb.design.config.IConfig;
import ru.lb.design.service.IService;
import ru.lb.impl.server.Server;

import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Service implements IService {
    private static final Logger LOG = LoggerFactory.getLogger(Service.class);
    private IConfig config;
    private Map<String, List<InetSocketAddress>> tempIP = new HashMap<>();

    public Service() {

    }

    public Map<String, List<InetSocketAddress>> getTempIP() {
        return tempIP;
    }

    public Service(IConfig config) {
        this.config = config;
    }

    @Override
    public void run() {

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        while (!Thread.currentThread().isInterrupted()){
            try{

                ipTemp(client);
            ipConfig(client);
            Thread.sleep(5000);

            }catch (InterruptedException e){
                if(LOG.isErrorEnabled())
                    LOG.error("", e);
                Thread.currentThread().interrupt();
            }
        }

    }

    private void ipTemp(HttpClient client) throws InterruptedException{
        for(Map.Entry<String, List<InetSocketAddress>> tempH : tempIP.entrySet()){
            for(InetSocketAddress address : tempH.getValue()){
                StringBuilder sb= new StringBuilder("http://").append(address.getHostName()).append(":").append(address.getPort());
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(sb.toString()))
                        .build();

                HttpResponse<String> response = null;
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if(response.statusCode() == 200){
                        addGroupHost(tempH.getKey(), address);
                    }
                } catch (IOException e) {
                    if(LOG.isErrorEnabled())
                        LOG.error("", e);
                }
            }
        }
    }

    private void ipConfig(HttpClient client) throws InterruptedException {
        for(Map.Entry<String, List<InetSocketAddress>> tempH : config.getGroupServer().entrySet()){
            for(InetSocketAddress address : tempH.getValue()){
                StringBuilder sb= new StringBuilder("http://").append(address.getHostName()).append(":").append(address.getPort()).append("/");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(sb.toString()))
                        .build();
                HttpResponse<String> response = null;
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if(response.statusCode() != 200){
                        addTempHost(tempH.getKey(), address);
                    }
                } catch (IOException e) {
                    if(LOG.isErrorEnabled())
                        LOG.error("", e);
                    addTempHost(tempH.getKey(), address);
                }
            }
        }
    }

    private void addTempHost(String name, InetSocketAddress address){
        if(tempIP.containsKey(name)){
            tempIP.get(name).add(address);
        } else {
            List<InetSocketAddress> listIp = new CopyOnWriteArrayList<>();
            listIp.add(address);
            tempIP.put(name, listIp);
        }

        config.getGroupServer().get(name).remove(address);
    }

    private void addGroupHost(String name, InetSocketAddress address){
        Map<String, List<InetSocketAddress>> group = config.getGroupServer();
        if(group.containsKey(name)){
            group.get(name).add(address);
        } else {
            List<InetSocketAddress> listIp = new ArrayList<>();
            listIp.add(address);
            group.put(name, listIp);
        }

        tempIP.get(name).remove(address);

    }
}
