package com.billooms.cutpoints;

import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.controls.CoarseFine;
import com.billooms.cutpoints.RosettePoint.Motion;
import static com.billooms.cutpoints.RosettePoint.ROSETTE_COLOR;
import static com.billooms.cutpoints.RosettePoint.ROSETTE_COLOR2;
import static com.billooms.cutpoints.RosettePoint.ROSETTE_COLOR3;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.RotMatrix;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.simple.Line;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import com.billooms.patterns.Patterns;
import com.billooms.rosette.Rosette;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Make a spiral with a RosettePoint.
 *
 * Note that any optional GoToPoints are not used and the location at the end of
 * cutting will be above the end point.
 *
 * Also note: If a rosette's starting amplitude is the same as the starting cut
 * depth, then the rosettes's amplitude will be scaled along with the cut depth
 * and at the end the rosettes' amplitude will be the same as the end cut depth.
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
    itemid = "SpiralRosette",
    icon16 = "com/billooms/cutpoints/icons/SpiralRos16.png",
    icon32 = "com/billooms/cutpoints/icons/SpiralRos32.png",
    name = "SpiralRosette",
    body = "SpiralRosette body",
    tooltip = "Make spirals at with a rosette.")
public class SpiralRosette extends SpiralCut {

  /**
   * Construct a new SpiralRosette from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   * @param patMgr pattern manager
   */
  public SpiralRosette(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    super(element, cutMgr, outline);
    NodeList ipNodes = element.getElementsByTagName("RosettePoint");
    beginPt = new RosettePoint((Element) ipNodes.item(0), cutMgr, outline, patMgr);
    beginPt.setCutter(this.cutter);   // make sure beginPt uses same cutter
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  /**
   * Construct a new SpiralRosette copying information from the given
   * SpiralRosette.
   *
   * @param pos new position
   * @param cpt SpiralRosette to copy from
   */
  public SpiralRosette(Point2D.Double pos, SpiralRosette cpt) {
    super(pos, cpt);
    beginPt = new RosettePoint(cpt.getBeginPoint().getPos2D(), (RosettePoint) cpt.getBeginPoint());
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  /**
   * Construct a new SpiralRosette at the given position with default values.
   * This is primarily used when adding a first SpiralRosette from the
   * OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   * @param patMgr pattern manager
   */
  public SpiralRosette(Point2D.Double pos, Cutter cut, Outline outline, Patterns patMgr) {
    super(pos, cut, outline);
    beginPt = new RosettePoint(pos, cut, outline, patMgr);
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }
  
  @Override
  public void clear() {
    super.clear();
    beginPt.removePropertyChangeListener(this);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<SpiralRosette"
        + xmlCutPointInfo2()     // don't write depth
        + " endDepth='" + F4.format(endCutDepth) + "'"
        + ">");
    indentMore();
    super.writeXML(out);    // for beginPt, Pt, Spiral
    indentLess();
    out.println(indent + "</SpiralRosette>");
  }
  
  /** 
   * Get the end move vector for PERP. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut.
   * 
   * @return normalized perpendicular vector
   */
  private Vector2d endPerpVectorN() {
    Vector2d perpVectN = getPerpVector(1.0);
    return new Vector2d(-perpVectN.x, -perpVectN.y);	// Always opposite direction from perpVector
  }
  
  /** 
   * Get the end move vector for TANGENT. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut.
   * 
   * @return normalized tangent vector
   */
  private Vector2d endTanVectorN() {
    Vector2d perpVectN = getPerpVector(1.0);
    switch (cutter.getLocation()) {
      case FRONT_INSIDE:
      default:
        return new Vector2d(perpVectN.y, -perpVectN.x);	// back toward center and downward
      case BACK_INSIDE:
        return new Vector2d(-perpVectN.y, perpVectN.x);	// forward toward center and downward
      case FRONT_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          return new Vector2d(-perpVectN.y, perpVectN.x);	// move out to front and downward
        } else {						// bottom of a shape
          return new Vector2d(perpVectN.y, -perpVectN.x);	// move out to front and upward
        }
      case BACK_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          return new Vector2d(perpVectN.y, -perpVectN.x);	// move out to back and downward
        } else {						// bottom of a shape
          return new Vector2d(-perpVectN.y, perpVectN.x);	// move out to back and upward
        }
    }
  }

  /**
   * Get the end scaled movement vector for the rosette. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut. 
   * In the case of BOTH or PERPTAN, it is the movement of only the first rosette.
   *
   * @return scaled movement vector
   */
  private Vector2d getEndMoveVectorS() {
    double xMove = 0.0, zMove = 0.0;
    RosettePoint rPt = (RosettePoint) beginPt;
    switch (rPt.getMotion()) {
      default:
      case PERP:
      case PERPTAN:
        Vector2d perpS = endPerpVectorN();
        if (rPt.getDepth() == rPt.getRosette().getPToP()) {
          perpS.scale(endCutDepth);   // special case: taper the rosette amplitude
        } else {
          perpS.scale(rPt.getRosette().getPToP());
        }
        return perpS;
      case TANGENT:
        Vector2d tanS = endTanVectorN();
        if (rPt.getDepth() == rPt.getRosette().getPToP()) {
          tanS.scale(endCutDepth);   // special case: taper the rosette amplitude
        } else {
          tanS.scale(rPt.getRosette().getPToP());
        }
        return tanS;

      case BOTH:
        if (rPt.getDepth() == rPt.getRosette().getPToP()) {
          xMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          xMove = rPt.getRosette().getPToP();      // otherwise, always get the motion from the primary rosette
        }
        break;
      case PUMP:
        if (rPt.getDepth() == rPt.getRosette().getPToP()) {
          zMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          zMove = rPt.getRosette().getPToP();
        }
        break;
      case ROCK:
        if (rPt.getDepth() == rPt.getRosette().getPToP()) {
          xMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          xMove = rPt.getRosette().getPToP();
        }
        break;
    }
    return rPt.correctForCutter(xMove, zMove);
  }

  /**
   * Get the end scaled movement vector for rosette2. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut. 
   * In the case of BOTH or PERPTAN, it is the movement of only the second rosette.
   *
   * @return scaled movement vector
   */
  private Vector2d getEndMoveVector2S() {
    double xMove = 0.0, zMove = 0.0;
    RosettePoint rPt = (RosettePoint) beginPt;
    switch (rPt.getMotion()) {
      default:      // should not call this except for BOTH and PERPTAN
      case PUMP:
      case ROCK:
      case PERP:
      case TANGENT:
        return new Vector2d();
      case PERPTAN:
        Vector2d tan = endTanVectorN();
        if (rPt.getDepth() == rPt.getRosette().getPToP()) {
          tan.scale(endCutDepth);   // special case: taper the rosette amplitude
        } else {
          tan.scale(rPt.getRosette().getPToP());
        }
        return tan;

      case BOTH:
        if (rPt.getDepth() == rPt.getRosette2().getPToP()) {
          zMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          zMove = rPt.getRosette2().getPToP();     // get the z movement from rosette2 only when there is both
        }
        break;
    }
    return rPt.correctForCutter(xMove, zMove);
  }

  /**
   * Build the specific drawable shapes for an SpiralIndex
   */
  @Override
  protected final void makeDrawables() {
    super.makeDrawables();
    pt.setColor(ROSETTE_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(ROSETTE_COLOR);	// the text
      drawList.get(1).setColor(ROSETTE_COLOR);	// the cutter
    }

    if (endCutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // Line indicating direction of the cut into the shape (which will always be perpendicular) 
    Vector2d endPerpVectorS = getPerpVector(endCutDepth);	// maximum cut displacement
    drawList.add(new Line(getPos2D(), endPerpVectorS.x, endPerpVectorS.y, ROSETTE_COLOR));

    // cut extent
    Vector2d endMoveVectorS = getEndMoveVectorS();		// scaled movement vector for rosette
    Vector2d endMoveVector2S = getEndMoveVector2S();	// scaled movement vector for rosette2
    Motion motion = ((RosettePoint) beginPt).getMotion();
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(endPerpVectorS.y, endPerpVectorS.x) * 180.0 / Math.PI;
        drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + endMoveVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR2));
        if (motion.usesBoth()) {
          drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVector2S.x, getZ() + endPerpVectorS.y + endMoveVector2S.y),
              cutter.getRadius(),
              cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR3));
        }
        drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x, getZ() + endPerpVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR));
        break;
      // Profile of drill & Fixed at cut depth
      case Fixed:
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + endMoveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVector2S.x, getZ() + endPerpVectorS.y + endMoveVector2S.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x, getZ() + endPerpVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + v1.y + endMoveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVector2S.x, getZ() + endPerpVectorS.y + v1.y + endMoveVector2S.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x, getZ() + endPerpVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        v1 = v1.rotate(180.0);
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + v1.y + endMoveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVector2S.x, getZ() + endPerpVectorS.y + v1.y + endMoveVector2S.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x, getZ() + endPerpVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
    }
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    // Note: the spirals are made from Rock rosette when BOTH are specified (not from pumping rosette)
    String mask = "";
    if (((RosettePoint) beginPt).getRosette() instanceof Rosette) {
      mask = ((Rosette)((RosettePoint) beginPt).getRosette()).getMask();
    }
    int repeat = ((RosettePoint) beginPt).getRosette().getRepeat();
    double phase = ((RosettePoint) beginPt).getRosette().getPhase();

    String fullMask = mask;
    if (!fullMask.isEmpty()) {
      while (fullMask.length() < repeat) {
        fullMask += mask;	// fill out to full length
      }
    }
    if ((((RosettePoint) beginPt).getRosette() instanceof Rosette)
        && (((Rosette) ((RosettePoint) beginPt).getRosette()).usesSymmetryWid())) {
      // This is for simple rosettes that use Symmetry Widths
      double[] angles = ((Rosette) ((RosettePoint) beginPt).getRosette()).getAngleBreaks();
      for (int i = 0; i < repeat; i++) {
        Line3D line = new Line3D(getPointsForLine());
        line.rotate(RotMatrix.Axis.Z, (phase / repeat) - angles[i]);
        list3D.add(line);
      }
    } else {
      // Simple rosettes that do NOT use Symmetry Widths
      // TODO: Compound Rosettes also use this, but repeat = 1 for only one line
      double ang = phase / repeat;
      for (int i = 0; i < repeat; i++) {
        if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
          Line3D line = new Line3D(getPointsForLine());
          line.rotate(RotMatrix.Axis.Z, ang);
          list3D.add(line);
        }
        ang -= 360.0 / (double) repeat;
      }
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("SpiralRosette.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }
  
  @Override
  protected ArrayList<CutPoint> makeListOfPoints() { 
    ArrayList<CutPoint> list = new ArrayList<>();
    
    Point3D[] rzcCutter = getCutterTwist();     // This is on the cutter curve
    Point3D[] rzcSurface = getSurfaceTwist();   // This is on the surface curve
    ArrayList<Point3D> xyzSurface = toXYZ(rzcSurface);

    double totLength = getTotalDistance(xyzSurface);	// actual length on the spiral on the surface
    if (totLength <= 0.0) {         // so we don't divide by zero further down
      list.add((RosettePoint) beginPt);
      return list;
    }

    double startDepth = beginPt.getDepth();
    double rosStartAmp = ((RosettePoint)beginPt).getRosette().getPToP();
    double rosStartPhase = ((RosettePoint)beginPt).getRosette().getPhase();
    int repeat = ((RosettePoint)beginPt).getRosette().getRepeat();

    double ros2StartAmp = 0.0, ros2StartPhase = 0.0;
    int repeat2 = 0;
    if (((RosettePoint)beginPt).getMotion().usesBoth()) {
      ros2StartAmp = ((RosettePoint)beginPt).getRosette2().getPToP();
      ros2StartPhase = ((RosettePoint)beginPt).getRosette2().getPhase();
      repeat2 = ((RosettePoint)beginPt).getRosette2().getRepeat();
    }
    
    double cumLength = 0.0;
    double rStart = rzcSurface[0].getX();
    double rEnd = rzcSurface[rzcSurface.length-1].getX();
    for (int i = 0; i < rzcSurface.length; i++) {
      if (i > 0) {
        // This uses the same calculation as within getTotalDistance()
        cumLength += xyzSurface.get(i).distance(xyzSurface.get(i - 1));
      }
      
      double radiusRatio = rzcSurface[i].getX() / rStart;
      double cumRatio = cumLength / totLength;
      double scaledDepth;
      if (Math.abs(rEnd) < (outline.getResolution() / 2.0)) {
        // if the end radius is essentially zero, then scale depth proportional to radius
        scaledDepth = startDepth * radiusRatio;
      } else if (endCutDepth == 0.0) {
        // if the end depth is zero, then scale depth proportional to change in radius
        scaledDepth = proportion(rStart, rzcSurface[i].getX(), rEnd, startDepth, endCutDepth);
      } else {
        double scaledEndDepth = endCutDepth * rStart / rEnd;
        scaledDepth = (startDepth + cumRatio * (scaledEndDepth - startDepth)) * radiusRatio;
      }
      
      // If this changes, be sure to change Cutpoints.spiralToPoints too!!!
      
    // newPt starts out as a copy of the beginPt then is modified along the length of the spiral
      RosettePoint newPt = new RosettePoint(beginPt.getPos2D(), (RosettePoint) beginPt);
      newPt.setDepth(scaledDepth);
      if (rosStartAmp == startDepth) {    // scale rosette amplitude if it's the same as the start depth
        newPt.getRosette().setPToP(scaledDepth);
      }
      newPt.getRosette().setPhase(rosStartPhase + rzcSurface[i].getZ() * (double)repeat);
      if (newPt.getMotion().usesBoth()) {
        if (ros2StartAmp == startDepth) {   // scale rosette amplitude if it's the same as the start depth
          newPt.getRosette2().setPToP(scaledDepth);
        }
        newPt.getRosette2().setPhase(ros2StartPhase + rzcSurface[i].getZ() * (double)repeat2);
      }
      newPt.move(rzcCutter[i].getX(), rzcCutter[i].getY());
      list.add(newPt);
//      System.out.println("x:" + F3.format(rzcSurface[i].getX())  + " -> " + F3.format(newPt.getX())
//        + " z:" + F3.format(rzcSurface[i].getY()) + " -> " + F3.format(newPt.getZ())
//        + " depth:" + F3.format(newPt.getDepth()) + " amp:" + F3.format(newPt.getRosette().getPToP()));
    }
    return list;
  }

  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot) {
    cutList.comment("SpiralRosette " + num);
    cutList.comment("Cutter: " + cutter);

    for (CutPoint cPt : makeListOfPoints()) {
      cPt.makeInstructions(controls, stepsPerRot);
    }
  }

}
