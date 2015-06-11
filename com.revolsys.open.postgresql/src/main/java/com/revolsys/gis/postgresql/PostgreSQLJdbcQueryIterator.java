package com.revolsys.gis.postgresql;

import java.util.Map;

import org.postgis.PGbox2d;
import org.postgis.Point;

import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class PostgreSQLJdbcQueryIterator extends JdbcQueryIterator {

  public PostgreSQLJdbcQueryIterator(final JdbcRecordStore dataStore, final Query query,
    final Map<String, Object> properties) {
    super(dataStore, query, properties);
  }

  @Override
  protected String getSql(Query query) {
    BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final RecordDefinition metaData = getRecordDefinition();
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        query = query.clone();
        final String geometryAttributeName = metaData.getGeometryFieldName();
        final GeometryFactory geometryFactory = metaData.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final double x1 = boundingBox.getMinX();
        final double y1 = boundingBox.getMinY();
        final double x2 = boundingBox.getMaxX();
        final double y2 = boundingBox.getMaxY();

        final PGbox2d box = new PGbox2d(new Point(x1, y1), new Point(x2, y2));
        query.and(new BinaryCondition(geometryAttributeName, "&&", box));
        setQuery(query);
      }
    }

    String sql = super.getSql(query);
    final int offset = query.getOffset();
    final int limit = query.getLimit();
    if (offset > 0) {
      sql += " OFFSET " + offset;
    }
    if (limit > -1) {
      sql += " LIMIT " + limit;
    }
    return sql;
  }

}
