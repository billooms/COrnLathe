package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.controls.CoarseFine;
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
import com.billooms.patternbar.PatternBar;
import com.billooms.patterns.Patterns;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import javax.swing.ProgressMonitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The beginning of a spiral line as if done on a straight line machine.
 * Direction of the cut is always perpendicular to the surface.
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
public class LinePoint extends CutPoint {

  /** Property name used when changing an IndexPoint's repeat */
  String PROP_REPEAT = PROP_PREFIX + "Repeat";
  /** Property name used when changing an IndexPoint's phase */
  String PROP_PHASE = PROP_PREFIX + "Phase";
  /** Property name used when changing an IndexPoint's mask */
  String PROP_MASK = PROP_PREFIX + "Mask";

  /** Default repeat. */
  public final static int DEFAULT_REPEAT = 8;
  /** Color of the drawn cut. */
  protected final static Color LINEPT_COLOR = Color.YELLOW;
  /** Color used for the maximum movement of the cut. */
  protected final static Color LINEPT_COLOR2 = Color.ORANGE;
  /** Go into the air by this much between cuts. */
  protected final static double LINEPOINT_SAFETY = 0.050;

  /** Number of cuts around the shape. */
  private int repeat = DEFAULT_REPEAT;
  /** Phase shift in degrees. */
  private double phase = 0.0;
  /** Mask some of the repeats -- 0 is skip, 1 is don't skip. */
  private String mask = "";
  /** PatternBar for the line. */
  private final PatternBar patternBar;

  /**
   * Construct a new LinePoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   * @param patMgr pattern manager
   */
  public LinePoint(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    super(element, cutMgr, outline);
    this.repeat = CLUtilities.getInteger(element, "repeat", DEFAULT_REPEAT);
    this.phase = CLUtilities.getDouble(element, "phase", 0.0);
    this.mask = CLUtilities.getString(element, "mask", "");
    NodeList pbNodes = element.getElementsByTagName("PatternBar");
    patternBar = new PatternBar((Element) pbNodes.item(0), patMgr);    // should always be one
    makeDrawables();
    patternBar.addPropertyChangeListener(this);
  }

  /**
   * Construct a new LinePoint at the given position with information from the
   * given LinePoint. This is primarily used when duplicating an LinePoint.
   *
   * @param pos new position
   * @param cpt LinePoint to copy from
   */
  public LinePoint(Point2D.Double pos, LinePoint cpt) {
    super(pos, cpt);
    this.repeat = cpt.getRepeat();
    this.phase = cpt.getPhase();
    this.mask = cpt.getMask();
    this.patternBar = new PatternBar(cpt.getPatternBar());
    makeDrawables();
    patternBar.addPropertyChangeListener(this);
  }

  /**
   * Construct a new LinePoint at the given position with default values. This
   * is primarily used when adding a first LinePoint from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   * @param patMgr pattern manager
   */
  public LinePoint(Point2D.Double pos, Cutter cut, Outline outline, Patterns patMgr) {
    super(pos, cut, outline);
    patternBar = new PatternBar(patMgr);
    makeDrawables();
    patternBar.addPropertyChangeListener(this);
  }

  @Override
  public String toString() {
    return super.toString() + " " + repeat + " " + F1.format(phase) + "deg";
  }

  @Override
  public synchronized void clear() {
    super.clear();
    patternBar.removePropertyChangeListener(this);
    patternBar.clear();
  }

  /**
   * Get the PatternBar.
   *
   * @return PatternBar
   */
  public PatternBar getPatternBar() {
    return patternBar;
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
//    this.phase = angleCheck(ph);    // When cutting surface, modPt may have negative phase
    this.phase = ph;
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
   * Calculate the moveVector.
   *
   * It is the direction of movement that will produce the pattern. At present,
   * this is always perpendicular to the curve.
   *
   * @param scale scale factor (use 1.0 for normalized)
   * @return the movement vector
   */
  protected Vector2d getMoveVector(double scale) {
    return getPerpVector(scale);
  }

  @Override
  protected synchronized final void makeDrawables() {
    super.makeDrawables();    // text and cutter at rest
    pt.setColor(LINEPT_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(LINEPT_COLOR);	// the text
      drawList.get(1).setColor(LINEPT_COLOR);	// the cutter
    }

    if (cutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // movement is always perpendicular
    Vector2d moveVectorS = getMoveVector(cutDepth);
    // Line indicating direction of moveVectorS
    drawList.add(new Line(getPos2D(), moveVectorS.x, moveVectorS.y, LINEPT_COLOR));

    // cut extent
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(moveVectorS.y, moveVectorS.x) * 180.0 / Math.PI;
        drawList.add(new Arc(new Point2D.Double(getX() + moveVectorS.x, getZ() + moveVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, LINEPT_COLOR2));
        break;
      // Profile of drill & Fixed at cut depth
      case Fixed:
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x, getZ() + moveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), LINEPT_COLOR2, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x + v1.x, getZ() + moveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), LINEPT_COLOR2, SOLID_LINE));
        v1 = v1.rotate(180.0);
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + moveVectorS.x + v1.x, getZ() + moveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), LINEPT_COLOR2, SOLID_LINE));
        break;
    }
  }

  @Override
  public void writeXML(PrintWriter out) {
    String optional = "";
    if (!mask.isEmpty()) {
      optional += " mask='" + mask + "'";
    }
    out.println(indent + "<LinePoint"
        + xmlCutPointInfo()
        + " repeat='" + repeat + "'"
        + " phase='" + F1.format(phase) + "'"
        + optional
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    patternBar.writeXML(out);
    indentLess();
    out.println(indent + "</LinePoint>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("LinePoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listening to the PatternBar
    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  /**
   * Make the 3D Lines for this LinePoint. Use this instead of make3DLines().
   *
   * @param xyz list of unrotated points in x, y, z space
   */
  void make3DLines(ArrayList<Point3D> xyz) {
    list3D.clear();
    String fullMask = mask;
    if (!fullMask.isEmpty()) {
      while (fullMask.length() < repeat) {
        fullMask += mask;	// fill out to full length
      }
    }
    double ang = phase / repeat;
    for (int i = 0; i < repeat; i++) {
      if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
        Line3D line = new Line3D(xyz);
        line.rotate(RotMatrix.Axis.Z, ang);
        list3D.add(line);
      }
      ang -= 360.0 / (double) repeat;
    }
  }

  @Override
  protected void make3DLines() {
    throw new UnsupportedOperationException("LinePoint.make3DLines() Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

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
        if (cutter.canFastRender()) {
          surface.cutSurface(cutter, cutX, cutZ, spindleC); // fast cut rendering only for IDEAL HCF
        } else {
          surface.cutSurface(cutter, cutX, cutZ);
        }
        lastC = spindleC;
      }
    }
    surface.rotateZ(360.0 - lastC);		// bring it back to the starting point
  }
  
  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot) {
    throw new UnsupportedOperationException("LinePoint.makeInstructions Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
