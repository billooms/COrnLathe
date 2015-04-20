package com.billooms.patternbar;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 * Pattern bar for a straight pattern.
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
public class PatternBar extends CLclass {

  /** All Rosette property change names start with this prefix */
  public final static String PROP_PREFIX = "PatternBar" + "_";
  /** Property name used for changing the pattern */
  public final static String PROP_PATTERN = PROP_PREFIX + "Pattern";
  /** Property name used for changing the peak-to-peak amplitude */
  public final static String PROP_PTOP = PROP_PREFIX + "PeakToPeak";
  /** Property name used for changing the period */
  public final static String PROP_PERIOD = PROP_PREFIX + "Period";
  /** Property name used for changing the phase */
  public final static String PROP_PHASE = PROP_PREFIX + "Phase";
  /** Property name used for changing the invert flag */
  public final static String PROP_INVERT = PROP_PREFIX + "Invert";
  /** Property name used for changing the optional 2nd integer parameter */
  public final static String PROP_N2 = PROP_PREFIX + "N2";
  /** Property name used for changing the optional 2nd amplitude */
  public final static String PROP_AMP2 = PROP_PREFIX + "Amp2";
  /** Some patterns need a repeat value, so use this. */
  private final static int DEFAULT_REPEAT = 8;

  /** Default Style Name (currently set to "SINE") */
  public final static String DEFAULT_PATTERN = "SINE";
  /** Default peak-to-peak amplitude (currently set to 0.1) */
  public final static double DEFAULT_PTOP = 0.1;
  /** Default period distance (currently set to 0.5) */
  public final static double DEFAULT_PERIOD = 0.5;
  /** Default phase (currently set to 0.0) */
  public final static double DEFAULT_PHASE = 0.0;
  /** Default for optional second integer parameter */
  public final static int DEFAULT_N2 = 3;
  /** Default for optional second amplitude parameter */
  public final static double DEFAULT_AMP2 = 0.1;

  /** Pattern for the rosette. */
  private Pattern pattern;
  /** Peak-to-Peak amplitude. */
  private double pToP = DEFAULT_PTOP;
  /** Period distance. */
  private double period = DEFAULT_PERIOD;
  /** Phase shift in degrees (0 to 360), +phase is CCW. */
  private double phase = DEFAULT_PHASE;
  /** Flag to invert the pattern (like rubbing on the backside). */
  private boolean invert = false;
  /** Optional 2nd integer parameter. */
  private int n2 = DEFAULT_N2;
  /** Optional 2nd amplitude parameter. */
  private double amp2 = DEFAULT_AMP2;

