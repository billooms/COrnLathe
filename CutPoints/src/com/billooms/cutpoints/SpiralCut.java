package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.Drawable;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.simple.Line;
import com.billooms.outline.Outline;
import com.billooms.spirals.Spiral;
import com.billooms.spirals.SpiralMgr;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point3D;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * General interface defining spirals between a start CutPoint and an end
 * CutPoint. This position defines the end of the spiral, and a separate
 * CutPoint defines the beginning. There may be additional GoToPoints to safely
 * direct the movement from end back to the beginning.
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
public abstract class SpiralCut extends CutPoint {

  /** Property name used when adding a GoToPoint */
  String PROP_ADD = PROP_PREFIX + "Add";
  /** Property name used when removing a GoToPoint */
  String PROP_REMOVE = PROP_PREFIX + "Remove";

  /** End point cut depth may be different than the start. */
  protected double endCutDepth = DEFAULT_CUTDEPTH;
  /** Instance of the kind of spiral used. */
  protected Spiral spiral = null;
  /** Start point of the spiral. */
  protected CutPoint beginPt = null;
  /** Internal GoToPoints. */
  protected final ArrayList<GoToPoint> goList = new ArrayList<>();

  /** The Spiral manager. */
  protected final SpiralMgr spiralMgr = Lookup.getDefault().lookup(SpiralMgr.class);

  /**
   * Constructor will read the endDepth and Spiral information from the given
   * DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   */
  public SpiralCut(Element element, Cutters cutMgr, Outline outline) {
    super(element, cutMgr, outline);
    this.endCutDepth = CLUtilities.getDouble(element, "endDepth", DEFAULT_CUTDEPTH);

    NodeList node = element.getChildNodes();
    for (int k = 0; k < node.getLength(); k++) {		// read data from xml file
      if (node.item(k) instanceof Element) {
        Element cutElement = (Element) node.item(k);
        if (cutElement.getTagName().equals("Spiral")) {
          spiral = new Spiral(cutElement);
          spiral.addPropertyChangeListener(this);
        }
        if (cutElement.getTagName().equals("GoToPoint")) {
          GoToPoint gPt = new GoToPoint(cutElement, cutMgr, outline);
          goList.add(gPt);
          gPt.addPropertyChangeListener(this);
        }
      }
    }
  }

  /**
   * Construct a new SpiralCut with information from the given SpiralCut.
   *
   * @param pos new position
   * @param cpt SpiralCut to copy from
   */
  public SpiralCut(Point2D.Double pos, SpiralCut cpt) {
    super(pos, cpt);
    this.endCutDepth = cpt.getEndDepth();
    this.spiral = new Spiral(cpt.getSpiral());
    spiral.addPropertyChangeListener(this);
    for (GoToPoint g : cpt.getAllGoTos()) {
      GoToPoint gPt = new GoToPoint(g.getPos2D(), g);
      goList.add(gPt);
      gPt.addPropertyChangeListener(this);
    }
  }

  /**
   * Construct a new SpiralCut at the given position with default values. This
   * is primarily used when adding a first SpiralCut from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public SpiralCut(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
    spiral = new Spiral();
    spiral.addPropertyChangeListener(this);
  }

  @Override
  public String toString() {
    String str = num + ": " + F3.format(pt.getX()) + " " + F3.format(pt.getZ());
    str += " " + F3.format(beginPt.getDepth()) + "->" + F3.format(endCutDepth);
    return str;
  }

  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);
    if (beginPt != null) {
      beginPt.paint(g2d);	// paint the begin point too
    }
    if (!goList.isEmpty()) {
      goList.stream().forEach((gPt) -> {
        gPt.paint(g2d);
      });
    }
  }

  @Override
  public void clear() {
    super.clear();
    spiral.removePropertyChangeListener(this);
    if (beginPt != null) {
      beginPt.removePropertyChangeListener(this);
      beginPt.clear();
    }
    for (GoToPoint gPt : goList) {
      gPt.removePropertyChangeListener(this);
      gPt.clear();
    }
    goList.clear();
  }

  /**
   * Set the number of this CutPoint. This fires a PROP_NUM property change with
   * the old and new values.
   *
   * @param n CutPoint number
   */
  @Override
  public void setNum(int n) {
    super.setNum(n);	// does makeDrawables() and fires PROP_NUM property change
    if (beginPt != null) {
      beginPt.setNum(n);	// does beginPt.makeDrawables() and fires PROP_NUM property change
    }
    goList.stream().forEach((gPt) -> {
      gPt.setNum(n);	// each fires PROP_NUM property change
    });
    makeDrawables();
  }

