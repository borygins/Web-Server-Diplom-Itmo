package ru.lb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.lb.design.config.IConfig;
import ru.lb.design.server.AServer;
import ru.lb.design.server.IServer;
import ru.lb.impl.config.Config;
import ru.lb.impl.config.ConfigIPServer;
import ru.lb.impl.server.HistoryQuery;
import ru.lb.impl.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lb.impl.service.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static Path path = Paths.get("./config.json");

    private static IConfig config;

    public static void main(String[] args) {

        if (args.length > 0) {
            for (String arg : args)
                switch (arg) {
                    case "createConfigFile":
                        config = new Config();
                        config.addIPserver("fasie.ru", new InetSocketAddress("185.9.147.48", 80));
                        config.addIPserver("fasie.ru", new InetSocketAddress("185.9.147.48", 80));
                        config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 443), true,0));
                        config.setIPserver(new ConfigIPServer(new InetSocketAddress("localhost", 80), false,0));
                        config.setCountBuf(512);
                        config.setSizeBuf(1024);
                        config.setPatternReadHeadHost("\\r\\nHost: (.+)(:|\\r\\n)");

                        try {
                            Files.createFile(path);
                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                            objectMapper.writeValue(path.toFile(), config);

                        } catch (IOException e) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Ошибка создания файла конфигурации.", e);
                            }
                            e.printStackTrace();
                        }
                        System.exit(0);
                        break;
                    case "configPath":
                        path = Paths.get(arg);
                        break;
                }
        } else {

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                config = objectMapper.readValue(path.toFile(), Config.class);
                Thread lbServer = null;
                for(ConfigIPServer ipServer : config.getIPlb()) {
                    lbServer = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            IServer server = AServer.serverFabric(config, ipServer, true);
                            server.setHistoryQuery(new HistoryQuery());
                            server.start();
                        }
                    });
                    lbServer.start();
                    for (int i = 0; i < ipServer.getCountSelector(); i++) {
                        lbServer = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                IServer server = AServer.serverFabric(config, ipServer, false);
                                server.setHistoryQuery(new HistoryQuery());
                                server.start();
                            }
                        });
                        lbServer.start();
                    }

                }


                new Thread(new Service(config)).start();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
