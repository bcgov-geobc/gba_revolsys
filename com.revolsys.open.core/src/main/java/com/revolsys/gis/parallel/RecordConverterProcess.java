package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.filter.Filter;
import com.revolsys.gis.converter.FilterRecordConverter;
import com.revolsys.gis.converter.SimpleRecordConveter;
import com.revolsys.gis.converter.process.CopyValues;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class RecordConverterProcess extends BaseInOutProcess<Record, Record> {
  private static final Logger LOG = LoggerFactory.getLogger(RecordConverterProcess.class);

  private Converter<Record, Record> defaultConverter;

  private Map<String, Collection<FilterRecordConverter>> typeFilterConverterMap = new LinkedHashMap<String, Collection<FilterRecordConverter>>();

  private Map<String, Converter<Record, Record>> typeConverterMap = new HashMap<String, Converter<Record, Record>>();

  private RecordDefinitionFactory targetMetaDataFactory;

  private Map<Object, Map<String, Object>> simpleMapping;

  private Statistics statistics = new Statistics("Converted");

  //
  // private Statistics ignoredStatistics = new Statistics("Ignored");

  public void addTypeConverter(final String typePath, final Converter<Record, Record> converter) {
    this.typeConverterMap.put(typePath, converter);
  }

  public void addTypeFilterConverter(final String typePath,
    final FilterRecordConverter filterConverter) {

    Collection<FilterRecordConverter> converters = this.typeFilterConverterMap.get(typePath);
    if (converters == null) {
      converters = new ArrayList<FilterRecordConverter>();
      this.typeFilterConverterMap.put(typePath, converters);
    }
    converters.add(filterConverter);
  }

  protected Record convert(final Record source) {
    int matchCount = 0;
    final RecordDefinition sourceMetaData = source.getRecordDefinition();
    final String sourceTypeName = sourceMetaData.getPath();
    final Collection<FilterRecordConverter> converters = this.typeFilterConverterMap.get(sourceTypeName);
    Record target = null;
    if (converters != null && !converters.isEmpty()) {
      for (final FilterRecordConverter filterConverter : converters) {
        final Filter<Record> filter = filterConverter.getFilter();
        if (filter.accept(source)) {
          final Converter<Record, Record> converter = filterConverter.getConverter();
          target = converter.convert(source);
          matchCount++;
        }
      }
      if (matchCount == 1) {
        return target;
      }
    }
    if (matchCount == 0) {
      final Converter<Record, Record> typeConveter = this.typeConverterMap.get(sourceTypeName);
      if (typeConveter != null) {
        target = typeConveter.convert(source);
        return target;
      } else if (this.defaultConverter == null) {
        return convertObjectWithNoConverter(source);
      } else {
        return this.defaultConverter.convert(source);

      }
    } else {
      final StringBuffer sb = new StringBuffer("Multiple conveters found: \n  ");
      for (final FilterRecordConverter filterConverter : converters) {
        final Filter<Record> filter = filterConverter.getFilter();
        if (filter.accept(source)) {
          sb.append(filter.toString());
          sb.append("\n  ");
        }
      }
      sb.append(source);
      LOG.error(sb.toString());
      return null;
    }
  }

  protected Record convertObjectWithNoConverter(final Record source) {
    LOG.error("No converter found for: " + source);
    return null;
  }

  public Converter<Record, Record> getDefaultConverter() {
    return this.defaultConverter;
  }

  public Map<String, Collection<FilterRecordConverter>> getFilterTypeConverterMap() {
    return this.typeFilterConverterMap;
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  public RecordDefinition getTargetMetaData(final String typePath) {
    return this.targetMetaDataFactory.getRecordDefinition(typePath);
  }

  public RecordDefinitionFactory getTargetMetaDataFactory() {
    return this.targetMetaDataFactory;
  }

  public Map<String, Converter<Record, Record>> getTypeConverterMap() {
    return this.typeConverterMap;
  }

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    super.postRun(in, out);
    this.statistics.disconnect();
    // ignoredStatistics.disconnect();
  }

  @Override
  protected void preRun(final Channel<Record> in, final Channel<Record> out) {
    this.statistics.connect();
    // ignoredStatistics.connect();
    if (this.simpleMapping != null) {
      for (final Entry<Object, Map<String, Object>> entry : this.simpleMapping.entrySet()) {
        final Object key = entry.getKey();
        String sourceTypeName;
        if (key instanceof String) {
          sourceTypeName = (String)key;
        } else {
          sourceTypeName = String.valueOf(key.toString());
        }
        final Map<String, Object> map = entry.getValue();
        final Object targetName = map.get("typePath");
        String targetTypeName;
        if (key instanceof String) {
          targetTypeName = (String)targetName;
        } else {
          targetTypeName = String.valueOf(targetName.toString());
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> attributeMapping = (Map<String, String>)map.get("attributeMapping");

        final RecordDefinition targetMetaData = getTargetMetaData(targetTypeName);
        final SimpleRecordConveter converter = new SimpleRecordConveter(targetMetaData);
        converter.addProcessor(new CopyValues(attributeMapping));
        addTypeConverter(sourceTypeName, converter);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record source) {
    final Record target = convert(source);
    if (target == null) {
      // ignoredStatistics.add(source);
    } else {
      out.write(target);
      if (source != target) {
        this.statistics.add(target);
      }
    }
  }

  public void setDefaultConverter(final Converter<Record, Record> defaultConverter) {
    this.defaultConverter = defaultConverter;
  }

  public void setSimpleMapping(final Map<Object, Map<String, Object>> simpleMapping) {
    this.simpleMapping = simpleMapping;
  }

  public void setStatistics(final Statistics statistics) {
    if (this.statistics != statistics) {
      this.statistics = statistics;
      statistics.connect();
    }
  }

  public void setTargetMetaDataFactory(final RecordDefinitionFactory targetMetaDataFactory) {
    this.targetMetaDataFactory = targetMetaDataFactory;
  }

  public void setTypeConverterMap(final Map<String, Converter<Record, Record>> typeConverterMap) {
    this.typeConverterMap = typeConverterMap;
  }

  public void setTypeFilterConverterMap(
    final Map<String, Collection<FilterRecordConverter>> typeConverterMap) {
    this.typeFilterConverterMap = typeConverterMap;
  }
}
