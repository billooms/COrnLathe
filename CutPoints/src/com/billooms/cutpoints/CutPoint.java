package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.controls.CoarseFine;
import com.billooms.cutlist.CutList;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.cutters.Frame;
import com.billooms.drawables.Drawable;
import com.billooms.drawables.geometry.CircleGeom;
import com.billooms.drawables.geometry.Intersection2;
import com.billooms.drawables.geometry.LineGeom;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.simple.Text;
import com.billooms.drawables.simple.Text.Justify;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.ProgressMonitor;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * General interface for a CutPoint.
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
public abstract class CutPoint extends CLclass {

  /** All CutPoint property change names start with this prefix. */
  public final static String PROP_PREFIX = "CutPoint" + "_";
  /** Property name used when changing number. */
  public final static String PROP_NUM = PROP_PREFIX + "Num";
  /** Property name used when changing snap. */
  public final static String PROP_SNAP = PROP_PREFIX + "Snap";
  /** Property name used when changing cutter. */
  public final static String PROP_CUTTER = PROP_PREFIX + "Cutter";
  /** Property name used when changing depth. */
  public final static String PROP_DEPTH = PROP_PREFIX + "Depth";

  /** Default cut depth. */
  public final static double DEFAULT_CUTDEPTH = 0.050;
  /** Default color. */
  private final static Color DEFAULT_COLOR = Color.RED;
  /** Font for text. */
  protected final static Font TEXT_FONT = new Font("SansSerif", Font.BOLD, 24);
  /** Justify for text. */
  protected final static Justify TEXT_JUSTIFY = Justify.BOT_LEFT;
  /** Used for comparing dimensions to avoid round-off error. */
  protected final static double COMPARE_ERROR = 1e-6;
  /** Used for making instructions. */
  protected final static boolean LAST = true, NOT_LAST = false, ROTATE_NEG = true, ROTATE_POS = false;
  /** For drawing purposes, the length of arc shown for cuts. */
  protected final static double ARC_ANGLE = 110.0;

  /** All CutPoints have a CutPt for the location is in XZ space. */
  protected final CutPt pt;
  /** CutPoints can be visible or hidden. */
  private boolean visible = true;
  /** CutPoint number. */
  protected int num;
  /** Snap the point onto the cutCurve. */
  protected boolean snap = true;
  /** Cutter for this CutPoint. */
  protected Cutter cutter = null;
  /** Cut Depth. */
  protected double cutDepth = DEFAULT_CUTDEPTH;

  /** Text for CutPoint number. */
  protected Text text;
  /** List of everything that should be drawn -- XY space, not XZ. */
  protected ArrayList<Drawable> drawList = new ArrayList<>();
  /** List of 3D lines to be drawn on the shape. */
  protected ArrayList<Line3D> list3D = new ArrayList<>();
  /** Saved copy of the outline. */
  protected final Outline outline;
  /** CutList for instructions. */
  protected CutList cutList = Lookup.getDefault().lookup(CutList.class);

  /**
   * Construct a new CutPoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   */
  public CutPoint(Element element, Cutters cutMgr, Outline outline) {
    this.outline = outline;
    this.num = CLUtilities.getInteger(element, "n", 0);
    this.snap = CLUtilities.getBoolean(element, "snap", true);
    this.cutter = cutMgr.getCutter(CLUtilities.getString(element, "cutter", cutMgr.get(0).getName()));
    this.cutDepth = CLUtilities.getDouble(element, "depth", DEFAULT_CUTDEPTH);
    NodeList ptNodes = element.getElementsByTagName("Pt");
    if (ptNodes.getLength() >= 0) {
      this.pt = new CutPt((Element) ptNodes.item(0)); // there should be only one
    } else {
      this.pt = new CutPt(new Point2D.Double());    // this should never happen
    }
//	makeDrawables();	// do this in the implementation
    pt.addPropertyChangeListener(this);	    // listen to the Pt
    cutter.addPropertyChangeListener(this); // listen to the cutter
  }

  /**
   * Construct a new CutPoint at the given position with information from the
   * given CutPoint. This is primarily used when duplicating a CutPoint.
   *
   * @param pos new position
   * @param cpt CutPoint to copy from
   */
  public CutPoint(Point2D.Double pos, CutPoint cpt) {
    this.outline = cpt.outline;
    this.num = cpt.getNum();
    this.snap = cpt.isSnap();
    this.cutter = cpt.getCutter();
    this.cutDepth = cpt.getDepth();
    this.pt = new CutPt(pos);
//	makeDrawables();	// do this in the implementation
    pt.addPropertyChangeListener(this);	    // listen to the Pt
    cutter.addPropertyChangeListener(this); // listen to the cutter
  }

