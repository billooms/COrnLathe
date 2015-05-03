package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.controls.CoarseFine;
import static com.billooms.cutlist.Speed.*;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.RotMatrix.Axis;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.geometry.CircleGeom;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.simple.Line;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import javafx.geometry.Point3D;
import javax.swing.ProgressMonitor;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.openide.text.ActiveEditorDrop;
import org.w3c.dom.Element;

/**
 * Cut with simple indexing.
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
    itemid = "IndexPoint",
    icon16 = "com/billooms/cutpoints/icons/Index16.png",
    icon32 = "com/billooms/cutpoints/icons/Index32.png",
    name = "IndexPoint",
    body = "IndexPoint body",
    tooltip = "Make cuts at regular intervals as with an index wheel.")
public class IndexPoint extends CutPoint implements ActiveEditorDrop {

  /** Property name used when changing an IndexPoint's direction */
  String PROP_DIRECTION = PROP_PREFIX + "Direction";
  /** Property name used when changing an IndexPoint's repeat */
  String PROP_REPEAT = PROP_PREFIX + "Repeat";
  /** Property name used when changing an IndexPoint's phase */
  String PROP_PHASE = PROP_PREFIX + "Phase";
  /** Property name used when changing an IndexPoint's mask */
  String PROP_MASK = PROP_PREFIX + "Mask";

  /** Default direction for index cuts. */
  public final static Direction DEFAULT_DIRECTION = Direction.INDEX_CURVE;
  /** Default repeat. */
  public final static int DEFAULT_REPEAT = 8;
  /** Color of the drawn cut. */
  protected final static Color INDEX_COLOR = Color.YELLOW;
  /** Color used for the maximum movement of the cut. */
  protected final static Color INDEX_COLOR2 = Color.CYAN;
  /** move cutter above cutCurve while moving for safety. */
  protected final static double INDEX_SAFETY = 0.020;
  /** Number of points per circle for ECF circles. */
  private final static int CIRCLE_PTS = 32;

  /** Directions for indexing. */
  public enum Direction {

    /** Index in the x-direction. */
    INDEX_X,
    /** Index in the z-direction. */
    INDEX_Z,
    /** Index in a
     * direction perpendicular to the curve. */
    INDEX_CURVE
  }

  /** Direction of the cuts. */
  private Direction direction = DEFAULT_DIRECTION;
  /** Number of cuts around the shape. */
  private int repeat = DEFAULT_REPEAT;
  /** Phase shift in degrees. */
  private double phase = 0.0;
  /** Mask some of the repeats -- 0 is skip, 1 is don't skip. */
  private String mask = "";

  /**
   * Construct a new IndexPoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   */
  public IndexPoint(Element element, Cutters cutMgr, Outline outline) {
    super(element, cutMgr, outline);
    this.direction = CLUtilities.getEnum(element, "direction", Direction.class, DEFAULT_DIRECTION);
    this.repeat = CLUtilities.getInteger(element, "repeat", DEFAULT_REPEAT);
    this.phase = CLUtilities.getDouble(element, "phase", 0.0);
    this.mask = CLUtilities.getString(element, "mask", "");
    makeDrawables();
  }

  /**
   * Construct a new IndexPoint at the given position with information from the
   * given IndexPoint. This is primarily used when duplicating an IndexPoint.
   *
   * @param pos new position
   * @param cpt IndexPoint to copy from
   */
  public IndexPoint(Point2D.Double pos, IndexPoint cpt) {
    super(pos, cpt);
    this.direction = cpt.getDirection();
    this.repeat = cpt.getRepeat();
    this.phase = cpt.getPhase();
    this.mask = cpt.getMask();
    makeDrawables();
  }

  /**
   * Construct a new IndexPoint at the given position with default values. This
   * is primarily used when adding a first IndexPoint from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public IndexPoint(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
    makeDrawables();
  }

  @Override
  public String toString() {
    return super.toString() + " " + direction.toString() + " " + repeat;
  }

  /**
   * Get the direction of the cut.
   *
   * @return direction of the cut
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Set the direction of the cut. This fires a PROP_DIRECTION property change
   * with the old and new values.
   *
   * @param dir direction of the cut
   */
  public void setDirection(Direction dir) {
    Direction old = this.direction;
    this.direction = dir;
    makeDrawables();
    pcs.firePropertyChange(PROP_DIRECTION, old, direction);
  }

  /**
   * Get the number of repeats.
   *
   * @return number of repeats
   */
  public int getRepeat() {
    return repeat;
  }

  /**
   * Set the number of repeats. This fires a PROP_REPEAT property change with
   * the old and new values.
   *
   * @param n number of repeats
   */
  public void setRepeat(int n) {
    if (n < 1) {
      return;
    }
    int old = this.repeat;
    this.repeat = n;
    makeDrawables();
    pcs.firePropertyChange(PROP_REPEAT, old, repeat);
  }

  /**
   * Get the phase shift in degrees.
   *
   * @return phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of the
   * repeat, etc.
   */
  public double getPhase() {
    return phase;
  }

  /**
   * Get the fractional phase shift.
   *
   * @return phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public double getPh() {
    return phase / 360.0;
  }

  /**
   * Set the phase shift. This fires a PROP_PHASE property change with the old
   * and new values.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public void setPhase(double ph) {
    double old = this.phase;
    this.phase = angleCheck(ph);
    makeDrawables();
    pcs.firePropertyChange(PROP_PHASE, old, phase);
  }

  /**
   * Set the fractional phase shift (in the range 0.0 to 1.0). This fires a
   * PROP_PHASE property change with the old and new values.
   *
   * @param ph fractional phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of
   * the repeat, etc.
   */
  public void setPh(double ph) {
    setPhase(ph * 360.0);
  }

  /**
   * Get the mask for this index point. A blank string means all index positions
   * are used. A 0 means skip this position, a 1 means use this position. The
   * string is read from left to right, and is re-used as often as needed until
   * the full number of repeats is covered.
   *
   * @return mask
   */
  public String getMask() {
    return mask;
  }

  /**
   * Set the mask for this index point. A blank string means all index positions
   * are used. A 0 means skip this position, a 1 means use this position. The
   * string is read from left to right, and is re-used as often as needed until
   * the full number of repeats is covered. Any other character besides "0" and
   * "1" is interpreted as "1". This fires a PROP_MASK property change with the
   * old and new values.
   *
   * @param m new mask
   */
  public void setMask(String m) {
    String old = this.mask;
    if (m == null) {
      this.mask = "";
    }
    this.mask = m;
    for (int i = 0; i < mask.length(); i++) {	// replace anything besides '0' with '1'
      char c = mask.charAt(i);
      if (c == '1') {
        continue;
      }
      if (c != '0') {
        mask = mask.replace(c, '1');
      }
    }
    makeDrawables();
    pcs.firePropertyChange(PROP_MASK, old, mask);
  }

  /**
   * Calculate the moveVector. It is the direction of movement that will produce
   * the pattern.
   *
   * @param scale scale factor (use 1.0 for normalized)
   * @return the movement vector
   */
  protected Vector2d getMoveVector(double scale) {
    Vector2d perpVectN = getPerpVector(1.0);
    Vector2d moveN;
    switch (direction) {
      case INDEX_X:
        if (cutter.getLocation().isFrontInOrBackOut()) {
          moveN = new Vector2d(1.0, 0.0);
        } else {
          moveN = new Vector2d(-1.0, 0.0);
        }
        break;
      case INDEX_Z:
        if (cutter.getLocation().isInside()) {
          moveN = new Vector2d(0.0, -1.0);
        } else {			// direction depends on cutting top versus cutting bottom
          if (perpVectN != null) {	// not sure why we're checking for null
            moveN = new Vector2d(0.0, Math.copySign(1.0, perpVectN.y));
          } else {
            moveN = new Vector2d(0.0, 1.0);
          }
        }
        break;
      case INDEX_CURVE:
      default:
        moveN = perpVectN;
        break;
    }
    moveN.scale(scale);
    return moveN;
  }

  /**
   * Build the specific drawable shapes for an IndexPoint
   */
  @Override
  protected final void makeDrawables() {
    super.makeDrawables();
    pt.setColor(INDEX_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(INDEX_COLOR);	// the text
      drawList.get(1).setColor(INDEX_COLOR);	// the cutter
    }

    if (cutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // Line indicating direction of moveVectorS
    Vector2d moveVectorS = getMoveVector(cutDepth);
    drawList.add(new Line(getPos2D(), moveVectorS.x, moveVectorS.y, INDEX_COLOR2));

    // cut extent
    Vector2d perpVectorN = getPerpVector(1.0);
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(perpVectorN.y, perpVectorN.x) * 180.0 / Math.PI;
        drawList.add(new Arc(new Point2D.Double(getX() + moveVectorS.x, getZ() + moveVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, INDEX_COLOR2));
        break;
      // Profile of drill at cut depth
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x, getZ() + moveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), INDEX_COLOR2, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x + v1.x, getZ() + moveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), INDEX_COLOR2, SOLID_LINE));
        v1 = v1.rotate(180.0);
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x + v1.x, getZ() + moveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), INDEX_COLOR2, SOLID_LINE));
        break;
    }
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    // TODO: This is only defined for HCF and ECF so far
    String fullMask = mask;
    if (!mask.isEmpty()) {
      while (fullMask.length() < getRepeat()) {
        fullMask += mask;	// fill out to full length
      }
    }
    double ang = phase / repeat;
    switch (cutter.getFrame()) {
      case HCF:
        Vector2d moveVectorS = getMoveVector(cutDepth);
        CircleGeom cutAtMax = new CircleGeom(new Point2D.Double(getX() + moveVectorS.x, getZ() + moveVectorS.y), cutter.getRadius());
        Point2D.Double[] pts = findIntersect(cutAtMax, outline.getCutCurve());
        if (pts == null) {
          return;
        }
        for (int i = 0; i < repeat; i++) {
          if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
            Line3D line = new Line3D(new Point3D(pts[0].x, 0.0, pts[0].y), new Point3D(pts[1].x, 0.0, pts[1].y));
            line.rotate(Axis.Z, ang);
            list3D.add(line);
          }
          ang -= 360.0 / (double) repeat;
        }
        break;
      case ECF:
        // make a circle, tilt is by UCFAngle, then translate it to starting point
        Line3D circle = new Line3D(new Point3D(0.0, 0.0, 0.0), cutter.getRadius(), CIRCLE_PTS);
        circle.rotate(Axis.Y, cutter.getUCFAngle());
        circle.translate(getX(), 0.0, getZ());
        // then make a copy of it rotated to the proper position
        for (int i = 0; i < repeat; i++) {
          if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
            Line3D newCircle = new Line3D(circle);
            newCircle.rotate(Axis.Z, ang);
            list3D.add(newCircle);
          }
          ang -= 360.0 / (double) repeat;
        }
        break;
      case UCF:
      case Drill:
      default:
        break;
    }
  }

  @Override
  public void writeXML(PrintWriter out) {
    String optional = "";
    if (!mask.isEmpty()) {
      optional += " mask='" + mask + "'";
    }
    out.println(indent + "<IndexPoint"
        + xmlCutPointInfo()
        + " direction='" + direction.toString() + "'"
        + " repeat='" + repeat + "'"
        + " phase='" + F1.format(phase) + "'"
        + optional
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    indentLess();
    out.println(indent + "</IndexPoint>");
  }

  /**
   * Cut the given surface with this CutPoint.
   *
   * @param surface Surface
   * @param monitor progress monitor which can be canceled
   */
  @Override
  public synchronized void cutSurface(Surface surface, ProgressMonitor monitor) {
    Vector2d cutVectorS = getMoveVector(cutDepth);	// cut direction scaled by depth
    double cutX = getX() + cutVectorS.x;
    double cutZ = getZ() + cutVectorS.y;

    double spindleC, lastC = 0.0;
    String fullMask = mask;
    if (!mask.isEmpty()) {
      while (fullMask.length() < getRepeat()) {
        fullMask += mask;	// fill out to full length
      }
    }
    for (int i = 0; i < getRepeat(); i++) {
      if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
        spindleC = 360.0 * (double) i / (double) getRepeat() - getPhase() / (double) getRepeat();	// minus to match rosette phase
        surface.rotateZ(spindleC - lastC);		// incremental rotate the surface
        if (cutter.isIdealHCF()) {
          surface.cutSurface(cutter, cutX, cutZ, spindleC); // fast cut rendering only for IDEAL HCF
        } else {
          surface.cutSurface(cutter, cutX, cutZ);
        }
        lastC = spindleC;
      }
    }
    surface.rotateZ(360.0 - lastC);		// bring it back to the starting point
  }

  /**
   * Make instructions for this CutPoint
   *
   * @param passDepth depth per pass (course cut)
   * @param passStep spindle steps per instruction (course cut)
   * @param lastDepth depth of final cut
   * @param lastStep spindle steps per instruction (final cut)
   * @param stepsPerRot steps per rotation
   * @param rotation Rotation of spindle
   */
  @Override
  public void makeInstructions(double passDepth, int passStep, double lastDepth, int lastStep, int stepsPerRot, CoarseFine.Rotation rotation) {
    Vector2d moveVectorN = getMoveVector(1.0);
    cutList.comment("IndexPoint " + num);
    cutList.comment("Cutter: " + cutter);
    cutList.spindleWrapCheck();			// note that PToP, PumpAmp, and PumpPh are not used!
    double spindleC;
    String fullMask = mask;
    if (!mask.isEmpty()) {
      while (fullMask.length() < getRepeat()) {
        fullMask += mask;	// fill out to full length
      }
    }
    for (int i = 0; i < getRepeat(); i++) {		// only cutDepth and INDEX_SAFETY are used
      if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
        spindleC = 360.0 * (double) i / (double) getRepeat() - getPhase() / (double) getRepeat();	// minus to match rosette phase
        cutList.goToXZC(FAST, getX() - INDEX_SAFETY * moveVectorN.x, getZ() - INDEX_SAFETY * moveVectorN.y, spindleC);
        cutList.goToXZC(VELOCITY, getX() + cutDepth * moveVectorN.x, getZ() + cutDepth * moveVectorN.y, spindleC);
        cutList.goToXZC(FAST, getX() - INDEX_SAFETY * moveVectorN.x, getZ() - INDEX_SAFETY * moveVectorN.y, spindleC);
      }
    }
    // This was commented out for some reason.
    // Maybe I should add something at the end of CutPoints.makeInstructions?
    // Or else at the end of GCodeOutput?
//    if (spindleC != 360.0) {
//      cutList.goToXZC(FAST, getX() - INDEX_SAFETY * moveVectorN.x, getZ() - INDEX_SAFETY * moveVectorN.y, 360.0);
//    }
    cutList.spindleWrapCheck();
    cutList.goToXZC(FAST, getX() - INDEX_SAFETY * moveVectorN.x, getZ() + -INDEX_SAFETY * moveVectorN.y, 0.0);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("IndexPoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  @Override
  public boolean handleTransfer(JTextComponent targetComponent) {
    throw new UnsupportedOperationException("IndexPoint.handleTransfer Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