  /**
   * Set the cutter for this CutPoint. This fires a PROP_CUTTER propertyChange.
   *
   * @param newCutter new cutter
   */
  @Override
  public synchronized void setCutter(Cutter newCutter) {
    super.setCutter(newCutter);
    if (beginPt != null) {
      beginPt.setCutter(newCutter);	// does beginPt.makeDrawables() and fires PROP_NUM property change
    }
    goList.stream().forEach((gPt) -> {
      gPt.setCutter(newCutter);	// each fires PROP_NUM property change
    });
    makeDrawables();
  }

  /**
   * Get the beginning cutPoint.
   *
   * @return beginning cutPoint
   */
  public CutPoint getBeginPoint() {
    return beginPt;
  }

  /**
   * Get the Spiral.
   *
   * @return spiral
   */
  public Spiral getSpiral() {
    return spiral;
  }

  /**
   * Get the end point cut depth.
   *
   * @return end point cut depth
   */
  public double getEndDepth() {
    return endCutDepth;
  }

  /**
   * Set the end point cut depth. This fires a PROP_DEPTH propertyChange.
   *
   * @param d new end point cut depth
   */
  public synchronized void setEndDepth(double d) {
    double old = this.endCutDepth;
    this.endCutDepth = d;
    makeDrawables();
    pcs.firePropertyChange(PROP_DEPTH, old, endCutDepth);
  }

  /**
   * Get the width of the cut at the end cut depth.
   *
   * @return width of the cut
   */
  public double getEndWidthAtMax() {
    return cutter.getWidthOfCut(endCutDepth);
  }

  /**
   * Add an internal GoToPoint. Follow this path from end to start for the next
   * index, but don't follow after the last pass. This does not call
   * this.makeDrawables(). This fires a PROP_ADD property change with the new
   * point.
   *
   * @param gPt GoToPoint
   */
  public void addGoToPoint(GoToPoint gPt) {
    if (containsGoTo(gPt)) {
      return;		// don't add it again
    }
    gPt.setNum(this.num);	// make it the same number
    goList.add(gPt);
    gPt.addPropertyChangeListener(this);    // listen to changes in GoToPoints
    makeDrawables();
    pcs.firePropertyChange(PROP_ADD, null, gPt);
  }

  /**
   * Remove the given GoToPoint from the internal list. This does not call
   * this.makeDrawables(). This fires a PROP_REMOVE property change with the old
   * CutPoint.
   *
   * @param gPt GoToPoint
   */
  public void removeGoToPoint(GoToPoint gPt) {
    if (goList.contains(gPt)) {
      goList.remove(gPt);
      gPt.removePropertyChangeListener(this);
      gPt.clear();
      pcs.firePropertyChange(PROP_REMOVE, gPt, null);
    }
  }

  /**
   * Get the number of internal GoToPoints.
   *
   * @return number of internal GoToPoints
   */
  public int getNumGoTos() {
    return goList.size();
  }

  /**
   * Get a specific internal GoToPoint.
   *
   * @param n number
   * @return GoToPoint (or null if it does not exist)
   */
  public GoToPoint getGoToPoint(int n) {
    if ((n >= goList.size()) || (n < 0)) {
      return null;
    }
    return goList.get(n);
  }

  /**
   * Get all the GoToPoints representing a safe path around the curve.
   *
   * @return list of points, an empty list if no points
   */
  public List<GoToPoint> getAllGoTos() {
    return goList;
  }

  /**
   * Check if the given GoToPoint is in the goList.
   *
   * @param gPt GoToPoint
   * @return true: the given GoToPoint is in the goList
   */
  public boolean containsGoTo(GoToPoint gPt) {
    return goList.contains(gPt);
  }

  @Override
  protected void makeDrawables() {
    super.makeDrawables();
    text.setText(num + "E");
    beginPt.makeDrawables();
    goList.stream().forEach((gpt) -> {
      gpt.makeDrawables();
    });

    // Lines showing motion from end point through GoToPoints back to start point
    if (!goList.isEmpty()) {
      GoToPoint gPt0 = goList.get(0), gPt1;
      Line line = new Line(getPos2D(), gPt0.getX() - getX(), gPt0.getZ() - getZ(), GoToPoint.GOTO_COLOR);
      line.setStroke(Drawable.LIGHT_DOT);
      drawList.add(line);
      for (int i = 1; i < goList.size(); i++) {
        gPt0 = goList.get(i - 1);
        gPt1 = goList.get(i);
        line = new Line(gPt0.getPos2D(), gPt1.getX() - gPt0.getX(), gPt1.getZ() - gPt0.getZ(), GoToPoint.GOTO_COLOR);
        line.setStroke(Drawable.LIGHT_DOT);
        drawList.add(line);
      }
      gPt0 = goList.get(goList.size() - 1);
      line = new Line(gPt0.getPos2D(), beginPt.getX() - gPt0.getX(), beginPt.getZ() - gPt0.getZ(), GoToPoint.GOTO_COLOR);
      line.setStroke(Drawable.LIGHT_DOT);
      drawList.add(line);
    }
  }

