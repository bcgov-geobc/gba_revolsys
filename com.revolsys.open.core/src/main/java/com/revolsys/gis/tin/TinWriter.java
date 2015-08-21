package com.revolsys.gis.tin;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.spring.resource.SpringUtil;

public class TinWriter {

  public static void write(final Resource resource, final TriangulatedIrregularNetwork tin) {
    final TinWriter tinWriter = new TinWriter(resource);
    try {
      tinWriter.write(tin);
    } finally {
      tinWriter.close();
    }
  }

  private final PrintWriter out;

  private int tinIndex = 0;

  public TinWriter(final Resource resource) {
    this.out = SpringUtil.getPrintWriter(resource);
    this.out.println("TIN");
  }

  public void close() {
    this.out.close();
  }

  public void write(final TriangulatedIrregularNetwork tin) {
    this.out.println("BEGT");

    this.out.print("TNAM tin-");
    this.out.println(++this.tinIndex);

    this.out.println("TCOL 255 255 255");

    int nodeIndex = 0;
    final Map<Coordinates, Integer> nodeMap = new HashMap<Coordinates, Integer>();
    final Set<Coordinates> nodes = tin.getNodes();
    this.out.print("VERT ");
    this.out.println(nodes.size());
    for (final Coordinates point : nodes) {
      nodeMap.put(point, ++nodeIndex);
      this.out.print(point.getX());
      this.out.print(' ');
      this.out.print(point.getY());
      this.out.print(' ');
      this.out.println(point.getZ());
    }

    final List<Triangle> triangles = tin.getTriangles();
    this.out.print("TRI ");
    this.out.println(triangles.size());
    for (final Triangle triangle : triangles) {
      for (int i = 0; i < 3; i++) {
        if (i > 0) {
          this.out.print(' ');
        }
        final Coordinates point = triangle.get(i);
        final Integer index = nodeMap.get(point);
        if (index == null) {
          System.out.println(point);
          System.out.println(triangle);
          System.out.println(tin.getBoundingBox().toPolygon());
          throw new NullPointerException();
        }
        this.out.print(index);
      }
      this.out.println();
    }

    this.out.println("ENDT");
  }
}