  /**
   * Construct a new CutPoint at the given position with default values. This is
   * primarily used when adding a first CutPoint from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public CutPoint(Point2D.Double pos, Cutter cut, Outline outline) {
    this.outline = outline;
    this.pt = new CutPt(pos);
    this.cutter = cut;
//	makeDrawables();	// do this in the implementation
    pt.addPropertyChangeListener(this);	    // listen to the Pt
  }

  /**
   * Make the text and cutter shape for drawing.
   */
  protected void makeDrawables() {
    list3D.clear();   // make them when needed
    drawList.clear();
    text = new Text(pt.getPoint2D(), Integer.toString(num), DEFAULT_COLOR, TEXT_FONT, TEXT_JUSTIFY);
    drawList.add(text);
    drawList.add(cutter.getDrawable(pt.getPoint2D(), DEFAULT_COLOR));
  }

  /**
   * Draw the CutPoint.
   *
   * @param g2d Graphics2D
   */
  public void paint(Graphics2D g2d) {
    if (visible) {
      pt.paint(g2d);
      drawList.stream().forEach((d) -> {
        d.paint(g2d);
      });
    }
  }

  @Override
  public String toString() {
    return num + ": " + F3.format(pt.getX()) + " " + F3.format(pt.getZ()) + " d:" + F3.format(cutDepth);
  }

  /**
   * Clear the CutPoint (mainly removes any PropertyChangeListeners).
   */
  public synchronized void clear() {
    pt.removePropertyChangeListener(this);	// quit listening to the Pt
    cutter.removePropertyChangeListener(this);	// quit listening to the cutter
    drawList.clear();
    list3D.clear();
  }

  /**
   * Get the CutPoint number.
   *
   * @return CutPoint number
   */
  public int getNum() {
    return num;
  }

  /**
   * Set the CutPoint number. This fires a PROP_NUM propertyChange.
   *
   * @param n new CutPoint number.
   */
  public synchronized void setNum(int n) {
    int old = this.num;
    this.num = n;
    if (text != null) {
      text.setText(Integer.toString(n));	  // faster
    }
//	makeDrawables();
    pcs.firePropertyChange(PROP_NUM, old, num);
  }

  /**
   * Get the x-coordinate.
   *
   * @return x-coordinate
   */
  public double getX() {
    return pt.getX();
  }

  /**
   * Set the x-coordinate
   *
   * @param x new x-coordinate
   */
  public synchronized void setX(double x) {
    pt.setX(x);
//    makeDrawables();    // pt.setX will fire a propertyChange
  }

  /**
   * Get the y-coordinate (which should always be zero).
   *
   * @return y-coordinate
   */
  public double getY() {
    return pt.getY();
  }

  /**
   * Get the z-coordinate.
   *
   * @return z-coordinate
   */
  public double getZ() {
    return pt.getZ();
  }

  /**
   * Set the z-coordinate
   *
   * @param z new z-coordinate
   */
  public synchronized void setZ(double z) {
    pt.setZ(z);
//    makeDrawables();    // pt.setZ will fire a propertyChange
  }

  /**
   * Get the position of this CutPoint in 2D XY space.
   *
   * @return position (note XZ is converted to XY)
   */
  public Point2D.Double getPos2D() {
    return pt.getPoint2D();
  }

  /**
   * Get the width of the cut at the cut depth.
   *
   * @return width of the cut
   */
  public double getWidthAtMax() {
    return cutter.getWidthOfCut(cutDepth);
  }

  /**
   * Get the 3D lines for this cut. They are made on demand when the list3D is
   * empty. The list is cleared at the beginning of makeDrawables().
   *
   * @return List of Line3D objects (empty if there are none);
   */
  public ArrayList<Line3D> get3DLines() {
    if (list3D.isEmpty()) {
      make3DLines();
    }
    return list3D;
  }

  /**
   * Drag the CutPoint to the given XZ position.
   *
   * @param pos new position
   */
  public synchronized void drag(Point2D.Double pos) {
    pt.drag(pos);
//    makeDrawables();    // pt.drag will fire a DRAG propertyChange
  }

  /**
   * Invert the CutPoint (change sign of Z coordinate).
   */
  public synchronized void invert() {
    pt.invert();
//    makeDrawables();    // pt.invert will fire a DRAG propertyChange
  }

