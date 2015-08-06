package com.revolsys.format.moep;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.format.saif.SaifConstants;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.grid.Bcgs20000RectangularMapGrid;
import com.revolsys.gis.grid.UtmRectangularMapGrid;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class MoepBinaryIterator extends AbstractObjectWithProperties implements Iterator<Record> {
  private static final int COMPLEX_LINE = 3;

  private static final int CONSTRUCTION_COMPLEX_LINE = 5;

  private static final int CONSTRUCTION_LINE = 4;

  private static final int POINT = 1;

  private static final int SIMPLE_LINE = 2;

  private static final int TEXT = 6;

  private static double getAngle(final double angle) {
    double orientation = (90 - angle) % 360;
    if (orientation < 0) {
      orientation = 360 + orientation;
    }
    return orientation;
  }

  public static void setGeometryProperties(final Record object) {
    final Geometry geometry = object.getGeometry();
    final Number angle = object.getValue(MoepConstants.ANGLE);
    if (angle != null) {
      final double orientation = getAngle(angle.doubleValue());
      JtsGeometryUtil.setGeometryProperty(geometry, "orientation", orientation);
    }
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), MoepConstants.TEXT_GROUP,
      object.getValue(MoepConstants.TEXT_GROUP));
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), MoepConstants.TEXT_INDEX,
      object.getValue(MoepConstants.TEXT_INDEX));
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), "text",
      object.getValue(MoepConstants.TEXT));
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), "textType",
      SaifConstants.TEXT_LINE);
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), "fontName",
      object.getValue(MoepConstants.FONT_NAME));
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), "characterHeight",
      object.getValue(MoepConstants.FONT_SIZE));
    JtsGeometryUtil.setGeometryProperty(object.getGeometry(), "other",
      object.getValue(MoepConstants.FONT_WEIGHT));
  }

  private char actionName;

  private final byte[] buffer = new byte[512];

  private Coordinates center;

  private byte coordinateBytes;

  private Record currentRecord;

  private final RecordFactory recordFactory;

  private final MoepDirectoryReader directoryReader;

  private GeometryFactory factory;

  private String featureCode;

  private byte fileType;

  private boolean hasNext = true;

  private final InputStream in;

  private boolean loadNextObject = true;

  private String originalFileType;

  private final String mapsheet;

  public MoepBinaryIterator(final MoepDirectoryReader directoryReader, final String fileName,
    final InputStream in, final RecordFactory recordFactory) {
    this.directoryReader = directoryReader;
    this.recordFactory = recordFactory;
    switch (fileName.charAt(fileName.length() - 5)) {
      case 'd':
        this.originalFileType = "dem";
      break;
      case 'm':
        this.originalFileType = "contours";
      break;
      case 'n':
        this.originalFileType = "nonPositional";
      break;
      case 'p':
        this.originalFileType = "planimetric";
      break;
      case 'g':
        this.originalFileType = "toponymy";
      break;
      case 'w':
        this.originalFileType = "woodedArea";
      break;
      case 's':
        this.originalFileType = "supplimentary";
      break;
      default:
        this.originalFileType = "unknown";
      break;
    }
    this.in = new BufferedInputStream(in, 10000);
    this.mapsheet = getMapsheetFromFileName(fileName);
    try {
      loadHeader();
    } catch (final IOException e) {
      throw new IllegalArgumentException("file cannot be opened", e);
    }
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.in);
  }

  private String getMapsheetFromFileName(final String fileName) {
    final File file = new File(fileName);
    final String baseName = FileUtil.getFileNamePrefix(file);
    final Pattern pattern = Pattern.compile("\\d{2,3}[a-z]\\d{3}");
    final Matcher matcher = pattern.matcher(baseName);
    if (matcher.find()) {
      return matcher.group();
    } else {
      return baseName;
    }
  }

  @Override
  public boolean hasNext() {
    if (!this.hasNext) {
      return false;
    } else if (this.loadNextObject) {
      return loadNextRecord() != null;
    } else {
      return true;
    }
  }

  private void loadHeader() throws IOException {
    this.fileType = (byte)read();
    if (this.fileType / 100 == 0) {
      this.coordinateBytes = 2;
    } else {
      this.fileType %= 100;
      this.coordinateBytes = 4;
    }
    String mapsheet = readString(11);
    mapsheet = mapsheet.replaceAll("\\.", "").toLowerCase();
    final Bcgs20000RectangularMapGrid bcgsGrid = new Bcgs20000RectangularMapGrid();
    final UtmRectangularMapGrid utmGrid = new UtmRectangularMapGrid();
    final double latitude = bcgsGrid.getLatitude(mapsheet) + 0.05;
    final double longitude = bcgsGrid.getLongitude(mapsheet) - 0.1;
    final int crsId = utmGrid.getNad83Srid(longitude, latitude);
    final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(crsId);

    final String submissionDateString = readString(6);

    final double centreX = readLEInt(this.in);
    final double centreY = readLEInt(this.in);
    this.center = new DoubleCoordinates(centreX, centreY);
    this.factory = GeometryFactory.getFactory(coordinateSystem.getId(), 1.0, 1.0);
    setProperty(IoConstants.GEOMETRY_FACTORY, this.factory);
  }

  protected Record loadNextRecord() {
    try {
      return loadRecord();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected Record loadRecord() throws IOException {
    final int featureKey = read();
    if (featureKey != 255) {

      final boolean hasFeatureCode = featureKey / 100 != 0;
      if (hasFeatureCode) {
        final String featureCode = readString(10);
        if (!featureCode.startsWith("HA9000")) {
          this.actionName = featureCode.charAt(6);
          this.featureCode = featureCode.substring(0, 6) + "0" + featureCode.substring(7);
        } else {
          this.actionName = 'W';
          this.featureCode = featureCode;
        }
      }

      final int extraParams = featureKey % 100 / 10;
      final int featureType = featureKey % 10;
      final byte numBytes = (byte)read();
      final Record object = this.recordFactory.createRecord(MoepConstants.META_DATA);
      object.setValue(MoepConstants.MAPSHEET_NAME, this.mapsheet);
      object.setValue(MoepConstants.FEATURE_CODE, this.featureCode);
      object.setValue(MoepConstants.ORIGINAL_FILE_TYPE, this.originalFileType);
      String attribute = null;
      if (numBytes > 0) {
        attribute = readString(numBytes);
      }
      switch (featureType) {
        case POINT:
          object.setValue(MoepConstants.DISPLAY_TYPE, "primary");
          final Point point = readPoint(this.in);
          object.setGeometryValue(point);
          if (extraParams == 1 || extraParams == 3) {
            final int angleInt = readLEInt(this.in);
            final int angle = angleInt / 10000;
            object.setValue(MoepConstants.ANGLE, angle);
          }
        break;
        case CONSTRUCTION_LINE:
        case CONSTRUCTION_COMPLEX_LINE:
          object.setValue(MoepConstants.DISPLAY_TYPE, "constructionLine");
          readLineString(extraParams, object);
        break;
        case SIMPLE_LINE:
        case COMPLEX_LINE:
          object.setValue(MoepConstants.DISPLAY_TYPE, "primaryLine");
          readLineString(extraParams, object);
        break;
        case TEXT:
          object.setValue(MoepConstants.DISPLAY_TYPE, "primary");
          final Point textPoint = readPoint(this.in);
          object.setGeometryValue(textPoint);
          if (extraParams == 1) {
            final int angleInt = readLEInt(this.in);
            final int angle = angleInt / 10000;
            object.setValue(MoepConstants.ANGLE, angle);
          }
          final int fontSize = readLEShort(this.in);
          final int numChars = read();
          final String text = readString(numChars);
          if (attribute == null) {
            object.setValue(MoepConstants.FONT_NAME, "31");
            object.setValue(MoepConstants.FONT_WEIGHT, "0");
          } else {
            final String fontName = new String(attribute.substring(0, 3).trim());
            object.setValue(MoepConstants.FONT_NAME, fontName);
            if (attribute.length() > 3) {
              final String other = new String(
                attribute.substring(3, Math.min(attribute.length(), 5)).trim());
              object.setValue(MoepConstants.FONT_WEIGHT, other);
            } else {
              object.setValue(MoepConstants.FONT_WEIGHT, "0");
            }
            if (attribute.length() > 5) {
              final String textGroup = new String(attribute.substring(4, 9).trim());
              object.setValue(MoepConstants.TEXT_GROUP, textGroup);
            }
          }
          object.setValue(MoepConstants.FONT_SIZE, fontSize);
          object.setValue(MoepConstants.TEXT, text);

          setGeometryProperties(object);
        break;
      }

      switch (this.actionName) {
        case 'W':
          setAdmissionHistory(object, this.actionName);
        break;
        case 'Z':
          setAdmissionHistory(object, this.actionName);
        break;
        case 'X':
          setRetirementHistory(object, this.actionName);
        break;
        case 'Y':
          setRetirementHistory(object, this.actionName);
        break;
        default:
          setAdmissionHistory(object, 'W');
        break;
      }
      this.currentRecord = object;
      this.loadNextObject = false;
      return this.currentRecord;
    } else {
      close();
      this.hasNext = false;
      return null;
    }
  }

  /**
   * Get the next data object read by this reader.
   *
   * @return The next record.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  @Override
  public Record next() {
    if (hasNext()) {
      this.loadNextObject = true;
      return this.currentRecord;
    } else {
      throw new NoSuchElementException();
    }
  }

  private int read() throws IOException {
    return this.in.read();
  }

  private LineString readContourLine(final int numCoords) throws IOException {
    final CoordinatesList coords = new DoubleCoordinatesList(numCoords, 2);
    for (int i = 0; i < numCoords; i++) {
      readCoordinate(this.in, coords, i);
    }
    return this.factory.createLineString(coords);
  }

  private void readCoordinate(final InputStream in, final CoordinatesList coords, final int index)
    throws IOException {
    for (int i = 0; i < 2; i++) {
      int coordinate;
      if (this.coordinateBytes == 2) {
        coordinate = readLEShort(in);
      } else {
        coordinate = readLEInt(in);
      }
      coords.setValue(index, i, this.center.getValue(i) + coordinate);
    }
    if (coords.getNumAxis() > 2) {
      final int z = readLEShort(in);
      coords.setValue(index, 2, z);
    }
  }

  private int readLEInt(final InputStream in) throws IOException {
    final int ch1 = in.read();
    final int ch2 = in.read();
    final int ch3 = in.read();
    final int ch4 = in.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    }
    return (ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24);

  }

  private short readLEShort(final InputStream in) throws IOException {
    final int ch1 = in.read();
    final int ch2 = in.read();
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (short)((ch1 << 0) + (ch2 << 8));

  }

  private void readLineString(final int extraParams, final Record object) throws IOException {
    int numCoords = 0;
    if (extraParams == 2 || extraParams == 4) {
      numCoords = readLEShort(this.in);

    } else {
      numCoords = read();
    }
    if (extraParams == 3 || extraParams == 4) {
      final int z = readLEShort(this.in);
      final LineString line = readContourLine(numCoords);
      object.setGeometryValue(line);
      object.setValue(MoepConstants.ELEVATION, new Integer(z));

    } else {
      final LineString line = readSimpleLine(numCoords);
      object.setGeometryValue(line);
    }
  }

  private Point readPoint(final InputStream in) throws IOException {
    final CoordinatesList coords = new DoubleCoordinatesList(1, 3);
    readCoordinate(in, coords, 0);
    return this.factory.createPoint(coords);
  }

  private LineString readSimpleLine(final int numCoords) throws IOException {
    final CoordinatesList coords = new DoubleCoordinatesList(numCoords, 3);
    for (int i = 0; i < numCoords; i++) {
      readCoordinate(this.in, coords, i);
    }
    return this.factory.createLineString(coords);
  }

  private String readString(final int length) throws IOException {
    final int read = this.in.read(this.buffer, 0, length);
    if (read > -1) {
      return new String(this.buffer, 0, read).trim();
    } else {
      return null;
    }
  }

  @Override
  public void remove() {
  }

  private void setAdmissionHistory(final Record object, final char reasonForChange) {
    if (this.directoryReader != null) {
      object.setValue(MoepConstants.ADMIT_SOURCE_DATE, this.directoryReader.getSubmissionDate());
      object.setValue(MoepConstants.ADMIT_INTEGRATION_DATE,
        this.directoryReader.getIntegrationDate());
      object.setValue(MoepConstants.ADMIT_REVISION_KEY, this.directoryReader.getRevisionKey());
      object.setValue(MoepConstants.ADMIT_SPECIFICATIONS_RELEASE,
        this.directoryReader.getSpecificationsRelease());
    }
    object.setValue(MoepConstants.ADMIT_REASON_FOR_CHANGE, String.valueOf(reasonForChange));
  }

  private void setRetirementHistory(final Record object, final char reasonForChange) {
    if (this.directoryReader != null) {
      object.setValue(MoepConstants.RETIRE_SOURCE_DATE, this.directoryReader.getSubmissionDate());
      object.setValue(MoepConstants.RETIRE_INTEGRATION_DATE,
        this.directoryReader.getIntegrationDate());
      object.setValue(MoepConstants.RETIRE_REVISION_KEY, this.directoryReader.getRevisionKey());
      object.setValue(MoepConstants.RETIRE_SPECIFICATIONS_RELEASE,
        this.directoryReader.getSpecificationsRelease());
    }
    object.setValue(MoepConstants.RETIRE_REASON_FOR_CHANGE, String.valueOf(reasonForChange));
  }
}
