package com.billooms.cutpoints;

import com.billooms.controls.CoarseFine;
import static com.billooms.controls.CoarseFine.Rotation.NEG_LAST;
import static com.billooms.controls.CoarseFine.Rotation.PLUS_ALWAYS;
import static com.billooms.cutlist.Speed.*;
import static com.billooms.cutpoints.CutPoint.ARC_ANGLE;
import static com.billooms.cutpoints.CutPoints.NUM_3D_PTS;
import static com.billooms.cutpoints.RosettePoint.ROSETTE_COLOR2;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.LIGHT_DOT;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import com.billooms.patterns.Patterns;
import com.billooms.rosette.CompoundRosette;
import com.billooms.rosette.Rosette;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import javafx.geometry.Point3D;
import org.w3c.dom.Element;

/**
 * A RosettePoint that can be added to an OffsetGroup. This is essentially the
 * same as a RosttePoint but with different methods for cutting the surface and
 * generating instructions.
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
public class OffRosettePoint extends RosettePoint implements OffPoint {

  /** Need to keep track of which OffsetGroup this belongs to. */
  private final OffsetGroup parent;

  /**
   * Construct a new RosettePoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   * @param patMgr pattern manager
   * @param parent parent OffsetGroup
   */
  public OffRosettePoint(Element element, Cutters cutMgr, Outline outline, Patterns patMgr, OffsetGroup parent) {
    super(element, cutMgr, outline, patMgr);
    this.parent = parent;
  }

  /**
   * Construct a new RosettePoint at the given position with information from
   * the given RosettePoint. This is primarily used when duplicating an
   * RosettePoint.
   *
   * @param pos new position
   * @param cpt RosettePoint to copy from
   * @param parent parent OffsetGroup
   */
  public OffRosettePoint(Point2D.Double pos, OffRosettePoint cpt, OffsetGroup parent) {
    super(pos, cpt);
    this.parent = parent;
  }

  /**
   * Construct a new RosettePoint at the given position with default values.
   * This is primarily used when adding a first RosettePoint from the
   * OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   * @param patMgr pattern manager
   * @param parent parent OffsetGroup
   */
  public OffRosettePoint(Point2D.Double pos, Cutter cut, Outline outline, Patterns patMgr, OffsetGroup parent) {
    super(pos, cut, outline, patMgr);
    this.parent = parent;
  }

  /**
   * Construct a new OffRosettePoint from the given RosettePoint.
   *
   * @param rpt RosettePoint
   * @param parent parent OffsetGroup
   */
  public OffRosettePoint(RosettePoint rpt, OffsetGroup parent) {
    super(rpt.getPos2D(), rpt);
    this.parent = parent;
    this.motion = rpt.getMotion();
    this.rosette = (rpt.getRosette() instanceof Rosette) ? new Rosette((Rosette) rpt.getRosette()) : new CompoundRosette((CompoundRosette) rpt.getRosette()); 
    if (motion.usesBoth()) {
      this.rosette2 = (rpt.getRosette2() instanceof Rosette) ? new Rosette((Rosette) rpt.getRosette2()) : new CompoundRosette((CompoundRosette) rpt.getRosette2()); 
    }
//    makeDrawables();
    rosette.addPropertyChangeListener(this);
    if (motion.usesBoth()) {
      rosette2.addPropertyChangeListener(this);
    }
  }

  /**
   * Get the scaled movement vector for the rosette. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut. 
   * In the case of BOTH or PERPTAN, it is the movement of only the first rosette.
   *
   * @return scaled movement vector
   */
  @Override
  protected Vector2d getMoveVectorS() {
    switch (motion) {       // TODO: not yet defined for PERP, TANGENT, or PERPTAN
      case ROCK:
      case PUMP:
      case BOTH:
        return (super.getMoveVectorS()).rotate(-parent.getTangentAngle());
      default:
        return super.getMoveVectorS();
    }
  }

  /**
   * Get the scaled movement vector for rosette2. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut. 
   * In the case of BOTH or PERPTAN, it is the movement of only the second rosette.
   *
   * @return scaled movement vector
   */
  @Override
  protected Vector2d getMoveVector2S() {
    switch (motion) {       // TODO: not yet defined for PERP, TANGENT, or PERPTAN
      case ROCK:
      case PUMP:
      case BOTH:
        return (super.getMoveVector2S()).rotate(-parent.getTangentAngle());
      default:
        return super.getMoveVector2S();
    }
  }

  /**
   * Mirror the given point around the parent's origin and tangent line.
   *
   * @param pt
   * @return new point
   */
  private Point2D.Double mirrorPt(Point2D.Double pt) {
    if (parent == null) {
      return null;
    }
    Point2D.Double parentOrigin = parent.getPos2D();
    double tangent = parent.getTangentAngle();
    Vector2d vect = new Vector2d(pt.x - parentOrigin.x, pt.y - parentOrigin.y);
    double vectAngle = Math.toDegrees(Math.atan2(pt.y - parentOrigin.y, pt.x - parentOrigin.x));
    double newAng = 180.0 - 2 * tangent - 2 * vectAngle;
    Vector2d newVect = vect.rotate(newAng);
    Point2D.Double newPt = new Point2D.Double(parentOrigin.x + newVect.x, parentOrigin.y + newVect.y);
//		System.out.println("OffRosettePoint:" + getNum() + " " + tangent + " " + vect.toString() + " " + vectAngle);
//		System.out.println("                " + newAng + " " + newVect.toString() + " " + newPt.toString());
    return newPt;
  }

  /**
   * Generate a suffix a,b,c,... for the text display.
   *
   * @return suffix
   */
  private String makeSuffix() {
    char suffix = 'a';
    if (parent != null) {
      int n = parent.getAllCutPoints().indexOf(this);
      suffix = (char) (suffix + n);
    }
    return Character.toString(suffix);
  }

  @Override
  protected synchronized void makeDrawables() {
    super.makeDrawables();    // text and cutter at rest
    text.setText(num + makeSuffix());
    if (drawList.size() >= 2) {
      drawList.remove(1);   // don't draw the original cutter
    }

    if (parent == null) {
      return;   // can happen on initialization
    }

    // cut extent
    Vector2d perpVectorS = getPerpVector(cutDepth);	// maximum cut displacement
    Vector2d moveVectorS = getMoveVectorS();		// scaled movement vector for rosette
    Vector2d moveVector2S = getMoveVector2S();		// scaled movement vector for rosette2
    Point2D.Double p;
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(perpVectorS.y, perpVectorS.x) * 180.0 / Math.PI;
        p = new Point2D.Double(getX() + perpVectorS.x + moveVectorS.x, getZ() + perpVectorS.y + moveVectorS.y);
        drawList.add(new Arc(p, cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR2));
        drawList.add(new Arc(mirrorPt(p), cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR2));
        if (motion.usesBoth()) {
          p = new Point2D.Double(getX() + perpVectorS.x + moveVectorS.x, getZ() + perpVectorS.y + moveVectorS.y);
          drawList.add(new Arc(p, cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR2));
          drawList.add(new Arc(mirrorPt(p), cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR2));
          p = new Point2D.Double(getX() + perpVectorS.x + moveVector2S.x, getZ() + perpVectorS.y + moveVector2S.y);
          drawList.add(new Arc(p, cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR3));
          drawList.add(new Arc(mirrorPt(p), cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR3));
        }
        p = new Point2D.Double(getX() + perpVectorS.x, getZ() + perpVectorS.y);
        drawList.add(new Arc(p, cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR));
        drawList.add(new Arc(mirrorPt(p), cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR));
        break;
      // Profile of drill & Fixed at cut depth
      case Fixed:
      case Drill:
        // Add the default cutter tip dotted outline
        drawList.add(cutter.getProfile().getDrawable(pt.getPoint2D(), cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR, LIGHT_DOT));
        p = new Point2D.Double(getX() + perpVectorS.x + moveVectorS.x, getZ() + perpVectorS.y + moveVectorS.y);
        drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR2, SOLID_LINE));
        drawList.add(cutter.getProfile().getDrawable(mirrorPt(p), cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          p = new Point2D.Double(getX() + perpVectorS.x + moveVector2S.x, getZ() + perpVectorS.y + moveVector2S.y);
          drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
              -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR3, SOLID_LINE));
          drawList.add(cutter.getProfile().getDrawable(mirrorPt(p), cutter.getTipWidth(),
              -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        p = new Point2D.Double(getX() + perpVectorS.x, getZ() + perpVectorS.y);
        drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR, SOLID_LINE));
        drawList.add(cutter.getProfile().getDrawable(mirrorPt(p), cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      // Note: the mirrored points have not been added yet.
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle() - parent.getTangentAngle());   // minus because + is toward front
        p = new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVectorS.x, getZ() + perpVectorS.y + v1.y + moveVectorS.y);
        drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          p = new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVector2S.x, getZ() + perpVectorS.y + v1.y + moveVector2S.y);
          drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
              -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        p = new Point2D.Double(getX() + perpVectorS.x + v1.x, getZ() + perpVectorS.y + v1.y);
        drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR, SOLID_LINE));

        v1 = v1.rotate(180.0);
        p = new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVectorS.x, getZ() + perpVectorS.y + v1.y + moveVectorS.y);
        drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          p = new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVector2S.x, getZ() + perpVectorS.y + v1.y + moveVector2S.y);
          drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
              -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        p = new Point2D.Double(getX() + perpVectorS.x + v1.x, getZ() + perpVectorS.y + v1.y);
        drawList.add(cutter.getProfile().getDrawable(p, cutter.getTipWidth(),
            -cutter.getUCFAngle() - parent.getTangentAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
    }
  }

  /**
   * Get a vector that represents the offset of this CutPoint relative to the
   * parent, then rotated by the parent's tangentAngle and moved to 0,0.
   *
   * @return offset vector
   */
  private Vector2d offsetFromParent() {
    Point2D.Double tangentPoint = outline.getCutSurfaceCurve().nearestPoint(getPos2D());
    Vector2d offset = new Vector2d(tangentPoint.x - parent.getTangentPointX(),
        tangentPoint.y - parent.getTangentPointY());
    return offset.rotate(parent.getTangentAngle());
  }

  /**
   * Make a 3D Line for this OffRosettePoint.
   *
   * @return 3D Line
   */
  Line3D make3DLine() {
    Line3D line = new Line3D();

    Vector2d offset = offsetFromParent();
    double x0 = offset.x;		// point where cutter contacts the surface
    double y0 = getY();     // (y should always be zero)		
    double z0 = offset.y;

    for (int i = 0; i < (NUM_3D_PTS + 1); i++) {
      double angDeg = (double) i * 360.0 / (double) NUM_3D_PTS;	// in degrees
      double angRad = Math.toRadians(angDeg);
      Vector2d xz = rosetteMove(angDeg, x0, z0);   // xz location due to rosette motion
      double r = Math.hypot(xz.x, y0);		// cylindrical radius  NOTE: y should always be 0.0
      if (cutter.getLocation().isBack()) {	// if in back, add 180 degrees rotation
        angRad += Math.PI;
      }
      line.add(new Point3D(r * Math.cos(-angRad), r * Math.sin(-angRad), xz.y));
    }
    return line;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("OffRosettePoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  @Override
  public void cutSurface(Surface surface, double x, double y) {
    Vector2d cutVectorS = getPerpVector(cutDepth).rotate(parent.getTangentAngle());
    double x0 = x + cutVectorS.x;			// cutter location with depth
    double z0 = y + cutVectorS.y;

    // this is where the main work is done
    int nSectors = surface.numSectors();		// number of sectors around shape
    double dAngle = 360.0 / (double) nSectors;	// angle increment degrees
    Vector2d cutXZ;						// Location of center of cutter
    double spindleC, lastC = 0.0;
    int count;
    for (count = 0, spindleC = 0.0; count < nSectors; count++, spindleC += dAngle) {
      cutXZ = rosetteMove(spindleC, x0, z0);
      surface.rotateZ(spindleC - lastC);		// incremental rotate the surface
      surface.cutSurface(cutter, cutXZ.x, cutXZ.y);
      lastC = spindleC;
    }
    surface.rotateZ(360.0 - lastC);		// bring it back to the starting point
  }

  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot, double x, double z) {
    cutList.comment("OffRosettePoint " + num);
    cutList.comment("Cutter: " + cutter);
    cutList.spindleWrapCheck();

    // move to position before applying depth
    Vector2d start = rosetteMove(0.0, x, z);
    cutList.goToXZC(FAST, start, 0.0);

    // Increasing cut depth with the coarse depth per cut
    double depth = 0.0;
    while (depth < (cutDepth - controls.getLastDepth())) {		// this is not the last cut
      depth = Math.min(cutDepth - controls.getLastDepth(), depth + controls.getPassDepth());
      boolean dir = (controls.getRotation() == NEG_LAST) ? NOT_LAST : ((controls.getRotation() == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followRosette(depth, controls.getPassStep(), dir, stepsPerRot, x, z);
    }
    if (controls.getLastDepth() > 0.0) {		// this is the last cut
      boolean dir = (controls.getRotation() == NEG_LAST) ? LAST : ((controls.getRotation() == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followRosette(cutDepth, controls.getLastStep(), dir, stepsPerRot, x, z);
    }

    // back to the starting position
    cutList.spindleWrapCheck();
    cutList.goToXZC(FAST, start, 0.0);
  }

  /**
   * Add instructions to the cutList for following rosettes.
   *
   * @param depth the depth of this cut
   * @param step the number of stepper motor steps per increment (for the
   * spindle motor)
   * @param last true=this is the last (turn the other way).
   * @param stepsPerRot steps per rotation
   * @param x new x-coordinate
   * @param z new z-coordinate
   */
  private void followRosette(double depth, int step, boolean last, int stepsPerRot, double x, double z) {
    Vector2d perpVectorN = getPerpVector(1.0).rotate(parent.getTangentAngle());
    double x0 = x + depth * perpVectorN.x;
    double z0 = z + depth * perpVectorN.y;
    followRosette(perpVectorN, x0, z0, depth, step, last, stepsPerRot);
  }

}
