package com.revolsys.gis.esri.gdb.file.capi;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.format.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.format.esri.gdb.xml.model.Domain;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;

public class FileGdbDomainCodeTable implements CodeTable {
  private final CodedValueDomain domain;

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDomainCodeTable.class);

  private final String name;

  private final CapiFileGdbRecordStore dataStore;

  private JComponent swingEditor;

  public FileGdbDomainCodeTable(final CapiFileGdbRecordStore dataStore,
    final CodedValueDomain domain) {
    this.dataStore = dataStore;
    this.domain = domain;
    this.name = domain.getDomainName();
  }

  @Override
  public FileGdbDomainCodeTable clone() {
    try {
      return (FileGdbDomainCodeTable)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  private Object createValue(final String name) {
    synchronized (dataStore) {
      final Object id = domain.addCodedValue(name);
      dataStore.alterDomain(domain);
      LOG.info(domain.getDomainName() + " created code " + id + "=" + name);
      return id;
    }
  }

  @Override
  public List<String> getAttributeAliases() {
    return domain.getAttributeAliases();
  }

  @Override
  public Map<Object, List<Object>> getCodes() {
    return domain.getCodes();
  }

  public Domain getDomain() {
    return domain;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getId(final Map<String, ? extends Object> values) {
    final Object id = domain.getId(values);
    if (id == null) {
      return (T)createValue(domain.getName(values));
    }
    return (T)id;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getId(final Object... values) {
    final Object id = domain.getId(values);
    if (id == null) {
      return (T)createValue((String)values[0]);
    }
    return (T)id;
  }

  @Override
  public String getIdAttributeName() {
    return domain.getIdAttributeName();
  }

  @Override
  public Map<String, ? extends Object> getMap(final Object id) {
    return domain.getMap(id);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public JComponent getSwingEditor() {
    return swingEditor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Object id) {
    return (V)domain.getValue(id);
  }

  @Override
  public List<String> getValueAttributeNames() {
    return domain.getValueAttributeNames();
  }

  @Override
  public List<Object> getValues(final Object id) {
    return domain.getValues(id);
  }

  @Override
  public void refresh() {
  }

  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  @Override
  public String toString() {
    return domain.toString();
  }
}
