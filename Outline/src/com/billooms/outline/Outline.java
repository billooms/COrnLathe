package com.billooms.outline;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.clclass.CLclass.indent;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.CutterEditPanel;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.BoundingBox;
import com.billooms.drawables.simple.Curve;
import static com.billooms.drawables.Drawable.LIGHT_DOT;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.FittedCurve;
import com.billooms.drawables.Pt;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Outline of the shape defined by a series of points which can represent either
 * the inner surface or the outer surface.
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
public class Outline extends CLclass {

  /** All Outline property change names start with this prefix. */
  public final static String PROP_PREFIX = "Outline" + "_";
  /** Property name used when changing the location of points. */
  public final static String PROP_LOCATION = PROP_PREFIX + "DotLocation";
  /** Property name used when changing the thickness. */
  public final static String PROP_THICKNESS = PROP_PREFIX + "Thickness";
  /** Property name used when changing the resolution. */
  public final static String PROP_RESOLUTION = PROP_PREFIX + "Resolution";
  /** Property name used when changing the color. */
  public final static String PROP_COLOR = PROP_PREFIX + "Color";
  /** Property name used when changing the layer thickness. */
  public final static String PROP_LAYER = PROP_PREFIX + "Layer";
  /** Property name used when deleting the SafePath. */
  public final static String PROP_SAFEPATH = PROP_PREFIX + "SafePath";
  /** Property name used when changing multiple points. */
  public final static String PROP_MULTI = PROP_PREFIX + "Multi";

  /** Default thickness of the shape (currently set to 0.100) */
  public final static double DEFAULT_THICKNESS = 0.100;
  /** Minimum permissible curve thickness */
  public final static double MIN_THICKNESS = 0.000;
  /** Default resolution of the curves (currently set to 0.010) */
  public final static double DEFAULT_RESOLUTION = 0.010;
  /** Minimum permissible curve resolution (currently set to 0.001) */
  public final static double MIN_RESOLUTION = 0.001;
  /** Default location for dots defining the curve */
  public final static Location DEFAULT_LOCATION = Location.FRONT_INSIDE;
  /** Default bulk color of the shape */
  public final static Color DEFAULT_COLOR = new Color(186, 99, 18);

  private final static Color DOT_CURVE_COLOR = Color.LIGHT_GRAY;
  private final static Color INSIDE_CURVE_COLOR = Color.MAGENTA;
  private final static Color OUTSIDE_CURVE_COLOR = Color.ORANGE;
  private final static Color CUT_CURVE_COLOR = Color.WHITE;

  /** Location of the points defining the shape. */
  private Location dotLocation = DEFAULT_LOCATION;
  /** Thickness of the shape. */
  private double thickness = DEFAULT_THICKNESS;
  /** Resolution of the generated curves. */
  private double resolution = DEFAULT_RESOLUTION;
  /** Color of the outer surface of the bowl. */
  private Color color = DEFAULT_COLOR;
  /** Thickness of the surface layer. */
  private double layer1 = 0.0;
  /** Color of the center layer under the surface. */
  private Color color1 = DEFAULT_COLOR;
  /** Thickness of the center layer. */
  private double layer2 = 0.0;
  /** Color of the inner layer under the surface. */
  private Color color2 = DEFAULT_COLOR;
  /** Curve holding all the points. */
  private FittedCurve dotCurve = null;
  /** Optional curve holding SafePath points from end to beginning. */
  private SafePath safePath = null;
  /** Inside curve. */
  private Curve insideCurve = null;
  /** Outside curve. */
  private Curve outsideCurve = null;
  /** List of curves representing the centerline of the various cutters. */
  private final ArrayList<Curve> cutterPaths = new ArrayList<>();
  /** Cutter manager. */
  private Cutters cutterMgr = null;
  /** Cutter that is currently selected in the CutterEditPanel. */
  private Cutter cutter = null;

