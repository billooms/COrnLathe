package com.billooms.outline;

import com.billooms.clclass.CLclass;
import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.PiecedLine;
import com.billooms.drawables.Pt;
import com.billooms.drawables.PtDefinedLine;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A SafePath is an optional list of points representing a path from the end of
 * the outline back to the beginning of the outline that doesn't hit the shape.
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
public class SafePath extends CLclass {

  private final static Color SAFE_CURVE_COLOR = Color.RED;

  /** All CustomProfile property change names start with this prefix */
  public final static String PROP_PREFIX = "SafePath" + "_";
  /** Property name used when requesting deletion. */
  public final static String PROP_REQ_DELETE = PROP_PREFIX + "requestDelete";

  /** Curve holding SafePath points from end to beginning. */
  private PiecedLine safeCurve = null;

  /** Construct a new empty SafePath. */
  public SafePath() {
    safeCurve = new PiecedLine(SAFE_CURVE_COLOR, SOLID_LINE);
    safeCurve.addPropertyChangeListener(this);	// listen to the curve, not to the individual points
  }

  /**
   * Construct the SafePath from an XML DOM Element.
   *
   * @param element XML DOM Element
   */
  public SafePath(Element element) {
    safeCurve = new PiecedLine(SAFE_CURVE_COLOR, SOLID_LINE);
    NodeList ptNodes = element.getChildNodes();
    for (int i = 0; i < ptNodes.getLength(); i++) {
      if (ptNodes.item(i) instanceof Element) {
        Element ptElement = (Element) ptNodes.item(i);
        if (ptElement.getTagName().equals("Pt")) {	// Read data points
          safeCurve.addPt(new SafePt(ptElement));	// Add points in order they were read
        }
      }
    }
    safeCurve.addPropertyChangeListener(this);	// listen to the curve, not to the individual points
  }

  @Override
  public String toString() {
    return "SafePath{" + safeCurve.getSize() + " pts}";
  }

  /**
   * Get the curve with the data points.
   *
   * @return curve with the data points
   */
  public PtDefinedLine getCurve() {
    return safeCurve;
  }

  /**
   * Clear the SafePath of all points and quit listening to it.
   */
  public synchronized void clearAll() {
    safeCurve.removePropertyChangeListener(this);
    clear();
  }

  /**
   * Clear the SafePath of all points but keep listening in case more points are
   * added.
   */
  public synchronized void clear() {
    safeCurve.clear();	// the safeCurve quits listening to the old points
    // However, this SafePath still listens to changes in the empty curve
    // so that as points are added this can respond.
  }

  /**
   * Requests that this CustomProfile be deleted by interested
   * PropertyChangeListeners.
   */
  public void requestDelete() {
    pcs.firePropertyChange(PROP_REQ_DELETE, null, null);
  }

  /**
   * Delete the given point from the safeCurve.
   *
   * @param pt point for deletion
   */
  public synchronized void deletePoint(SafePt pt) {
    safeCurve.deletePt(pt);
  }

  /**
   * Add the given point at the appropriate position in the safeCurve (which is
   * sorted in y-direction).
   *
   * @param pt new point
   */
  public synchronized void addPoint(SafePt pt) {
    safeCurve.insertPtY(pt);
  }

  /**
   * Get the points defining the shape of the SafePath.
   *
   * @return points defining the shape
   */
  public ArrayList<Pt> getPoints() {
    return safeCurve.getAllPoints();
  }

  @Override
  public void writeXML(PrintWriter out) {
    if (!safeCurve.isempty()) {	  // Don't write anything if there are no points
      out.println(indent + "<SafePath"
          + ">");
      indentMore();
      safeCurve.getAllPoints().stream().forEach((p) -> {
        p.writeXML(out);
      });
      indentLess();
      out.println(indent + "</SafePath>");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("SafePath.propertyChange: " + evt.getSource() + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

}
