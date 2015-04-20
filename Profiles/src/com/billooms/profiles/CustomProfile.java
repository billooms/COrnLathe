package com.billooms.profiles;

import com.billooms.clclass.CLUtilities;
import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.drawables.BoundingBox;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.FittedCurve;
import com.billooms.drawables.PiecedLine;
import com.billooms.drawables.Pt;
import com.billooms.drawables.PtDefinedLine;
import com.billooms.drawables.SquarePt;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Pattern defined by a Fitted Line.
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
public class CustomProfile extends BasicProfile {

  /** All CustomProfile property change names start with this prefix */
  public final static String PROP_PREFIX = "Profile" + "_";
  /** Property name used when adding a point */
  public final static String PROP_ADDPT = PROP_PREFIX + "AddPt";
  /** Property name used when deleting a point */
  public final static String PROP_DELETEPT = PROP_PREFIX + "DeletePt";
  /** Property name used when clearing all points */
  public final static String PROP_CLEAR = PROP_PREFIX + "Clear";
  /** Property name used when changing the name or display name */
  public final static String PROP_NAME = PROP_PREFIX + "Name";
  /** Property name used when requesting deletion. */
  public final static String PROP_REQ_DELETE = PROP_PREFIX + "requestDelete";

  /** Two different styles of custom pattern */
  public static enum CustomStyle {

    /** Straight line segments between points */
    STRAIGHT,
    /** Curve fit between points */
    CURVE
  }

  /** Tolerance for checking normalization. */
  private final static double TOLERANCE = .0002;
  private final static CustomStyle DEFAULT_STYLE = CustomStyle.STRAIGHT;

  /** Either STRAIGHT or CURVE. */
  private CustomStyle customStyle;

  /**
   * Create a Fitted Line pattern with the given display name.
   *
   * @param displayName display name
   * @param st Style of the pattern
   */
  public CustomProfile(String displayName, CustomStyle st) {
    super(displayName);
    this.customStyle = st;
    switch (st) {
      case STRAIGHT:
        line = new PiecedLine(IMAGE_COLOR, SOLID_LINE);
        break;
      case CURVE:
        line = new FittedCurve(IMAGE_COLOR, SOLID_LINE);
        break;
    }
    builtIn = false;	// this is not a built-in pattern
    line.addPropertyChangeListener(this); // listen to the curve, not to the individual points
  }

  /**
   * Create a new custom pattern from the given DOM Element. Note: This does not
   * fire any PropertyChange events.
   *
   * @param element DOM Element
   */
  public CustomProfile(Element element) {
    super(CLUtilities.getString(element, "displayName", "No Name"));
    builtIn = false;	// this is not a built-in pattern
    this.name = CLUtilities.getString(element, "name", "NONAME");   // in case it's different
    this.customStyle = CLUtilities.getEnum(element, "style", CustomStyle.class, DEFAULT_STYLE);
    switch (customStyle) {
      case STRAIGHT:
        line = new PiecedLine(IMAGE_COLOR, SOLID_LINE);
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
          line.addPt(new SquarePt(ptElement, IMAGE_COLOR));	// Add points in order they were read
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
    return super.toString() + ": " + customStyle.toString();
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
   * Get the line object.
   *
   * @return the line object
   */
  public PtDefinedLine getLine() {
    return line;
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
   * Calculate the profile of the cutter at a given distance from the center.
   *
   * This is a positive number indicating the distance back from the tip of the
   * cutter, where the tip of the cutter is 0.0. Return -1.0 if the given
   * distance is greater then the radius of the cutter.
   *
   * @param d distance from the center of the cutter (allow for both positive
   * and negative)
   * @param rodRadius rodDiameter/2.0
   * @return profile at the given distance. Return -1.0 if beyond the diameter
   * of the cutter.
   */
  @Override
  public double profileAt(double d, double rodRadius) {
    if (Math.abs(d) > rodRadius) {
      return -1.0;
    }
    return rodRadius * line.getYforX(d / rodRadius);
  }

  /**
   * Calculate the width of the cutter at the given depth.
   *
   * @param d depth
   * @param rodDiameter diameter of the cutter rod
   * @return width of the cutter at the given depth
   */
  @Override
  public double widthAtDepth(double d, double rodDiameter) {
    return Math.min(rodDiameter, rodDiameter * Math.abs(line.getXforY(d)));
  }

  /**
   * Add a new point to the curve.
   *
   * @param pt location of new point
   */
  public synchronized void addPt(Point2D.Double pt) {
    pt.x = Math.min(pt.x, 1.0);
    pt.y = Math.min(pt.y, 1.0);
    pt.x = Math.max(pt.x, -1.0);
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
    return (Math.abs(bb.min.x + 1.0) > TOLERANCE)
        || (Math.abs(bb.max.x - 1.0) > TOLERANCE)
        || (Math.abs(bb.min.y) > TOLERANCE);
  }

  /**
   * Normalize the pattern -- offset and stretch to fit in the range of -1.0 <=
   * x <= 1.0 and y >= 0.0.
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
      line.offsetY(bb.min.y);	// brings the bottom to zero
      line.offsetX((bb.max.x + bb.min.x) / 2.0);  // centers it horizontally
      line.scale(2.0 / bb.getWidth(), 1.0 / bb.getHeight());  // scale to fit
      updateImage();	// because offset and scale use drag propertyChanges
    }
  }

  /**
   * Mirror the positive points and add them to the negative side for a
   * symmetrical profile.
   *
   * Note: any negative points will be removed.
   */
  public synchronized void mirror() {
    if (line.getSize() < 2) {	// nothing is done if less than 2 points.
      return;
    }
    ArrayList<Pt> list = new ArrayList<>(line.getAllPoints());	// needs to be a copy so we can modify
    for (Pt p : list) {
      if (p.getX() < 0.0) {
        line.deletePt(p);	  // line will fire propertyChangeEvent
      }
    }
    for (Pt p : list) {
      if (p.getX() > 0.0) {	  // line will fire propertyChangeEvent
        line.insertPtX(new SquarePt(new Point2D.Double(-p.getX(), p.getY()), IMAGE_COLOR));
      }
    }
  }

  /**
   * Write the data to an xml file.
   *
   * @param out output stream for writing
   */
  @Override
  public void writeXML(PrintWriter out) {
    if (line.getSize() != 0) {
      out.println(indent + "<CustomProfile"
          + " name='" + getName() + "'"
          + " displayName='" + getDisplayName() + "'"
          + " style='" + customStyle.toString() + "'"
          + ">");
      indentMore();
      line.getAllPoints().stream().forEach((pt) -> {
        pt.writeXML(out);
      });
      indentLess();
      out.println(indent + "</CustomProfile>");
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
    if (!evt.getPropertyName().contains("Drag")) {  // don't keep updating when dragging a point
      updateImage();
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }
}
