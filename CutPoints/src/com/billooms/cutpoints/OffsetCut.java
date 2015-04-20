package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.controls.CoarseFine;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.simple.Line;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import com.billooms.rosette.Rosette;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.w3c.dom.Element;

/**
 * This represents an offset CutPoint for use with an elliptical or dome chuck.
 * The x,y coordinates represent a new 0,0 point with the piece oriented such
 * that the nearest tangential point will be the new top of the work. The
 * CutPoint will be cut with respect to this new origin.
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
public abstract class OffsetCut extends CutPoint {

  /** Property name used when changing an OffsetCut's repeat. */
  String PROP_REPEAT = PROP_PREFIX + "Repeat";
  /** Property name used for changing the offset of the repeated set. */
  public final static String PROP_OFFSET = PROP_PREFIX + "Offset";

  /** Number of points in 3D line. */
  int NUM_3D_PTS = 360 / 5;		// every 5 degrees
  /** Valid values for repeat using a 24 or 35 hole index wheel. */
  public final ArrayList<Integer> VALID_REPEATS = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 12, 24, 35));
  /** Default offset phase of the repeated pattern (currently set to 0). */
  public final static int DEFAULT_OFFSET = 0;

  /** Color of the drawn cut. */
  protected final static Color OFFSET_COLOR = Color.PINK;

  /** Number of repeats around the shape. */
  protected int repeat = Rosette.DEFAULT_REPEAT;
  /** Offset phase shift (number of holes in 24 or 35 hole index wheel). */
  protected int indexOffset = DEFAULT_OFFSET;

  /**
   * Construct a new OffsetCut from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   */
  public OffsetCut(Element element, Cutters cutMgr, Outline outline) {
    super(element, cutMgr, outline);
    this.repeat = CLUtilities.getInteger(element, "repeat", Rosette.DEFAULT_REPEAT);
    this.indexOffset = CLUtilities.getInteger(element, "indexOffset", 0);
  }

  /**
   * Construct a new OffsetCut with information from the given OffsetCut.
   *
   * @param pos new position
   * @param cpt OffsetCut to copy from
   */
  public OffsetCut(Point2D.Double pos, OffsetCut cpt) {
    super(pos, cpt);
    this.repeat = cpt.getRepeat();
    this.indexOffset = cpt.getIndexOffset();
  }

  /**
   * Construct a new OffsetCut at the given position with default values. This
   * is primarily used when adding a first OffsetCut from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public OffsetCut(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
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
   * Set the number of repeats. Note that this does not call makeDrawables()
   * because the repeat doesn't change anything in the drawables. This fires a
   * PROP_REPEAT property change with the old and new values.
   *
   * @param n number of repeats
   */
  public void setRepeat(int n) {
    if (VALID_REPEATS.contains(n)) {
      int old = this.repeat;
      this.repeat = n;
      makeDrawables();
      pcs.firePropertyChange(PROP_REPEAT, old, repeat);
    }
  }

  /**
   * Get the offset phase shift of the repeated group. This is an integer
   * representing the number of holes in a 24 or 35 hole index wheel.
   *
   * @return offset number of holes
   */
  public int getIndexOffset() {
    return indexOffset;
  }

  /**
   * Set the offset phase shift of the repeated group. This is an integer
   * representing the number of holes in a 24 or 35 hole index wheel. This fires
   * a PROP_OFFSET property change with the old and new values.
   *
   * @param offset number of holes
   */
  public void setIndexOffset(int offset) {
    if (validOffset(offset)) {
      int old = this.indexOffset;
      this.indexOffset = offset;
      makeDrawables();
      pcs.firePropertyChange(PROP_OFFSET, old, offset);
    }
  }

  /**
   * Determine if the given offset is valid for the current repeat value.
   *
   * @param offset offset number of index holes
   * @return true == OK
   */
  private boolean validOffset(int offset) {
    return (offset < (indexWheelHoles() / repeat));
  }

  /**
   * For the current repeat, identify which index wheel is used.
   *
   * @return which index wheel is used (or zero if invalid repeat value)
   */
  protected int indexWheelHoles() {
    if (!VALID_REPEATS.contains(repeat)) {
      return 0;
    }
    if ((repeat == 1) || (repeat == 5) || (repeat == 7)) {
      return 35;
    }
    return 24;
  }

  /**
   * Calculate the degrees absolute offset for the current repeat and offset.
   *
   * @return angle in degrees
   */
  protected double indexOffsetDegrees() {
    return (double) indexOffset * 360.0 / (double) indexWheelHoles();
  }

  /**
   * Get the x coordinate of the tangent point on the surface.
   *
   * @return tangent point x-coordinate
   */
  public double getTangentPointX() {
    return getTangentPoint().x;
  }

  /**
   * Get the y coordinate of the tangent point on the surface.
   *
   * @return tangent point x-coordinate
   */
  public double getTangentPointY() {
    return getTangentPoint().y;
  }

  /**
   * Get the tangent point on the surface.
   *
   * @return tangent point
   */
  public Point2D.Double getTangentPoint() {
    return (outline.getCutCurve()).nearestPoint(getPos2D());
  }

  /**
   * Get the tangent angle (relative to the flat top) in degrees.
   *
   * @return tangent angle in degrees
   */
  public double getTangentAngle() {
    Vector2d tanVector = getTangentVectorN();
    return Math.toDegrees(Math.atan2(-tanVector.x, -tanVector.y));
  }

  /**
   * Get the straight line distance between the surface tangent point and top of
   * outside curve.
   *
   * @return straight line distance
   */
  public double getDistanceToTop() {
    Point2D.Double tangentPoint = getTangentPoint();
    Point2D.Double top = outline.getOutsideCurve().getTopPoint();
    return Math.hypot(tangentPoint.x - top.x, tangentPoint.y - top.y);
  }

  /**
   * Generate the normalized tangent vector.
   *
   * @return normalized tangent vector (or 0,0 if there was a problem)
   */
  private Vector2d getTangentVectorN() {
    Vector2d tanVector;
    Point2D.Double tangentPoint = getTangentPoint();
    if (snap) {			// (this should always be the case)
      if (outline.getCutterPathCurve() == null) {
        return new Vector2d();    // not sure why this happens sometimes
      }
      tanVector = outline.getCutterPathCurve().perpendicular(getPos2D(), cutter.getLocation().isFrontInOrBackOut());
      if (tanVector == null) {		// this is a kludge
        return new Vector2d();
      }
    } else {			// (just in case -- point should always be snapped)
      tanVector = new Vector2d(tangentPoint.x - getX(), tangentPoint.y - getZ());
    }
    tanVector.normalize();
    if (Math.abs(tanVector.x) < 1E-12) {
      tanVector.x = 0.0;		// to prevent -0.0
    }
    if (Math.abs(tanVector.y) < 1E-12) {
      tanVector.y = 0.0;		// to prevent -0.0
    }
    return tanVector;
  }

  @Override
  protected void makeDrawables() {
    super.makeDrawables();    // text and cutter at rest
    pt.setColor(OFFSET_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(OFFSET_COLOR);	// the text
      drawList.get(1).setColor(OFFSET_COLOR);	// the cutter
    }
    // Compute the vector to the point on the curve
    // which is the direction perpendicular to curve for snapped points
    // or the direction to the nearest point for unsnapped points.
    Vector2d tanVector = getTangentVectorN();
    if (tanVector == null) {		// this is a kludge
      return;
    }
    switch (cutter.getFrame()) {
      case HCF:
      case UCF:
        tanVector.scale(cutter.getRadius());
        break;
      case Drill:
      case ECF:
        tanVector.scale(0.3);   // arbitrary so that we have something to see
        break;
    }

    // Line indicating direction of nearest point on the curve
    drawList.add(new Line(getPos2D(), tanVector.x, tanVector.y, OFFSET_COLOR));
    // Line indicating direction of tangent
    drawList.add(new Line(getPos2D(), -tanVector.y, tanVector.x, OFFSET_COLOR));
    drawList.add(new Line(getPos2D(), tanVector.y, -tanVector.x, OFFSET_COLOR));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("OffsetCut.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  @Override
  public void writeXML(PrintWriter out) {
    super.writeXML(out);	// for point
  }

  @Override
  public void makeInstructions(double passDepth, int passStep, double lastDepth, int lastStep, int stepsPerRot, CoarseFine.Rotation rotation) {
    // Only show comments pertaing to the offset
    cutList.comment("************************");
    cutList.comment("OffsetCut " + num);
    cutList.comment("Cutter: " + cutter);
    cutList.comment("  tangent angle is " + F1.format(getTangentAngle()));
    cutList.comment("  distance to top is " + F3.format(getDistanceToTop()));
    cutList.comment("  repeat is " + repeat);
    cutList.comment("    use " + indexWheelHoles() + " hole index wheel, offset " + (-indexOffset) + " holes");
    String str = "";
    int n = -indexOffset;
    if (n < 0) {
      n += indexWheelHoles() / repeat;
    }
    for (int i = n; i < indexWheelHoles(); i += indexWheelHoles() / repeat) {
      str = str + i + "  ";
    }
    cutList.comment("    skip " + indexWheelHoles() / repeat + " holes each repeat:  holes " + str);
    cutList.comment("************************");
  }

}
