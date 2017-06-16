package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.controls.CoarseFine;
import static com.billooms.cutlist.Speed.FAST;
import static com.billooms.cutlist.Speed.VELOCITY;
import static com.billooms.cutpoints.LinePoint.LINEPT_COLOR;
import static com.billooms.cutpoints.LinePoint.LINEPT_COLOR2;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.simple.Line;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import com.billooms.patternbar.PatternBar;
import com.billooms.patterns.Patterns;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A spiral line that is cut over a distance with a pattern as with a straight
 * line machine.
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
@PaletteItemRegistration(paletteid = "MyPalette",
    category = "CutPoints",
    itemid = "SpiralLine",
    icon16 = "com/billooms/cutpoints/icons/SpiralLine16.png",
    icon32 = "com/billooms/cutpoints/icons/SpiralLine32.png",
    name = "SpiralLine",
    body = "SpiralLine body",
    tooltip = "Make spiral lines at regular intervals as with a straight line machine.")
public class SpiralLine extends SpiralCut {

  /** Property name used when changing the ScaleDepth flag */
  String PROP_SCALEDEPTH = PROP_PREFIX + "ScaleDepth";
  /** Property name used when changing the ScaleAmplitude flag */
  String PROP_SCALEAMPLITUDE = PROP_PREFIX + "ScaleAmplitude";

  /** Flag indicating that the depth should be scaled with diameter. */
  private boolean scaleDepth = false;
  /** Flag indicating that the amplitude should be scaled with diameter. */
  private boolean scaleAmplitude = false;

