package com.billooms.rosette;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.drawables.Drawable;
import com.billooms.drawables.simple.Circle;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.simple.Plus;
import com.billooms.patterns.Patterns;
import static com.billooms.rosette.Rosette.DEFAULT_PHASE;
import static com.billooms.rosette.Rosette.DEFAULT_PTOP;
import static com.billooms.rosette.Rosette.DEFAULT_REPEAT;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.w3c.dom.Element;


/**
 * Abstract class for a generalized Rose Engine rosette.
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
public abstract class BasicRosette extends CLclass {
  
  /** All Rosette property change names start with this prefix */
  public final static String PROP_PREFIX = "Rosette" + "_";
  /** Property name used for changing the peak-to-peak amplitude */
  public final static String PROP_PTOP = PROP_PREFIX + "PeakToPeak";
  /** Property name used for changing the repeat */
  public final static String PROP_REPEAT = PROP_PREFIX + "Repeat";
  /** Property name used for changing the phase */
  public final static String PROP_PHASE = PROP_PREFIX + "Phase";
  /** Property name used for changing the invert flag */
  public final static String PROP_INVERT = PROP_PREFIX + "Invert";
  
  /** Default peak-to-peak amplitude (currently set to 0.1) */
  public final static double DEFAULT_PTOP = 0.1;
  /** Default repeat (currently set to 8) */
  public final static int DEFAULT_REPEAT = 8;
  /** Default phase (currently set to 0.0) */
  public final static double DEFAULT_PHASE = 0.0;
  
  /* Information for drawing */
  static final BasicStroke SOLID_LINE = new BasicStroke(1.0f);
  static final BasicStroke DOT_LINE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{3, 3}, 0);
  static final Color OUTLINE_COLOR = Color.BLACK;
  static final Color RADIUS_COLOR = Color.BLUE;
  static final int NUM_POINTS = 720;	    // draw a point every 1/2 degree
  final Point2D.Double center = new Point2D.Double(0.0, 0.0);   // center of the rosette is always 0.0, 0.0
  final ArrayList<Drawable> drawList = new ArrayList<>();   // a list of things to draw for a visual representaiton of the rosette

  /** Pattern manager. */
  protected Patterns patternMgr = null;
  /** Peak-to-Peak amplitude. */
  protected double pToP = DEFAULT_PTOP;
  /** Number of repeats around the rosette. */
  protected int repeat = DEFAULT_REPEAT;
  /** Phase shift in degrees (0 to 360), +phase is CCW. */
  protected double phase = DEFAULT_PHASE;
  /** Flag to invert the pattern (like rubbing on the backside). */
  protected boolean invert = false;
  
  /**
   * Construct a BasicRosette with default values and a null pattern manager. 
   */
  public BasicRosette() {
  }
  
  /**
   * Construct a BasicRosette with default values and the given pattern manager.
   * 
   * @param patMgr pattern manager
   */
  public BasicRosette(Patterns patMgr) {
    this.patternMgr = patMgr;
  }
  
  /**
   * Construct a BasicRosette from the given BasicRosette. 
   * 
   * @param r some BasicRosette
   */
  public BasicRosette(BasicRosette r) {
    this.patternMgr = r.patternMgr;
    this.pToP = r.pToP;
    this.repeat = r.repeat;
    this.phase = r.phase;
    this.invert = r.invert;
  }

  /**
   * Define a new BasicRosette from DOM Element.
   * Note that repeat is dependent on the extension to this abstract class.
   *
   * @param element DOM Element
   * @param patMgr pattern manager with available patterns
   */
  public BasicRosette(Element element, Patterns patMgr) {
    this.patternMgr = patMgr;
    this.pToP = CLUtilities.getDouble(element, "amp", DEFAULT_PTOP);
    this.phase = CLUtilities.getDouble(element, "phase", DEFAULT_PHASE);
    this.invert = CLUtilities.getBoolean(element, "invert", false);
  }

  /**
   * Get the peak-to-peak amplitude of the rosette.
   *
   * @return peak-to-peak amplitude
   */
  public double getPToP() {
    return pToP;
  }

  /**
   * Set the peak-to-peak amplitude of the rosette.
   *
   * This fires a PROP_PTOP property change with the old and new amplitudes.
   *
   * @param p peak-to-peak amplitude
   */
  public void setPToP(double p) {
    double old = this.pToP;
    this.pToP = p;
    this.pcs.firePropertyChange(PROP_PTOP, old, pToP);
  }
  
  /**
   * Get the number of repeats on the rosette.
   *
   * @return number of repeats
   */
  public int getRepeat() {
    return repeat;
  }

  /**
   * Set the number of repeats on the rosette.
   * Not all extensions to BasicRosette will use a repeat, in which case it 
   * should always be set to 1.
   *
   * This fires a PROP_REPEAT property change with the old and new repeats.
   *
   * @param n number of repeats
   */
  public abstract void setRepeat(int n);

  /**
   * Get the phase of the rosette
   *
   * @return phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of the
   * repeat, etc.
   */
  public double getPhase() {
    return phase;
  }

  /**
   * Get the phase of the rosette as a fraction of a repeat.
   *
   * @return phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public double getPh() {
    return phase / 360.0;
  }

  /**
   * Set the phase of the rosette.
   *
   * Phase can be negative. 
   * This fires a PROP_PHASE property change with the old and new phases.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public void setPhase(double ph) {
    double old = this.phase;
    this.phase = ph;
    this.pcs.firePropertyChange(PROP_PHASE, old, phase);
  }

  /**
   * Set the phase of the rosette.
   *
   * Phase can be negative. 
   * This fires a PROP_PHASE property change with the old and new phases.
   *
   * @param ph phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public void setPh(double ph) {
    setPhase(ph * 360.0);
  }

  /**
   * Get the invert flag.
   *
   * @return invert flag
   */
  public boolean getInvert() {
    return invert;
  }

  /**
   * Set the invert flag.
   *
   * This fires a PROP_INVERT property change with the old and new values.
   *
   * @param inv true=invert
   */
  public void setInvert(boolean inv) {
    boolean old = invert;
    this.invert = inv;
    this.pcs.firePropertyChange(PROP_INVERT, old, invert);
  }
  
  /**
   * Clear the rosette as required.
   */
  public abstract void clear();

  /**
   * Get the amplitude (offset from nominal radius) of the rosette at a given
   * angle in degrees. A returned value of zero means zero deflection from its
   * nominal radius.
   *
   * @param ang Angle in degrees around the rosette
   * @param inv invert the returned value (as if rubbing on the backside of the
   * rosette).
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public abstract double getAmplitudeAt(double ang, boolean inv);

  /**
   * Get the amplitude (offset from nominal radius) of the rosette at a given
   * angle in degrees. A returned value of zero means zero deflection from its
   * nominal radius.
   *
   * @param ang Angle in degrees around the rosette
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public abstract double getAmplitudeAt(double ang);

  /**
   * Make sure angle is in range 0.0 <= a < 360.0
   *
   * @param ang angle in degrees
   * @return angle in range 0.0 <= a < 360.0
   */
  protected double angleCheck(double ang) {
    while (ang < 0.0) {
      ang += 360.0;
    }
    while (ang >= 360.0) {
      ang -= 360.0;
    }
    return ang;
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   * @param nomRadius Nominal radius of the drawn rosette
   */
  public void paint(Graphics2D g2d, double nomRadius) {
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setStroke(SOLID_LINE);

    makeDrawables(nomRadius);    // make drawables when needed for painting
    drawList.stream().forEach((item) -> {
      // paint everything in the drawlist
      item.paint(g2d);
    });
  }

  /**
   * Make the rosette appearance based on stored values.
   * 
   * @param nomRadius Nominal radius of the drawn rosette
   */
  private void makeDrawables(double nomRadius) {
    drawList.clear();			// clear out the old drawlist
    drawList.add(new Plus(center, RADIUS_COLOR));  // always draw a center mark
    drawList.add(new Circle(new Point2D.Double(0.0, 0.0), nomRadius, RADIUS_COLOR, DOT_LINE));  // circle at nominal radius

    Point2D.Double[] pts = new Point2D.Double[NUM_POINTS + 1];     // add 1 for wrap-around
    double rad, r;
    for (int i = 0; i <= NUM_POINTS; i++) {
      // Add PI so that the pattern starts on the left side.
      // Minus sign so that pattern goes clockwise such that 
      // a positive spindle rotation brings the feature to the left side.
      rad = -Math.toRadians((double) i) + Math.PI;
      r = nomRadius - getAmplitudeAt((double) i);
      pts[i] = new Point2D.Double(r * Math.cos(rad), r * Math.sin(rad));
    }
    drawList.add(new Curve(pts, OUTLINE_COLOR, SOLID_LINE));
  }
}
