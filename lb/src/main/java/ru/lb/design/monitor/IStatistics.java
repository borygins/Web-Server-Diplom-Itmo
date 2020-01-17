package ru.lb.design.monitor;

public interface IStatistics extends IMonitoring {

    int getActivConnectionServers();
    int addActivConnectionServers(int count);
    int getAllServers();
}
