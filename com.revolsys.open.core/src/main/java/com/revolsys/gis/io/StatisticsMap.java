package com.revolsys.gis.io;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.data.record.Record;
import com.revolsys.gis.data.model.RecordDefinition;
import com.revolsys.util.CollectionUtil;

public class StatisticsMap {
  private final Map<String, Statistics> statisticsMap = new TreeMap<String, Statistics>();

  private int providerCount = 0;

  private String prefix;

  private boolean logCounts;

  public StatisticsMap() {
  }

  public StatisticsMap(final String prefix) {
    this.prefix = prefix;
  }

  public void add(final String statisticName, final Record object) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(object);

  }

  public void add(final String statisticName, final Record object,
    final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(object, count);
  }

  public void add(final String statisticName, final RecordDefinition type) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(type);
  }

  public void add(final String statisticName, final RecordDefinition type,
    final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(type, count);
  }

  public synchronized void add(final String name, final Statistics statistics) {
    statisticsMap.put(name, statistics);
    statistics.connect();
  }

  public void add(final String statisticName, final String name) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(name);
  }

  public void add(final String statisticName, final String path,
    final long count) {
    final Statistics statistics = getStatistics(statisticName);
    statistics.add(path, count);
  }

  public synchronized void addCountsText(final StringBuffer sb) {
    for (final Statistics stats : statisticsMap.values()) {
      stats.addCountsText(sb);
    }
  }

  public void clear() {
    statisticsMap.clear();
  }

  @PostConstruct
  public synchronized void connect() {
    providerCount++;
  }

  @PreDestroy
  public synchronized void disconnect() {
    providerCount--;
    if (providerCount <= 0) {
      for (final Statistics statistics : statisticsMap.values()) {
        statistics.disconnect();
      }
    }
  }

  public synchronized String getCountsText() {
    final StringBuffer sb = new StringBuffer();
    addCountsText(sb);
    return sb.toString();
  }

  public String getPrefix() {
    return prefix;
  }

  public synchronized Statistics getStatistics(final String statisticName) {
    if (statisticName == null) {
      return null;
    } else {
      final String name = CollectionUtil.toString(" ", prefix, statisticName);
      Statistics statistics = statisticsMap.get(name);
      if (statistics == null) {
        statistics = new Statistics(name);
        statistics.setLogCounts(logCounts);
        statisticsMap.put(name, statistics);
      }
      return statistics;
    }
  }

  public synchronized Set<String> getStatisticsNames() {
    return statisticsMap.keySet();
  }

  public synchronized void setLogCounts(final boolean logCounts) {
    this.logCounts = logCounts;
    for (final Statistics statistics : statisticsMap.values()) {
      statistics.setLogCounts(logCounts);
    }
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }
}