  /**
   * Get an array of numbers representing the twist at each point on the cut
   * surface. Note: Twist starts at zero, so the twist is relative. You must add
   * the phase of the initial point to these numbers!
   *
   * @return an array of Point3D with x,y from the original pts and z=twist in
   * degrees (or null if there are no points given). Note: In lathe coordinates,
   * this is actually radius, z, c(degrees).
   */
  public Point3D[] getSurfaceTwist() {
    Curve cutCurve = outline.getCutCurve();
    if (cutCurve == null) {
      return null;
    }
    return spiral.getStyle().makeSpiral(cutCurve.subsetPoints(cutCurve.nearestPoint(beginPt.getPos2D()), cutCurve.nearestPoint(getPos2D())), spiral.getTwist(), spiral.getAmp());
  }

  /**
   * Get an array of numbers representing the twist at each point on the cut
   * surface reflected back to the nearest points on the cutter curve. Note:
   * Twist starts at zero, so the twist is relative. You must add the phase of
   * the initial point to these numbers!
   *
   * @return an array of Point3d with x,y from the original pts and z=twist in
   * degrees (or null if there are no points given). Note: In lathe coordinates,
   * this is actually radius, z, c(degrees).
   */
  public Point3D[] getCutterTwist() {
    // first find points on the surface (cutCurve)
    Point3D[] rzc = getSurfaceTwist();
    if (rzc == null) {
      return null;
    }
    switch (cutter.getFrame()) {
      case Fixed:
      case Drill:
      case ECF:
        return rzc;     // for Drill adn ECF the cut path is on the surface
    }

    // use a much finer resolution cutterPathCurve for smoothness
    Curve fineCut = outline.getCutterPathCurve(cutter);
    fineCut.reSample(outline.getResolution() / 10.0);

    // then look for the closest point on the fine resolution cut curve
    Point3D[] newPts = new Point3D[rzc.length];
    for (int i = 0; i < rzc.length; i++) {
      Point2D.Double near = fineCut.nearestPoint(new Point2D.Double(rzc[i].getX(), rzc[i].getY()));
      newPts[i] = new Point3D(near.x, near.y, rzc[i].getZ());
    }
    return newPts;
  }

  /**
   * Get an array of numbers representing the 3D position of spiral points on
   * the surface. Note: Twist starts at zero, so the twist is relative. You must
   * rotate the points as needed for proper rotation!
   *
   * @return a list of Point3D with XYZ (lathe coordinates) for the spiral
   */
  public ArrayList<Point3D> getPointsForLine() {
    return toXYZ(getSurfaceTwist());
  }

  /**
   * Convert a getSurfaceTwist or getCutterTwist (which is radius, z, c) to
   * actual x, y, z in lathe coordinates.
   *
   * @param rzc array from getSurfaceTwist or getCutterTwist
   * @return list of the points converted to x, y, z
   */
  protected ArrayList<Point3D> toXYZ(Point3D[] rzc) {
    ArrayList<Point3D> xyz = new ArrayList<>();
    if (rzc != null) {
      for (Point3D rzc1 : rzc) {
        xyz.add(new Point3D(
            rzc1.getX() * Math.cos(Math.toRadians(rzc1.getZ())), 
            rzc1.getX() * Math.sin(Math.toRadians(rzc1.getZ())), 
            rzc1.getY()));
      }
    }
    return xyz;
  }

  /**
   * Calculate the total distance of the list of x,y,z points.
   *
   * @param xyz array of x,y,z points
   * @return total distance
   */
  protected double getTotalDistance(List<Point3D> xyz) {
    double dist = 0.0;
    if (xyz.size() >= 2) {
      for (int i = 1; i < xyz.size(); i++) {
        dist += xyz.get(i).distance(xyz.get(i - 1));
      }
    }
    return dist;
  }

  @Override
  public void writeXML(PrintWriter out) {
    super.writeXML(out);	// for point
    beginPt.writeXML(out);  // for beginPt
    spiral.writeXML(out);   // <Spiral...
    goList.stream().forEach((gpt) -> {
      gpt.writeXML(out);
    });
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("SpiralCut.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }
}