  /**
   * Construct a new SpiralLine from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   * @param patMgr pattern manager
   */
  public SpiralLine(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    super(element, cutMgr, outline);
    this.scaleDepth = CLUtilities.getBoolean(element, "scaleDepth", false);
    this.scaleAmplitude = CLUtilities.getBoolean(element, "scaleAmplitude", false);
    NodeList ipNodes = element.getElementsByTagName("LinePoint");
    beginPt = new LinePoint((Element) ipNodes.item(0), cutMgr, outline, patMgr);
    beginPt.setCutter(this.cutter);   // make sure beginPt uses same cutter
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  /**
   * Construct a new SpiralLine copying information from the given SpiralLine.
   *
   * @param pos new position
   * @param cpt SpiralLine to copy from
   */
  public SpiralLine(Point2D.Double pos, SpiralLine cpt) {
    super(pos, cpt);
    this.scaleDepth = cpt.getScaleDepth();
    this.scaleAmplitude = cpt.getScaleAmplitude();
    beginPt = new LinePoint(cpt.getBeginPoint().getPos2D(), (LinePoint) cpt.getBeginPoint());
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  /**
   * Construct a new SpiralLine at the given position with default values. This
   * is primarily used when adding a first SpiralRosette from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   * @param patMgr pattern manager
   */
  public SpiralLine(Point2D.Double pos, Cutter cut, Outline outline, Patterns patMgr) {
    super(pos, cut, outline);
    beginPt = new LinePoint(pos, cut, outline, patMgr);
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  @Override
  public void clear() {
    super.clear();
    beginPt.removePropertyChangeListener(this);
  }

  /**
   * Get the flag indicating if cut depth is proportional to radius of the
   * shape.
   *
   * @return true=cutDepth is proportional
   */
  public boolean getScaleDepth() {
    return scaleDepth;
  }

  /**
   * Set the flag indicating if cut depth is proportional to radius of the
   * shape. This fires a PROP_SCALEDEPTH property change with the old and new
   * values. When setting to true, it changes the endCutDepth.
   *
   * @param scale true=cutDepth is proportional
   */
  public void setScaleDepth(boolean scale) {
    if (scale) {
      setEndDepth(newEndDepth());
    }
    boolean old = this.scaleDepth;
    this.scaleDepth = scale;
    makeDrawables();
    pcs.firePropertyChange(PROP_SCALEDEPTH, old, scaleDepth);
  }

  /**
   * Get the flag indicating if cut amplitude is proportional to radius of the
   * shape.
   *
   * @return true=cutAmplitude is proportional
   */
  public boolean getScaleAmplitude() {
    return scaleAmplitude;
  }

  /**
   * Set the flag indicating if cut amplitude is proportional to radius of the
   * shape. 
   * This fires a PROP_SCALEAMPLITUDE property change with the old and new values. 
   *
   * @param scale true=cutDepth is proportional
   */
  public void setScaleAmplitude(boolean scale) {
    boolean old = this.scaleAmplitude;
    this.scaleAmplitude = scale;
    makeDrawables();
    pcs.firePropertyChange(PROP_SCALEAMPLITUDE, old, scaleAmplitude);
  }

  /**
   * Calculate a new end depth that is scaled proportional to the radius.
   *
   * @return new end depth
   */
  private double newEndDepth() {
    return ((LinePoint) getBeginPoint()).getDepth() * this.getX() / getBeginPoint().getX();
  }

  @Override
  public void setEndDepth(double d) {
    if (scaleDepth) {
      return;			// don't let the user change the end depth if it is scaled
    }
    super.setEndDepth(d);
  }

  @Override
  public void setX(double x) {
    super.setX(x);
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public void setZ(double z) {
    super.setZ(z);
    if (scaleDepth) {		// setZ might have moved X during reSnap()
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public void drag(Point2D.Double p) {
    super.drag(p);
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public void move(Point2D.Double pos) {
    super.move(pos);
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public void move(double x, double z) {
    super.move(x, z);
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public void setSnap(boolean s) {
    super.setSnap(s);
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public synchronized void snapToCurve() {
    super.snapToCurve();
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public synchronized void setDepth(double d) {
    super.setDepth(d);
    if (scaleDepth) {
      super.setEndDepth(newEndDepth());
    }
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<SpiralLine"
        + xmlCutPointInfo2()     // don't write depth
        + " scaleDepth='" + scaleDepth + "'"
        + " scaleAmplitude='" + scaleAmplitude + "'"
        + " endDepth='" + F4.format(endCutDepth) + "'"
        + ">");
    indentMore();
    super.writeXML(out);    // for beginPt, Pt, Spiral
    indentLess();
    out.println(indent + "</SpiralLine>");
  }

  /**
   * Calculate the end moveVector.
   *
   * It is the direction of movement that will produce the pattern. At present,
   * this is always perpendicular to the curve.
   *
   * @param scale scale factor (use 1.0 for normalized)
   * @return the end movement vector
   */
  protected Vector2d getEndMoveVector(double scale) {
    return getPerpVector(scale);
  }

  /**
   * Build the specific drawable shapes for a SpiralLine
   */
  @Override
  protected final void makeDrawables() {
    super.makeDrawables();
    pt.setColor(LINEPT_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(LINEPT_COLOR);	// the text
      drawList.get(1).setColor(LINEPT_COLOR);	// the cutter
    }

    if (endCutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // movement is always perpendicular
    Vector2d endMoveVectorS = getEndMoveVector(endCutDepth);
    // Line indicating direction of moveVectorS
    drawList.add(new Line(getPos2D(), endMoveVectorS.x, endMoveVectorS.y, LINEPT_COLOR));

    // cut extent
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(endMoveVectorS.y, endMoveVectorS.x) * 180.0 / Math.PI;
        drawList.add(new Arc(new Point2D.Double(getX() + endMoveVectorS.x, getZ() + endMoveVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, LINEPT_COLOR2));
        break;
      // Profile of drill & Fixed at cut depth
      case Fixed:
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endMoveVectorS.x, getZ() + endMoveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), LINEPT_COLOR2, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endMoveVectorS.x + v1.x, getZ() + endMoveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), LINEPT_COLOR2, SOLID_LINE));
        v1 = v1.rotate(180.0);
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endMoveVectorS.x + v1.x, getZ() + endMoveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), LINEPT_COLOR2, SOLID_LINE));
        break;
    }
  }

  /**
   * Make an array of additional twist in degrees made by the PatternBar from
   * the given SurfaceTwist.
   *
   * @param tw SurfaceTwist
   * @return additional twist in degrees for each point.
   */
  private double[] makePatternTwist(Point3D[] tw) {
    PatternBar pb = ((LinePoint) beginPt).getPatternBar();
    double startR = beginPt.getX();
    double[] addTwist = new double[tw.length];
    double dist = 0.0;		// cummulative distance along tw
    if (scaleAmplitude) {
      addTwist[0] = degreesForD(pb.getAmplitudeAt(dist) * tw[0].getX()/startR, tw[0].getX());	// reminder: tw[].x is radius in lathe coords
    } else {
      addTwist[0] = degreesForD(pb.getAmplitudeAt(dist), tw[0].getX());	// reminder: tw[].x is radius in lathe coords
    }
    for (int i = 1; i < tw.length; i++) {
      dist += distance(tw[i - 1], tw[i]);
      if (scaleAmplitude) {
        addTwist[i] = degreesForD(pb.getAmplitudeAt(dist) * tw[i].getX()/startR, tw[i].getX());
      } else {
        addTwist[i] = degreesForD(pb.getAmplitudeAt(dist), tw[i].getX());
      }
    }
    return addTwist;
  }

  /**
   * Calculate the straight line distance between the two given points. Each
   * point is radius, z, angle (degrees) in lathe coordinates
   *
   * @param p1
   * @param p2
   * @return straight line distance
   */
  private double distance(Point3D p1, Point3D p2) {
    Point3D p1x = new Point3D(p1.getX() * Math.sin(Math.toRadians(p1.getZ())), p1.getX() * Math.cos(Math.toRadians(p1.getZ())), p1.getY());
    Point3D p2x = new Point3D(p2.getX() * Math.sin(Math.toRadians(p2.getZ())), p2.getX() * Math.cos(Math.toRadians(p2.getZ())), p2.getY());
    return Math.abs(p1x.distance(p2x));
  }

  /**
   * Calculate the rotation in degrees for the given distance along the
   * circumference at the given radius.
   *
   * @param d distance along the circumference
   * @param r radius
   * @return degrees
   */
  private double degreesForD(double d, double r) {
    return 360.0 * d / (Math.PI * 2.0 * r);
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    Point3D[] tw = getSurfaceTwist();
    double[] addTwist = makePatternTwist(tw);
    for (int i = 0; i < addTwist.length; i++) {
      tw[i] = new Point3D(tw[i].getX(), tw[i].getY(), tw[i].getZ() + addTwist[i]);  // add the phase of the pattern to all twist[]
    }
    ArrayList<Point3D> xyz = toXYZ(tw);
    ((LinePoint)beginPt).make3DLines(xyz);
    list3D.addAll(beginPt.list3D);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("SpiralLine.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }
  
  @Override
  protected ArrayList<CutPoint> makeListOfPoints() { 
    ArrayList<CutPoint> list = new ArrayList<>();
    
    Point3D[] rzcCutter = getCutterTwist();     // This is on the cutter curve
    Point3D[] rzcSurface = getSurfaceTwist();   // This is on the surface curve
    ArrayList<Point3D> xyzSurface = toXYZ(rzcSurface);

    double totLength = getTotalDistance(xyzSurface);	// actual length on the spiral
    if (totLength <= 0.0) {         // so we don't divide by zero further down
//      beginPt.cutSurface(surface);	// no movement, so just cut this one place
      return list;
    }

    double[] addTwist = makePatternTwist(getSurfaceTwist());

    double startDepth = beginPt.getDepth();
    double deltaDepth = endCutDepth - startDepth;
    double beginPhase = ((LinePoint) beginPt).getPhase();
    double repeat = ((LinePoint) beginPt).getRepeat();
    
    double cumLength = 0.0;
    for (int i = 0; i < rzcSurface.length; i++) {  // xyz is the same length as tw
      if (i > 0) {
        // This uses the same calculation as within getTotalDistance()
        cumLength += xyzSurface.get(i).distance(xyzSurface.get(i - 1));
      }
      // newPt starts out as a copy of the beginPt then is modified along the length of the spiral
      LinePoint newPt = new LinePoint(beginPt.getPos2D(), (LinePoint) beginPt);	// this is modified along the length of the spiral
      newPt.setPhase(beginPhase + (rzcSurface[i].getZ() + addTwist[i]) * repeat);
      newPt.move(rzcCutter[i].getX(), rzcCutter[i].getY());
      if (scaleDepth) {
        newPt.setDepth(startDepth * newPt.getX() / getBeginPoint().getX());
      } else {
        newPt.setDepth(startDepth + cumLength / totLength * deltaDepth);
      }
      list.add(newPt);
    }
    return list;
  }
  
  /**
   * Make instructions for this CutPoint.
   * Note: This doesn't use makeListOfPoints() because we want to cut long
   * lines rather than lots of individual IndexPoints
   *
   * @param controls control panel data
   * @param stepsPerRot steps per rotation
   */
  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot) {
    cutList.comment("SpiralLine " + num);
    cutList.comment("Cutter: " + cutter);

    Point3D[] rzc = getCutterTwist();			// x,y and z=twist indegrees for each point
    ArrayList<Point3D> xyz = toXYZ(rzc);        // actual lathe x, y, z points

    double totLength = getTotalDistance(xyz);	// actual length on the spiral
    if (totLength <= 0.0) {		// so we don't divide by zero further down
//			beginPt.makeInstructions(passDepth, passStep, lastDepth, lastStep, stepsPerRot);	// no movement, so just cut this one place
      return;
    }

    double[] addTwist = makePatternTwist(getSurfaceTwist());	// addTwist will have same length as cutTW

    Vector2d[] cuts = new Vector2d[rzc.length];		// array of cut depth/direction at each point
    double startDepth = ((LinePoint) beginPt).getDepth();
    double deltaDepth = endCutDepth - startDepth;
    // use finer resolution for smoothness
    Curve fineCut = outline.getCutterPathCurve(cutter);
    fineCut.reSample(outline.getResolution() / 10.0);
    double cumLength = 0.0, depth;
    for (int i = 0; i < rzc.length; i++) {
      if (i > 0) {
        cumLength += xyz.get(i).distance(xyz.get(i - 1));
      }
      if (scaleDepth) {
        depth = startDepth * xyz.get(i).getX() / getBeginPoint().getX();
      } else {
        depth = startDepth + cumLength / totLength * deltaDepth;
      }
      Vector2d perp = fineCut.perpendicular(new Point2D.Double(rzc[i].getX(), rzc[i].getY()), cutter.getLocation().isFrontInOrBackOut());
      perp.scale(depth);
      cuts[i] = perp;
    }

    Vector2d safety = ((LinePoint) beginPt).getMoveVector(LinePoint.LINEPOINT_SAFETY);
    Vector2d endSafety = this.getEndMoveVector(LinePoint.LINEPOINT_SAFETY);
    double beginPhase = ((LinePoint) beginPt).getPhase();
    double repeat = ((LinePoint) beginPt).getRepeat();
    String mask = ((LinePoint) beginPt).getMask();
    String fullMask = mask;
    if (!mask.isEmpty()) {
      while (fullMask.length() < repeat) {
        fullMask += mask;	// fill out to full length
      }
    }

    double lastC = 0.0;
    cutList.spindleWrapCheck();
    boolean didACut = false;    // flag to indicate we did a cut already
    for (int i = 0; i < repeat; i++) {
      if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
        if (didACut) {      // Follow the GoToPoints back to the beginning
          for (GoToPoint gPt : goList) {
            cutList.goToXZ(FAST, gPt.getX(), gPt.getZ());
          }
        } 
        double cAngle = 360.0 * (double) i / repeat - beginPhase / repeat;	// minus to match rosette phase
        cutList.goToXZC(FAST, beginPt.getX() - safety.x, beginPt.getZ() - safety.y, cAngle - rzc[0].getZ() - addTwist[0]);	// go there first without cutting
        lastC = cAngle - rzc[0].getZ() - addTwist[0];
        for (int j = 0; j < rzc.length; j++) {
          cutList.goToXZC(VELOCITY, rzc[j].getX() + cuts[j].x, rzc[j].getY() + cuts[j].y, cAngle - rzc[j].getZ() - addTwist[j]);
          lastC = cAngle - rzc[j].getZ() - addTwist[j];
        }
        cutList.goToXZ(VELOCITY, getX() - endSafety.x, getZ() - endSafety.y);			// pull out before moving
        didACut = true;
      }
    }
    // always go to 360.0 at the end of the SpiralIndexPoint
    if (lastC != 360.0) {
      cutList.turn(360.0);
    }
    cutList.spindleWrapCheck();
    // Note: we don't go back to the beginning when all cuts are done
  }

}
