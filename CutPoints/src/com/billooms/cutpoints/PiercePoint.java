package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.controls.CoarseFine;
import static com.billooms.cutlist.Speed.*;
import static com.billooms.cutpoints.CutPoints.NUM_3D_PTS;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.SOLID_LINE;
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
 * Cut pierces into the work while rotating (like a regular lathe).
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
    itemid = "PiercePoint",
    icon16 = "com/billooms/cutpoints/icons/Pierce16.png",
    icon32 = "com/billooms/cutpoints/icons/Pierce32.png",
    name = "PiercePoint",
    body = "PiercePoint body",
    tooltip = "Cuts made with the lathe spinning fase as a regular lathe.")
public class PiercePoint extends CutPoint implements ActiveEditorDrop {

  /** Property name used when changing an PiercePoint's direction */
  String PROP_DIRECTION = PROP_PREFIX + "Direction";

  /** Default direction for index cuts. */
  public final static Direction DEFAULT_DIRECTION = Direction.MOVE_Z;
  /** Color of the drawn cut. */
  private final static Color PIERCE_COLOR = Color.YELLOW;
  /** Color used for the maximum movement of the cut. */
  private final static Color PIERCE_COLOR2 = Color.ORANGE;
  /** move cutter above cutCurve while moving for safety. */
  private final static double PIERCE_SAFETY = 0.020;    // TODO: use this?

  /** Directions for indexing. */
  public enum Direction {

    /** Move in the x-direction. */
    MOVE_X,
    /** Move in the z-direction. */
    MOVE_Z,
    /** Move in a direction perpendicular to the curve. */
    MOVE_CURVE
  }

  /** Direction of the cuts. */
  private Direction direction = DEFAULT_DIRECTION;

  /**
   * Construct a new PiercePoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   */
  public PiercePoint(Element element, Cutters cutMgr, Outline outline) {
    super(element, cutMgr, outline);
    this.direction = CLUtilities.getEnum(element, "direction", Direction.class, DEFAULT_DIRECTION);
    makeDrawables();
  }

  /**
   * Construct a new PiercePoint at the given position with information from the
   * given PiercePoint. This is primarily used when duplicating an PiercePoint.
   *
   * @param pos new position
   * @param cpt PiercePoint to copy from
   */
  public PiercePoint(Point2D.Double pos, PiercePoint cpt) {
    super(pos, cpt);
    this.direction = cpt.getDirection();
    makeDrawables();
  }

  /**
   * Construct a new PiercePoint at the given position with default values. This
   * is primarily used when adding a first PiercePoint from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public PiercePoint(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
    makeDrawables();
  }

  @Override
  public String toString() {
    return super.toString() + " " + direction.toString();
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
      case MOVE_X:
        if (cutter.getLocation().isFrontInOrBackOut()) {
          moveN = new Vector2d(1.0, 0.0);
        } else {
          moveN = new Vector2d(-1.0, 0.0);
        }
        break;
      case MOVE_Z:
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
      case MOVE_CURVE:
      default:
        moveN = perpVectN;
        break;
    }
    moveN.scale(scale);
    return moveN;
  }

  /**
   * Build the specific drawable shapes for an PiercePoint
   */
  @Override
  protected final void makeDrawables() {
    super.makeDrawables();
    pt.setColor(PIERCE_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(PIERCE_COLOR);	// the text
      drawList.get(1).setColor(PIERCE_COLOR);	// the cutter
    }

    if (cutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // Line indicating direction of moveVectorS
    Vector2d moveVectorS = getMoveVector(cutDepth);
    drawList.add(new Line(getPos2D(), moveVectorS.x, moveVectorS.y, PIERCE_COLOR2));

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
            angle, ARC_ANGLE, PIERCE_COLOR2));
        break;
      // Profile of drill at cut depth
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x, getZ() + moveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), PIERCE_COLOR2, SOLID_LINE));
        break;
      // PiercePoint should not be used for ECF
      case ECF:
        break;
    }
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    Line3D line = new Line3D();

    double x0 = getX();		// point where cutter contacts the surface
    double y0 = getY();     // (y should always be zero)		
    double z0 = getZ();
    switch (cutter.getFrame()) {
      case HCF:
      case UCF:
        Vector2d cutPerpR = getPerpVector(cutter.getRadius());
        x0 = x0 + cutPerpR.x;	// offset for cutter radius
        z0 = z0 + cutPerpR.y;	// these are now lathe/view3d coordinates
        break;
      default:
        break;
    }

    double r = Math.hypot(x0, y0);		// cylindrical radius  NOTE: y should always be 0.0
    for (int i = 0; i < (NUM_3D_PTS + 1); i++) {
      double angRad = Math.toRadians((double) i * 360.0 / (double) NUM_3D_PTS);
      if (cutter.getLocation().isBack()) {	// if in back, add 180 degrees rotation
        angRad += Math.PI;
      }
      line.add(new Point3D(r * Math.cos(-angRad), r * Math.sin(-angRad), z0));
    }
    list3D.add(line);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<PiercePoint"
        + xmlCutPointInfo()
        + " direction='" + direction.toString() + "'"
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    indentLess();
    out.println(indent + "</PiercePoint>");
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

    int nSamples = surface.numSectors();
    double spindleC, lastC = 0.0;
    for (int i = 0; i < nSamples; i++) {
      spindleC = 360.0 * (double) i / (double) nSamples;
      surface.rotateZ(spindleC - lastC);		// incremental rotate the surface
      surface.cutSurface(cutter, cutX, cutZ, spindleC);   // fast cut rendering
      lastC = spindleC;
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
    Vector2d moveVectorS = getMoveVector(cutDepth);
    cutList.comment("PiercePoint " + num);
    cutList.comment("Cutter: " + cutter);
    cutList.goToXZC(FAST, getX(), getZ(), 0.0);
    cutList.goToXZC(VELOCITY, getX() + moveVectorS.x, getZ() + moveVectorS.y, 0.0);	// go to x,z at spindleC=0.0
    cutList.goToXZC(FAST, getX(), getZ(), 0.0);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PiercePoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    
    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  @Override
  public boolean handleTransfer(JTextComponent targetComponent) {
    throw new UnsupportedOperationException("PiercePoint.handleTransfer Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
