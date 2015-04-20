package com.billooms.drawables;

import com.billooms.drawables.geometry.LineGeom;
import com.billooms.drawables.vecmath.Vector2d;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A curve-fit curve defined by a number of Points.
 *
 * Points are ordered, bottom first and top last. The curve-fit algorithm is a
 * quadratic Bezier curve. Note that a quadratic fit will not do a good job at
 * points of inflection because one control point between two data points will
 * give a straight line (at best) at points of inflection. So for segments that
 * have an inflection, a cubic Bezier fit is used.
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class FittedCurve extends PtDefinedLine {

  /** Fit a segment either Quadratic or Cubic. */
  private enum FitType {

    /** Quadratic fit. */
    QUAD,
    /** Cubic fit. */
    CUBIC
  };

  /**
   * Data for a segment of a curve which can be a Quadratic fit or a Cubic fit.
   */
  private class SegData {

    protected FitType type;	    // QUAD or CUBIC
    protected Point2D.Double p1;    // control point for QUAD, or 1st point for CUBIC
    protected Point2D.Double p2;    // 2nd control point for CUBIC

    /**
     * Create the segment data from 2 control points (implies CUBIC).
     *
     * @param p1 1st control point
     * @param p2 2nd control point
     */
    public SegData(Point2D.Double p1, Point2D.Double p2) {    // 2 control points for a cubic fit
      this.type = FitType.CUBIC;
      this.p1 = p1;
      this.p2 = p2;
    }

    /**
     * Create the segment data from 1 control point (implies QUAD). Note that p2
     * is set to null.
     *
     * @param p1 control point
     */
    public SegData(Point2D.Double p1) {		// 1 control point for a quadratic fit
      this.type = FitType.QUAD;
      this.p1 = p1;
      this.p2 = null;
    }
  }

  /** Point spacing. */
  private double ptSpacing = 0.01;
  /** Most recent fit points that have been calculated. */
  private Point2D.Double[] curvePts;
  /** Control points for this curve. */
  private ArrayList<SegData> cPoints;
  /** Angle at each point. */
  private double[] pAng;

  /**
   * A curve-fit curve defined by a number of Points.
   *
   * @param c Color line color
   * @param s Stroke line stroke
   */
  public FittedCurve(Color c, BasicStroke s) {
    super(c, s);
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);	  // paints Points, set line color and stroke
    if (isVisible()) {
      if (ptList.size() <= 1) {     // no line for only one point
        return;
      }
      Pt p, pm1;
      if (ptList.size() == 2) {       // straight line for 2 points
        pm1 = ptList.get(0);
        p = ptList.get(1);
        g2d.draw(new Line2D.Double(pm1.getX(), pm1.getY(), p.getX(), p.getY()));
        return;
      }

      if (ptSpacing == 0.0) {
        return;
      }

      curvePts = buildCurvePoints(ptSpacing);   // curvePts are made when needed for painting
      for (int i = 1; i < curvePts.length; i++) {
        g2d.draw(new Line2D.Double(curvePts[i - 1].x, curvePts[i - 1].y, curvePts[i].x, curvePts[i].y));
      }

      getControlPoints().stream().forEach((pt) -> {
        g2d.draw(new Line2D.Double(pt.x, pt.y, pt.x, pt.y));	// should be just a 1 pixel dot
      });
    }
  }

  @Override
  protected void update() {
    makeControlPts();
  }

  /**
   * Set the point spacing.
   *
   * @param spacing new point spacing
   */
  public void setPtSpacing(double spacing) {
    this.ptSpacing = spacing;
  }

  /**
   * Get a list of the control points for the fit curve.
   *
   * @return list of control points
   */
  private ArrayList<Point2D.Double> getControlPoints() {
    ArrayList<Point2D.Double> cPts = new ArrayList<>();
    for (SegData segData : cPoints) {
      switch (segData.type) {
        case QUAD:
          cPts.add(segData.p1);
          break;
        case CUBIC:
          cPts.add(segData.p1);
          cPts.add(segData.p2);
          break;
      }
    }
    return cPts;
  }

  /**
   * Build and return an array of Point2D for the points on the fit curve in
   * inches that are fit from the original array of given points.
   *
   * @param dd approximate distance that should be between returned points
   * @return array of Point2D for the points on the fit curve, empty array if no
   * points
   */
  public synchronized Point2D.Double[] buildCurvePoints(double dd) {
    ptSpacing = dd;
    ArrayList<Point2D.Double> pts = new ArrayList<>();

    if (ptList.isEmpty()) {
      return pts.toArray(new Point2D.Double[pts.size()]);
    }

    if (ptList.size() == 1) {
      pts.add(ptList.get(0).getPoint2D());
      return pts.toArray(new Point2D.Double[pts.size()]);
    }

    if (ptList.size() == 2) {      // straight line between 2 points
      Point2D.Double d0 = ptList.get(0).getPoint2D();
      Point2D.Double d1 = ptList.get(1).getPoint2D();
      double dx = d1.x - d0.x;
      double dy = d1.y - d0.y;
      double length = Math.sqrt(dx * dx + dy * dy);
      int npts = (int) (length / dd);
      for (int i = 0; i <= npts; i++) {
        double x = d0.x + (d1.x - d0.x) * i / npts;
        double y = d0.y + (d1.y - d0.y) * i / npts;
        pts.add(new Point2D.Double(x, y));
      }
      pAng = new double[ptList.size()];		// angle is the same at each point
      pAng[0] = pAng[1] = Math.atan2(dy, dx);
      return pts.toArray(new Point2D.Double[pts.size()]);
    }

    makeControlPts();
    pts.add(new Point2D.Double(ptList.get(0).getX(), ptList.get(0).getY()));	// first point
    for (int i = 0; i < ptList.size() - 1; i++) {
      SegData seg = cPoints.get(i);
      double t, t_;
      double a, b, c, d;
      Point2D.Double di = ptList.get(i).getPoint2D();
      Point2D.Double di1 = ptList.get(i + 1).getPoint2D();
      double length = Math.hypot(di1.x - di.x, di1.y - di.y);
      int pps = Math.max((int) (length / dd), 1);	// number of points for this segment of the curve
      double step = 1.0 / (double) pps;
      switch (seg.type) {
        case QUAD:
          for (int j = 1; j <= pps; j++) {	// Quadratic Bezier Curve
            t = j * step;
            t_ = 1.0 - t;
            a = t_ * t_;          // a = (1-t)(1-t)
            b = 2 * t_ * t;       // b = 2(1-t)t
            c = t * t;            // c = tt
            // B(t)= a*P0 + b*P1 + c*P2 + d*P3
            // P0 = pt[i]
            // P1 = cPoints[i].p1
            // P2 = pt[i+1]
            pts.add(new Point2D.Double(a * di.x + b * cPoints.get(i).p1.x + c * di1.x,
                a * di.y + b * cPoints.get(i).p1.y + c * di1.y));
          }
          break;
        case CUBIC:
          for (int j = 1; j <= pps; j++) {	// Cubic Bezier Curve
            t = j * step;
            t_ = 1.0 - t;
            a = t_ * t_ * t_;     // a = (1-t)(1-t)(1-t)
            b = 3 * t_ * t_ * t;  // b = 3(1-t)(1-t)t
            c = 3 * t_ * t * t;   // c = 3(1-t)tt
            d = t * t * t;        // d = ttt
            // B(t)= a*P0 + b*P1 + c*P2 + d*P3
            // P0 = pt[i]
            // P1 = cPoints[i].p1
            // P2 = cPoints[i].p3
            // P3 = pt[i+1]
            pts.add(new Point2D.Double(a * di.x + b * cPoints.get(i).p1.x + c * cPoints.get(i).p2.x + d * di1.x,
                a * di.y + b * cPoints.get(i).p1.y + c * cPoints.get(i).p2.y + d * di1.y));
          }
          break;
      }
    }
    return pts.toArray(new Point2D.Double[pts.size()]);
  }

  /**
   * Make the control points for this curve.
   */
  private void makeControlPts() {
    if (ptList.size() < 3) {	  // don't need to fit a line with 2 points
      return;
    }
    Point2D.Double[] pt = new Point2D.Double[ptList.size()];
    for (int i = 0; i < pt.length; i++) {	// make an array of Point2D.Double for each of the Points
      pt[i] = ptList.get(i).getPoint2D();
    }

    double[] segAng = new double[pt.length - 1];  // the angle of each segment
    double[] segL = new double[pt.length - 1];	  // the length of each segment
    for (int i = 0; i < segAng.length; i++) {
      double dx = pt[i + 1].x - pt[i].x;
      double dy = pt[i + 1].y - pt[i].y;
      segAng[i] = Math.atan2(dy, dx);		// the angle of each segment
      if (i > 0) {
        if ((segAng[i - 1] > Math.PI / 2.0) && (segAng[i] < 0)) {
          segAng[i] += 2.0 * Math.PI;		// add 2PI so angles continue smoothly
        }
        if ((segAng[i - 1] < -Math.PI / 2.0) && (segAng[i] > 0)) {
          segAng[i] -= 2.0 * Math.PI;		// subtract 2PI so angles continue smoothly
        }
      }
      segL[i] = Math.hypot(dx, dy);		// the length of each segment
//      System.out.println(i + " segAng:" + segAng[i] * 180. / Math.PI + " segL:" + segL[i]);
    }

    pAng = new double[ptList.size()];		// angle at each point
    for (int i = 1; i < pAng.length - 1; i++) {	// proportional to adjacent segments' length
      pAng[i] = segAng[i - 1] + (segAng[i] - segAng[i - 1]) * segL[i - 1] / (segL[i - 1] + segL[i]);
    }
    pAng[0] = 2.0 * segAng[0] - pAng[1];
    pAng[pAng.length - 1] = 2.0 * segAng[pt.length - 2] - pAng[pt.length - 2];
//    for (int i = 0; i < pAng.length; i++) {
//      System.out.println(i + " pAng:" + pAng[i] * 180. / Math.PI);
//    }

    cPoints = new ArrayList<>();	// control points -- at intersection of pAng lines
    for (int i = 0; i < pt.length - 1; i++) {
      LineGeom line0 = new LineGeom(pt[i], pAng[i]);
      LineGeom line1 = new LineGeom(pt[i + 1], pAng[i + 1]);
      if (((segAng[i] > pAng[i]) && (pAng[i + 1] < segAng[i]))
          || ((segAng[i] < pAng[i]) && (pAng[i + 1] > segAng[i]))) {  // point of inflection
        LineGeom ll = new LineGeom(pt[i], pt[i + 1]);
        double x1, y1, x2, y2;
        if ((ll.isVertical()) || (Math.abs(ll.getSlope()) > 1.0)) {	// for steep lines
          y1 = 0.25 * (pt[i + 1].y - pt[i].y) + pt[i].y;	// use 25% and 75% of y
          x1 = line0.getX(y1);
          y2 = 0.75 * (pt[i + 1].y - pt[i].y) + pt[i].y;
          x2 = line1.getX(y2);
        } else {						// for shallow lines
          x1 = 0.25 * (pt[i + 1].x - pt[i].x) + pt[i].x;	// use 25% and 75% of x
          y1 = line0.getY(x1);
          x2 = 0.75 * (pt[i + 1].x - pt[i].x) + pt[i].x;
          y2 = line1.getY(x2);
        }
        cPoints.add(new SegData(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2)));
      } else {
        Point2D.Double iSect = line0.intersection(line1);	// find intersection of 2 straight lines
        if (iSect == null) {	// if same slope, choose a point half way
          cPoints.add(new SegData(new Point2D.Double((pt[i].x + pt[i + 1].x) / 2.0, (pt[i].y + pt[i + 1].y) / 2.0)));
        } else {
          cPoints.add(new SegData(new Point2D.Double(iSect.x, iSect.y)));
        }
      }
    }
  }

  /**
   * Offset all the Points in the curve.
   *
   * @param d offset amount
   * @param dir Direction: true = FRONT_INSIDE or BACK_OUTSIDE, false =
   * FRONT_OUTSIDE or BACK_INSIDE
   */
  public synchronized void offsetDots(double d, boolean dir) {
    if (ptList.isEmpty()) {
      return;
    }
    makeControlPts();		// make sure that pAng is up to date
    for (int i = 0; i < ptList.size(); i++) {
      Pt pt = ptList.get(i);
      Vector2d perp = perpendicularAt(i, dir);
      perp.scale(d);
      pt.offsetQuiet(perp);
      // Don't us this as it fires propertyChanges!
      // and as each point is moved the curve is changed and re-fitted.
//      pt.offset(perp);    
    }
    makeControlPts();
    // Now fire a propertyChange for all the points after they are moved
    // so that the Node is updated properly
    for(Pt pt : ptList) {
      pt.fireDrag();
    }
  }

  /**
   * Perpendicular vector at a given dot
   *
   * @param i index of desire dot
   * @param dir Direction: true = FRONT_INSIDE or BACK_OUTSIDE, false =
   * FRONT_OUTSIDE or BACK_INSIDE
   * @return normalized perpendicular vector at the dot (null if index is
   * invalid or only 1 point)
   */
  private Vector2d perpendicularAt(int i, boolean dir) {
    Vector2d v = null;
    if ((i >= pAng.length) || (i < 0)) {
      return v;
    }
    if (pAng.length < 2) {
      return v;
    }
    if (dir) {
      v = new Vector2d(Math.sin(pAng[i]), -Math.cos(pAng[i]));
    } else {
      v = new Vector2d(-Math.sin(pAng[i]), Math.cos(pAng[i]));
    }
    v.normalize();
    return v;
  }

  /**
   * Get the x-value for a given y-value by interpolation. Note: this assumes
   * the points are in sorted-by-Y order.
   *
   * @param y y-value
   * @return x-value (or zero if there are no points).
   */
  @Override
  public double getXforY(double y) {
    if (ptList.isEmpty()) {
      return 0.0;
    }
    if (y <= ptList.get(0).getY()) {			// less than first point
      return ptList.get(0).getX();
    }
    if (y >= getLastPt().getY()) {			// more than last point
      return getLastPt().getX();
    }
    double xmin, xmax, ymin, ymax;
    xmin = ptList.get(0).getX();
    ymin = ptList.get(0).getY();
    xmax = getLastPt().getX();
    ymax = getLastPt().getY();
    if (ptList.size() == 2) {				// straight line for 2 points
      return (y - ymin) / (ymax - ymin) * (xmax - xmin) + xmin;
    }
    if (ptSpacing == 0.0) {
      ptSpacing = Math.abs((ymax - ymin) / 100);
    }
    Point2D.Double[] pts = buildCurvePoints(ptSpacing);
    for (int i = 1; i < pts.length; i++) {      // point is somewhere between
      xmin = pts[i - 1].x;
      xmax = pts[i].x;
      ymin = pts[i - 1].y;
      ymax = pts[i].y;
      if ((y >= ymin) && (y < ymax)) {
        return (y - ymin) / (ymax - ymin) * (xmax - xmin) + xmin;
      }
    }
    return 0.0;		// should never get here
  }

  /**
   * Get the y-value for a given x-value by interpolation. Note: this assumes
   * the points are in sorted-by-X order.
   *
   * @param x x-value
   * @return y-value (or zero if there are no points).
   */
  @Override
  public double getYforX(double x) {
    if (ptList.isEmpty()) {
      return 0.0;
    }
    if (x <= ptList.get(0).getX()) {			// less than first point
      return ptList.get(0).getY();
    }
    if (x >= getLastPt().getX()) {			// more than last point
      return getLastPt().getY();
    }
    double xmin, xmax, ymin, ymax;
    xmin = ptList.get(0).getX();
    ymin = ptList.get(0).getY();
    xmax = getLastPt().getX();
    ymax = getLastPt().getY();
    if (ptList.size() == 2) {				// straight line for 2 points
      return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
    }
    if (ptSpacing == 0.0) {
      ptSpacing = Math.abs((ymax - ymin) / 100);
    }
    Point2D.Double[] pts = buildCurvePoints(ptSpacing);
    for (int i = 1; i < pts.length; i++) {      // point is somewhere between
      xmin = pts[i - 1].x;
      xmax = pts[i].x;
      ymin = pts[i - 1].y;
      ymax = pts[i].y;
      if ((x >= xmin) && (x < xmax)) {
        return (x - xmin) / (xmax - xmin) * (ymax - ymin) + ymin;
      }
    }
    return 0.0;		// should never get here
  }

}