  /**
   * Construct the Outline from an XML DOM Element.
   *
   * @param element XML DOM Element
   * @param cutMgr Cutter manager
   */
  public Outline(Element element, Cutters cutMgr) {
    this.cutterMgr = cutMgr;
    dotCurve = new FittedCurve(DOT_CURVE_COLOR, LIGHT_DOT);
    insideCurve = new Curve(INSIDE_CURVE_COLOR, SOLID_LINE);
    outsideCurve = new Curve(OUTSIDE_CURVE_COLOR, SOLID_LINE);
    this.dotLocation = CLUtilities.getEnum(element, "dotLocation", Location.class, DEFAULT_LOCATION);
    this.thickness = CLUtilities.getDouble(element, "thickness", DEFAULT_THICKNESS);
    this.resolution = CLUtilities.getDouble(element, "resolution", DEFAULT_RESOLUTION);
    this.color = CLUtilities.getColor(element, "color", DEFAULT_COLOR);
    this.layer1 = CLUtilities.getDouble(element, "layer1", 0.0);
    this.color1 = CLUtilities.getColor(element, "color1", this.color);
    this.layer2 = CLUtilities.getDouble(element, "layer2", 0.0);
    this.color2 = CLUtilities.getColor(element, "color2", this.color);
    NodeList ptNodes = element.getChildNodes();
    for (int i = 0; i < ptNodes.getLength(); i++) {
      if (ptNodes.item(i) instanceof Element) {
        Element ptElement = (Element) ptNodes.item(i);
        if (ptElement.getTagName().equals("Pt")) {	// Read data points
          dotCurve.addPt(new OutlinePt(ptElement));	// Add points in order they were read
        }
        if (ptElement.getTagName().equals("SafePath")) {	// Read SafePath points
          safePath = new SafePath(ptElement);
        }
      }
    }
    if (dotCurve != null) {
      updateCurves();
      dotCurve.addPropertyChangeListener(this);	// listen to the curve, not to the individual points
    }
    if (safePath != null) {
      safePath.addPropertyChangeListener(this);	// listen to the SafePath object, not to the individual points
    }
    cutterMgr.addPropertyChangeListener(this);  // listen to cutter manager in case new cutters are added to update curves
  }

  /**
   * Update all of the curves.
   */
  private void updateCurves() {
    if (dotCurve == null) {
      return;
    }
    if (dotLocation.isInside()) {
      insideCurve.setPoints(dotCurve.buildCurvePoints(resolution));
      outsideCurve.setPoints(insideCurve.ptsOffset(dotLocation.isFront() ? thickness : -thickness));
    } else {
      outsideCurve.setPoints(dotCurve.buildCurvePoints(resolution));
      insideCurve.setPoints(outsideCurve.ptsOffset(dotLocation.isFront() ? -thickness : thickness));
    }
    if (cutter != null) {
      if (dotLocation.isFront() != cutter.getLocation().isFront()) {
        insideCurve.flipX();
        outsideCurve.flipX();
      }
    }
    insideCurve.reSample(resolution);		// uniform point spacing on all curves
    outsideCurve.reSample(resolution);

    // Build curves for all available cutters
    cutterPaths.clear();
    for (Cutter c : cutterMgr.getAllCutters()) {
      Curve cPath = new Curve(CUT_CURVE_COLOR, SOLID_LINE);
      cPath.setPoints(dotCurve.buildCurvePoints(resolution));
      cPath.offsetPts(dotToCutter(c));
      if (cutter != null) {
        if (dotLocation.isFront() != cutter.getLocation().isFront()) {
          cPath.flipX();
        }
      }
      cPath.reSample(resolution);		// uniform point spacing on all curves
      cutterPaths.add(cPath);
    }
  }

  /**
   * Calculate the offset from dotCurve to cutterPath curve when the curve
   * surface is digitized.
   *
   * @param ct cutter
   * @return offset
   */
  private double dotToCutter(Cutter ct) {
    double offset = 0.0;
    switch (ct.getFrame()) {	// offset for HCF & UCF
      case HCF:
      case UCF:
        if (dotLocation.isInside() == ct.getLocation().isInside()) {
          offset = ct.getRadius();
        } else {
          offset = ct.getRadius() + thickness;
        }
        if ((dotLocation.isFront() && ct.getLocation().isInside())
            || (dotLocation.isBack() && ct.getLocation().isOutside())) {
          offset = -offset;
        }
        break;
      case Drill:
      case ECF:
        if (dotLocation.isInside() != ct.getLocation().isInside()) {
          offset = thickness;
        }
        if ((dotLocation.isFront() && ct.getLocation().isInside())
            || (dotLocation.isBack() && ct.getLocation().isOutside())) {
          offset = -offset;
        }
        break;
    }
    return offset;
  }