  private static COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);
  private Patterns patternMgr = null;

  /**
   * Define a PatternBar with the given values.
   *
   * @param patternName Pattern name of the pattern to use
   * @param amplitude Peak-to-Peak
   * @param period period distance
   * @param phase Phase in degrees, where 360 means one pattern repeat
   * @param patMgr pattern manager with available patterns
   */
  public PatternBar(String patternName, double amplitude, double period, double phase, Patterns patMgr) {
    this.patternMgr = patMgr;
    pattern = patternMgr.getPattern(patternName);
    this.period = period;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
      this.phase = 0.0;
    } else {
      this.pToP = amplitude;
      this.phase = angleCheck(phase);
      if (pattern instanceof CustomPattern) {
        patternMgr.addPropertyChangeListener(this);
      }
    }
  }

  /**
   * Define a PatternBar with default values.
   *
   * @param patMgr pattern manager with available patterns
   */
  public PatternBar(Patterns patMgr) {
    this(DEFAULT_PATTERN, DEFAULT_PTOP, DEFAULT_PERIOD, DEFAULT_PHASE, patMgr);
  }

  /**
   * Make a new PatternBar with the same values as the given PatternBar
   *
   * @param pb PatternBar
   */
  public PatternBar(PatternBar pb) {
    this(pb.pattern.getName(), pb.getPToP(), pb.getPeriod(), pb.getPhase(), pb.patternMgr);
    this.invert = pb.getInvert();
    this.amp2 = pb.getAmp2();
    this.n2 = pb.getN2();
  }

  /**
   * Define a new PatternBar from DOM Element.
   *
   * @param element DOM Element
   * @param patMgr pattern manager with available patterns
   */
  public PatternBar(Element element, Patterns patMgr) {
    this(CLUtilities.getString(element, "pattern", DEFAULT_PATTERN),
        CLUtilities.getDouble(element, "amp", DEFAULT_PTOP),
        CLUtilities.getDouble(element, "period", DEFAULT_PERIOD),
        CLUtilities.getDouble(element, "phase", DEFAULT_PHASE),
        patMgr);
    invert = CLUtilities.getBoolean(element, "invert", false);
    n2 = CLUtilities.getInteger(element, "n2", DEFAULT_N2);
    amp2 = CLUtilities.getDouble(element, "amp2", DEFAULT_AMP2);
  }

  /**
   * Create a string representation for this object.
   *
   * @return string representation.
   */
  @Override
  public String toString() {
    String str = pattern.getName();
    if (invert) {
      str += "(I)";
    }
    str += " A:" + F3.format(pToP);
    str += " P:" + F3.format(period);
    if (prefs.isFracPhase()) {
      str += " ph:" + F4.format(phase / 360.0);
    } else {
      str += " ph:" + F1.format(phase);
    }
    return str;
  }

  /**
   * The only thing this does is remove any CustomPattern
   * propertyChangeListener.
   */
  public void clear() {
    if (pattern instanceof CustomPattern) {
      ((CustomPattern) pattern).removePropertyChangeListener(this);
    }
  }

  /**
   * Get the pattern of the rosette.
   *
   * @return pattern of the rosette
   */
  public Pattern getPattern() {
    return pattern;
  }

  /**
   * Set the pattern for the rosette.
   *
   * This fires a PROP_PATTERN property change with the old and new patterns.
   *
   * @param p new pattern
   */
  public synchronized void setPattern(Pattern p) {
    if (pattern instanceof CustomPattern) {   // quit listening to the old pattern
      ((CustomPattern) pattern).removePropertyChangeListener(this);
    }
    Pattern old = this.pattern;
    this.pattern = p;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
      this.phase = 0.0;
    }
    if (pattern instanceof CustomPattern) {   // listen to customs for changes
      ((CustomPattern) pattern).addPropertyChangeListener(this);
    }
    this.pcs.firePropertyChange(PROP_PATTERN, old, pattern);
  }

  /**
   * Get the period of the PatternBar.
   *
   * @return period
   */
  public double getPeriod() {
    return period;
  }

  /**
   * Set the period of the PatternBar. This fires a PROP_PERIOD property change
   * with the old and new repeats.
   *
   * @param per new period
   */
  public synchronized void setPeriod(double per) {
    double old = period;
    this.period = per;
    this.pcs.firePropertyChange(PROP_PERIOD, old, period);
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
  public synchronized void setPToP(double p) {
    double old = this.pToP;
    this.pToP = p;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
    }
    this.pcs.firePropertyChange(PROP_PTOP, old, pToP);
  }

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
   * Phase is adjusted to be in the range of 0 to 360. This fires a PROP_PHASE
   * property change with the old and new phases.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public synchronized void setPhase(double ph) {
    double old = this.phase;
    this.phase = angleCheck(ph);
    if (pattern.getName().equals("NONE")) {
      this.phase = 0.0;
    }
    this.pcs.firePropertyChange(PROP_PHASE, old, phase);
  }

  /**
   * Set the phase of the rosette.
   *
   * Values of less than 0.0 will be set to 0.0 and values greater than 1.0 will
   * be set to 1.0. This fires a PROP_PHASE property change with the old and new
   * phases.
   *
   * @param ph phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public synchronized void setPh(double ph) {
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
   * Get the optional 2nd integer parameter.
   *
   * @return optional 2nd integer parameter
   */
  public int getN2() {
    return n2;
  }

  /**
   * Set the optional 2nd integer parameter.
   *
   * This fires a PROP_N2 property change with the old and new repeats.
   *
   * @param n optional 2nd integer parameter
   */
  public void setN2(int n) {
    int old = n2;
    this.n2 = n;
    this.pcs.firePropertyChange(PROP_N2, old, n2);
  }

  /**
   * Get the optional 2nd amplitude parameter.
   *
   * @return optional 2nd amplitude parameter
   */
  public double getAmp2() {
    return amp2;
  }

  /**
   * Set the optional 2nd amplitude parameter.
   *
   * This fires a PROP_AMP2 property change with the old and new values.
   *
   * @param a optional 2nd amplitude parameter
   */
  public synchronized void setAmp2(double a) {
    double old = this.amp2;
    this.amp2 = a;
    this.pcs.firePropertyChange(PROP_AMP2, old, amp2);
  }

  /**
   * Make sure angle is in range 0.0 <= a < 360.0
   *
   * @param ang angle in degrees
   * @return angle in range 0.0 <= a < 360.0
   */
  private double angleCheck(double ang) {
    while (ang < 0.0) {
      ang += 360.0;
    }
    while (ang >= 360.0) {
      ang -= 360.0;
    }
    return ang;
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

  /**
   * Get the amplitude (offset from baseline zero) of the PatternBar at a given
   * distance. A returned value of zero means zero deflection from its baseline
   * zero.
   *
   * @param dist distance
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public double getAmplitudeAt(double dist) {
    if (dist < 0.0) {
      return 0.0;
    }
    double offset = period * phase / 360.0;
    double fracX = fractional((dist + offset) / period);
    double d;
    if (pattern.needsOptions()) {
      d = pToP * pattern.getValue(fracX, DEFAULT_REPEAT, n2, amp2);
    } else if (pattern.needsRepeat()) {
      d = pToP * pattern.getValue(fracX, DEFAULT_REPEAT);
    } else {
      d = pToP * pattern.getValue(fracX);
    }
    if (invert) {
      d = pToP - d;
    }
    return d;
  }

  /**
   * Get the amplitude (offset from baseline zero) of the PatternBar at a given
   * distance. A returned value of zero means zero deflection from its baseline
   * zero.
   *
   * @param dist distance
   * @param inv invert the returned value (as if rubbing on the backside of the
   * PatternBar).
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public double getAmplitudeAt(double dist, boolean inv) {
    if (inv) {
      return pToP - getAmplitudeAt(dist);
    }
    return getAmplitudeAt(dist);
  }

  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    if (invert) {
      opt = opt + " invert='" + invert + "'";
    }
    if (pattern.needsN2()) {
      opt = opt + " n2='" + n2 + "'";
    }
    if (pattern.needsAmp2()) {
      opt = opt + " amp2='" + F4.format(amp2) + "'";
    }
    out.println(indent + "<PatternBar"
        + " pattern='" + pattern.getName() + "'"
        + " amp='" + F4.format(pToP) + "'"
        + " period='" + F4.format(period) + "'"
        + " phase='" + F1.format(phase) + "'"
        + opt
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PatternBar.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens for changes in CustomPattern
    // If this patternBar's pattern is deleted, use the default.
    if (evt.getPropertyName().equals(Patterns.PROP_DELETE)) {
      if (evt.getOldValue().equals(pattern.getName())) {
        setPattern(patternMgr.getDefaultPattern());	// deleted the patternBar's pattern --> use default.
      }
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

}
