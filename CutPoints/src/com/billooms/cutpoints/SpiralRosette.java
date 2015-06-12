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
import com.billooms.cutpoints.surface.Surface;
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
import javax.swing.ProgressMonitor;
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
   * Get the normalized end movement vector for PERP and CONTOUR motion. It is
   * the direction of movement that will produce the rosette pattern. It will be
   * in a direction AWAY from the point of deepest cut.
   *
   * @return the normalized end movement vector
   */
  protected Vector2d getEndMoveVectorN() {
    Vector2d perpVectN = getPerpVector(1.0);
    Vector2d moveN = new Vector2d();
    Motion motion = ((RosettePoint) beginPt).getMotion();
    if (motion == RosettePoint.Motion.PERP) {
      moveN = new Vector2d(-perpVectN.x, -perpVectN.y);	// Always opposite direction from perpVector
    } else if (motion == RosettePoint.Motion.TANGENT) {			// movement is right angle to perpVector
      switch (cutter.getLocation()) {
        case FRONT_INSIDE:
        default:
          moveN = new Vector2d(perpVectN.y, -perpVectN.x);	// back toward center and downward
          break;
        case BACK_INSIDE:
          moveN = new Vector2d(-perpVectN.y, perpVectN.x);	// forward toward center and downward
          break;
        case FRONT_OUTSIDE:
          if (isTopOutside()) {			// top of a shape
            moveN = new Vector2d(-perpVectN.y, perpVectN.x);	// move out to front and downward
          } else {						// bottom of a shape
            moveN = new Vector2d(perpVectN.y, -perpVectN.x);	// move out to front and upward
          }
          break;
        case BACK_OUTSIDE:
          if (isTopOutside()) {			// top of a shape
            moveN = new Vector2d(perpVectN.y, -perpVectN.x);	// move out to back and downward
          } else {						// bottom of a shape
            moveN = new Vector2d(-perpVectN.y, perpVectN.x);	// move out to back and upward
          }
          break;
      }
    }
    return moveN;
  }

  /**
   * Get the scaled end movement vector. It is the direction of movement that
   * will produce the rosette pattern. It will be in a direction AWAY from the
   * point of deepest cut. In the case of BOTH, it is the total movement of both
   * rosettes together.
   *
   * @return
   */
  private Vector2d getEndMoveVectorS() {
    Vector2d moveS;
    double xMove = 0.0, zMove = 0.0;
    RosettePoint rPt = (RosettePoint) beginPt;
    switch (rPt.getMotion()) {
      default:
      case PERP:
      case TANGENT:
        moveS = getEndMoveVectorN();
        if (((RosettePoint) beginPt).getDepth() == ((RosettePoint) beginPt).getRosette().getPToP()) {
          moveS.scale(endCutDepth);   // special case: taper the rosette amplitude
        } else {
          moveS.scale(rPt.getRosette().getPToP());
        }
        return moveS;

      case BOTH:
        if (((RosettePoint) beginPt).getDepth() == ((RosettePoint) beginPt).getRosette2().getPToP()) {
          zMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          zMove = rPt.getRosette2().getPToP();     // get the z movement from rosette2 only when there is both
        }
        if (((RosettePoint) beginPt).getDepth() == ((RosettePoint) beginPt).getRosette().getPToP()) {
          xMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          xMove = rPt.getRosette().getPToP();      // otherwise, always get the motion from the primary rosette
        }
        break;
      case PUMP:
        if (((RosettePoint) beginPt).getDepth() == ((RosettePoint) beginPt).getRosette().getPToP()) {
          zMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          zMove = rPt.getRosette().getPToP();
        }
        break;
      case ROCK:
        if (((RosettePoint) beginPt).getDepth() == ((RosettePoint) beginPt).getRosette().getPToP()) {
          xMove = endCutDepth;    // special case: taper the rosette amplitude
        } else {
          xMove = rPt.getRosette().getPToP();
        }
        break;
    }
    switch (cutter.getLocation()) {		// correct the sign of the motion for location of cutter
      case FRONT_INSIDE:
      default:
        moveS = new Vector2d(-xMove, zMove);	// move back to center and/or upward
        break;
      case BACK_INSIDE:
        moveS = new Vector2d(xMove, zMove);		// move forward to center and/or upward
        break;
      case FRONT_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          moveS = new Vector2d(xMove, zMove);		// move out to front and/or up
        } else {						// bottom of a shape
          moveS = new Vector2d(xMove, -zMove);	// move out to front and/or down
        }
        break;
      case BACK_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          moveS = new Vector2d(-xMove, zMove);	// move out to back and/or up
        } else {						// bottom of a shape
          moveS = new Vector2d(-xMove, -zMove);	// move out to back and/or down
        }
        break;
    }
    return moveS;
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
    Vector2d endMoveVectorS = getEndMoveVectorS();		// scaled movement vector
    Motion motion = ((RosettePoint) beginPt).getMotion();
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(endPerpVectorS.y, endPerpVectorS.x) * 180.0 / Math.PI;
        if (motion.equals(RosettePoint.Motion.BOTH)) {
          drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVectorS.x, getZ() + endPerpVectorS.y),
              cutter.getRadius(),
              cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR2));
          drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x, getZ() + endPerpVectorS.y + endMoveVectorS.y),
              cutter.getRadius(),
              cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR3));
        } else {
          drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + endMoveVectorS.y),
              cutter.getRadius(),
              cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR2));
        }
        drawList.add(new Arc(new Point2D.Double(getX() + endPerpVectorS.x, getZ() + endPerpVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR));
        break;
      // Profile of drill at cut depth
      case Drill:
        if (motion.equals(RosettePoint.Motion.BOTH)) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVectorS.x, getZ() + endPerpVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x, getZ() + endPerpVectorS.y + endMoveVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        } else {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + endMoveVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x, getZ() + endPerpVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        if (motion.equals(RosettePoint.Motion.BOTH)) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + v1.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x, getZ() + endPerpVectorS.y + v1.y + endMoveVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        } else {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + v1.y + endMoveVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x, getZ() + endPerpVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        v1 = v1.rotate(180.0);
        if (motion.equals(RosettePoint.Motion.BOTH)) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + v1.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x, getZ() + endPerpVectorS.y + v1.y + endMoveVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        } else {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endPerpVectorS.x + v1.x + endMoveVectorS.x, getZ() + endPerpVectorS.y + v1.y + endMoveVectorS.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
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
    if (((RosettePoint) beginPt).getRosette() instanceof Rosette) {
      if (((Rosette)((RosettePoint) beginPt).getRosette()).usesSymmetryWid()) {
        double[] angles = ((Rosette)((RosettePoint) beginPt).getRosette()).getAngleBreaks();
        for (int i = 0; i < repeat; i++) {
          Line3D line = new Line3D(getPointsForLine());
          line.rotate(RotMatrix.Axis.Z, (phase / repeat) - angles[i]);
          list3D.add(line);
        }
      }
    } else {
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
  public synchronized void cutSurface(Surface surface, ProgressMonitor monitor) {
    Point3D[] rzc = getCutterTwist();
    ArrayList<Point3D> xyz = toXYZ(rzc);

    double totLength = getTotalDistance(xyz);	// actual length on the spiral
    if (totLength <= 0.0) {         // so we don't divide by zero further down
      beginPt.cutSurface(surface, monitor);	// no movement, so just cut this one place
      return;
    }

    // modPt starts out as a copy of the beginPt then is modified along the length of the spiral
    RosettePoint modPt = new RosettePoint(beginPt.getPos2D(), (RosettePoint) beginPt);
    double startDepth = modPt.getDepth();
    double deltaDepth = endCutDepth - startDepth;

    double rosStartAmp = modPt.getRosette().getPToP();
    double deltaRosAmp = 0.0;
    if (rosStartAmp == startDepth) {
      deltaRosAmp = endCutDepth - rosStartAmp;  // taper the rocking/perp amplitude
    }
    double rosPhase = modPt.getRosette().getPhase();

    double ros2StartAmp = 0.0, deltaRos2Amp = 0.0, ros2StartPhase = 0.0;
    if (modPt.getMotion().equals(Motion.BOTH)) {
      ros2StartAmp = modPt.getRosette2().getPToP();
      if (ros2StartAmp == startDepth) {
        deltaRos2Amp = endCutDepth - ros2StartAmp;  // taper the pumping amplitude
      }
      ros2StartPhase = modPt.getRosette2().getPhase();
    }

    // NOTE: if Motion == BOTH, then pump rosette gets the same repeat!
    int repeat = modPt.getRosette().getRepeat();
    double cumLength = 0.0;
    
    double rStart = rzc[0].getX();
    double deltaR = rzc[rzc.length-1].getX() - rStart;
    for (int i = 0; i < rzc.length; i++) {
      if (i > 0) {
        cumLength += xyz.get(i).distance(xyz.get(i - 1));
      }
      // Cut depth (and rosette amplitude) should always scale with radius
      // unless radius is constant -- then scale with cumLength
      if (deltaR != 0.0) {
        modPt.setDepth(startDepth + (rzc[i].getX() - rStart) / deltaR * deltaDepth);
        modPt.getRosette().setPToP(rosStartAmp + (rzc[i].getX() - rStart) / deltaR * deltaRosAmp);
      } else {
        modPt.setDepth(startDepth + cumLength / totLength * deltaDepth);
        modPt.getRosette().setPToP(rosStartAmp + cumLength / totLength * deltaRosAmp);
      }
      modPt.getRosette().setPhase(rosPhase + rzc[i].getZ() * repeat);
      if (modPt.getMotion().equals(Motion.BOTH)) {
        if (deltaR != 0.0) {
          modPt.getRosette2().setPToP(ros2StartAmp + (rzc[i].getX() - rStart) / deltaR * deltaRos2Amp);
        } else {
          modPt.getRosette2().setPToP(ros2StartAmp + cumLength / totLength * deltaRos2Amp);
        }
        modPt.getRosette2().setPhase(ros2StartPhase + rzc[i].getZ() * repeat);
      }
      modPt.move(rzc[i].getX(), rzc[i].getY());
      monitor.setProgress(num+1);
      monitor.setNote("CutPoint " + getNum() + ": " + i + "/" + rzc.length + "\n");
      modPt.cutSurface(surface, monitor);
      if (monitor.isCanceled()) {
        break;
      }
    }
  }

  @Override
  public void makeInstructions(double passDepth, int passStep, double lastDepth, int lastStep, int stepsPerRot, CoarseFine.Rotation rotation) {
    cutList.comment("SpiralRosette " + num);
    cutList.comment("Cutter: " + cutter);

    Point3D[] rzc = getCutterTwist();
    ArrayList<Point3D> xyz = toXYZ(rzc);

    double totLength = getTotalDistance(xyz);	// actual length on the spiral
    if (totLength <= 0.0) {         // so we don't divide by zero further down
      beginPt.makeInstructions(passDepth, passStep, lastDepth, lastStep, stepsPerRot, rotation);	// no movement, so just cut this one place
      return;
    }

    // modPt starts out as a copy of the beginPt then is modified along the length of the spiral
    RosettePoint modPt = new RosettePoint(beginPt.getPos2D(), (RosettePoint) beginPt);
    double startDepth = modPt.getDepth();
    double deltaDepth = endCutDepth - startDepth;

    double rosStartAmp = modPt.getRosette().getPToP();
    double deltaRosAmp = 0.0;
    if (rosStartAmp == startDepth) {
      deltaRosAmp = endCutDepth - rosStartAmp;  // taper the rocking/perp amplitude
    }
    double rosStartPhase = modPt.getRosette().getPhase();

    double ros2StartAmp = 0.0, deltaRos2Amp = 0.0, ros2StartPhase = 0.0;
    if (modPt.getMotion().equals(Motion.BOTH)) {
      ros2StartAmp = modPt.getRosette2().getPToP();
      if (ros2StartAmp == startDepth) {
        deltaRos2Amp = endCutDepth - ros2StartAmp;  // taper the pumping amplitude
      }
      ros2StartPhase = modPt.getRosette2().getPhase();
    }

    // NOTE: if Motion == BOTH, then pump rosette gets the same repeat!
    int repeat = modPt.getRosette().getRepeat();
    double cumLength = 0.0;
    
    double rStart = rzc[0].getX();
    double deltaR = rzc[rzc.length-1].getX() - rStart;
    for (int i = 0; i < rzc.length; i++) {
      if (i > 0) {
        cumLength += xyz.get(i).distance(xyz.get(i - 1));
      }
      // Cut depth (and rosette amplitude) should always scale with radius
      // unless radius is constant -- then scale with cumLength
      if (deltaR != 0.0) {
        modPt.setDepth(startDepth + (rzc[i].getX() - rStart) / deltaR * deltaDepth);
        modPt.getRosette().setPToP(rosStartAmp + (rzc[i].getX() - rStart) / deltaR * deltaRosAmp);
      } else {
        modPt.setDepth(startDepth + cumLength / totLength * deltaDepth);
        modPt.getRosette().setPToP(rosStartAmp + cumLength / totLength * deltaRosAmp);
      }
      modPt.getRosette().setPhase(rosStartPhase + rzc[i].getZ() * repeat);
      if (modPt.getMotion().equals(Motion.BOTH)) {
        if (deltaR != 0.0) {
          modPt.getRosette2().setPToP(ros2StartAmp + (rzc[i].getX() - rStart) / deltaR * deltaRos2Amp);
        } else {
          modPt.getRosette2().setPToP(ros2StartAmp + cumLength / totLength * deltaRos2Amp);
        }
        modPt.getRosette2().setPhase(ros2StartPhase + rzc[i].getZ() * repeat);
      }
      modPt.move(rzc[i].getX(), rzc[i].getY());
      modPt.makeInstructions(passDepth, passStep, lastDepth, lastStep, stepsPerRot, rotation);
//      System.out.println("x:" + rzc[i].getX() + " z:" + rzc[i].getY() + " depth:" + modPt.getDepth() + " amp:" + modPt.getRosette().getPToP());
    }
  }

}
