package com.billooms.rosette;

import com.billooms.clclass.CLUtilities;
import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 * A simple Rose Engine rosette.
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
public class Rosette extends BasicRosette {

  /** Property name used for changing the pattern */
  public final static String PROP_PATTERN = PROP_PREFIX + "Pattern";
  /** Property name used for changing the mask */
  public final static String PROP_MASK = PROP_PREFIX + "Mask";
  /** Property name used for changing the maskHiLo flag */
  public final static String PROP_HILO = PROP_PREFIX + "HiLo";
  /** Property name used for changing the mask phase */
  public final static String PROP_MASKPHASE = PROP_PREFIX + "MaskPhase";
  /** Property name used for changing the optional 2nd integer parameter */
  public final static String PROP_N2 = PROP_PREFIX + "N2";
  /** Property name used for changing the optional 2nd amplitude */
  public final static String PROP_AMP2 = PROP_PREFIX + "Amp2";
  /** Property name used for changing the symmetry amplitudes. */
  public final static String PROP_SYMAMP = PROP_PREFIX + "SymmetryAmp";
  /** Property name used for changing the symmetry widths. */
  public final static String PROP_SYMWID = PROP_PREFIX + "SymmetryWid";

  /** Default Style Name (currently set to "SINE") */
  public final static String DEFAULT_PATTERN = "SINE";
  /** Default mask (currently set to "") */
  public final static String DEFAULT_MASK = "";
  /** Default mask phase (currently set to 0.0) */
  public final static double DEFAULT_MASK_PHASE = 0.0;
  /** Default for optional second integer parameter */
  public final static int DEFAULT_N2 = 3;
  /** Default for optional second amplitude parameter */
  public final static double DEFAULT_AMP2 = 0.1;
  /** Default value for optional symmetry amplitudes. */
  public final static String DEFAULT_SYMAMP = "";
  /** Default value for optional symmetry widths. */
  public final static String DEFAULT_SYMWID = "";

  /** Two different ways to mask */
  public static enum Mask {
    /** Mask the rosette to the highest point */
    HIGH,
    /** Mask the rosette to the lowest point */
    LOW
  }

  /** Pattern for the rosette. */
  private Pattern pattern;
  /** Mask some of the pattern repeats -- 0 is skip, 1 is don't skip. */
  private String mask = DEFAULT_MASK;
  /** Flag if mask is to a high point or low point of the rosette. */
  private Mask maskHiLo = Mask.HIGH;
  /** Mask phase shift in degrees (0 to 360). */
  private double maskPhase = DEFAULT_MASK_PHASE;
  /** Optional 2nd integer parameter. */
  private int n2 = DEFAULT_N2;
  /** Optional 2nd amplitude parameter. */
  private double amp2 = DEFAULT_AMP2;
  /** Optional amplitude symmetry parameters. */
  private DoubleArray symmetryAmp = new DoubleArray(DEFAULT_SYMAMP);
  /** Optional width symmetry parameters. */
  private DoubleArray symmetryWid = new DoubleArray(DEFAULT_SYMWID);

  private static COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  /**
   * Construct a rosette with default values.
   *
   * @param patMgr pattern manager with available patterns
   */
  public Rosette(Patterns patMgr) {
    super(patMgr);    // assigns patternManager
    this.pattern = patternMgr.getDefaultPattern();
  }

  /**
   * Construct a new rosette with the same values as the given rosette.
   *
   * @param rosette Rosette
   */
  public Rosette(Rosette rosette) {
    super(rosette);   // copies patternManager, pToP, repeat, phase, int
    this.pattern = patternMgr.getPattern(rosette.pattern.getName());
    this.mask = rosette.getMask();
    this.maskHiLo = rosette.getMaskHiLo();
    this.maskPhase = rosette.getMaskPhase();
    this.amp2 = rosette.getAmp2();
    this.n2 = rosette.getN2();
    this.symmetryAmp = new DoubleArray(rosette.getSymmetryAmp());
    this.symmetryWid = new DoubleArray(rosette.getSymmetryWid());
  }