  @Override
  public String toString() {
    return "Outline{" + dotLocation.toString() + ", t=" + thickness + '}';
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D g
   */
  public void paint(Graphics2D g2d) {
    if (dotCurve != null) {
      dotCurve.paint(g2d);
    }
    if (safePath != null) {
      safePath.getCurve().paint(g2d);
    }
    if (insideCurve != null) {
      insideCurve.paint(g2d);
    }
    if (outsideCurve != null) {
      outsideCurve.paint(g2d);
    }
    if (cutter != null) {
      if (getCutterPathCurve(cutter) != null) {
        getCutterPathCurve(cutter).paint(g2d);
      }
    }
  }

  /**
   * Get the location of the dots.
   *
   * @return the value of dotLocation
   */
  public Location getDotLocation() {
    return dotLocation;
  }

  /**
   * Set the location of the dots.
   *
   * @param dotLocation new value of dotLocation
   */
  public synchronized void setDotLocation(Location dotLocation) {
    Location old = this.dotLocation;
    this.dotLocation = dotLocation;
    updateCurves();
    pcs.firePropertyChange(PROP_LOCATION, old, dotLocation);
  }

  /**
   * Get the thickness of the shape.
   *
   * @return the value of thickness
   */
  public double getThickness() {
    return thickness;
  }

  /**
   * Set the thickness of the shape.
   *
   * @param thickness new value of thickness
   */
  public synchronized void setThickness(double thickness) {
    double old = this.thickness;
    this.thickness = thickness;
    updateCurves();
    pcs.firePropertyChange(PROP_THICKNESS, old, thickness);
  }

  /**
   * Get the resolution of the generated curves.
   *
   * @return the value of resolution
   */
  public double getResolution() {
    return resolution;
  }

  /**
   * Set the resolution of the generated curves.
   *
   * @param resolution new value of resolution
   */
  public synchronized void setResolution(double resolution) {
    double old = this.resolution;
    this.resolution = resolution;
    updateCurves();
    pcs.firePropertyChange(PROP_RESOLUTION, old, resolution);
  }

  /**
   * Get the color of the outer surface of the bowl. If no layers are used, then
   * this is the bulk color.
   *
   * @return outer surface color
   */
  public Color getColor() {
    return color;
  }

  /**
   * Set the color of the outer surface of the bowl. If no layers are used, then
   * this is the bulk color.
   *
   * @param c new color
   */
  public synchronized void setColor(Color c) {
    Color old = this.color;
    this.color = c;
    pcs.firePropertyChange(PROP_COLOR, old, color);
  }

  /**
   * Get the thickness of the surface layer. A value of zero means no layers are
   * used.
   *
   * @return thickness of the surface layer (or zero if no layers used)
   */
  public double getLayer1() {
    return layer1;
  }

  /**
   * Set the thickness of the surface layer. A value of zero means no layers are
   * used. Setting this to zero will also set layer2 to zero.
   *
   * @param d new thickness (or zero if no layers used)
   */
  public synchronized void setLayer1(double d) {
    double old = this.layer1;
    this.layer1 = Math.max(d, 0.0);   // can't go negative
    if (layer1 <= 0.0) {
      layer2 = 0.0;
    }
    pcs.firePropertyChange(PROP_LAYER, old, layer1);
  }

  /**
   * Get the color of the first layer under the surface.
   *
   * @return first layer color
   */
  public Color getColor1() {
    return color1;
  }

  /**
   * Set the color of the first layer under the surface.
   *
   * @param c new color
   */
  public synchronized void setColor1(Color c) {
    Color old = this.color1;
    this.color1 = c;
    pcs.firePropertyChange(PROP_COLOR, old, color1);
  }

  /**
   * Get the thickness of the center layer. A value of zero means no layers are
   * used.
   *
   * @return thickness of the surface layer (or zero if no layers used)
   */
  public double getLayer2() {
    return layer2;
  }

  /**
   * Set the thickness of the center layer. A value of zero means no layers are
   * used. Layer1 must be set to a non-zero value first.
   *
   * @param d new thickness (or zero if no layers used)
   */
  public synchronized void setLayer2(double d) {
    double old = this.layer2;
    this.layer2 = Math.max(d, 0.0);   // can't go negative
    if (layer1 <= 0.0) {
      layer2 = 0.0;
    }
    pcs.firePropertyChange(PROP_LAYER, old, layer2);
  }

  /**
   * Get the color of the second layer under the surface.
   *
   * @return second layer color
   */
  public Color getColor2() {
    return color2;
  }

  /**
   * Set the color of the second layer under the surface.
   *
   * @param c new color
   */
  public synchronized void setColor2(Color c) {
    Color old = this.color2;
    this.color2 = c;
    pcs.firePropertyChange(PROP_COLOR, old, color2);
  }

  /**
   * Get the sum of layer1 and layer2.
   *
   * @return sum of layer1 and layer2
   */
  public double getLayer1plus2() {
    return layer1 + layer2;
  }

  /**
   * Determine if layers are used.
   *
   * @return true: layers are used
   */
  public boolean usesLayers() {
    return (layer1 > 0.0);
  }

  /**
   * Get the number of points defining the outline.
   *
   * @return number of points
   */
  public int getNumPts() {
    return dotCurve.getSize();
  }

  /**
   * Get the points defining the shape of the Outline.
   *
   * @return points defining the shape
   */
  public ArrayList<Pt> getPoints() {
    return dotCurve.getAllPoints();
  }

  /**
   * Get the SafePath (which may be null).
   *
   * @return safe path
   */
  public SafePath getSafePath() {
    return safePath;
  }

  /**
   * Get the dotCurve.
   *
   * @return dotCurve
   */
  public FittedCurve getDotCurve() {
    return dotCurve;
  }

  /**
   * Get the inside curve.
   *
   * @return inside curve
   */
  public Curve getInsideCurve() {
    return insideCurve;
  }

  /**
   * Get the outside curve.
   *
   * @return outside curve
   */
  public Curve getOutsideCurve() {
    return outsideCurve;
  }

  /**
   * Get the curve to cut, which is either the insideCurve or the outsideCurve
   * (depending on the cutter location).
   *
   * @return curve to cut (or null if cutter has not been set)
   */
  public Curve getCutCurve() {
    if (cutter == null) {
      return insideCurve;   // arbitrary
    }
    if (cutter.getLocation().isInside()) {
      return insideCurve;
    } else {
      return outsideCurve;
    }
  }

  /**
   * Get the cutter path curve for the currently selected cutter.
   *
   * @param c cutter
   * @return curve
   */
  public Curve getCutterPathCurve(Cutter c) {
    if (c == null || cutterPaths.isEmpty()) {
      return null;
    }
    return cutterPaths.get(cutterMgr.indexOf(c.getName()));
  }

  /**
   * Get the cutterPathCurve which is the path of the center of the cutter.
   *
   * @return cutterPathCurve
   */
  public Curve getCutterPathCurve() {
    return getCutterPathCurve(cutter);
  }

  /**
   * Clear the Outline.
   */
  public synchronized void clear() {
    dotCurve.clear();	// the dotCurve quits listening to the old points
    if (safePath != null) {
      safePath.clear();	// and the curves are not set to null
    }
    // However, this Outline still listens to changes in the empty curve
    // so that as points are added this can respond.
    insideCurve.clear();
    outsideCurve.clear();
    cutterPaths.clear();
  }

  /**
   * Get the bounding box for all of the curves.
   *
   * @return bounding box of all curves
   */
  public BoundingBox getBoundingBox() {
    BoundingBox bb = new BoundingBox(0.0, 0.0, 2.0, 2.0);	// default size is 2 x 2
    if (dotCurve == null) {
      return bb;
    }
    bb = dotCurve.getBoundingBox();
    if (insideCurve != null) {
      bb = new BoundingBox(bb, insideCurve.getBoundingBox());
    }
    if (outsideCurve != null) {
      bb = new BoundingBox(bb, outsideCurve.getBoundingBox());
    }
    if (dotCurve != null) {
      bb = new BoundingBox(bb, dotCurve.getBoundingBox());
    }
    if (cutter != null) {
      bb = new BoundingBox(bb, getCutterPathCurve(cutter).getBoundingBox());
    }
    return bb;
  }

  /**
   * Add the given point at the appropriate position in the dotCurve (which is
   * sorted in y-direction).
   *
   * @param pt new point
   */
  public synchronized void addPoint(OutlinePt pt) {
    if (dotCurve.isempty() && (cutter != null)) {
      // When first point is added, set location to that of the current cutter
      setDotLocation(Location.valueOf(cutter.getLocation().toString()));
    }
    dotCurve.insertPtY(pt);
  }

  /**
   * Find the closest outline point to the given point which is within a given
   * distance.
   *
   * @param pt given point
   * @param dist distance to search
   * @return closest point (or null if no point with in the given distance)
   */
  public synchronized OutlinePt getClosestPt(Point2D.Double pt, double dist) {
    return (OutlinePt) dotCurve.closestPt(pt, dist);
  }

  /**
   * Delete the given point.
   *
   * @param pt point to delete
   */
  public synchronized void deletePoint(OutlinePt pt) {
    dotCurve.deletePt(pt);
  }

  /**
   * Add the given point at the appropriate position in the safePath (which is
   * sorted in y-direction).
   *
   * @param pt new point
   */
  public synchronized void addSafePoint(SafePt pt) {
    if (safePath == null) {
      safePath = new SafePath();
      safePath.addPropertyChangeListener(this);
    }
    safePath.addPoint(pt);
  }

  /**
   * Delete the SafePath.
   */
  public synchronized void deleteSafePath() {
    if (safePath != null) {
      safePath.clearAll();    // so it quits listening to the line
      safePath.removePropertyChangeListener(this);
      safePath = null;
      pcs.firePropertyChange(PROP_SAFEPATH, null, null);
    }
  }

  /**
   * Find the closest safePath point to the given point which is within a given
   * distance.
   *
   * @param pt given point
   * @param dist distance to search
   * @return closest point (or null if no point with in the given distance)
   */
  public SafePt getClosestSafePt(Point2D.Double pt, double dist) {
    if (safePath == null) {
      return null;
    }
    return (SafePt) safePath.getCurve().closestPt(pt, dist);
  }

  /**
   * Delete the given point from the safePath.
   *
   * @param pt point for deletion
   */
  public synchronized void deleteSafePt(SafePt pt) {
    if (safePath != null) {
      safePath.deletePoint(pt);
    }
  }

  /**
   * Set a new cutter and update all curves.
   *
   * @param newCutter new cutter
   */
  private synchronized void setCutter(Cutter newCutter) {
    if (cutter != null) {
      cutter.removePropertyChangeListener(this);  // quit listening to old one
    }
    this.cutter = newCutter;
    if (newCutter != null) {
      cutter.addPropertyChangeListener(this);	  // listen to the new one
    }
    updateCurves();
  }

  /**
   * When there are two points, make them exactly vertical.
   *
   * An error dialog is displayed if there are not two points.
   */
  public synchronized void set2PtsVertical() {
    if (dotCurve.getSize() != 2) {
      NotifyDescriptor d = new NotifyDescriptor.Message(
          "This only works for 2 points!",
          NotifyDescriptor.WARNING_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
    dotCurve.getPt(1).setX(dotCurve.getPt(0).getX());
  }

  /**
   * When there are two points, make them exactly horizontal.
   *
   * An error dialog is displayed if there are not two points.
   */
  public synchronized void set2PtsHorizontal() {
    if (dotCurve.getSize() != 2) {
      NotifyDescriptor d = new NotifyDescriptor.Message(
          "This only works for 2 points!",
          NotifyDescriptor.WARNING_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
    dotCurve.getPt(1).setY(dotCurve.getPt(0).getY());
  }

  /**
   * Offset the curve vertically by subtracting the given value from all
   * coordinates.
   *
   * @param delta offset value
   */
  public synchronized void offsetVertical(double delta) {
    dotCurve.offsetY(delta);
    if (safePath != null) {
      safePath.getCurve().offsetY(delta);
    }
    updateCurves();	// because offsetY uses drag propertyChanges
    pcs.firePropertyChange(PROP_MULTI, null, null);
  }

  /**
   * Offset all the points in the dotCurve.
   *
   * @param d offset amount
   */
  public synchronized void offsetDotCurve(double d) {
    dotCurve.offsetDots(d, dotLocation.isFrontInOrBackOut());
    updateCurves();	// because offsetDots uses drag propertyChanges
    pcs.firePropertyChange(PROP_MULTI, null, null);
  }

  /**
   * Offset the curve after probing to compensate for the diameter of the
   * cutter. Note that nothing is done for Drill or ECF.
   */
  public synchronized void offsetForCutter() {
    switch (cutter.getFrame()) {
      default:
      case Drill:
      case ECF:
        return;
      case HCF:
      case UCF:
        offsetDotCurve(-cutter.getRadius());
        break;
    }
  }

  /**
   * Invert the curve from top to bottom.
   *
   * Note that the order of the points is reversed so the bottom point is first
   * and the top point is last.
   */
  public synchronized void invert() {
    dotCurve.invert();
    if (safePath != null) {
      safePath.getCurve().invert();
    }
    updateCurves();
    pcs.firePropertyChange(PROP_MULTI, null, null);   // because invert uses drag propertyChanges
  }

  /**
   * Scale the curve by the given factor.
   * 
   * @param factor scale factor (Must be in the range 0.1 to 10.0).
   */
  public synchronized void scale(double factor) {   // limit the range
    if ((factor < 0.1) && (factor > 10.0) && (factor == 1.0)) {
      return;
    }
    dotCurve.scale(factor);
    if (safePath != null) {
      safePath.getCurve().scale(factor);
    }
    updateCurves();
    pcs.firePropertyChange(PROP_MULTI, null, null);   // because invert uses drag propertyChanges
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Outline.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens to dotCurve, safePath, cutter, cutterMgr, and CutterEditPanel
    if (evt.getPropertyName().equals(CutterEditPanel.PROP_CUTTER)) {
      setCutter((Cutter) evt.getNewValue());	    // grab any newly selected cutter
    }
    if (evt.getPropertyName().contains("Drag")) {
      return; // don't keep updating when dragging a point, and don't pass the propChange on
    }
    if (evt.getPropertyName().equals(SafePath.PROP_REQ_DELETE)) {
      if (evt.getSource() instanceof SafePath) {
        deleteSafePath();
      }
    }
    updateCurves();	  // update the curves for everything but a "Drag"
    if (evt.getPropertyName().startsWith(CutterEditPanel.PROP_PREFIX)
        || evt.getPropertyName().startsWith(Cutter.PROP_PREFIX)) {
    // Use this instead to note a change in selected cutter so that the "WRITE G-Code" button gets flagged
//    if (evt.getPropertyName().startsWith(Cutter.PROP_PREFIX)) {       
      pcs.firePropertyChange("TOP_Ignore", evt.getOldValue(), evt.getNewValue());
      return;	  // changes from CutterEditPanel or cutter are ignored at top level, but OutlineEditor needs them
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    if (layer1 != 0.0) {
      opt += " layer1='" + F3.format(layer1) + "'";
      opt += " color1='rgb(" + color1.getRed() + ", " + color1.getGreen() + ", " + color1.getBlue() + ")'";
    }
    if (layer2 != 0.0) {
      opt += " layer2='" + F3.format(layer2) + "'";
      opt += " color2='rgb(" + color2.getRed() + ", " + color2.getGreen() + ", " + color2.getBlue() + ")'";
    }
    out.println(indent + "<Outline"
        + " dotLocation='" + dotLocation.toString() + "'"
        + " thickness='" + F3.format(thickness) + "'"
        + " resolution='" + F3.format(resolution) + "'"
        + " color='rgb(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")'"
        + opt
        + ">");
    indentMore();
    dotCurve.getAllPoints().stream().forEach((p) -> {
      p.writeXML(out);
    });
    if (safePath != null) {
      safePath.writeXML(out);
    }
    indentLess();
    out.println(indent + "</Outline>");
  }

}
