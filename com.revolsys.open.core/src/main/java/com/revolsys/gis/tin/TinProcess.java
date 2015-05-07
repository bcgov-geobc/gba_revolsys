package com.revolsys.gis.tin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.revolsys.data.record.Record;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class TinProcess extends BaseInOutProcess<Record, Record> {
  private static final Logger LOG = LoggerFactory.getLogger(TinProcess.class);

  private TriangulatedIrregularNetwork tin;

  private Channel<Record> tinIn;

  private Reader<Record> tinReader;

  private BoundingBox boundingBox;

  private Map<String, Object> updatedAttributeValues;

  private File tinCache;

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public File getTinCache() {
    return tinCache;
  }

  public Channel<Record> getTinIn() {
    return tinIn;
  }

  public Reader<Record> getTinReader() {
    return tinReader;
  }

  public Map<String, Object> getUpdatedAttributeValues() {
    return updatedAttributeValues;
  }

  @Override
  protected void init() {
    super.init();
    if (tinIn != null) {
      tinIn.readConnect();
    }
  }

  protected void loadTin() {
    if (tinCache != null && tinCache.exists()) {
      final File tinFile = new File(tinCache, boundingBox.getId() + ".tin");
      if (tinFile.exists()) {
        LOG.info("Loading tin from file " + tinFile);
        final FileSystemResource resource = new FileSystemResource(tinFile);
        try {
          tin = TinReader.read(boundingBox, resource);
        } catch (final Exception e) {
          LOG.error("Unable to read tin file " + resource, e);
        }
      }
    }
    if (tin == null) {
      LOG.info("Loading tin from database");
      tin = new TriangulatedIrregularNetwork(boundingBox);
      List<CoordinatesList> lines = new ArrayList<CoordinatesList>();
      if (tinIn != null) {
        readTinFeatures(tin, lines, tinIn);
      }
      if (tinReader != null) {
        readTinFeatures(tin, lines, tinReader);
      }
      for (final CoordinatesList line : lines) {
        tin.insertNodes(line);
      }
      for (final CoordinatesList line : lines) {
        tin.insertEdge(line);
      }
      lines = null;
      tin.finishEditing();
      if (tinCache != null) {
        final File tinFile = new File(tinCache, boundingBox.getId() + ".tin");
        try {
          tinCache.mkdirs();
          final FileSystemResource resource = new FileSystemResource(tinFile);
          TinWriter.write(resource, tin);
        } catch (final Exception e) {
          LOG.error("Unable to cache tin to file " + tinFile);
        }
      }
    }
  }

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    if (tin == null) {
      LOG.info("Tin not created as there were no features");
    }
    this.tin = null;
    if (tinIn != null) {
      tinIn.readDisconnect();
    }
    if (tinReader != null) {
      tinReader.close();
    }
    this.boundingBox = null;
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    if (tin == null) {
      loadTin();
    }
    final LineString geometry = object.getGeometryValue();
    if (geometry instanceof LineString) {
      final LineString line = geometry;
      final Envelope envelope = line.getEnvelopeInternal();
      if (envelope.intersects(boundingBox)) {
        final LineString newLine = tin.getElevation(line);
        if (line != newLine) {
          object.setGeometryValue(newLine);
        }
        if (updatedAttributeValues != null) {
          for (final Entry<String, Object> entry : updatedAttributeValues.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (object.getValueByPath(key) == null) {
              object.setValueByPath(key, value);
            }
          }
        }
      }
    }
    out.write(object);
  }

  private void readTinFeatures(final TriangulatedIrregularNetwork tin,
    final List<CoordinatesList> lines, final Iterable<Record> iterable) {
    for (final Record object : iterable) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        tin.insertNode(point);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final CoordinatesList points = CoordinatesListUtil.get(line);
        lines.add(points);
      }
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setTinCache(final File tinCache) {
    this.tinCache = tinCache;
  }

  public void setTinIn(final Channel<Record> tinIn) {
    this.tinIn = tinIn;
  }

  public void setTinReader(final Reader<Record> tinReader) {
    this.tinReader = tinReader;
  }

  public void setUpdatedAttributeValues(
    final Map<String, Object> updatedAttributeValues) {
    this.updatedAttributeValues = updatedAttributeValues;
  }
}
