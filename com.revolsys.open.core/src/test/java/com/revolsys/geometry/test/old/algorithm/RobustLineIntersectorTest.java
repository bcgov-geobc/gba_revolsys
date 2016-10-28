/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.geometry.test.old.algorithm;

import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

import junit.framework.TestCase;

/**
 * Basic functionality tests for RobustLineIntersector.
 *
 * @version 1.7
 */
public class RobustLineIntersectorTest extends TestCase {

  public static void main(final String[] args) {
    final String[] testCaseName = {
      RobustLineIntersectorTest.class.getName()
    };
    junit.textui.TestRunner.main(testCaseName);
  }// public static void main(String[] args)

  RobustLineIntersector i = new RobustLineIntersector();

  public RobustLineIntersectorTest(final String Name_) {
    super(Name_);
  }// public RobustLineIntersectorTest(String Name_)

  public void test2Lines() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Point p1 = new PointDoubleXY((double)10, 10);
    final Point p2 = new PointDoubleXY((double)20, 20);
    final Point q1 = new PointDoubleXY((double)20, 10);
    final Point q2 = new PointDoubleXY((double)10, 20);
    final Point x = new PointDoubleXY((double)15, 15);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.POINT_INTERSECTION, i.getIntersectionCount());
    assertEquals(1, i.getIntersectionCount());
    assertEquals(x, i.getIntersection(0));
    assertTrue(i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testA() {
    final Point p1 = new PointDoubleXY((double)-123456789, -40);
    final Point p2 = new PointDoubleXY(381039468754763d, 123456789);
    final Point q = new PointDoubleXY((double)0, 0);
    final GeometryFactory geometryFactory = GeometryFactory.DEFAULT;
    final LineString l = geometryFactory.lineString(new Point[] {
      p1, p2
    });
    final Point p = geometryFactory.point(q);
    assertEquals(false, l.intersects(p));
    assertEquals(false, geometryFactory.lineString(p1, p2).isOnLine(q));
    assertEquals(-1, CGAlgorithmsDD.orientationIndex(p1, p2, q));
  }

  public void testCollinear1() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Point p1 = new PointDoubleXY((double)10, 10);
    final Point p2 = new PointDoubleXY((double)20, 10);
    final Point q1 = new PointDoubleXY((double)22, 10);
    final Point q2 = new PointDoubleXY((double)30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.NO_INTERSECTION, i.getIntersectionCount());
    assertTrue(!i.isProper());
    assertTrue(!i.hasIntersection());
  }

  public void testCollinear2() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Point p1 = new PointDoubleXY((double)10, 10);
    final Point p2 = new PointDoubleXY((double)20, 10);
    final Point q1 = new PointDoubleXY((double)20, 10);
    final Point q2 = new PointDoubleXY((double)30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.POINT_INTERSECTION, i.getIntersectionCount());
    assertTrue(!i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear3() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Point p1 = new PointDoubleXY((double)10, 10);
    final Point p2 = new PointDoubleXY((double)20, 10);
    final Point q1 = new PointDoubleXY((double)15, 10);
    final Point q2 = new PointDoubleXY((double)30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.COLLINEAR_INTERSECTION, i.getIntersectionCount());
    assertTrue(!i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear4() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Point p1 = new PointDoubleXY((double)30, 10);
    final Point p2 = new PointDoubleXY((double)20, 10);
    final Point q1 = new PointDoubleXY((double)10, 10);
    final Point q2 = new PointDoubleXY((double)30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.COLLINEAR_INTERSECTION, i.getIntersectionCount());
    assertTrue(i.hasIntersection());
  }

  public void testEndpointIntersection() {
    this.i.computeIntersection(new PointDoubleXY((double)100, 100),
      new PointDoubleXY((double)10, 100),
      new PointDoubleXY((double)100, 10),
      new PointDoubleXY((double)100, 100));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionCount());
  }

  public void testEndpointIntersection2() {
    this.i.computeIntersection(new PointDoubleXY((double)190, 50),
      new PointDoubleXY((double)120, 100),
      new PointDoubleXY((double)120, 100),
      new PointDoubleXY((double)50, 150));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionCount());
    assertEquals(new PointDoubleXY((double)120, 100),
      this.i.getIntersection(1));
  }

  public void testIsCCW() {
    assertEquals(1,
      CGAlgorithmsDD.orientationIndex(
        new PointDoubleXY((double)-123456789, -40),
        new PointDoubleXY((double)0, 0),
        new PointDoubleXY(381039468754763d, 123456789)));
  }

  public void testIsCCW2() {
    assertEquals(0,
      CGAlgorithmsDD.orientationIndex(new PointDoubleXY((double)10, 10),
        new PointDoubleXY((double)20, 20),
        new PointDoubleXY((double)0, 0)));
  }

  public void testIsProper1() {
    this.i.computeIntersection(new PointDoubleXY((double)30, 10),
      new PointDoubleXY((double)30, 30),
      new PointDoubleXY((double)10, 10),
      new PointDoubleXY((double)90, 11));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionCount());
    assertTrue(this.i.isProper());
  }

  public void testIsProper2() {
    this.i.computeIntersection(new PointDoubleXY((double)10, 30),
      new PointDoubleXY((double)10, 0),
      new PointDoubleXY((double)11, 90),
      new PointDoubleXY((double)10, 10));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionCount());
    assertTrue(!this.i.isProper());
  }

  public void testOverlap() {
    this.i.computeIntersection(new PointDoubleXY((double)180, 200),
      new PointDoubleXY((double)160, 180),
      new PointDoubleXY((double)220, 240),
      new PointDoubleXY((double)140, 160));
    assertTrue(this.i.hasIntersection());
    assertEquals(2, this.i.getIntersectionCount());
  }

}
