package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.data.equals.Equals;
import com.vividsolutions.jts.geom.Envelope;

public class Area implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 2662773652065582230L;

  private final Authority authority;

  private final boolean deprecated;

  private final Envelope latLonBounds;

  private final String name;

  public Area(final String name, final Envelope latLonBounds, final Authority authority,
    final boolean deprecated) {
    this.name = name;
    this.latLonBounds = latLonBounds;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Area) {
      final Area area = (Area)obj;
      if (!Equals.equal(this.latLonBounds, area.latLonBounds)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public Envelope getLatLonBounds() {
    return this.latLonBounds;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.latLonBounds.hashCode();
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
