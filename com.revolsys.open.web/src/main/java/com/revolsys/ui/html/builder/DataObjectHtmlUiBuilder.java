package com.revolsys.ui.html.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gis.data.query.Or;
import com.revolsys.gis.data.query.Q;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.io.Reader;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.view.TabElementContainer;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.JavaBeanUtil;

public class DataObjectHtmlUiBuilder extends HtmlUiBuilder<Record> {

  private RecordStore dataStore;

  private String tableName;

  public DataObjectHtmlUiBuilder() {
  }

  public DataObjectHtmlUiBuilder(final String typePath, final String title) {
    super(typePath, title);
  }

  public DataObjectHtmlUiBuilder(final String typePath, final String title,
    final String pluralTitle) {
    super(typePath, title, pluralTitle);
  }

  public DataObjectHtmlUiBuilder(final String typePath, final String tableName,
    final String idPropertyName, final String title, final String pluralTitle) {
    super(typePath, title, pluralTitle);
    this.tableName = tableName;
    setIdPropertyName(idPropertyName);
  }

  public Object createDataTableHandler(final HttpServletRequest request,
    final String pageName) {
    final Map<String, Object> parameters = Collections.emptyMap();
    return createDataTableHandler(request, pageName, parameters);
  }

  public Object createDataTableHandler(final HttpServletRequest request,
    final String pageName, final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return createDataTableMap(request, pageName, parameters);
    } else {
      final TabElementContainer tabs = new TabElementContainer();
      addTabDataTable(tabs, this, pageName, parameters);
      return tabs;
    }
  }

  public Object createDataTableHandlerOrRedirect(
    final HttpServletRequest request, final HttpServletResponse response,
    final String pageName, final Object parentBuilder,
    final String parentPageName, final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return createDataTableMap(request, pageName, parameters);
    } else {
      return redirectToTab(parentBuilder, parentPageName, pageName);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> createDataTableMap(
    final HttpServletRequest request, final String pageName,
    final Map<String, Object> parameters) {
    final RecordDefinition metaData = getMetaData();
    Query query = (Query)parameters.get("query");
    if (query == null) {
      final Map<String, Object> filter = (Map<String, Object>)parameters.get("filter");
      query = Query.and(metaData, filter);
    }
    final String fromClause = (String)parameters.get("fromClause");
    query.setFromClause(fromClause);

    return createDataTableMap(request, pageName, query);
  }

  protected Map<String, Object> createDataTableMap(
    final HttpServletRequest request, final String pageName, final Query query) {
    final String search = request.getParameter("sSearch");
    if (StringUtils.hasText(search)) {
      final List<KeySerializer> serializers = getSerializers(pageName, "list");
      final Or or = new Or();
      final int numSortColumns = HttpServletUtils.getIntegerParameter(request,
        "iColumns");
      for (int i = 0; i < numSortColumns; i++) {
        if (HttpServletUtils.getBooleanParameter(request, "bSearchable_" + i)) {
          final KeySerializer serializer = serializers.get(i);
          final String columnName = JavaBeanUtil.getFirstName(serializer.getKey());
          or.add(Q.iLike("T." + columnName, search));
        }
      }
      if (!or.isEmpty()) {
        query.and(or);
      }
    }
    final Map<String, Boolean> orderBy = getDataTableSortOrder(request);
    query.setOrderBy(orderBy);
    final ResultPager<Record> pager = getResultPager(query);
    try {
      return createDataTableMap(request, pager, pageName);
    } finally {
      pager.close();
    }
  }

  public Object createDataTableMap(final String pageName,
    final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return createDataTableMap(request, pageName, parameters);
  }

  @Override
  protected Record createObject() {
    return dataStore.create(tableName);
  }

  public void deleteObject(final Object id) {
    final Record object = loadObject(id);
    if (object != null) {
      dataStore.delete(object);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.destroy();
    dataStore = null;
    tableName = null;
  }

  public RecordStore getDataStore() {
    return dataStore;
  }

  protected RecordDefinition getMetaData() {
    return getDataStore().getRecordDefinition(getTableName());
  }

  public ResultPager<Record> getResultPager(final Query query) {
    return dataStore.page(query);
  }

  public String getTableName() {
    return tableName;
  }

  @Override
  protected void insertObject(final Record object) {
    if (object.getIdValue() == null) {
      object.setIdValue(dataStore.createPrimaryIdValue(tableName));
    }
    dataStore.insert(object);
  }

  protected boolean isPropertyUnique(final Record object,
    final String attributeName) {
    final String value = object.getValue(attributeName);
    final RecordStore dataStore = getDataStore();
    final RecordDefinition metaData = dataStore.getRecordDefinition(tableName);
    if (metaData == null) {
      return true;
    } else {
      final Query query = Query.equal(metaData, attributeName, value);
      final Reader<Record> results = dataStore.query(query);
      final List<Record> objects = results.read();
      if (object.getState() == RecordState.New) {
        return objects.isEmpty();
      } else {
        final Object id = object.getIdValue();
        for (final Iterator<Record> iterator = objects.iterator(); iterator.hasNext();) {
          final Record matchedObject = iterator.next();
          final Object matchedId = matchedObject.getIdValue();
          if (EqualsInstance.INSTANCE.equals(id, matchedId)) {
            iterator.remove();
          }
        }
        return objects.isEmpty();
      }
    }
  }

  @Override
  public Record loadObject(final Object id) {
    return loadObject(tableName, id);
  }

  public Record loadObject(final String typeName, final Object id) {
    final Record object = dataStore.load(typeName, id);
    return object;
  }

  public void setDataStore(final RecordStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  @Override
  protected void updateObject(final Record object) {
    dataStore.update(object);
  }
}
