package com.revolsys.gis.cs;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.SpringUtil;

public class WktCsParser {
  public static CoordinateSystem read(final String wkt) {
    return new WktCsParser(wkt).parse();
  }

  private int index = 0;

  private final Stack<String> nameStack = new Stack<String>();

  private final String value;

  public WktCsParser(final InputStream in) {
    this(FileUtil.createUtf8Reader(in));
  }

  public WktCsParser(final Reader reader) {
    this(FileUtil.getString(reader));
  }

  public WktCsParser(final Resource resource) {
    this(SpringUtil.getString(resource));
  }

  public WktCsParser(final String value) {
    this.value = value;
  }

  public CoordinateSystem parse() {
    if (this.value.length() == 0) {
      return null;
    } else {
      return (CoordinateSystem)parseValue();
    }

  }

  private String parseName() {
    final int startIndex = this.index;

    while (this.value.charAt(this.index) != '[' && this.value.charAt(this.index) != ']') {
      this.index++;
    }
    final String name = new String(this.value.substring(startIndex, this.index));
    return name;
  }

  private double parseNumber() {
    final int startIndex = this.index;

    char currentChar = this.value.charAt(this.index);
    while (Character.isDigit(currentChar) || currentChar == '.' || currentChar == '-') {
      this.index++;
      currentChar = this.value.charAt(this.index);
    }
    final String string = this.value.substring(startIndex, this.index);
    return Double.parseDouble(string);
  }

  private String parseString() {
    final int startIndex = this.index;

    char currentChar = this.value.charAt(this.index);
    while (currentChar != '"') {
      this.index++;
      currentChar = this.value.charAt(this.index);
    }
    final String string = new String(this.value.substring(startIndex, this.index));
    this.index++;
    return string;
  }

  private Object parseValue() {
    char currentChar = this.value.charAt(this.index);
    if (currentChar == '"') {
      this.index++;
      return parseString();
    } else if (Character.isDigit(currentChar) || currentChar == '-') {
      return parseNumber();
    } else {
      final String name = parseName();
      this.nameStack.push(name);
      try {
        final List<Object> values = new ArrayList<Object>();
        currentChar = this.value.charAt(this.index);
        if (currentChar == '[') {
          do {
            this.index++;
            currentChar = skipWhitespace();
            if (currentChar != ']') {
              final Object value = parseValue();
              values.add(value);
            }
            currentChar = skipWhitespace();
          } while (currentChar == ',');
          this.index++;
          if (name.equals("AUTHORITY")) {
            return processAuthority(values);
          } else if (name.equals("AXIS")) {
            return processAxis(values);
          } else if (name.equals("DATUM")) {
            return processDatum(values);
          } else if (name.equals("GEOGCS")) {
            return processGeographicCoordinateSystem(values);
          } else if (name.equals("PRIMEM")) {
            return processPrimeMeridian(values);
          } else if (name.equals("PROJCS")) {
            return processProjectedCoordinateSystem(values);
          } else if (name.equals("PROJECTION")) {
            return processProjection(values);
          } else if (name.equals("SPHEROID")) {
            return processSpheroid(values);
          } else if (name.equals("TOWGS84")) {
            return processToWgs84(values);
          } else if (name.equals("UNIT")) {
            if (this.nameStack.get(this.nameStack.size() - 2).equals("GEOGCS")) {
              return processAngularUnit(values);
            } else {
              return processLinearUnit(values);
            }
          } else {
            return Collections.singletonMap(name, values);
          }
        } else {
          return name;
        }
      } finally {
        this.nameStack.pop();
      }
    }
  }

  private AngularUnit processAngularUnit(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number conversionFactor = (Number)values.get(1);
    Authority authority = null;
    if (values.size() > 2) {
      authority = (Authority)values.get(2);
    }
    return new AngularUnit(name, conversionFactor.doubleValue(), authority);
  }

  private Authority processAuthority(final List<Object> values) {
    final String name = (String)values.get(0);
    final String code = (String)values.get(1);
    return new BaseAuthority(name, code);
  }

  private Axis processAxis(final List<Object> values) {
    final String name = (String)values.get(0);
    final String direction = (String)values.get(1);
    return new Axis(name, direction);
  }

