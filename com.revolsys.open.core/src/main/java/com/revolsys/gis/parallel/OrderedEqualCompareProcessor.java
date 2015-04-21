package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.revolsys.data.record.Record;
import com.revolsys.gis.data.model.RecordLog;
import com.revolsys.gis.data.model.RecordDefinition;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.MultiInputSelector;
import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.parallel.process.AbstractInProcess;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.PrecisionModel;

public class OrderedEqualCompareProcessor extends AbstractInProcess<Record> {

  private Channel<Record> otherIn;

  private int otherInBufferSize = 0;

  private String attributeName;

  private boolean running;

  private String sourceName = "Source";

  private String otherName = "Other";

  private List<String> equalExclude = new ArrayList<String>();

  private final PrecisionModel precisionModel = new PrecisionModel(1000);

  private boolean equals(final Geometry geometry1, final Geometry geometry2) {
    if (geometry1 == null) {
      return geometry2 == null;
    } else if (geometry2 == null) {
      return false;
    } else if (geometry1.getClass() == geometry2.getClass()) {
      if (geometry1 instanceof GeometryCollection) {
        if (geometry1.getNumGeometries() == geometry2.getNumGeometries()) {
          for (int i = 0; i < geometry1.getNumGeometries(); i++) {
            final Geometry subGeometry1 = geometry1.getGeometryN(i);
            final Geometry subGeometry2 = geometry2.getGeometryN(i);
            if (!equals(subGeometry1, subGeometry2)) {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }
      } else {
        final List<CoordinatesList> parts1 = CoordinatesListUtil.getAll(geometry1);
        final List<CoordinatesList> parts2 = CoordinatesListUtil.getAll(geometry2);
        if (parts1.size() == parts2.size()) {
          for (int i = 0; i < parts1.size(); i++) {
            final CoordinatesList points1 = parts1.get(i);
            final CoordinatesList points2 = parts2.get(i);
            if (points1.size() == points2.size()
              && points1.getNumAxis() == points2.getNumAxis()) {
              for (int j = 0; j < points1.size(); j++) {
                for (int k = 0; k < points1.getNumAxis(); k++) {
                  double value1 = points1.getValue(j, k);
                  double value2 = points2.getValue(j, k);
                  value1 = precisionModel.makePrecise(value1);
                  value2 = precisionModel.makePrecise(value2);
                  if (Double.compare(value1, value2) != 0) {
                    return false;
                  }
                }
              }
            } else {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }

      }
    } else {
      return false;
    }
  }

  protected boolean geometryEquals(final Record object1,
    final Record object2) {
    final Geometry geometry1 = object1.getGeometryValue();
    final Geometry geometry2 = object2.getGeometryValue();

    return equals(geometry1, geometry2);
  }

  public String getAttributeName() {
    return attributeName;
  }

  public List<String> getEqualExclude() {
    return equalExclude;
  }

  protected Set<String> getNotEqualAttributeNames(final Record object1,
    final Record object2) {
    final RecordDefinition metaData = object1.getMetaData();
    final Set<String> notEqualAttributeNames = new LinkedHashSet<String>();
    final String geometryAttributeName = metaData.getGeometryAttributeName();
    for (final String attributeName : metaData.getAttributeNames()) {
      if (!equalExclude.contains(attributeName)
        && !attributeName.equals(geometryAttributeName)) {
        final Object value1 = object1.getValue(attributeName);
        final Object value2 = object2.getValue(attributeName);
        if (!valueEquals(value1, value2)) {
          notEqualAttributeNames.add(attributeName);
        }
      }
    }
    return notEqualAttributeNames;
  }

  /**
   * @return the in
   */
  public Channel<Record> getOtherIn() {
    if (otherIn == null) {
      if (otherInBufferSize < 1) {
        setOtherIn(new Channel<Record>());
      } else {
        final Buffer<Record> buffer = new Buffer<Record>(
          otherInBufferSize);
        setOtherIn(new Channel<Record>(buffer));
      }
    }
    return otherIn;
  }

  public int getOtherInBufferSize() {
    return otherInBufferSize;
  }

  public String getOtherName() {
    return otherName;
  }

  public String getSourceName() {
    return sourceName;
  }

  protected void logNoMatch(final Record object, final boolean other) {
    if (other) {
      RecordLog.error(getClass(), otherName + " has no match in "
        + sourceName, object);
    } else {
      RecordLog.error(getClass(), sourceName + " has no match in "
        + otherName, object);
    }
  }

  private void logNoMatch(final Record[] objects,
    final Channel<Record> channel, final boolean other) {
    if (objects[0] != null) {
      logNoMatch(objects[0], false);
    }
    if (objects[1] != null) {
      logNoMatch(objects[1], true);
    }
    while (running) {
      final Record object = readObject(channel);
      logNoMatch(object, other);
    }
  }

  protected void logNotEqual(final Record sourceObject,
    final Record otherObject, final Set<String> notEqualAttributeNames,
    final boolean geometryEquals) {
    final String attributeNames = CollectionUtil.toString(",",
      notEqualAttributeNames);
    RecordLog.error(getClass(), sourceName + " " + attributeNames,
      sourceObject);
    RecordLog.error(getClass(), otherName + " " + attributeNames,
      otherObject);
  }

  protected Record readObject(final Channel<Record> channel) {
    return channel.read();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  protected void run(final Channel<Record> in) {
    running = true;
    final Channel<Record>[] channels = new Channel[] {
      in, otherIn
    };
    Record previousEqualObject = null;

    final Record[] objects = new Record[2];
    final boolean[] guard = new boolean[] {
      true, true
    };
    final MultiInputSelector alt = new MultiInputSelector();
    while (running) {
      final int index = alt.select(channels, guard);
      if (index == -1) {
        if (in.isClosed()) {
          logNoMatch(objects, otherIn, true);
          return;
        } else if (otherIn.isClosed()) {
          logNoMatch(objects, in, false);
          return;
        } else {
        }
      } else {
        final Channel<Record> channel = channels[index];
        final Record readObject = readObject(channel);
        if (readObject != null) {
          if (previousEqualObject != null
            && EqualsInstance.INSTANCE.equals(previousEqualObject, readObject)) {
            if (index == 0) {
              RecordLog.error(getClass(), "Duplicate in " + sourceName,
                readObject);
            } else {
              RecordLog.error(getClass(), "Duplicate in " + otherName,
                readObject);
            }
          } else {
            Record sourceObject;
            Record otherObject;
            final int oppositeIndex = (index + 1) % 2;
            if (index == 0) {
              sourceObject = readObject;
              otherObject = objects[oppositeIndex];
            } else {
              sourceObject = objects[oppositeIndex];
              otherObject = readObject;
            }
            final Object value = readObject.getValue(attributeName);
            if (value == null) {
              RecordLog.error(getClass(), "Missing key value for "
                + attributeName, readObject);
            } else if (objects[oppositeIndex] == null) {
              objects[index] = readObject;
              guard[index] = false;
              guard[oppositeIndex] = true;
            } else {
              final Object sourceValue = sourceObject.getValue(attributeName);
              final Comparable<Object> sourceComparator;
              if (sourceValue instanceof Number) {
                final Number number = (Number)sourceValue;
                final Double doubleValue = number.doubleValue();
                sourceComparator = (Comparable)doubleValue;
              } else {
                sourceComparator = (Comparable<Object>)sourceValue;
              }
              Object otherValue = otherObject.getValue(attributeName);
              if (otherValue instanceof Number) {
                final Number number = (Number)otherValue;
                otherValue = number.doubleValue();
              }
              // TODO duplicates
              final int compare = sourceComparator.compareTo(otherValue);
              if (compare == 0) {
                final Set<String> notEqualAttributeNames = getNotEqualAttributeNames(
                  sourceObject, otherObject);

                final boolean geometryEquals = geometryEquals(sourceObject,
                  otherObject);
                if (!geometryEquals) {
                  final String geometryAttributeName = sourceObject.getMetaData()
                    .getGeometryAttributeName();
                  notEqualAttributeNames.add(geometryAttributeName);
                }
                if (!notEqualAttributeNames.isEmpty()) {
                  logNotEqual(sourceObject, otherObject,
                    notEqualAttributeNames, geometryEquals);
                }
                objects[0] = null;
                objects[1] = null;
                guard[0] = true;
                guard[1] = true;
                previousEqualObject = sourceObject;
              } else if (compare < 0) { // other object is bigger, keep other
                                        // object
                logNoMatch(sourceObject, false);
                objects[0] = null;
                objects[1] = otherObject;
                guard[0] = true;
                guard[1] = false;

              } else { // source is bigger, keep source object
                logNoMatch(otherObject, true);
                objects[0] = sourceObject;
                objects[1] = null;
                guard[0] = false;
                guard[1] = true;
              }
            }
          }
        }
      }
    }
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  public void setEqualExclude(final List<String> equalExclude) {
    this.equalExclude = equalExclude;
  }

  /**
   * @param in the in to set
   */
  public void setOtherIn(final Channel<Record> in) {
    this.otherIn = in;
    in.readConnect();
  }

  public void setOtherInBufferSize(final int otherInBufferSize) {
    this.otherInBufferSize = otherInBufferSize;
  }

  public void setOtherName(final String otherName) {
    this.otherName = otherName;
  }

  public void setSourceName(final String sourceName) {
    this.sourceName = sourceName;
  }

  protected boolean valueEquals(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return true;
      } else if (value2 instanceof String) {
        final String string2 = (String)value2;
        return !StringUtils.hasText(string2);
      }
    } else if (value2 == null) {
      if (value1 instanceof String) {
        final String string1 = (String)value1;
        return !StringUtils.hasText(string1);
      } else {
        return false;
      }
    } else if (value1 instanceof String && value2 instanceof String) {
      if (!StringUtils.hasText((String)value1)
        && !StringUtils.hasText((String)value2)) {
        return true;
      }
    }
    return EqualsInstance.INSTANCE.equals(value1, value2);
  }
}
