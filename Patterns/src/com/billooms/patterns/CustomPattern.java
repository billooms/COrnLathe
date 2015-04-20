package com.billooms.patterns;

import com.billooms.clclass.CLUtilities;
import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.drawables.ArcSegLine;
import com.billooms.drawables.BoundingBox;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.FittedCurve;
import com.billooms.drawables.PiecedLine;
import com.billooms.drawables.Pt;
import com.billooms.drawables.PtDefinedLine;
import com.billooms.drawables.SquarePt;
import com.billooms.drawables.SquarePt2;
import com.billooms.drawables.TrigSegLine;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Custom pattern for a rosette.
 *
 * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class CustomPattern extends BasicPattern {

  /** All CustomPattern property change names start with this prefix. */
  public final static String PROP_PREFIX = "Pattern" + "_";
  /** Property name used when changing dual flag. */
  public final static String PROP_DUAL = PROP_PREFIX + "Dual";
  /** Property name used when adding a point. */
  public final static String PROP_ADDPT = PROP_PREFIX + "AddPt";
  /** Property name used when deleting a point. */
  public final static String PROP_DELETEPT = PROP_PREFIX + "DeletePt";
  /** Property name used when clearing all points. */
  public final static String PROP_CLEAR = PROP_PREFIX + "Clear";
  /** Property name used when changing the name or display name.. */
  public final static String PROP_NAME = PROP_PREFIX + "Name";
  /** Property name used when requesting deletion. */
  public final static String PROP_REQ_DELETE = PROP_PREFIX + "requestDelete";

  /** Different styles of custom pattern */
  public static enum CustomStyle {

    /** Straight line segments between points */
    STRAIGHT,
    /** Every 3
     * points is a curve fit segment with odd points shared */
    ARCS,
    /** Curve fit between points */
    CURVE,
    /** Quarter sine/cosine segments */
    TRIG
  }

  /** Tolerance for checking normalization. */
  private final static double TOLERANCE = .0002;
  /** Default line style. */
  private final static CustomStyle DEFAULT_STYLE = CustomStyle.STRAIGHT;
  /** Color for optional 2nd dot. */
  public final static Color IMAGE_COLOR2 = Color.GREEN;

  /** Either STRAIGHT, ARCS, CURVE, or TRIG. */
  private CustomStyle customStyle;
  /** Flag indicating a dual line pattern. */
  private boolean dual = false;
  /** A 2nd line used for display in the pattern editor. */
  private PtDefinedLine line2 = null;

  /**
   * Create a Fitted Line pattern with the given display name.
   *
   * @param displayName display name
   * @param st Style of the pattern
   */
  public CustomPattern(String displayName, CustomStyle st) {
    super(displayName);
    this.customStyle = st;
    switch (st) {
      case STRAIGHT:
        line = new PiecedLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case ARCS:
        line = new ArcSegLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case TRIG:
        line = new TrigSegLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case CURVE:
        line = new FittedCurve(IMAGE_COLOR, SOLID_LINE);
        break;
    }
    builtIn = false;	// this is not a built-in pattern
    line.addPropertyChangeListener(this); // listen to the curve, not to the individual points
  }

  /**
   * Create a new custom pattern from the given DOM Element.
   *
   * @param element DOM Element
   */
  public CustomPattern(Element element) {
    super(CLUtilities.getString(element, "displayName", "No Name"));
    builtIn = false;	// this is not a built-in pattern
    this.name = CLUtilities.getString(element, "name", "NONAME");   // in case it's different
    this.customStyle = CLUtilities.getEnum(element, "style", CustomStyle.class, DEFAULT_STYLE);
    this.dual = CLUtilities.getBoolean(element, "dual", false);
    switch (customStyle) {
      case STRAIGHT:
        line = new PiecedLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case ARCS:
        line = new ArcSegLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case TRIG:
        line = new TrigSegLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case CURVE:
        line = new FittedCurve(IMAGE_COLOR, SOLID_LINE);
        break;
    }
    NodeList ptNodes = element.getChildNodes();
    for (int i = 0; i < ptNodes.getLength(); i++) {
      if (ptNodes.item(i) instanceof Element) {
        Element ptElement = (Element) ptNodes.item(i);
        if (ptElement.getTagName().equals("Pt")) {	// Read data points
          if (dual) {
            line.addPt(new SquarePt2(ptElement, IMAGE_COLOR, IMAGE_COLOR2));  // Add points in order they were read
          } else {
            line.addPt(new SquarePt(ptElement, IMAGE_COLOR));	// Add points in order they were read
          }
        }
      }
    }
    updateImage();
    line.addPropertyChangeListener(this); // listen to the curve, not to the individual points
  }

  /**
   * Update the image.
   */
  private void updateImage() {
    image = null;	// this forces it to be generated again
    getImage();
  }

  /**
   * Get a string describing this object
   *
   * @return string name
   */
  @Override
  public String toString() {
    return super.toString() + ": " + customStyle.toString() + (dual ? " dual" : "");
  }

  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);
    if (dual) {
      if (line2 == null) {
        buildLine2();
      }
      line2.paint(g2d);
    }
  }

  /**
   * Line2 is constructed on demand for painting. It was set to null when a
   * point was changed.
   */
  private void buildLine2() {
    if (line2 != null) {
      return;
    }
    switch (customStyle) {
      case STRAIGHT:
        line2 = new PiecedLine(IMAGE_COLOR2, SOLID_LINE);
        break;
      case ARCS:
        line2 = new ArcSegLine(IMAGE_COLOR2, SOLID_LINE);
        break;
      case TRIG:
        line2 = new TrigSegLine(IMAGE_COLOR2, SOLID_LINE);
        break;
      case CURVE:
        line2 = new FittedCurve(IMAGE_COLOR2, SOLID_LINE);
        break;
    }
    ArrayList<Pt> allPoints = line.getAllPoints();
    for (Pt pt : allPoints) {
      line2.addPt(new SquarePt(new Point2D.Double(pt.getX(), pt.getZ()), IMAGE_COLOR2));
    }
  }

  /**
   * Get the style of the CustomPattern.
   *
   * @return style
   */
  public CustomStyle getCustomStyle() {
    return customStyle;
  }

  /**
   * Determine if this pattern is a dual line pattern.
   *
   * @return true: dual line pattern
   */
  public boolean isDual() {
    return dual;
  }

  /**
   * Set the dual line pattern flag.
   *
   * @param dual new flag
   */
  public void setDual(boolean dual) {
    boolean old = this.dual;
    this.dual = dual;
    if (this.dual != old) {
      if (this.dual) {
        convertToPtXYY();
      } else {
        convertToPtXY();
      }
      pcs.firePropertyChange(PROP_DUAL, old, this.dual);
    }
  }

  /**
   * Convert line from dual points to single points
   */
  private void convertToPtXY() {
    ArrayList<Pt> pts = new ArrayList<>();
    pts.addAll(line.getAllPoints());
    if (!pts.isEmpty()) {
      line.clear();
      pts.stream().forEach((p) -> {
        line.addPt(new SquarePt(p.getPoint2D(), IMAGE_COLOR));
      });
    }
  }

  /**
   * Convert line from single points to dual points
   */
  private void convertToPtXYY() {
    ArrayList<Pt> pts = new ArrayList<>();
    pts.addAll(line.getAllPoints());
    if (!pts.isEmpty()) {
      line.clear();
      pts.stream().forEach((p) -> {
        line.addPt(new SquarePt2(p.getPoint2D(), IMAGE_COLOR, IMAGE_COLOR2));
      });
    }
  }

  /**
   * Set a new name (all capital letters).
   *
   * Convert everything to uppercase, delete anything after the first blank, and
   * remove anything that is not an uppercase. This fires a PROP_NAME property
   * change with old an new names.
   *
   * @param newName new name
   */
  public synchronized void setName(String newName) {
    String old = this.name;
    this.name = filterName(newName);
    pcs.firePropertyChange(PROP_NAME, old, this.name);
  }

  /**
   * Set a new display name.
   *
   * This fires a PROP_NAME property change with old an new names.
   *
   * @param newName new display name
   */
  public synchronized void setDisplayName(String newName) {
    String old = this.displayName;
    this.displayName = newName;
    pcs.firePropertyChange(PROP_NAME, old, this.displayName);
  }

  /**
   * Get the points of the line defining the profile.
   *
   * @return list of points, an empty list if no points
   */
  public synchronized List<Pt> getAllPoints() {
    return line.getAllPoints();
  }

  /**
   * Get the points representing the 2nd line.
   *
   * @return list of points, an empty list if no points
   */
  public List<Pt> getAllPoints2() {
    if (isDual()) {
      if (line2 == null) {
        buildLine2();
      }
      return line2.getAllPoints();
    }
    return new ArrayList<>();
  }

  /**
   * Get the line object.
   *
   * @return the line object
   */
  public PtDefinedLine getLine() {
    return line;
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  @Override
  public double getValue(double n) {
    return line.getYforX(n);
  }

  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1) on the 2nd line.
   *
   * @param n given value in the range of 0.0 to 1.0
   * @return value in the range of 0.0 to 1.0
   */
  public double getValue2(double n) {
    if (isDual()) {
      if (line2 == null) {
        buildLine2();
      }
      return line2.getYforX(n);
    }
    return 0.0;
  }

  /**
   * Clear the line of all points and quit listening to the line.
   */
  public synchronized void clearAll() {
    line.removePropertyChangeListener(this);
    clear();
  }

  /**
   * Clear the line of all points but keep listening to the line in case more
   * points are added.
   */
  public synchronized void clear() {
    line.clear();	// the line quits listening to the old points
    updateImage();
    // However, this CustomProfile still listens to changes in the empty curve
    // so that as points are added this can respond.
  }

  /**
   * Add a new point to the curve.
   *
   * @param pt location of new point
   */
  public synchronized void addPt(Point2D.Double pt) {
    pt.x = Math.min(pt.x, 1.0);
    pt.y = Math.min(pt.y, 1.0);
    pt.x = Math.max(pt.x, 0.0);
    pt.y = Math.max(pt.y, 0.0);
    line.insertPtX(new SquarePt(pt, IMAGE_COLOR));  // line will fire propertyChangeEvent
  }

  /**
   * Delete the given point.
   *
   * @param pt point to delete
   */
  public synchronized void deletePt(Pt pt) {
    line.deletePt(pt);	  // line will fire propertyChangeEvent
  }

  /**
   * Check if the pattern needs to be normalized (that is, if the range is not
   * exactly 0.0 to 1.0 in both x and y).
   *
   * @return true=needs to be normalized
   */
  public boolean needToNormalize() {
    BoundingBox bb = line.getBoundingBox();
    return (Math.abs(bb.min.x) > TOLERANCE)
        || (Math.abs(bb.min.y) > TOLERANCE)
        || (Math.abs(bb.max.x - 1.0) > TOLERANCE)
        || (Math.abs(bb.max.y - 1.0) > TOLERANCE);
  }

  /**
   * Normalize the pattern -- offset and stretch to fit in the range of 0.0, 0.0
   * to 1.0, 1.0.
   *
   * Note that nothing is done if less than 2 points or if it does not need to
   * be normalized (i.e. within TOLERANCE limits).
   */
  public synchronized void normalize() {
    if (line.getSize() < 2) {	// nothing is done if less than 2 points.
      return;
    }
    if (needToNormalize()) {
      BoundingBox bb = line.getBoundingBox();
      line.offsetX(bb.min.x);
      line.offsetY(bb.min.y);
      line.scale(1.0 / bb.getWidth(), 1.0 / bb.getHeight());
      updateImage();	// because offset and scale use drag propertyChanges
    }
  }

  /**
   * Mirror the left side points and add them to the right side for a
   * symmetrical pattern.
   *
   * Note: any right side points will be removed.
   */
  public synchronized void mirror() {
    if (line.getSize() < 2) {	// nothing is done if less than 2 points.
      return;
    }
    ArrayList<Pt> list = new ArrayList<>(line.getAllPoints());	// needs to be a copy so we can modify
    for (Pt p : list) {
      if (p.getX() > 0.50) {
        line.deletePt(p);	  // line will fire propertyChangeEvent
      }
    }
    for (Pt p : list) {
      if (p.getX() < 0.50) {	  // line will fire propertyChangeEvent
        if (isDual()) {
          line.insertPtX(new SquarePt2(1.0 - p.getX(), p.getY(), p.getY2(), IMAGE_COLOR, IMAGE_COLOR2));
        } else {
          line.insertPtX(new SquarePt(new Point2D.Double(1.0 - p.getX(), p.getY()), IMAGE_COLOR));
        }
      }
    }
  }

  /**
   * Invert the pattern.
   */
  public void invert() {
    if (line.getSize() < 0) {	// nothing is done if no points.
      return;
    }
    line.getAllPoints().stream().forEach((p) -> {
      p.setY(1.0 - p.getY());
    });
  }

  /**
   * Write the data to an xml file.
   *
   * @param out output stream for writing
   */
  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    if (isDual()) {
      opt += " dual='" + dual + "'";
    }
    if (line.getSize() != 0) {
      out.println(indent + "<CustomPattern"
          + " name='" + getName() + "'"
          + " displayName='" + getDisplayName() + "'"
          + " style='" + customStyle.toString() + "'"
          + opt
          + ">");
      indentMore();
      line.getAllPoints().stream().forEach((pt) -> {
        pt.writeXML(out);
      });
      indentLess();
      out.println(indent + "</CustomPattern>");
    }
  }

  /**
   * Requests that this CustomProfile be deleted by interested
   * PropertyChangeListeners.
   */
  public void requestDelete() {
    pcs.firePropertyChange(PROP_REQ_DELETE, null, null);
  }

  /**
   * Respond to changes in the line.
   *
   * @param evt
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CustomProfile.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    if (evt.getPropertyName().startsWith(Pt.PROP_PREFIX) || evt.getPropertyName().startsWith(PtDefinedLine.PROP_PREFIX)) { // look for point changes
      line2 = null;     // line2 is no longer valid
    }
    if (!evt.getPropertyName().contains("Drag")) {  // don't keep updating when dragging a point
      updateImage();
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }
}