  /**
   * Define a new RosettePoint from DOM Element.
   *
   * @param element DOM Element
   * @param patMgr pattern manager with available patterns
   */
  public Rosette(Element element, Patterns patMgr) {
    super(element, patMgr);   // stores patternMgr, reads pToP, phase, invert
    this.pattern = patternMgr.getPattern(CLUtilities.getString(element, "pattern", DEFAULT_PATTERN));
    this.repeat = CLUtilities.getInteger(element, "repeat", DEFAULT_REPEAT);
    mask = CLUtilities.getString(element, "mask", DEFAULT_MASK);
    maskHiLo = CLUtilities.getEnum(element, "hilo", Mask.class, Mask.HIGH);
    maskPhase = CLUtilities.getDouble(element, "maskPhase", DEFAULT_MASK_PHASE);
    n2 = CLUtilities.getInteger(element, "n2", DEFAULT_N2);
    amp2 = CLUtilities.getDouble(element, "amp2", DEFAULT_AMP2);
    symmetryAmp.setData(CLUtilities.getString(element, "symmetryAmp", DEFAULT_SYMAMP));
    symmetryWid.setData(CLUtilities.getString(element, "symmetryWid", DEFAULT_SYMWID));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Rosette.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens for changes in CustomPattern
    // If this rosette's pattern is deleted, use the default.
    if (evt.getPropertyName().equals(Patterns.PROP_DELETE)) {
      if (evt.getOldValue().equals(pattern.getName())) {
        setPattern(patternMgr.getDefaultPattern());	// deleted the rosettes pattern --> use default.
      }
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
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
    str += " " + repeat;
    str += " A:" + F3.format(pToP);
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
  @Override
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
   * @param p pattern
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
   * Set the number of repeats on the rosette.
   *
   * This fires a PROP_REPEAT property change with the old and new repeats.
   *
   * @param n number of repeats
   */
  @Override
  public synchronized void setRepeat(int n) {
    int old = repeat;
    this.repeat = Math.max(n, pattern.getMinRepeat());
    checkSymWid();    // in case repeat gets smaller than symmetryWid
    this.pcs.firePropertyChange(PROP_REPEAT, old, repeat);
  }

  /**
   * Set the peak-to-peak amplitude of the rosette.
   *
   * This fires a PROP_PTOP property change with the old and new amplitudes.
   *
   * @param p peak-to-peak amplitude
   */
  @Override
  public synchronized void setPToP(double p) {
    double old = this.pToP;
    this.pToP = p;
    if (pattern.getName().equals("NONE")) {
      this.pToP = 0.0;
    }
    this.pcs.firePropertyChange(PROP_PTOP, old, pToP);
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
  @Override
  public synchronized void setPhase(double ph) {
    double old = this.phase;
    this.phase = ph;
    if (pattern.getName().equals("NONE")) {
      this.phase = 0.0;
    }
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
  @Override
  public synchronized void setPh(double ph) {
    setPhase(ph * 360.0);
  }

  /**
   * Get the mask for this rosette.
   *
   * A blank string means all repeat positions are used. A 0 means skip this
   * position, a 1 means use this position. The string is read from left to
   * right, and is re-used as often as needed until the full number of repeats
   * is covered.
   *
   * @return mask
   */
  public String getMask() {
    return mask;
  }

  /**
   * Set the mask for this rosette.
   *
   * A blank string means all repeat positions are used. A 0 means skip this
   * position, a 1 means use this position. The string is read from left to
   * right, and is re-used as often as needed until the full number of repeats
   * is covered. Any other character besides "0" and "1" is interpreted as "1".
   * This fires a PROP_MASK property change with the old and new values.
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
    pcs.firePropertyChange(PROP_MASK, old, mask);
  }

  /**
   * Get the HiLo flag for masking.
   *
   * @return HiLo flag
   */
  public Mask getMaskHiLo() {
    return maskHiLo;
  }

  /**
   * Set the HiLo flag for masking.
   *
   * This fires a PROP_HILO property change with the old and new values.
   *
   * @param hiLo new HiLo flag
   */
  public void setMaskHiLo(Mask hiLo) {
    Mask old = this.maskHiLo;
    this.maskHiLo = hiLo;
    pcs.firePropertyChange(PROP_HILO, old.toString(), maskHiLo.toString());
  }

  /**
   * Get the mask phase of the rosette
   *
   * @return mask phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public double getMaskPhase() {
    return maskPhase;
  }

  /**
   * Get the mask phase of the rosette as a fraction of a repeat.
   *
   * @return phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public double getMaskPh() {
    return maskPhase / 360.0;
  }

  /**
   * Set the mask phase of the rosette.
   *
   * This fires a PROP_MASKPHASE property change with the old and new phases.
   *
   * @param ph phase in degrees: 180 means 1/2 of the repeat, 90 means 1/4 of
   * the repeat, etc.
   */
  public void setMaskPhase(double ph) {
    double old = this.maskPhase;
    this.maskPhase = angleCheck(ph);
    if (pattern.getName().equals("NONE")) {
      this.maskPhase = 0.0;
    }
    this.pcs.firePropertyChange(PROP_MASKPHASE, old, maskPhase);
  }

  /**
   * Set the mask phase of the rosette.
   *
   * Values of less than 0.0 will be set to 0.0 and values greater than 1.0 will
   * be set to 1.0. This fires a PROP_MASKPHASE property change with the old and
   * new phases.
   *
   * @param ph phase: 0.5 means 1/2 of the repeat, 0.25 means 1/4 of the repeat,
   * etc.
   */
  public synchronized void setMaskPh(double ph) {
    setMaskPhase(ph * 360.0);
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
   * Determine if this rosette uses symmetryAmp.
   * 
   * @return true = yes it does
   */
  public boolean usesSymmetryAmp() {
    return (symmetryAmp != null) && (symmetryAmp.size() > 0);
  }
  
  /**
   * Get the optional symmetry amplitudes.
   * 
   * @return Optional symmetry amplitudes
   */
  public DoubleArray getSymmetryAmp() {
    return symmetryAmp;
  }
  
  /**
   * Get the optional symmetry amplitudes as a String.
   * 
   * @return Optional symmetry amplitudes as a String
   */
  public String getSymAmpStr() {
    return symmetryAmp.toString();
  }
  
  /**
   * Set the optional symmetry amplitudes.
   *
   * This fires a PROP_SYMAMP property change with the old and new values.
   * 
   * @param newStr string with new values comma delimited
   */
  public void setSymAmpStr(String newStr) {
    String old = this.symmetryAmp.toString();
    this.symmetryAmp.setData(newStr);
    this.pcs.firePropertyChange(PROP_SYMAMP, old, symmetryAmp.toString());
  }
  
  /**
   * Determine if this rosette uses symmetryWid.
   * 
   * @return true = yes it does
   */
  public boolean usesSymmetryWid() {
    return (symmetryWid != null) && (symmetryWid.size() > 0);
  }
  
  /**
   * Get the optional symmetry widths.
   * 
   * @return Optional symmetry widths
   */
  public DoubleArray getSymmetryWid() {
    return symmetryWid;
  }
  
  /**
   * Get the optional symmetry widths as a String.
   * 
   * @return Optional symmetry widths as a String
   */
  public String getSymWidStr() {
    return symmetryWid.toString();
  }
  
  /**
   * Set the optional symmetry widths.
   *
   * This fires a PROP_SYMWID property change with the old and new values.
   * 
   * @param newStr string with new values comma delimited
   */
  public void setSymWidStr(String newStr) {
    String old = this.symmetryWid.toString();
    this.symmetryWid.setData(newStr);
    checkSymWid();
    this.pcs.firePropertyChange(PROP_SYMWID, old, symmetryWid.toString());
  }
  
  private void checkSymWid() {
    if (!usesSymmetryWid()) {
      return;
    }
    if (symmetryWid.size() > repeat) {    // trim off any extra values
      double[] newVals = new double[repeat];
      System.arraycopy(symmetryWid.getData(), 0, newVals, 0, repeat);
      symmetryWid.setData(newVals);
    }
    if (symmetryWid.size() == repeat) {
      double n = 0.0;
      for (int i = 0; i < symmetryWid.size() - 1; i++) {
        n += symmetryWid.getData()[i];      // add up all but the last value
      }
      symmetryWid.getData()[symmetryWid.size() - 1] = repeat - n;   // make sure last value is correct
    }
  }

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
  @Override
  public double getAmplitudeAt(double ang, boolean inv) {
    if (inv) {
      return pToP - getAmplitudeAt(ang);
    }
    return getAmplitudeAt(ang);
  }

  /**
   * Get the amplitude (offset from nominal radius) of the rosette at a given
   * angle in degrees. A returned value of zero means zero deflection from its
   * nominal radius.
   *
   * @param ang Angle in degrees around the rosette
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  @Override
  public double getAmplitudeAt(double ang) {
    if (isMasked(ang)) {
      switch (maskHiLo) {
        case HIGH:
          return 0.0;
        case LOW:
          return pToP;
      }
    }
    int m = 0;        // which repeat is the pattern in (0 to repeat)
    double deltaR;    // rosette deflection from nominal radius
    double patternFraction;   // fraction into the pattern
    double anglePerRepeat = 360.0 / repeat;             // degrees per every repeat of pattern
    double phaseAdjustedAngle = angleCheck(ang + phase / repeat);	// angle relative to the start of first pattern (based on symmetrical rosette)
    if (!usesSymmetryWid()) {   // symmetrical widths
      m = (int) (phaseAdjustedAngle / anglePerRepeat);          // which repeat is the pattern in (0 to repeat)
      double patternAngle = phaseAdjustedAngle - m * anglePerRepeat;	// degrees into the pattern
      patternFraction = patternAngle / anglePerRepeat;   // fraction into the pattern
    } else {    // not symmetrical widths
      // TODO: is there a way to calculate just once?
      // factors are the amount each repeat is stretched (>1.0) or shrunk (<1.0)
      double[] factors = getFactors();
      // angleBreaks are the angles where each repeat starts/ends
      double[] angleBreaks = getAngleBreaks(factors);
      for (int i = angleBreaks.length - 1; i >= 0; i--) {   // find which repeat the angle is in
        if (phaseAdjustedAngle >= angleBreaks[i]) {
          m = i;
          break;
        }
      }
      double patternAngle = phaseAdjustedAngle - angleBreaks[m];                   // degrees into the pattern
      patternFraction = patternAngle / anglePerRepeat / factors[m];   // fraction into the pattern
    }
    deltaR = pToP * getPatternValue(patternFraction);
    if (usesSymmetryAmp()) {    // are there amplitude variations?
      deltaR = symmetryAmp.getData()[m % symmetryAmp.size()] * deltaR;    // multiply by the symmetry amplitude
      if (getPatternValue(0.0) >= 0.99) {    // just in case it's not exacly 1.0
        // for patterns that start at 1.0, offset so that repeats match up
        deltaR = deltaR + pToP * (1.0 - symmetryAmp.getData()[m % symmetryAmp.size()]);
      }
    }
    if (invert) {
      return pToP - deltaR;
    }
    return deltaR;
  }
  
  /**
   * Factors are the amount each repeat is stretched (> 1) or shrunk (< 1).
   * 
   * @return array of factors
   */
  private double[] getFactors() {
    double[] factors = new double[repeat + 1];  // the extra is for wrap-around
    double sum = 0.0;
    for (int i = 0; i < repeat - 1; i++) {
      factors[i] = symmetryWid.getData()[i % symmetryWid.size()];   // keep repeating symmetryWid to fill out the factors
      sum += factors[i];
    }
    factors[repeat - 1] = repeat - sum; // last one has to add up to repeat
    factors[repeat] = factors[0];     // repeat for wrap around
    return factors;
  }
  
  /**
   * AngleBreaks are the angles where each repeat starts/ends.
   * 
   * @param factors stretch factors
   * @return array of angles
   */
  private double[] getAngleBreaks(double[] factors) {
    double[] angleBreaks = new double[repeat + 1];  // the extra is for wrap-around
    angleBreaks[0] = 0.0;
    for (int i = 1; i < angleBreaks.length; i++) {
      angleBreaks[i] = angleBreaks[i - 1] + 360.0 / repeat * factors[i - 1];
    }
    return angleBreaks;
  }
  
  /**
   * AngleBreaks are the angles where each repeat starts/ends.
   * 
   * @return array of angles
   */
  public double[] getAngleBreaks() {
    return getAngleBreaks(getFactors());
  }
  
  /**
   * Get a normalized value (in the range of 0 to 1) for the given normalized
   * input (also in the range of 0 to 1).
   * 
   * Use this rather than directly calling pattern.getValue so that we call the 
   * correct method depending on if the pattern requires the repeat or other optional 
   * parameters. 
   *
   * @param n input value (in the range of 0.0 to 1.0)
   * @return normalized pattern value (in the range 0.0 to 1.0)
   */
  private double getPatternValue(double n) {
    if (pattern.needsOptions()) {
      return pattern.getValue(n, repeat, n2, amp2);
    } else if (pattern.needsRepeat()) {
      return pattern.getValue(n, repeat);
    } else {
      return pattern.getValue(n);
    }
  }

  /**
   * Determine if the rosette is masked at the given angle.
   *
   * @param ang Angle in degrees around the rosette
   * @return true=masked, false=not masked
   */
  private boolean isMasked(double ang) {      // TODO: Masking doesn't work right with symmetryWid
    if (mask.isEmpty()) {		// nothing is masked
      return false;
    }
    String fullMask = mask;
    while (fullMask.length() <= getRepeat()) {	// note: "<=" because m can equal repeat!				
      fullMask += mask;			// fill out to full length
    }
    
    int m = 0;        // which repeat is the pattern in (0 to repeat)
    double anglePerRepeat = 360.0 / repeat;	// degrees per every repeat of pattern
    double phaseAdjustedAngle = angleCheck(ang + phase / repeat - maskPhase / repeat);	// angle relative to the start of first pattern (based on symmetrical rosette)
    if (!usesSymmetryWid()) {   // symmetrical widths
      m = (int) (phaseAdjustedAngle / anglePerRepeat);          // which repeat is the pattern in (0 to repeat)
    } else {    // not symmetrical widths
      // TODO: is there a way to calculate just once?
      // factors are the amount each repeat is stretched (>1.0) or shrunk (<1.0)
      double[] factors = getFactors();
      // angleBreaks are the angles where each repeat starts/ends
      double[] angleBreaks = getAngleBreaks(factors);
      for (int i = angleBreaks.length - 1; i >= 0; i--) {   // find which repeat the angle is in
        if (phaseAdjustedAngle >= angleBreaks[i]) {
          m = i;
          break;
        }
      }
    }

    return (fullMask.charAt(m) == '0');
  }

  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    if (invert) {
      opt = opt + " invert='" + invert + "'";
    }
    if (!mask.isEmpty()) {
      opt = opt + " mask='" + mask + "'"
          + " hilo='" + maskHiLo.toString() + "'"
          + " maskPhase='" + F1.format(maskPhase) + "'";
    }
    if (pattern.needsN2()) {
      opt = opt + " n2='" + n2 + "'";
    }
    if (pattern.needsAmp2()) {
      opt = opt + " amp2='" + F4.format(amp2) + "'";
    }
    if (usesSymmetryAmp()) {
      opt = opt + " symmetryAmp='" + symmetryAmp.toString() + "'";
    }
    if (usesSymmetryWid()) {
      opt = opt + " symmetryWid='" + symmetryWid.toString() + "'";
    }
    out.println(indent + "<Rosette"
        + " pattern='" + pattern.getName() + "'"
        + " repeat='" + repeat + "'"
        + " amp='" + F3.format(pToP) + "'"
        + " phase='" + F1.format(phase) + "'"
        + opt
        + "/>");
  }
}
