package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.controls.CoarseFine.Rotation;
import static com.billooms.controls.CoarseFine.Rotation.NEG_LAST;
import static com.billooms.controls.CoarseFine.Rotation.PLUS_ALWAYS;
import static com.billooms.cutlist.Speed.*;
import static com.billooms.cutpoints.CutPoint.NOT_LAST;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.RotMatrix;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.Pt;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.drawables.vecmath.Vector3d;
import com.billooms.outline.Outline;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import static com.billooms.rosette.Rosette.DEFAULT_PATTERN;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javafx.geometry.Point3D;
import javax.swing.ProgressMonitor;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.openide.*;
import org.w3c.dom.Element;

/**
 * An extention to OffsetCut point where the cutter follows a custom pattern.
 *
 * At present, this is the only kind of CutPoint that is corrected for the curve
 * of the shape.
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
    itemid = "PatternPoint",
    icon16 = "com/billooms/cutpoints/icons/PatternPt16.png",
    icon32 = "com/billooms/cutpoints/icons/PatternPt32.png",
    name = "PatternPoint",
    body = "PatternPoint body",
    tooltip = "Offset cut that makes a custom pattern.")
public class PatternPoint extends OffsetCut {

  /** Property name used for changing the pattern. */
  public final static String PROP_PATTERN = PROP_PREFIX + "Pattern";
  /** Property name used for changing the pattern repeat. */
  public final static String PROP_PATTERNREPEAT = PROP_PREFIX + "Repeat";
  /** Property name used for changing the pattern phase. */
  public final static String PROP_PHASE = PROP_PREFIX + "Phase";
  /** Property name used for changing the optimize flag. */
  public final static String PROP_OPTIMIZE = PROP_PREFIX + "Optimize";

  /** Color used for the maximum contour movement */
  private final static Color CONTOUR_COLOR = Color.GREEN;

  /** Pattern to use. */
  private CustomPattern pattern = null;
  /** Number of repeats of the basic pattern (usually just 1). */
  private int patternRepeat = 1;
  /** Phase shift in degrees (0 to 360) of the pattern, +phase is CCW. */
  private double phase = 0.0;
  /** Pattern should be optimized for curvature of the surface. */
  private boolean optimize = false;

  /**
   * Construct a new PatternPoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   * @param patMgr pattern manager
   */
  public PatternPoint(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    super(element, cutMgr, outline);
    Pattern pat = patMgr.getPattern(CLUtilities.getString(element, "pattern", DEFAULT_PATTERN));
    if (pat instanceof CustomPattern) {
      this.pattern = (CustomPattern) pat;
    }
    this.patternRepeat = CLUtilities.getInteger(element, "patternRepeat", 1);
    this.phase = CLUtilities.getDouble(element, "phase", 0.0);
    this.optimize = CLUtilities.getBoolean(element, "optimize", false);
    makeDrawables();
    if (pattern != null) {
      pattern.addPropertyChangeListener(this);
    }
  }

  /**
   * Construct a new PatternPoint at the given position with information from
   * the given PatternPoint. This is primarily used when duplicating a
   * PatternPoint.
   *
   * @param pos new position
   * @param cpt PatternPoint to copy from
   */
  public PatternPoint(Point2D.Double pos, PatternPoint cpt) {
    super(pos, cpt);
    this.pattern = cpt.getPattern();
    this.patternRepeat = cpt.getPatternRepeat();
    this.phase = cpt.getPhase();
    this.optimize = cpt.getOptimize();
    makeDrawables();
    if (pattern != null) {
      pattern.addPropertyChangeListener(this);
    }
  }

  /**
   * Construct a new PatternPoint at the given position with default values.
   * This is primarily used when adding a first PatternPoint from the
   * OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   * @param patMgr pattern manager
   */
  public PatternPoint(Point2D.Double pos, Cutter cut, Outline outline, Patterns patMgr) {
    super(pos, cut, outline);
    if (patMgr.getAllCustom().isEmpty()) {    // can't leave patter as null!
      patMgr.add(new CustomPattern("undefined", CustomPattern.CustomStyle.STRAIGHT));
    }
    this.pattern = patMgr.getAllCustom().get(0);    // use the first CustomPattern
    makeDrawables();
    if (pattern != null) {
      pattern.addPropertyChangeListener(this);
    }
  }

  @Override
  public String toString() {
    return super.toString() + " " + pattern.toString();
  }

  /**
   * The only thing this does is remove any CustomPattern
   * propertyChangeListener.
   */
  @Override
  public void clear() {
    super.clear();
    if (pattern != null) {
      pattern.removePropertyChangeListener(this);
    }
  }

  /**
   * Get the pattern.
   *
   * @return pattern
   */
  public CustomPattern getPattern() {
    return pattern;
  }

  /**
   * Set the pattern. This fires a PROP_PATTERN property change with the old and
   * new patterns.
   *
   * @param p new pattern
   */
  public synchronized void setPattern(CustomPattern p) {
    if ((p != null) && (p != pattern)) {
      if (pattern != null) {
        pattern.removePropertyChangeListener(this);
      }
      Pattern old = this.pattern;
      this.pattern = p;
      makeDrawables();
      pattern.addPropertyChangeListener(this);
      this.pcs.firePropertyChange(PROP_PATTERN, old, pattern);
    }
  }

  /**
   * Get the number of repeats of the basic pattern.
   *
   * @return pattern repeats
   */
  public int getPatternRepeat() {
    return patternRepeat;
  }

  /**
   * Set the number of repeats of the basic pattern. This fires a
   * PROP_PATTERNREPEAT property change with the old and new repeats.
   *
   * @param r pattern repeats
   */
  public void setPatternRepeat(int r) {
    int old = patternRepeat;
    this.patternRepeat = r;
    makeDrawables();
    this.pcs.firePropertyChange(PROP_PATTERNREPEAT, old, patternRepeat);
  }

  /**
   * Get the phase of the pattern
   *
   * @return phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of the
   * repeat, etc.
   */
  public double getPhase() {
    return phase;
  }

  /**
   * Get the phase of the pattern as a fraction of a repeat.
   *
   * @return phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public double getPh() {
    return phase / 360.0;
  }

  /**
   * Set the phase of the pattern. This fires a PROP_PHASE property change with
   * the old and new phases.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public synchronized void setPhase(double ph) {
    double old = this.phase;
    this.phase = angleCheck(ph);
    makeDrawables();
    this.pcs.firePropertyChange(PROP_PHASE, old, phase);
  }

  /**
   * Set the phase of the pattern. Values of less than 0.0 will be set to 0.0
   * and values greater than 1.0 will be set to 1.0. This fires a PROP_PHASE
   * property change with the old and new phases.
   *
   * @param ph phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public synchronized void setPh(double ph) {
    setPhase(ph * 360.0);
  }

  /**
   * Get the optimize flag.
   *
   * @return optimize flag
   */
  public boolean getOptimize() {
    return optimize;
  }

  /**
   * Set the optimize flag. This fires a PROP_OPTIMIZE property change with the
   * old and new values.
   *
   * @param opt optimize flag
   */
  public void setOptimize(boolean opt) {
    boolean old = this.optimize;
    if (opt && !okToOptimize()) {	// if cut falls off top/bottom, don't optimize
      this.optimize = false;
    } else {
      this.optimize = opt;
    }
    makeDrawables();
    this.pcs.firePropertyChange(PROP_OPTIMIZE, old, optimize);
  }

  /**
   * Check if it's OK to optimize. If the cutter would fall off the top or
   * bottom of the shape, then NO.
   *
   * @return true=OK to optimize (probably)
   */
  private boolean okToOptimize() {
    Curve c;
    if (cutter.getLocation().isInside()) {
      c = outline.getInsideCurve();
    } else {
      c = outline.getOutsideCurve();
    }
    if (c.getTopPoint().distance(getTangentPoint()) < getWidthAtMax()) {
      return false;
    }
    if (c.getBottomPoint().distance(getTangentPoint()) < getWidthAtMax()) {
      return false;
    }
    return true;
  }

  // Need to Override anything that moves the point to check if optimize is still OK.
  @Override
  public void setX(double x) {
    super.setX(x);
    if (optimize && !okToOptimize()) {		// if cut falls off top/bottom, don't optimize
      setOptimize(false);
    }
  }

  @Override
  public void setZ(double z) {
    super.setZ(z);
    if (optimize && !okToOptimize()) {		// if cut falls off top/bottom, don't optimize
      setOptimize(false);
    }
  }

  @Override
  public void move(Point2D.Double p) {
    super.move(p);
    if (optimize && !okToOptimize()) {		// if cut falls off top/bottom, don't optimize
      setOptimize(false);
    }
  }

  @Override
  public void move(double x, double z) {
    super.move(x, z);
    if (optimize && !okToOptimize()) {		// if cut falls off top/bottom, don't optimize
      setOptimize(false);
    }
  }

  @Override
  public void setSnap(boolean s) {
    super.setSnap(s);
    if (optimize && !okToOptimize()) {		// if cut falls off top/bottom, don't optimize
      setOptimize(false);
    }
  }

  @Override
  public void snapToCurve() {
    super.snapToCurve();
    if (optimize && !okToOptimize()) {		// if cut falls off top/bottom, don't optimize
      setOptimize(false);
    }
  }

  /**
   * Get the required cut depth to make a cut of the given width. Note this is
   * for HCF only and is based on a flat surface and does not take curvature
   * into consideration.
   *
   * @param w desired width
   * @return cut depth
   */
  private double getDepthForWidth(double w) {
    if (w >= (2.0 * cutter.getRadius())) {
      return cutter.getRadius();
    }
    return cutter.getRadius() * (1.0 - Math.sqrt(1.0 - Math.pow(w / (2.0 * cutter.getRadius()), 2.0)));
  }

  /**
   * Get the width of a cut for a given depth. Note this is for HCF only and is
   * based on a flat surface and does not take curvature into consideration.
   *
   * @param d depth of cut
   * @return cut width
   */
  private double getWidthForDepth(double d) {
    if (d >= cutter.getRadius()) {
      return 2.0 * cutter.getRadius();
    }
    return 2 * cutter.getRadius() * Math.sqrt(1.0 - Math.pow(1.0 - d / cutter.getRadius(), 2.0));
  }

  /**
   * Get the normalized movement vector for the pattern motion. It is the
   * direction of movement that will produce the cut pattern. It will be in a
   * direction AWAY from the center (front cuts toward front, back cuts toward
   * back).
   *
   * @param scale scale factor (use 1.0 if you want normalized)
   * @return movement vector
   */
  private Vector2d getMoveVector(double scale) {
    Vector2d perpVectN = getPerpVector(1.0);
    Vector2d moveN;
    // movement is right angle to perpVector (like a CONTOUR rosette)
    switch (cutter.getLocation()) {
      case FRONT_INSIDE:
      case BACK_INSIDE:
      default:
        moveN = new Vector2d(-perpVectN.y, perpVectN.x);	// move out to front and downward
        break;
      case FRONT_OUTSIDE:
      case BACK_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          moveN = new Vector2d(-perpVectN.y, perpVectN.x);	// move out to front and downward
        } else {						// bottom of a shape
          moveN = new Vector2d(perpVectN.y, -perpVectN.x);	// move out to front and upward
        }
        break;
    }
    moveN.scale(scale);
    return moveN;
  }

  /**
   * Build the specific drawable shapes for an RosettePoint
   */
  @Override
  protected synchronized final void makeDrawables() {
    super.makeDrawables();    // text and cutter at rest and tangent lines

    // Generate the various vectors
    Vector2d perpVectorS = getPerpVector(cutDepth);	// maximum cut displacement
    Vector2d moveVectorS = getMoveVector(getWidthAtMax() / 2.0);		// scaled movement vector

    // cut extent
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(perpVectorS.y, perpVectorS.x) * 180.0 / Math.PI;
        // graphics similar to contour motion on a regular rosette
        drawList.add(new Arc(new Point2D.Double(getX() + perpVectorS.x + moveVectorS.x, getZ() + perpVectorS.y + moveVectorS.y),
            cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, CONTOUR_COLOR));
        drawList.add(new Arc(new Point2D.Double(getX() + perpVectorS.x - moveVectorS.x, getZ() + perpVectorS.y - moveVectorS.y),
            cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, CONTOUR_COLOR));
        // The cut at zero movement at max cut depth
        drawList.add(new Arc(new Point2D.Double(getX() + perpVectorS.x, getZ() + perpVectorS.y),
            cutter.getRadius(), cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, OFFSET_COLOR));
        break;
      // PatternPoint doesn't work for Drill or ECF
      case Drill:
      case ECF:
        break;
    }
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    Line3D line = new Line3D();
    for (int i = 0; i < (NUM_3D_PTS + 1); i++) {
      double angDeg = (double) i * 360.0 / (double) NUM_3D_PTS;	// in degrees
      double angRad = Math.toRadians(angDeg);
      Vector2d xz = patternMove(angDeg, optimize);   // xz location due to rosette motion
      xz.x = xz.x + getWidthForDepth(-xz.y) / 2.0;	// show max extent, not center of cutter
      double r = Math.hypot(xz.x, getY());		// cylindrical radius  NOTE: y should always be 0.0
      if (cutter.getLocation().isBack()) {	// if in back, add 180 degrees rotation
        angRad += Math.PI;
      }
      line.add(new Point3D(r * Math.cos(-angRad), r * Math.sin(-angRad), xz.y + cutDepth));
    }

    line.rotate(RotMatrix.Axis.Y, getTangentAngle());     // rotate around Y axis
    line.translate(getTangentPointX(), 0.0, getTangentPointY()); // and offset to the tangent point
    if (indexOffset != 0) {
      double angleDeg = -360.0 * (double) indexOffset / (double) indexWheelHoles();
      line.rotate(RotMatrix.Axis.Z, angleDeg);
    }
    for (int i = 0; i < repeat; i++) {
      list3D.add(new Line3D(line.getPoints()));
      line.rotate(RotMatrix.Axis.Z, 360.0 / repeat);
    }
  }

  @Override
  public synchronized void cutSurface(Surface surface, ProgressMonitor monitor) {
    double zRotation = indexOffsetDegrees();
    if (zRotation != 0.0) {
      surface.rotateZ(zRotation);			// initial repeatPhase rotation
    }
    for (int i = 0; i < repeat; i++) {
      if (i > 0) {
        surface.rotateZ(360.0 / repeat);	// rotate to the next one
        zRotation += 360.0 / repeat;		// keep track of cumulative rotation
      }
      surface.offset(-getX(), 0.0, -getZ());			// move the surface over the offset point
      surface.rotateY(-getTangentAngle());

      // this is where the main work is done
      int nSectors = surface.numSectors();		// number of sectors around shape
      double dAngle = 360.0 / (double) nSectors;	// angle increment degrees
      Vector2d cutXZ;						// Location of center of cutter
      double spindleC, lastC = 0.0;
      int count;
      for (count = 0, spindleC = 0.0; count < nSectors; count++, spindleC += dAngle) {
        cutXZ = patternMove(spindleC, optimize);
        surface.rotateZ(spindleC - lastC);		// incremental rotate the surface
        surface.cutSurface(cutter, cutXZ.x, cutXZ.y);
        lastC = spindleC;
      }
      surface.rotateZ(360.0 - lastC);		// bring it back to the starting point

      surface.rotateY(getTangentAngle());
      surface.offset(getX(), 0.0, getZ());	// move the surface back so that the z rotation will be around the center
    }
    if (zRotation < 360.0) {
      surface.rotateZ(360.0 - zRotation);		// this should get you back to original rotation
    }
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
  public void makeInstructions(double passDepth, int passStep, double lastDepth, int lastStep, int stepsPerRot, Rotation rotation) {
    super.makeInstructions(passDepth, passStep, lastDepth, lastStep, stepsPerRot, rotation);	// writes comments only
    cutList.comment("PatternPoint " + num);
    cutList.comment("Cutter: " + cutter);
    cutList.spindleWrapCheck();

    // move to position before applying depth
    Vector2d start = patternMove(0.0, optimize);
    start.y = start.y + cutDepth;			// to make sure the cutter is in the air
    cutList.goToXZC(FAST, start, 0.0);

    // Increasing cut depth with the coarse depth per cut
    double depth = 0.0;
    while (depth < (cutDepth - lastDepth)) {		// this is not the last cut
      depth = Math.min(cutDepth - lastDepth, depth + passDepth);
      boolean dir = (rotation == NEG_LAST) ? NOT_LAST : ((rotation == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followPattern(depth, passStep, dir, stepsPerRot);
    }
    if (lastDepth > 0.0) {		// this is the last cut
      boolean dir = (rotation == NEG_LAST) ? LAST : ((rotation == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followPattern(cutDepth, lastStep, dir, stepsPerRot);
    }

    // back to the starting position
    cutList.spindleWrapCheck();
    cutList.goToXZC(FAST, start, 0.0);
  }

  /**
   * Add instructions to the cutList for following patterns.
   *
   * @param depth the depth of this cut
   * @param step the number of stepper motor steps per increment (for the
   * spindle motor)
   * @param last true=this is the last (turn the other way).
   * @param stepsPerRot steps per rotation
   */
  private void followPattern(double depth, int step, boolean last, int stepsPerRot) {
    // If custom pattern with STRAIGHT line segments, just use the breakpoints.
    // It runs faster!
    if (pattern instanceof CustomPattern) {
      if (pattern.getCustomStyle() == CustomPattern.CustomStyle.STRAIGHT) {
        ArrayList<Double> angles = new ArrayList<>();		// a list of the angles
        for (int i = 0; i < patternRepeat; i++) {
          double cc = (double) i / (double) patternRepeat * 360.0;
          for (Pt pt : pattern.getAllPoints()) {
            double c = pt.getX() * 360.0 / (double) patternRepeat + cc - phase / patternRepeat;	// angle including phase
            if (c < 0.0) {				// make sure all angles are 0.0 <= c <= 360.0
              c = c + 360.0;
            } else if (c > 360.0) {
              c = c - 360.0;
            }
            angles.add(c);
          }
        }
        angles.add(0.0);			// make sure we include 0.0 and 360.0
        angles.add(360.0);
        // reminder: line2 has same x coordinates as the first line
        Collections.sort(angles);	// sorted order 0.0 to 360.0
        for (int i = angles.size() - 1; i > 0; i--) {	// delete any duplicates
          if (aboutEqual(angles.get(i), angles.get(i - 1))) {
            angles.remove(i);
          }
        }
        boolean firstPt = true;
        if (last) {			// go in reverse the last time
          for (int i = angles.size() - 1; i >= 0; i--) {
            double a = angles.get(i) - 360.0;
            if (firstPt) {
              cutList.goToXZC(VELOCITY, patternMove(a, optimize), a);	// first point at velocity
              firstPt = false;
            } else {
              cutList.goToXZC(RPM, patternMove(a, optimize), a);	// go to this point at rpm
            }
          }
        } else {			// other passes go forward
          for (Double a : angles) {
            if (firstPt) {
              cutList.goToXZC(VELOCITY, patternMove(a, optimize), a);	// first point at velocity
              firstPt = false;
            } else {
              cutList.goToXZC(RPM, patternMove(a, optimize), a);	// go to this point at rpm
            }
          }
        }
        return;
      }
    }

    // This is the regular way of cutting a pattern (i.e. not straight lines)
    for (int i = 0; i <= stepsPerRot; i = i + step) {
      double c = 360.0 * (double) i / (double) stepsPerRot;
      if (last) {
        c = -c;	// other direction last time
      }
      if (i == 0) {
        cutList.goToXZC(VELOCITY, patternMove(c, optimize), c);	// first point at velocity
      } else {
        cutList.goToXZC(RPM, patternMove(c, optimize), c);	// and other points at RPM
      }
    }
    // When step is not a submultiple of stepsPerRot, 
    // then we might not be back at +/- 360.0
    if ((stepsPerRot % step) != 0) {
      if (last) {
        cutList.goToXZC(RPM, patternMove(-360, optimize), -360.0);
      } else {
        cutList.goToXZC(RPM, patternMove(360, optimize), 360.0);
      }
    }
  }

  @Override
  public void writeXML(PrintWriter out) {
    String optional = "";
    if (patternRepeat != 1) {
      optional += " patternRepeat='" + repeat + "'";
    }
    if (phase != 0.0) {
      optional += " phase='" + F1.format(phase) + "'";
    }
    if (optimize) {
      optional += " optimize='" + optimize + "'";
    }
    out.println(indent + "<PatternPoint"
        + xmlCutPointInfo()
        + " repeat='" + repeat + "'"
        + " indexOffset='" + indexOffset + "'"
        + " pattern='" + pattern.getName() + "'"
        + optional
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    indentLess();
    out.println(indent + "</PatternPoint>");
  }

  /**
   * Determine the offset caused by a pattern at a given angle. The x value is
   * the offset from the PatternPoint and the y value is the cut depth (always a
   * negative number).
   *
   * @param angDeg angle in degrees
   * @param compensate true = compensate for the curvature of the shape
   * @return x,y offset from zero.
   */
  private Vector2d patternMove(double angDeg, boolean compensate) {
    double fracX = fractional(angleCheck(angDeg + phase / patternRepeat) / (360.0 / patternRepeat));	// remainder in the range 0 to 1
    double fracYMax = pattern.getValue(fracX);				// also in the range 0 to 1
    double fracYMin = 0.0;
    if (pattern.isDual()) {
      fracYMin = pattern.getValue2(fracX);		// could be negative
    }
    double widthOfCut = (fracYMax - fracYMin) * getWidthAtMax();
    double depthOfCut = getDepthForWidth(widthOfCut);
    double xMove = widthOfCut / 2.0;
    if (pattern.isDual() && (fracYMin != 0.0)) {
      xMove = (fracYMax + fracYMin) / 2.0 * getWidthAtMax();
    }
//		System.out.println("ang:" + angDeg + " min:" + fracYMin + " max:" + fracYMax + " w:" + widthOfCut + " xMove:" + xMove);
    double yMove = depthOfCut;

    // This is to compensate for the curvature of the shape
    if (compensate && (depthOfCut > 0.0) && (widthOfCut > 0.0)) {		// skip angles where there is no cut
      double widthToMax = fracYMax * getWidthAtMax();
      // p2 is where we want the max cut to end up
      Point3D p2 = findPoint(widthToMax, angDeg);
      // pStart is the beginning of the cut -- usually at the tangent point
      Point3D pStart = new Point3D(getTangentPointX(), getTangentPointY(), 0.0);
      if (pattern.isDual() && (fracYMin != 0.0)) {
        pStart = findPoint(fracYMin * getWidthAtMax(), angDeg);		// pStart is beginning of the cut
      }
      if ((p2 == null) || (pStart == null)) {
        setOptimize(false);
        NotifyDescriptor d = new NotifyDescriptor.Message("PatternPoint" + this.toString()
            + "\nUnable to provide curve compensation."
            + "\nOptimization turned OFF for this point",
            NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
      } else {
        // v1 is the vector from the start point to p2
        Vector3d v1 = new Vector3d(p2.getX() - pStart.getX(), p2.getY() - pStart.getY(), p2.getZ() - pStart.getZ());
        // vPerp is perpendicular from the tangent point
        Vector3d vPerp = new Vector3d(getPerpVector(1.0).x, getPerpVector(1.0).y, 0.0);
        // aRad is the angle we need to tilt down into the shape to compensate
        double aRad = Math.PI / 2.0 - vPerp.angle(v1);
        // now correct the cutter center (v2) by tilting it down to v3
        Vector2d v2 = new Vector2d(cutter.getRadius() - depthOfCut, xMove);
        Vector2d v3 = v2.rotate(Math.toDegrees(aRad));
//        System.out.println("ang=" + F2.format(angDeg)
//            + "  xMove=" + F3.format(xMove)
//            + "  yMove=" + F3.format(yMove)
//            + "  widthOfCut=" + F3.format(widthOfCut)
//            + "  depthOfCut=" + F3.format(depthOfCut)
//            + "  pStart=(" + F2.format(pStart.x) + "," + F2.format(pStart.y) + "," + F2.format(pStart.z) + ")"
//            + "  p2=(" + F2.format(p2.x) + "," + F2.format(p2.y) + "," + F2.format(p2.z) + ")"
//            + "  v1.l=" + F3.format(v1.length())
//            + "  aRad=" + F2.format(Math.toDegrees(aRad))
//            + "  v2=(" + F3.format(v2.x) + "," + F3.format(v2.y) + ")"
//            + "  v3=(" + F3.format(v3.x) + "," + F3.format(v3.y) + ")"
//            + "  d=" + F3.format(cutter.getCutRadius() - v3.x)
//            + "  xMove=" + F3.format(v3.y)
//            + "  yMove=" + F3.format(cutter.getCutRadius() - v3.x)
//        );
        xMove = v3.y;
        yMove = cutter.getRadius() - v3.x;
      }
    }

    switch (cutter.getLocation()) {	// correct sign for cutter location
      case FRONT_INSIDE:
      case FRONT_OUTSIDE:		// no need to check for top/bottom because shape is rotated
      default:
        return new Vector2d(xMove, -yMove);
      case BACK_INSIDE:
      case BACK_OUTSIDE:
        return new Vector2d(-xMove, -yMove);
    }
  }

  /**
   * Find the point on the surface that is a given distance away from the
   * tangent point at a given angle.
   *
   * @param dist distance (positive means down the curve)
   * @param angDeg angle in degrees from down the curve
   * @return point on surface
   */
  private Point3D findPoint(double dist, double angDeg) {
    // p1 is down (or up) from the tangent point and is on the circle of the max cut
    Point2D.Double p1 = outline.getOutsideCurve().interpolateDown(getTangentPoint(), -dist * Math.cos(Math.toRadians(angDeg)));
    if (p1 == null) {
      return null;
    }
    // phiRad is how far out on the circle of max cut
    double phiRad = 2.0 * Math.asin(dist * Math.sin(Math.toRadians(angDeg)) / (2.0 * p1.x));
    // p2 is where we want the max cut to end up
    return new Point3D(p1.x * Math.cos(phiRad), p1.y, p1.x * Math.sin(phiRad));
  }

  /**
   * Get the fractional part of the given number.
   *
   * @param n
   * @return fractional part in the range 0 to 0.99999
   */
  private double fractional(double n) {
    return n - (int) n;
  }
}
