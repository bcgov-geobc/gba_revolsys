package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Factory;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.algorithm.index.PointDataObjectMap;
import com.revolsys.gis.algorithm.linematch.LineMatchGraph;
import com.revolsys.gis.data.model.RecordLog;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.filter.DataObjectGeometryFilter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.gis.jts.filter.LineIntersectsFilter;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.parallel.channel.Channel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class CompareProcessor extends AbstractMergeProcess {

  private final boolean cleanDuplicatePoints = true;

  private Statistics duplicateOtherStatistics = new Statistics(
    "Duplicate Other");

  private Statistics duplicateSourceStatistics = new Statistics(
    "Duplicate Source");

  private Factory<Filter<Record>, Record> equalFilterFactory;

  private Statistics equalStatistics = new Statistics("Equal");

  private Filter<Record> excludeFilter;

  private Statistics excludeNotEqualOtherStatistics = new Statistics(
    "Exclude Not Equal Other");

  private Statistics excludeNotEqualSourceStatistics = new Statistics(
    "Exclude Not Equal Source");

  private String label;

  private Statistics notEqualOtherStatistics = new Statistics("Not Equal Other");

  private Statistics notEqualSourceStatistics = new Statistics(
    "Not Equal Source");

  private DataObjectQuadTree otherIndex = new DataObjectQuadTree();

  private PointDataObjectMap otherPointMap = new PointDataObjectMap();

  private Set<Record> sourceObjects = new LinkedHashSet<Record>();

  private final PointDataObjectMap sourcePointMap = new PointDataObjectMap();

  private boolean logNotEqualSource = true;

  @Override
  protected void addOtherObject(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      boolean add = true;
      if (cleanDuplicatePoints) {
        final List<Record> objects = otherPointMap.getObjects(object);
        if (!objects.isEmpty()) {
          final Filter<Record> filter = equalFilterFactory.create(object);
          add = !FilterUtil.matches(objects, filter);
        }
        if (add) {
          otherPointMap.add(object);
        } else {
          duplicateOtherStatistics.add(object);
        }
      }
    } else if (geometry instanceof LineString) {
      otherIndex.insert(object);
    }
  }

  @Override
  protected void addSourceObject(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      boolean add = true;
      if (cleanDuplicatePoints) {
        final List<Record> objects = sourcePointMap.getObjects(object);
        if (!objects.isEmpty()) {
          final Filter<Record> filter = equalFilterFactory.create(object);
          add = !FilterUtil.matches(objects, filter);
        }
      }
      if (add) {
        sourcePointMap.add(object);
      } else {
        duplicateSourceStatistics.add(object);
      }
    } else if (geometry instanceof LineString) {
      sourceObjects.add(object);
    }
  }

  public Statistics getDuplicateOtherStatistics() {
    return duplicateOtherStatistics;
  }

  public Statistics getDuplicateSourceStatistics() {
    return duplicateSourceStatistics;
  }

  public Factory<Filter<Record>, Record> getEqualFilterFactory() {
    return equalFilterFactory;
  }

  public Statistics getEqualStatistics() {
    return equalStatistics;
  }

  public Filter<Record> getExcludeFilter() {
    return excludeFilter;
  }

  public Statistics getExcludeNotEqualOtherStatistics() {
    return excludeNotEqualOtherStatistics;
  }

  public Statistics getExcludeNotEqualSourceStatistics() {
    return excludeNotEqualSourceStatistics;
  }

  public String getLabel() {
    return label;
  }

  public Statistics getNotEqualOtherStatistics() {
    return notEqualOtherStatistics;
  }

  public Statistics getNotEqualSourceStatistics() {
    return notEqualSourceStatistics;
  }

  public boolean isLogNotEqualSource() {
    return logNotEqualSource;
  }

  private void logError(final Record object, final String message,
    final boolean source) {
    if (excludeFilter == null || !excludeFilter.accept(object)) {
      if (source) {
        notEqualSourceStatistics.add(object);
      } else {
        notEqualOtherStatistics.add(object);
      }
      RecordLog.error(getClass(), message, object);
    } else {
      if (source) {
        excludeNotEqualSourceStatistics.add(object);
      } else {
        excludeNotEqualOtherStatistics.add(object);
      }
    }
  }

  private void processExactLineMatch(final Record sourceObject) {
    final LineString sourceLine = sourceObject.getGeometryValue();
    final LineEqualIgnoreDirectionFilter lineEqualFilter = new LineEqualIgnoreDirectionFilter(
      sourceLine, 3);
    final Filter<Record> geometryFilter = new DataObjectGeometryFilter<LineString>(
      lineEqualFilter);
    final Filter<Record> equalFilter = equalFilterFactory.create(sourceObject);
    final Filter<Record> filter = new AndFilter<Record>(equalFilter,
      geometryFilter);

    final Record otherObject = otherIndex.queryFirst(sourceObject, filter);
    if (otherObject != null) {
      equalStatistics.add(sourceObject);
      removeObject(sourceObject);
      removeOtherObject(otherObject);
    }
  }

  private void processExactLineMatches() {
    for (final Record object : new ArrayList<Record>(sourceObjects)) {
      processExactLineMatch(object);
    }
  }

  private void processExactPointMatch(final Record sourceObject) {
    final Filter<Record> equalFilter = equalFilterFactory.create(sourceObject);
    final Record otherObject = otherPointMap.getFirstMatch(sourceObject,
      equalFilter);
    if (otherObject != null) {
      final Point sourcePoint = sourceObject.getGeometryValue();
      final double sourceZ = CoordinatesListUtil.get(sourcePoint).getZ(0);

      final Point otherPoint = otherObject.getGeometryValue();
      final double otherZ = CoordinatesListUtil.get(otherPoint).getZ(0);

      if (sourceZ == otherZ || Double.isNaN(sourceZ) && Double.isNaN(otherZ)) {
        equalStatistics.add(sourceObject);
        removeObject(sourceObject);
        removeOtherObject(otherObject);
      }
    }
  }

  private void processExactPointMatches() {
    for (final Record object : new ArrayList<Record>(
      sourcePointMap.getAll())) {
      processExactPointMatch(object);
    }
  }

  @Override
  protected void processObjects(final RecordDefinition metaData,
    final Channel<Record> out) {
    if (otherIndex.size() + otherPointMap.size() == 0) {
      if (logNotEqualSource) {
        for (final Record object : sourceObjects) {
          logError(object, "Source missing in Other", true);
        }
      }
    } else {
      processExactPointMatches();
      processExactLineMatches();
      processPartialMatches();
    }
    for (final Record object : otherIndex.queryAll()) {
      logError(object, "Other missing in Source", false);
    }
    for (final Record object : otherPointMap.getAll()) {
      logError(object, "Other missing in Source", false);
    }
    if (logNotEqualSource) {
      for (final Record object : sourceObjects) {
        logError(object, "Source missing in Other", true);
      }
    }
    sourceObjects.clear();
    otherIndex = new DataObjectQuadTree();
    otherPointMap.clear();
  }

  private void processPartialMatch(final Record sourceObject) {
    final Geometry sourceGeometry = sourceObject.getGeometryValue();
    if (sourceGeometry instanceof LineString) {
      final LineString sourceLine = (LineString)sourceGeometry;

      final LineIntersectsFilter intersectsFilter = new LineIntersectsFilter(
        sourceLine);
      final Filter<Record> geometryFilter = new DataObjectGeometryFilter<LineString>(
        intersectsFilter);
      final Filter<Record> equalFilter = equalFilterFactory.create(sourceObject);
      final Filter<Record> filter = new AndFilter<Record>(equalFilter,
        geometryFilter);
      final List<Record> otherObjects = otherIndex.queryList(
        sourceGeometry, filter);
      if (!otherObjects.isEmpty()) {
        final LineMatchGraph<Record> graph = new LineMatchGraph<Record>(
          sourceObject, sourceLine);
        for (final Record otherObject : otherObjects) {
          final LineString otherLine = otherObject.getGeometryValue();
          graph.add(otherLine);
        }
        final MultiLineString nonMatchedLines = graph.getNonMatchedLines(0);
        if (nonMatchedLines.isEmpty()) {
          removeObject(sourceObject);

        } else {
          removeObject(sourceObject);
          if (nonMatchedLines.getNumGeometries() == 1
            && nonMatchedLines.getGeometryN(0).getLength() == 1) {
          } else {
            for (int j = 0; j < nonMatchedLines.getNumGeometries(); j++) {
              final Geometry newGeometry = nonMatchedLines.getGeometryN(j);
              final Record newObject = DataObjectUtil.copy(sourceObject,
                newGeometry);
              addSourceObject(newObject);
            }
          }
        }
        for (int i = 0; i < otherObjects.size(); i++) {
          final Record otherObject = otherObjects.get(i);
          final MultiLineString otherNonMatched = graph.getNonMatchedLines(
            i + 1, 0);
          for (int j = 0; j < otherNonMatched.getNumGeometries(); j++) {
            final Geometry newGeometry = otherNonMatched.getGeometryN(j);
            final Record newOtherObject = DataObjectUtil.copy(otherObject,
              newGeometry);
            addOtherObject(newOtherObject);
          }
          removeOtherObject(otherObject);
        }
      }
    }
  }

  private void processPartialMatches() {
    for (final Record object : new ArrayList<Record>(sourceObjects)) {
      processPartialMatch(object);
    }
  }

  private void removeObject(final Record object) {
    sourceObjects.remove(object);
  }

  private void removeOtherObject(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      otherPointMap.remove(object);
    } else {
      otherIndex.remove(object);
    }
  }

  public void setEqualFilterFactory(
    final Factory<Filter<Record>, Record> equalFilterFactory) {
    this.equalFilterFactory = equalFilterFactory;
  }

  public void setExcludeFilter(final Filter<Record> excludeFilter) {
    this.excludeFilter = excludeFilter;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setLogNotEqualSource(final boolean logNotEqualSource) {
    this.logNotEqualSource = logNotEqualSource;
  }

  @Override
  protected void setUp() {
    equalStatistics.connect();
    notEqualSourceStatistics.connect();
    notEqualOtherStatistics.connect();
    duplicateSourceStatistics.connect();
    duplicateOtherStatistics.connect();
    excludeNotEqualSourceStatistics.connect();
    excludeNotEqualOtherStatistics.connect();
  }

  @Override
  protected void tearDown() {
    sourceObjects = null;
    sourcePointMap.clear();
    otherPointMap = null;
    otherIndex = null;
    equalStatistics.disconnect();
    notEqualSourceStatistics.disconnect();
    notEqualOtherStatistics.disconnect();
    duplicateSourceStatistics.disconnect();
    duplicateOtherStatistics.disconnect();
    excludeNotEqualSourceStatistics.disconnect();
    excludeNotEqualOtherStatistics.disconnect();
    equalStatistics = null;
    notEqualSourceStatistics = null;
    notEqualOtherStatistics = null;
    duplicateSourceStatistics = null;
    duplicateOtherStatistics = null;
    excludeNotEqualSourceStatistics = null;
    excludeNotEqualOtherStatistics = null;
  }
}
