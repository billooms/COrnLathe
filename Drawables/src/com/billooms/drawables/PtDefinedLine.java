package com.billooms.drawables;

import com.billooms.drawables.vecmath.Vector2d;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

/**
 * Abstract definition of a line or curve defined by a number of points.
 *
 * Points are ordered, bottom first and top last.
 *
 * In addition to color, visible flag, and stroke, a PtDefinedLine has a
 * PointList of Points.
 *
 * Extend this to define lines with particular characteristics (i.e.
 * FittedCurve, PiecedLine, etc).
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public abstract class PtDefinedLine implements Drawable, PropertyChangeListener {

  /** All PtDefinedLine property change names start with this prefix. */
  public final static String PROP_PREFIX = "PtLine" + "_";
  /** Property name used when changing the color. */
  public final static String PROP_COLOR = PROP_PREFIX + "Color";
  /** Property name used when changing the visible flag. */
  public final static String PROP_VISIBLE = PROP_PREFIX + "Visible";
  /** Property name used when changing the stroke. */
  public final static String PROP_STROKE = PROP_PREFIX + "Stroke";
  /** Property name used when adding or deleting points. */
  public final static String PROP_POINT = PROP_PREFIX + "Point";
  /** Property name used when changing multiple points. */
  public final static String PROP_MULTI = PROP_PREFIX + "MultiPoint";

  private final static Color DEFAULT_COLOR = Color.RED;	  // You'll want to over-ride this

  /** The color of the drawn point. */
  private Color color = DEFAULT_COLOR;
  /** Flag indicating if the grid is visible or not. */
  private boolean visible = true;
  /** Stroke used for the line. */
  private BasicStroke stroke = SOLID_LINE;

  /** The list of points */
  protected ArrayList<Pt> ptList = new ArrayList<>();

  /** All PtDefinedLine objects can fire propertyChanges. */
  protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /**
   * A line or curve defined by a number of points.
   *
   * @param c Color
   * @param s Stroke
   */
  public PtDefinedLine(Color c, BasicStroke s) {
    this.color = c;
    this.stroke = s;
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {		// This is further customized by extensions
    if (visible) {
      ptList.stream().forEach((pt) -> {
        pt.paint(g2d);
      });
      g2d.setColor(getColor());
      float scale = (float) g2d.getTransform().getScaleX();
      if (scale < 0.0) {
        return;		// not sure why this happens sometimes
      }
      float array[] = getStroke().getDashArray();
      if ((array == null) || (array.length == 0)) {
        g2d.setStroke(new BasicStroke(1.0f / scale));
      } else {
        for (int i = 0; i < array.length; i++) {
          array[i] = array[i] / scale;
        }
        g2d.setStroke(new BasicStroke(1.0f / scale, getStroke().getEndCap(), getStroke().getLineJoin(), 1.0f, array, getStroke().getDashPhase() / scale));
      }
    }
  }

  @Override
  public BasicStroke getStroke() {
    return stroke;
  }

  @Override
  public synchronized void setStroke(BasicStroke s) {
    BasicStroke old = this.stroke;
    this.stroke = s;
    pcs.firePropertyChange(PROP_STROKE, old, s);
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public synchronized void setColor(Color c) {
    Color old = this.color;
    this.color = c;
    pcs.firePropertyChange(PROP_COLOR, old, c);
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public synchronized void setVisible(boolean v) {
    boolean old = this.visible;
    this.visible = v;
    pcs.firePropertyChange(PROP_VISIBLE, old, v);
  }

  /**
   * Clear the point list.
   *
   * This fires a PROP_MULTI property change with nulls.
   */
  public synchronized void clear() {
    ptList.stream().forEach(pt -> pt.removePropertyChangeListener(this));
    ptList.clear();
    update();
    pcs.firePropertyChange(PROP_MULTI, null, null);
  }

  /**
   * Determine if the list of points is empty.
   *
   * @return true if there are no points
   */
  public boolean isempty() {
    return ptList.isEmpty();
  }

  /**
   * Get the number of dots defining the curve.
   *
   * @return The number of dots
   */
  public int getSize() {
    return ptList.size();
  }

  /**
   * Get all points.
   *
   * @return list of all points
   */
  public ArrayList<Pt> getAllPoints() {
    return ptList;
  }

  /**
   * Replace the points with the given list of new points.
   *
   * This fires a PROP_MULTI property change with the new list.
   *
   * @param newPoints new points
   */
  public synchronized void setAllPoints(ArrayList<Pt> newPoints) {
    clear();	  // gets will quit listening to the old points
    ptList = newPoints;
    update();
    ptList.stream().forEach(pt -> pt.addPropertyChangeListener(this));  // listen for changes in the points
    pcs.firePropertyChange(PROP_MULTI, null, newPoints);
  }

  /**
   * Get a point from the point list.
   *
   * @param idx Index of the desired point
   * @return the point or null if idx >= size() or is negative
   */
  public Pt getPt(int idx) {
    if ((idx < 0) || (idx >= ptList.size())) {
      return null;
    }
    return ptList.get(idx);
  }

  /**
   * Get the last (that is, the uppermost) point.
   *
   * @return the point
   */
  public Pt getLastPt() {
    return getPt(ptList.size() - 1);
  }

  /**
   * Add a point at the end (that is, the top) of the curve.
   *
   * This fires a PROP_POINT property change with the new point.
   *
   * @param pt new point
   */
  public synchronized void addPt(Pt pt) {
    ptList.add(pt);
    update();
    pcs.firePropertyChange(PROP_POINT, null, pt);
    pt.addPropertyChangeListener(this);
  }

  /**
   * Insert a point at the correct location in the list (sorted by
   * y-coordinate).
   *
   * This fires a PROP_POINT property change with the new point.
   *
   * Note: This assumes that all points have been added with insertPtY() so that
   * other points are in sorted-by-Y order.
   *
   * @param newPt the point to be inserted
   */
  public synchronized void insertPtY(Pt newPt) {
    if (ptList.isEmpty()) {
      ptList.add(newPt);
    } else {
      double newYInch = newPt.getY();
      if (newYInch < ptList.get(0).getY()) {      // point is below the bottom
        ptList.add(0, newPt);
      } else if (newYInch >= getLastPt().getY()) {        // point is above the top
        ptList.add(newPt);
      } else {
        for (int i = 1; i < ptList.size(); i++) {      // point is somewhere between
          if ((newYInch >= ptList.get(i - 1).getY()) && (newYInch < ptList.get(i).getY())) {
            ptList.add(i, newPt);
            break;
          }
        }
      }
    }
    update();
    pcs.firePropertyChange(PROP_POINT, null, newPt);
    newPt.addPropertyChangeListener(this);
  }

  /**
   * Insert a point at the correct location in the list (sorted by
   * x-coordinate).
   *
   * This fires a PROP_POINT property change with the new point.
   *
   * Note: This assumes that all points have been added with insertPtX() so that
   * other points are in sorted-by-X order.
   *
   * @param newPt the point to be inserted
   */
  public synchronized void insertPtX(Pt newPt) {
    if (ptList.isEmpty()) {
      ptList.add(newPt);
    } else {
      double newXInch = newPt.getX();
      if (newXInch < ptList.get(0).getX()) {      // point is left of the first point
        ptList.add(0, newPt);
      } else if (newXInch >= getLastPt().getX()) {        // point is right of the last point
        ptList.add(newPt);
      } else {
        for (int i = 1; i < ptList.size(); i++) {      // point is somewhere between
          if ((newXInch >= ptList.get(i - 1).getX()) && (newXInch < ptList.get(i).getX())) {
            ptList.add(i, newPt);
            break;
          }
        }
      }
    }
    update();
    pcs.firePropertyChange(PROP_POINT, null, newPt);
    newPt.addPropertyChangeListener(this);
  }

  /**
   * Delete a given point.
   *
   * This fires a PROP_POINT property change with the old point.
   *
   * @param pt point to be deleted
   * @return true if the point was removed
   */
  public synchronized boolean deletePt(Pt pt) {
    pt.clear();	  // doesn't do anything, but it may in the future
    pt.removePropertyChangeListener(this);
    Boolean ok = ptList.remove(pt);
    update();
    pcs.firePropertyChange(PROP_POINT, pt, null);
    return ok;
  }

  /**
   * Find the closest point (within dis) to the given point.
   *
   * @param given given point
   * @param dis distance to measure
   * @return the closest point (within dis), null if no point is within dis
   */
  public Pt closestPt(Point2D.Double given, double dis) {
    Pt closest = null;
    double minSep = dis;
    for (Pt p : ptList) {
      double sep = p.distance(given);
      if (sep < minSep) {
        minSep = sep;
        closest = p;
      }
    }
    return closest;
  }

  /**
   * Get the bounding box for the points defining this shape.
   *
   * @return bounding box (which might not include 0.0, 0.0)
   */
  public BoundingBox getBoundingBox() {
    if (ptList.isEmpty()) {
      return new BoundingBox(0.0, 0.0, 0.0, 0.0);
    }
    Pt p0 = ptList.get(0);
    double minX = p0.getX(), maxX = p0.getX(), minY = p0.getY(), maxY = p0.getY();
    for (Pt pt : ptList) {
      double py = pt.getY();
      if (py < minY) {
        minY = py;
      }
      if (py > maxY) {
        maxY = py;
      }
      double px = pt.getX();
      if (px > maxX) {
        maxX = px;
      }
      if (px < minX) {
        minX = px;
      }
    }
    return new BoundingBox(minX, minY, maxX, maxY);
  }

  /**
   * Invert the points around zero and reverse the order of the points.
   *
   * Note: Each point will fire a propertyChange.
   */
  public synchronized void invert() {
    ArrayList<Pt> newList = new ArrayList<>();
    for (int i = ptList.size() - 1; i >= 0; i--) {
      Pt p = ptList.get(i);
      p.invert();
      newList.add(p);		// make a new list with the points in reverse order
    }
    ptList = newList;
    update();
  }

  /**
   * Offset in the y-direction by subtracting the given value from y-coordinate.
   *
   * Note: Each point will fire a propertyChange.
   *
   * @param deltaY offset amount (in inches);
   */
  public synchronized void offsetY(double deltaY) {
    ptList.stream().forEach(pt -> pt.offsetY(deltaY));
    update();
  }

  /**
   * Offset in the x-direction by subtracting the given value from x-coordinate.
   *
   * Note: Each point will fire a propertyChange.
   *
   * @param deltaX offset amount (in inches);
   */
  public synchronized void offsetX(double deltaX) {
    ptList.stream().forEach(pt -> pt.offsetX(deltaX));
    update();
  }

  /**
   * Offset by subtracting the given values from the coordinates.
   *
   * Note: Each point will fire a propertyChange.
   *
   * @param delta offset amount (in inches);
   */
  public synchronized void offset(Vector2d delta) {
    ptList.stream().forEach(pt -> pt.offset(delta));
    update();
  }

  /**
   * Scale all coordinates by the given vector.
   *
   * Note: Each point will fire a propertyChange.
   *
   * @param scaleX x-scale factor
   * @param scaleY y-scale factor
   */
  public synchronized void scale(double scaleX, double scaleY) {
    ptList.stream().forEach(pt -> pt.scale(scaleX, scaleY));
    update();
  }

  /**
   * Scale all x-coordinates by the given factor.
   *
   * Note: Each point will fire a propertyChange.
   *
   * @param factor scale factor
   */
  public synchronized void scaleX(Double factor) {
    ptList.stream().forEach(pt -> pt.scaleX(factor));
    update();
  }

  /**
   * Get the x-value for a given y-value by interpolation.
   *
   * Note: this assumes the points are in sorted-by-Y order.
   *
   * @param y y-value
   * @return x-value (or zero if there are no points).
   */
  public abstract double getXforY(double y);

  /**
   * Get the y-value for a given x-value by interpolation.
   *
   * Note: this assumes the points are in sorted-by-X order.
   *
   * @param x x-value
   * @return y-value (or zero if there are no points).
   */
  public abstract double getYforX(double x);

  /**
   * Update the curve fitting as required
   */
  protected abstract void update();

  /**
   * Add the given listener to this object.
   *
   * @param listener
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Remove the given listener to this object.
   *
   * @param listener
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PtDefinedLine.propertyChange: " + evt.getSource() + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // watch for points asking to be to be deleted
    if (evt.getPropertyName().equals(Pt.PROP_REQ_DELETE)) {
      if (evt.getSource() instanceof Pt) {
        deletePt((Pt) evt.getSource());
      }
    } else {
      update();
      // pass the info through
      pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }
}