  private Datum processDatum(final List<Object> values) {
    final String name = (String)values.get(0);
    Spheroid spheroid = null;
    Authority authority = null;
    ToWgs84 toWgs84 = null;
    for (int i = 1; i < values.size(); i++) {
      final Object value = values.get(i);
      if (value instanceof Spheroid) {
        spheroid = (Spheroid)value;
      } else if (value instanceof Authority) {
        authority = (Authority)value;
      } else if (value instanceof ToWgs84) {
        toWgs84 = (ToWgs84)value;
      }
    }
    return new Datum(name, spheroid, toWgs84, authority);
  }

  private GeographicCoordinateSystem processGeographicCoordinateSystem(final List<Object> values) {
    final String name = (String)values.get(0);
    final Datum datum = (Datum)values.get(1);
    final PrimeMeridian primeMeridian = (PrimeMeridian)values.get(2);
    final AngularUnit angularUnit = (AngularUnit)values.get(3);
    int index = 4;
    List<Axis> axis = null;
    if (index < values.size() && values.get(index) instanceof Axis) {
      axis = Arrays.asList((Axis)values.get(index++), (Axis)values.get(index++));

    }
    Authority authority = null;
    if (index < values.size()) {
      authority = (Authority)values.get(index);
    }
    final int authorityId;
    if (authority == null) {
      authorityId = 0;
    } else {
      final String authorityCode = authority.getCode();
      if (authorityCode == null) {
        authorityId = 0;
      } else {
        authorityId = Integer.parseInt(authorityCode);
      }
    }
    return new GeographicCoordinateSystem(authorityId, name, datum, primeMeridian, angularUnit,
      axis, authority);
  }

  private LinearUnit processLinearUnit(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number conversionFactor = (Number)values.get(1);
    Authority authority = null;
    if (values.size() > 2) {
      authority = (Authority)values.get(2);
    }
    return new LinearUnit(name, conversionFactor.doubleValue(), authority);
  }

  private PrimeMeridian processPrimeMeridian(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number longitude = (Number)values.get(1);
    Authority authority = null;
    if (values.size() > 2) {
      authority = (Authority)values.get(2);
    }
    return new PrimeMeridian(name, longitude.doubleValue(), authority);
  }

  @SuppressWarnings("unchecked")
  private ProjectedCoordinateSystem processProjectedCoordinateSystem(final List<Object> values) {
    int index = 0;
    final String name = (String)values.get(index++);
    final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)values
      .get(index++);
    Projection projection = null;
    final Map<String, Object> parameters = new HashMap<String, Object>();

    LinearUnit linearUnit = null;
    final List<Axis> axis = new ArrayList<Axis>();
    Authority authority = null;

    while (index < values.size()) {
      final Object value = values.get(index++);
      if (value instanceof Projection) {
        projection = (Projection)value;
      } else if (value instanceof Map) {
        final Map<String, List<Object>> map = (Map<String, List<Object>>)value;
        final String key = map.keySet().iterator().next();
        if (key.equals("PARAMETER")) {
          final List<Object> paramValues = map.get(key);
          final String paramName = (String)paramValues.get(0);
          final Object paramValue = paramValues.get(1);
          parameters.put(paramName, paramValue);
        }
      } else if (value instanceof LinearUnit) {
        linearUnit = (LinearUnit)value;
      } else if (value instanceof Axis) {
        axis.add((Axis)value);
      } else if (value instanceof Authority) {
        authority = (Authority)value;
      }
    }
    int srid = -1;
    if (authority != null) {
      final String code = authority.getCode();
      if (code != null) {
        srid = Integer.parseInt(code);
      }
    }
    return new ProjectedCoordinateSystem(srid, name, geographicCoordinateSystem, projection,
      parameters, linearUnit, axis, authority);
  }

  private Projection processProjection(final List<Object> values) {
    final String name = (String)values.get(0);
    return new Projection(name);
  }

  private Spheroid processSpheroid(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number semiMajorAxis = (Number)values.get(1);
    final Number inverseFlattening = (Number)values.get(2);
    Authority authority = null;
    if (values.size() > 3) {
      authority = (Authority)values.get(3);
    }
    return new Spheroid(name, semiMajorAxis.doubleValue(), inverseFlattening.doubleValue(),
      authority);
  }

  private ToWgs84 processToWgs84(final List<Object> values) {
    return new ToWgs84(values);
  }

  private char skipWhitespace() {
    char currentChar = this.value.charAt(this.index);
    while (Character.isWhitespace(currentChar)) {
      this.index++;
      currentChar = this.value.charAt(this.index);
    }
    return currentChar;
  }
}
