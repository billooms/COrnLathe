package com.billooms.drawables;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.drawables.vecmath.Vector2d;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import javafx.geometry.Point3D;
import org.w3c.dom.Element;

/**
 * A Point in 3D space.
 *
 * A Pt is an abstract extension to CLclass that can be constructed as an XYZ
 * point, or an XY or XZ point (where the other coordinate is always zero).
 *
 * The point is Drawable in 2D space (using either XY or XZ as appropriate). The
 * specific appearance is provided by extensions to this abstract class. Any
 * dragging operation or mass change of points will issue a "Drag"
 * propertyChange so that listeners can choose to ignore them for the purpose of
 * avoiding time consuming operations (like 3D rendering). One should always
 * fire a non-Drag propertyChange when all the dragging (or mass change of
 * points) so that the listener can take the appropriate final action.
 *
 * @author Bill Ooms Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public abstract class Pt extends CLclass implements Drawable {

  /** All Pt property change names start with this prefix. */
  public final static String PROP_PREFIX = "Pt" + "_";
  /** Property name used when changing the color. */
  public final static String PROP_COLOR = PROP_PREFIX + "Color";
  /** Property name used when changing the visible flag. */
  public final static String PROP_VISIBLE = PROP_PREFIX + "Visible";
  /** Property name used when changing the x-coordinate. */
  public final static String PROP_X = PROP_PREFIX + "X";
  /** Property name used when changing the y-coordinate. */
  public final static String PROP_Y = PROP_PREFIX + "Y";
  /** Property name used when changing the z-coordinate. */
  public final static String PROP_Z = PROP_PREFIX + "Z";
  /** Property name used when changing multi coordinates. */
  public final static String PROP_MOVE = PROP_PREFIX + "Move";
  /** Property name used when dragging a point or other mass operations. */
  public final static String PROP_DRAG = PROP_PREFIX + "Drag";
  /** Property name used when requesting deletion. */
  public final static String PROP_REQ_DELETE = PROP_PREFIX + "requestDelete";

  private final static Color DEFAULT_COLOR = Color.RED;	  // You'll want to over-ride this

  /** Style of the point */
  public static enum Style {

    /** The point uses all of XYZ. */
    XYZ,
    /** The point uses only XY and Z is always
     * zero. */
    XY,
    /** The point uses only XZ and Y is always
     * zero. */
    XZ,
    /** The point uses two Y
     * values associated with each X. */
    XYY
  }

  /** The color of the drawn point. */
  private Color color = DEFAULT_COLOR;
  /** Flag indicating if the grid is visible or not. */
  private boolean visible = true;
  /** x-coordinate. */
  private double x = 0.0;
  /** y-coordinate. */
  private double y = 0.0;
  /** z-coordinate. */
  private double z = 0.0;
  /** the style of point */
  private final Style style;

  /**
   * Construct a new XYZ Pt.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @param z z-coordinate
   */
  public Pt(double x, double y, double z) {
    this.style = Style.XYZ;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Construct a new Pt with the given coordinates and style.
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @param z z-coordinate
   * @param style Style of point
   */
  public Pt(double x, double y, double z, Style style) {
    this.style = style;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Construct a new XY, XZ, or XYY Pt. Note that for XYY the y2 value will be
   * set to zero.
   *
   * @param x x-coordinate
   * @param yz y-coordinate or z-coordinate
   * @param style XY or XZ
   */
  public Pt(double x, double yz, Style style) {
    this.style = style;
    this.x = x;
    if (style == Style.XY || style == Style.XYY) {
      this.y = yz;
    } else {
      this.z = yz;
    }
  }

  /**
   * Construct the Pt from an XML DOM Element.
   *
   * @param element XML DOM Element
   * @param style XYZ, XY, or XZ
   */
  public Pt(Element element, Style style) {
    this.style = style;
    this.x = CLUtilities.getDouble(element, "x", 0.0);
    if (style == Style.XY || style == Style.XYZ || style == Style.XYY) {
      this.y = CLUtilities.getDouble(element, "y", 0.0);
    }
    if (style == Style.XZ || style == Style.XYZ) {
      this.z = CLUtilities.getDouble(element, "z", 0.0);
    }
    if (style == Style.XYY) {
      this.z = CLUtilities.getDouble(element, "y2", 0.0);
    }
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
   * Get x-coordinate.
   *
   * @return x-coordinate
   */
  public double getX() {
    return x;
  }

  /**
   * Set x-coordinate. Fires a PROP_X propertyChange with old and new
   * coordinates.
   *
   * @param x new x-coordinate
   */
  public synchronized void setX(double x) {
    double old = this.x;
    this.x = x;
    pcs.firePropertyChange(PROP_X, old, x);
  }

  /**
   * Get y-coordinate.
   *
   * @return y-coordinate
   */
  public double getY() {
    return y;
  }

  /**
   * Set y-coordinate. Fires a PROP_Y propertyChange with old and new
   * coordinates.
   *
   * @param y new y-coordinate
   */
  public synchronized void setY(double y) {
    double old = this.y;
    this.y = y;
    pcs.firePropertyChange(PROP_Y, old, y);
  }

  /**
   * Get z-coordinate.
   *
   * @return z-coordinate
   */
  public double getZ() {
    return z;
  }

  /**
   * Set z-coordinate. Fires a PROP_Z propertyChange with old and new
   * coordinates.
   *
   * @param z new z-coordinate
   */
  public synchronized void setZ(double z) {
    double old = this.z;
    this.z = z;
    pcs.firePropertyChange(PROP_Z, old, z);
  }

  /**
   * Get y2-coordinate.
   *
   * @return y2-coordinate
   */
  public double getY2() {
    return z;
  }

  /**
   * Set y2-coordinate. Fires a PROP_Z propertyChange with old and new
   * coordinates.
   *
   * @param y2 new y2-coordinate
   */
  public synchronized void setY2(double y2) {
    double old = this.z;
    this.z = y2;
    pcs.firePropertyChange(PROP_Z, old, this.z);
  }

  /**
   * Move the point to the new position. Always fires a PROP_MOVE propertyChange
   * with new coordinates.
   *
   * @param pt new position
   */
  public synchronized void move(Point2D.Double pt) {
    this.x = pt.x;
    if (style == Style.XY || style == Style.XYY) {
      this.y = pt.y;
    }
    if (style == Style.XZ) {
      this.z = pt.y;
    }
    // always fire because move after drag may be same coordinates
    pcs.firePropertyChange(PROP_MOVE, null, pt);
  }

  /**
   * Move the point to the new position. Always fires a PROP_MOVE
   * propertyChange.
   *
   * @param x new x-coordinate
   * @param yz new y or z-coordinate (depending on style)
   */
  public synchronized void move(double x, double yz) {
    this.x = x;
    if (style == Style.XY || style == Style.XYY) {
      this.y = yz;
    }
    if (style == Style.XZ) {
      this.z = yz;
    }
    // always fire because move after drag may be same coordinates
    pcs.firePropertyChange(PROP_MOVE, null, null);
  }

  /**
   * Drag the point to the new position.
   *
   * Note that this is just like doing move() except that it fires a PROP_DRAG
   * property change. This is useful to prevent time consuming calculations in
   * some circumstances (such as 3D rendering). At the conclusion of a drag
   * operation, one should always do a final call to move().
   *
   * @param pt new position
   */
  public synchronized void drag(Point2D.Double pt) {
    this.x = pt.x;
    if (style == Style.XY || style == Style.XYY) {
      this.y = pt.y;
    }
    if (style == Style.XZ) {
      this.z = pt.y;
    }
    pcs.firePropertyChange(PROP_DRAG, null, pt);
  }

  /**
   * Get the style of the point.
   *
   * @return XYZ, XY, or XZ
   */
  protected Style getStyle() {
    return style;
  }

  /**
   * Get the coordinates as a Point2D.
   *
   * Note: If the style is XZ, then the Point2D will contain the x and z
   * coordinates.
   *
   * @return new Point2D
   */
  public Point2D.Double getPoint2D() {
    return new Point2D.Double(x, (style == Style.XZ) ? z : y);
  }

  /**
   * Get the coordinates as a Point3D.
   *
   * @return new Point3D
   */
  public Point3D getPoint3D() {
    return new Point3D(x, y, z);
  }

  @Override
  public String toString() {
    String str = "Point" + style.toString() + "{" + F3.format(x);
    if (style == Style.XY || style == Style.XYZ || style == Style.XYY) {
      str += ", " + F3.format(y);
    }
    if (style == Style.XZ || style == Style.XYZ || style == Style.XYY) {
      str += ", " + F3.format(z);
    }
    str += '}';
    return str;
  }

  /**
   * Clear the Pt.
   */
  public void clear() {
    // do nothing
  }

  /**
   * Requests that this Pt be deleted by interested PropertyChangeListeners.
   */
  public void requestDelete() {
    pcs.firePropertyChange(PROP_REQ_DELETE, null, null);
  }

  @Override
  public void writeXML(PrintWriter out) {
    String yz = "";
    if (style == Style.XY || style == Style.XYZ || style == Style.XYY) {
      yz += " y='" + F4.format(y) + "'";
    }
    if (style == Style.XZ || style == Style.XYZ) {
      yz += " z='" + F4.format(z) + "'";
    }
    if (style == Style.XYY) {
      yz += " y2='" + F4.format(z) + "'";
    }
    out.println(indent + "<Pt"
        + " x='" + F4.format(x) + "'"
        + yz
        + "/>");
  }

  /**
   * Get the distance from this point to the given point.
   *
   * @param given given point
   * @return distance in inches
   */
  public synchronized double distance(Point2D.Double given) {
    return getPoint2D().distance(given);
  }

  /**
   * Invert the point around the y-axis (for XY points) or z-axis (for XZ or
   * XYZ).
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with old and new coordinates.
   */
  public synchronized void invert() {
    double old = 0.0;
    switch (style) {
      case XY:
        old = this.y;
        y = -y;
        pcs.firePropertyChange(PROP_DRAG, old, y);
        break;
      case XYY:
        old = this.y;
        y = -y;
        z = -z;
        pcs.firePropertyChange(PROP_DRAG, old, y);
        break;
      case XZ:
      case XYZ:
        old = this.z;
        z = -z;
        pcs.firePropertyChange(PROP_DRAG, old, z);
        break;
    }
  }

  /**
   * Scale all coordinates by the given factor.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change.
   *
   * @param factor scale factor;
   */
  public synchronized void scale(double factor) {
    x = x * factor;
    y = y * factor;
    z = z * factor;
    pcs.firePropertyChange(PROP_DRAG, null, null);
  }

  /**
   * Offset in the x-direction by subtracting the given value from x-coordinate.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with old and new x-coordinates.
   *
   * @param deltaX offset amount (in inches);
   */
  public synchronized void offsetX(double deltaX) {
    double old = this.x;
    x = x - deltaX;
    pcs.firePropertyChange(PROP_DRAG, old, x);
  }

  /**
   * Offset in the y-direction by subtracting the given value from y-coordinate.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with old and new y-coordinates.
   *
   * @param deltaY offset amount (in inches);
   */
  public synchronized void offsetY(double deltaY) {
    double old = this.y;
    y = y - deltaY;
    pcs.firePropertyChange(PROP_DRAG, old, y);
  }

  /**
   * Offset in the z-direction by subtracting the given value from z-coordinate.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with old and new z-coordinates.
   *
   * @param deltaZ offset amount (in inches);
   */
  public synchronized void offsetZ(double deltaZ) {
    double old = this.z;
    z = z - deltaZ;
    pcs.firePropertyChange(PROP_DRAG, old, z);
  }

  /**
   * Offset in the y2-direction by subtracting the given value from
   * y2-coordinate.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with old and new z-coordinates.
   *
   * @param deltaY2 offset amount (in inches);
   */
  public synchronized void offsetY2(double deltaY2) {
    double old = this.z;
    z = z - deltaY2;
    pcs.firePropertyChange(PROP_DRAG, old, this.z);
  }

  /**
   * Offset by subtracting the given values from the coordinates.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with the old and new position.
   *
   * @param delta offset amount (in inches);
   */
  public synchronized void offset(Vector2d delta) {
    Point2D.Double old = getPoint2D();
    x = x - delta.x;
    y = y - delta.y;
    pcs.firePropertyChange(PROP_DRAG, old, getPoint2D());
  }

  /**
   * Offset by subtracting the given values from the coordinates. Note: This
   * does NOT fire any propertyChange events!
   *
   * @param delta offset amount (in inches);
   */
  public synchronized void offsetQuiet(Vector2d delta) {
    x = x - delta.x;
    y = y - delta.y;
  }
  
  /**
   * This fires a PROP_DRAG property change with the new position.
   * Use this as needed after using offsetQuiet(). 
   */
  public synchronized void fireDrag() {
    pcs.firePropertyChange(PROP_DRAG, null, getPoint2D());
  }

  /**
   * Scale the coordinates by the given factor.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with the old and new position.
   *
   * @param scaleX x-scale factor
   * @param scaleY y-scale factor
   */
  public synchronized void scale(double scaleX, double scaleY) {
    Point2D.Double old = getPoint2D();
    x = x * scaleX;
    y = y * scaleY;
    if (style == Style.XYY) {
      z = z * scaleY;
    }
    pcs.firePropertyChange(PROP_DRAG, old, getPoint2D());
  }

  /**
   * Scale the x-coordinates by the given factor.
   *
   * Because this is usually done on multiple points at one time, it fires a
   * PROP_DRAG property change with the old and new position.
   *
   * @param scaleX x-scale factor
   */
  public synchronized void scaleX(double scaleX) {
    double old = x;
    x = x * scaleX;
    pcs.firePropertyChange(PROP_DRAG, old, x);
  }
}