  /**
   * Scale the coordinates of the CutPoint.
   * 
   * @param factor scale factor
   */
  public synchronized void scale(double factor) {
    pt.scale(factor);
//    makeDrawables();    // pt.invert will fire a DRAG propertyChange
  }

  /**
   * Offset the CutPoint vertically by subtracting the given value from
   * z-coordinate.
   *
   * @param d vertical offset
   */
  public synchronized void offSetVertical(double d) {
    pt.offsetZ(d);
//    makeDrawables();    // pt.offsetZ will fire a DRAG propertyChange
  }

  /**
   * Move the CutPoint to the given XZ position.
   *
   * @param pos new position
   */
  public synchronized void move(Point2D.Double pos) {
    pt.move(pos);
//    makeDrawables();    // pt.move always fires a propertyChange
  }

  /**
   * Move the CutPoint to the given XZ position.
   *
   * @param x new x-coordinate
   * @param z new z-coordinate
   */
  public synchronized void move(double x, double z) {
    pt.move(x, z);
//    makeDrawables();    // pt.move always fires a propertyChange
  }

  /**
   * Determine if the CutPoint is visible.
   *
   * @return true: visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Set the visibility of the CutPoint.
   *
   * @param vis true: visible
   */
  public synchronized void setVisible(boolean vis) {
    this.visible = vis;
    pt.setVisible(vis);	  // fires a propertyChangeEvent
  }

  /**
   * Determine if the CutPoint is snapped to the Outline's cutCurve.
   *
   * @return true: snapped to a point on the cutCurve; false: free to move
   * anywhere
   */
  public boolean isSnap() {
    return snap;
  }

  /**
   * Set the snap flag. This fires a PROP_SNAP propertyChange.
   *
   * @param snap true: snapped to a point on the cutCurve; false: free to move
   */
  public synchronized void setSnap(boolean snap) {
    boolean old = this.snap;
    this.snap = snap;
    pcs.firePropertyChange(PROP_SNAP, old, this.snap);
  }

  /**
   * Snap the CutPoint to the nearest point on the given cutter curve.
   */
  public synchronized void snapToCurve() {
    if (snap) {
      Point2D.Double newPos = outline.getCutterPathCurve(cutter).nearestPoint(this.getPos2D());
      if (!newPos.equals(this.getPos2D())) {
        this.move(outline.getCutterPathCurve(cutter).nearestPoint(this.getPos2D()));
//      makeDrawables();    // move always fires a propertyChange
      }
    }
  }

  /**
   * Get the cutter assigned for this CutPoint.
   *
   * @return cutter
   */
  public Cutter getCutter() {
    return cutter;
  }

  /**
   * Set the cutter for this CutPoint. This fires a PROP_CUTTER propertyChange.
   *
   * @param newCutter new cutter
   */
  public synchronized void setCutter(Cutter newCutter) {
    cutter.removePropertyChangeListener(this);
    Cutter old = this.cutter;
    this.cutter = newCutter;
    makeDrawables();
    pcs.firePropertyChange(PROP_CUTTER, old, cutter);
    cutter.addPropertyChangeListener(this);
  }

  /**
   * Get the cut depth.
   *
   * @return cut depth
   */
  public double getDepth() {
    return cutDepth;
  }

  /**
   * Set the cut depth. This fires a PROP_DEPTH propertyChange.
   *
   * @param d new cut depth
   */
  public synchronized void setDepth(double d) {
    double old = this.cutDepth;
    this.cutDepth = d;
    makeDrawables();
    pcs.firePropertyChange(PROP_DEPTH, old, cutDepth);
  }

  /**
   * Get the separation from this point to the specified point.
   *
   * @param p XZ position in inches
   * @return distance in inches
   */
  public double separation(Point2D.Double p) {
    return Math.hypot(p.x - pt.getX(), p.y - pt.getZ());
  }

  /**
   * String with xml information that is common to all CutPoints.
   *
   * @return string
   */
  protected String xmlCutPointInfo() {
    String str = "";
    str += " n='" + num + "'";
    if (!snap) {
      str += " snap='false'";
    }
    str += " cutter='" + cutter.getName() + "'";
    str += " depth='" + F4.format(cutDepth) + "'";
    return str;
  }

  /**
   * String with xml information that is common to all CutPoints, except 
   * no cutDepth.
   *
   * @return string
   */
  protected String xmlCutPointInfo2() {
    String str = "";
    str += " n='" + num + "'";
    if (!snap) {
      str += " snap='false'";
    }
    str += " cutter='" + cutter.getName() + "'";
    return str;
  }

  @Override
  public void writeXML(PrintWriter out) {
    pt.writeXML(out);
  }

