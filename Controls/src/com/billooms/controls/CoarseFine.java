package com.billooms.controls;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.controls.Controls.PROP_PREFIX;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Object for holding information about depth and resolution of coarse and fine
 * passes of cuts.
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
public class CoarseFine extends CLclass {

  /** Property name used for changing the pass depth */
  public final static String PROP_PASSDEPTH = PROP_PREFIX + "PassDepth";
  /** Property name used for changing the last depth */
  public final static String PROP_LASTDEPTH = PROP_PREFIX + "LastDepth";
  /** Property name used for changing the pass step */
  public final static String PROP_PASSSTEP = PROP_PREFIX + "PassStep";
  /** Property name used for changing the last step */
  public final static String PROP_LASTSTEP = PROP_PREFIX + "LastStep";
  /** Property name used for changing the last direction */
  public final static String PROP_ROTATION = PROP_PREFIX + "Rotation";

  /** Default depth on rough passes. */
  public final static double DEFAULT_PASS_DEPTH = 0.020;
  /** Minimum pass depth. */
  public final static double MIN_PASS_DEPTH = 0.0;
  /** Default depth for final pass. */
  public final static double DEFAULT_LAST_DEPTH = 0.005;
  /** Minimum last depth. */
  public final static double MIN_LAST_DEPTH = 0.0;
  /** Default step size on rough passes. */
  public final static int DEFAULT_PASS_STEP = 5;
  /** Default step size on final pass. */
  public final static int DEFAULT_LAST_STEP = 1;
  /** Default spindle rotation. */
  public final static Rotation DEFAULT_ROTATION = Rotation.PLUS_ALWAYS;

  /** Rotation of spindle. */
  public enum Rotation {

    /** All passes positive. */
    PLUS_ALWAYS("Always + rotation"),
    /** All passes negative. */
    NEG_ALWAYS("Always - rotation"),
    /** Positive first, then negative on the last pass. */
    NEG_LAST("+ on rough, - on final");

    public final String text;

    Rotation(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  }

  /** Depth of cut on rough passes. */
  private double passDepth = DEFAULT_PASS_DEPTH;
  /** Depth of cut on final pass. */
  private double lastDepth = DEFAULT_LAST_DEPTH;
  /** Step size on rough passes. */
  private int passStep = DEFAULT_PASS_STEP;
  /** Step size on final pass. */
  private int lastStep = DEFAULT_LAST_STEP;
  /** Spindle rotation. */
  private Rotation rotation = DEFAULT_ROTATION;

  /**
   * Construct a new CoarseFine object with default values.
   */
  public CoarseFine() {
    // do nothing -- use defaults
  }

  /**
   * Construct a new CoarseFine object.
   *
   * @param element XML DOM Element
   */
  public CoarseFine(Element element) {
    this.passDepth = CLUtilities.getDouble(element, "passDepth", DEFAULT_PASS_DEPTH);
    this.passStep = CLUtilities.getInteger(element, "passStep", DEFAULT_PASS_STEP);
    this.lastDepth = CLUtilities.getDouble(element, "lastDepth", DEFAULT_LAST_DEPTH);
    this.lastStep = CLUtilities.getInteger(element, "lastStep", DEFAULT_LAST_STEP);
    this.rotation = CLUtilities.getEnum(element, "rotation", Rotation.class, DEFAULT_ROTATION);
  }

  @Override
  public String toString() {
    return "Course:" + F3.format(passDepth) + " Fine:" + F3.format(lastDepth);
  }

  /**
   * Get the depth of rough cut passes.
   *
   * @return depth of rough cut passes
   */
  public double getPassDepth() {
    return passDepth;
  }

  /**
   * Set the depth of rough cut passes.
   *
   * @param passDepth new depth
   */
  public void setPassDepth(double passDepth) {
    double old = this.passDepth;
    this.passDepth = Math.max(MIN_PASS_DEPTH, passDepth);
    pcs.firePropertyChange(PROP_PASSDEPTH, old, this.passDepth);
  }

  /**
   * Get the depth of last cut pass.
   *
   * @return depth of last cut pass
   */
  public double getLastDepth() {
    return lastDepth;
  }

  /**
   * Set the depth of last cut pass.
   *
   * @param lastDepth new depth
   */
  public void setLastDepth(double lastDepth) {
    double old = this.lastDepth;
    this.lastDepth = Math.max(MIN_LAST_DEPTH, lastDepth);
    pcs.firePropertyChange(PROP_LASTDEPTH, old, this.lastDepth);
  }

  /**
   * Get the number of micro-steps per movement of rough cut passes.
   *
   * @return number of micro-steps
   */
  public int getPassStep() {
    return passStep;
  }

  /**
   * Set the number of micro-steps per movement of rough cut passes.
   *
   * @param passStep new number of micro-steps
   */
  public void setPassStep(int passStep) {
    int old = this.passStep;
    this.passStep = passStep;
    pcs.firePropertyChange(PROP_PASSSTEP, old, passStep);
  }

  /**
   * Get the number of micro-steps per movement of last pass.
   *
   * @return number of micro-steps
   */
  public int getLastStep() {
    return lastStep;
  }

  /**
   * Set the number of micro-steps per movement of last pass.
   *
   * @param lastStep number of micro-steps
   */
  public void setLastStep(int lastStep) {
    int old = this.lastStep;
    this.lastStep = lastStep;
    pcs.firePropertyChange(PROP_LASTSTEP, old, lastStep);
  }

  /**
   * Get the direction of rotation during cuts.
   *
   * @return direction of rotation
   */
  public Rotation getRotation() {
    return rotation;
  }

  /**
   * Set the direction of rotation during cuts.
   *
   * @param rotation direction of rotation
   */
  public void setRotation(Rotation rotation) {
    Rotation old = this.rotation;
    this.rotation = rotation;
    pcs.firePropertyChange(PROP_ROTATION, old, rotation);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<CoarseFine "
        + " passDepth='" + F4.format(passDepth) + "'"
        + " passStep='" + passStep + "'"
        + " lastDepth='" + F4.format(lastDepth) + "'"
        + " lastStep='" + lastStep + "'"
        + " rotation='" + rotation.toString() + "'"
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("CoarseFine.propertyChange Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