  /**
   * Cut the surface with the CutPoint.
   *
   * @param surface surface
   * @param monitor progress monitor which can be canceled
   */
  public abstract void cutSurface(Surface surface, ProgressMonitor monitor);

  /**
   * Make instructions for this CutPoint
   *
   * @param controls control panel data
   * @param stepsPerRot steps per rotation
   */
  public abstract void makeInstructions(CoarseFine controls, int stepsPerRot);

  /**
   * Make a line to display on the shape (like a pen chuck). This can be done
   * lazy -- if list3D is empty, then call make3DLines() to make them.
   */
  protected abstract void make3DLines();

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CutPoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // CutPoints listen to the Pt and to the cutter
    if (evt.getPropertyName().startsWith(Cutter.PROP_PREFIX)) {
      makeDrawables();        // changes from cutter are ignored at top level, but OutlineEditor needs them
      pcs.firePropertyChange("TOP_Ignore", evt.getOldValue(), evt.getNewValue());
    } else {
      // pass the Pt info through
      pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

  /** ***** Various utilities used by some (but not all) CutPoints ******** */
  /**
   * Find the width of a cut which is the intersection of the circle of a cut
   * and a curve. Note: this is to be used with HCF only. UCF rotations are not
   * yet calculated.
   *
   * @param circle circle of a cut
   * @param curve Curve
   * @return width
   */
  protected double findWidth(CircleGeom circle, Curve curve) {
    Point2D.Double[] pts = findIntersect(circle, curve);
    if (pts == null) {
      return 0.0;
    }
    return Math.hypot(pts[0].x - pts[1].x, pts[0].y - pts[1].y);
  }

  /**
   * Find the points on the given circle that intersect with the curve. Note:
   * this is to be used with HCF only.
   *
   * @param circle circle of a cut
   * @param curve Curve
   * @return two points (or null if they don't intersect)
   */
  protected Point2D.Double[] findIntersect(CircleGeom circle, Curve curve) {
    if (cutter.getFrame() != Frame.HCF) {	// This only works for HCF
      return null;
    }
    if (circle == null) {
      return null;
    }
    Point2D.Double[] pts = curve.getPoints();
    if (pts.length < 2) {
      return null;
    }

    ArrayList<Point2D.Double> iPts = new ArrayList<>();
    Intersection2 isect;
    for (int i = 0; i < pts.length - 1; i++) {
      final Point2D.Double p0 = pts[i];
      final Point2D.Double p1 = pts[i + 1];
      isect = new Intersection2(new LineGeom(p0, p1), circle);
      if (isect.p0 != null) {
        if (ptOnSegment(isect.p0, p0, p1)) {
          iPts.add(isect.p0);
        }
      }
      if (isect.p1 != null) {
        if (ptOnSegment(isect.p1, p0, p1)) {
          iPts.add(isect.p1);
        }
      }
    }
    if (iPts.isEmpty()) {
      return null;
    }
    if (iPts.size() == 1) {
      if (circle.isInside(pts[0])) {
        iPts.add(0, pts[0]);			// add the first point on the curve
      } else if (circle.isInside(pts[pts.length - 1])) {
        iPts.add(pts[pts.length - 1]);	// add the last point on the curve
      } else {
        return null;
      }
    }
//    return (Point2D.Double[]) iPts.toArray();   // TODO: what's wrong with this?
    Point2D.Double[] p = new Point2D.Double[2];
    p[0] = iPts.get(0);
    p[1] = iPts.get(iPts.size() - 1);
    return p;
  }

  /**
   * Is the point p between points p0 and p1 (inclusive)?
   *
   * @param p given point
   * @param p0 starting point
   * @param p1 ending point
   * @return true=point is between p0 and p1, else false
   */
  private boolean ptOnSegment(Point2D.Double p, Point2D.Double p0, Point2D.Double p1) {
    if (p0.x < p1.x) {
      if ((p.x < p0.x) || (p.x > p1.x)) {
        return false;
      }
    } else {
      if ((p.x < p1.x) || (p.x > p0.x)) {
        return false;
      }
    }
    if (p0.y < p1.y) {
      if ((p.y < p0.y) || (p.y > p1.y)) {
        return false;
      }
    } else {
      if ((p.y < p1.y) || (p.y > p0.y)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compute the perpendicular vector. It is the perpendicular direction from
   * the CutPoint to the curve for snapped points or the direction to the
   * nearest point on the curve for unsnapped points.
   *
   * @param scale scale factor (use 1.0 for normalized)
   * @return normalized perpendicular vector (might be null if less than two
   * points on curve)
   */
  protected Vector2d getPerpVector(double scale) {
    Vector2d perp;
    if (snap) {
      perp = outline.getCutterPathCurve(cutter).perpendicular(getPos2D(), cutter.getLocation().isFrontInOrBackOut());
    } else {
      final Point2D.Double nearest = outline.getCutSurfaceCurve().nearestPoint(getPos2D());
      perp = new Vector2d(nearest.x - getX(), nearest.y - getZ());
      if ((perp.x == 0.0) && (perp.y == 0.0)) {
        // can't normalize 0,0 or you get NaN, so fall back to perp to curve
        perp = outline.getCutterPathCurve(cutter).perpendicular(getPos2D(), cutter.getLocation().isFrontInOrBackOut());
      } else {
        perp.normalize();
      }
    }
    if (perp == null) {
      return null;		// must be less than 2 points on the curve
    }
    if (Math.abs(perp.x) < 1E-12) {
      perp.x = 0.0;		// to prevent -0.0
    }
    if (Math.abs(perp.y) < 1E-12) {
      perp.y = 0.0;		// to prevent -0.0
    }
    perp.scale(scale);
    return perp;
  }

  /**
   * Make sure angle is in range 0.0 <= a < 360.0
   *
   * @param a angle in degrees
   * @return angle in range 0.0 <= a < 360.0
   */
  protected double angleCheck(double a) {
    while (a < 0.0) {
      a += 360.0;
    }
    while (a >= 360.0) {
      a -= 360.0;
    }
    return a;
  }

//  /**
//   * Rotate the given points around the z-axis by the given incremental angle.
//   *
//   * @param pts array of points
//   * @param rad incremental angle in degrees
//   */
//  protected void rotateZ(Point3d[] pts, double deg) {
//    if (deg == 0.0) {
//      return;
//    }
//    Transform3D tRotZ = new Transform3D();
//    tRotZ.rotZ(Math.toRadians(deg));
//    for (int n = 0; n < pts.length; n++) {
//      tRotZ.transform(pts[n]);
//    }
//  }
//
//  /**
//   * Rotate the given points around the y-axis by the given incremental angle.
//   *
//   * @param pts array of points
//   * @param rad incremental angle in degrees
//   */
//  protected void rotateY(Point3d[] pts, double deg) {
//    if (deg == 0.0) {
//      return;
//    }
//    Transform3D tRotY = new Transform3D();
//    tRotY.rotY(Math.toRadians(deg));
//    for (int n = 0; n < pts.length; n++) {
//      tRotY.transform(pts[n]);
//    }
//  }
//
//  /**
//   * Offset all points by the given incremental amount.
//   *
//   * @param pts array of points
//   * @param x incremental x-axis offset
//   * @param y incremental y-axis offset
//   * @param z incremental z-axis offset
//   */
//  protected void offset(Point3d[] pts, double x, double y, double z) {
//    Transform3D tOffset = new Transform3D();
//    tOffset.setTranslation(new Vector3d(x, y, z));
//    for (int n = 0; n < pts.length; n++) {
//      tOffset.transform(pts[n]);
//    }
//  }
  /**
   * Determine from the perpVector and cutter if we are cutting on the top
   * outside of the shape.
   *
   * @return true=top and outside; false=bottom or inside
   */
  protected boolean isTopOutside() {
    Vector2d perp = getPerpVector(1.0);
    return (perp.y < 0) && (cutter.getLocation().isOutside());
  }

  /**
   * Compare two numbers to see if they are about equal (within COMPARE_ERROR).
   *
   * @param a first number
   * @param b second number
   * @return true: about equal
   */
  protected boolean aboutEqual(double a, double b) {
    return Math.abs(a - b) < COMPARE_ERROR;
  }

  /**
   * Compare three numbers to see if they are about equal (within
   * COMPARE_ERROR).
   *
   * @param a first number
   * @param b second number
   * @param c third number
   * @return true: about equal
   */
  protected boolean aboutEqual(double a, double b, double c) {
    return aboutEqual(a, b) && aboutEqual(b, c);
  }
  
  /**
   * Find a value within y with the same proportions as the value within x.
   * 
   * @param x1 start of x interval
   * @param x value within x interval
   * @param x2 end of x interval
   * @param y1 start of y interval
   * @param y2 end of y interval
   * @return 
   */
  public static double proportion(double x1, double x, double x2, double y1, double y2) {
    return y1 + (y2 - y1)/(x2 - x1) * (x - x1);
  }
  
}
