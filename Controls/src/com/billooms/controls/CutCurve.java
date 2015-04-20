package com.billooms.controls;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.controls.Controls.PROP_PREFIX;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Object for holding information about cuts following the contour of the shape.
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
public class CutCurve extends CLclass {

  /** Property name used for changing the step size. */
  public final static String PROP_STEP = PROP_PREFIX + "Step";
  /** Property name used for changing the direction. */
  public final static String PROP_DIRECTION = PROP_PREFIX + "Direction";
  /** Property name used for changing counts. */
  public final static String PROP_COUNT = PROP_PREFIX + "Count";
  /** Property name used for changing back-off and depths. */
  public final static String PROP_DEPTH = PROP_PREFIX + "Depth";

  /** Default step size. */
  public final static double DEFAULT_STEP = 0.050;
  /** Maximum step size. */
  public final static double MAX_STEP = 0.100;
  /** Minimum step size. */
  public final static double MIN_STEP = 0.001;
  /** Default back-off. */
  public final static double DEFAULT_BACKOFF = 0.0;
  /** Minimum value for back-off. */
  public final static double MIN_BACKOFF = 0.0;
  /** Default direction. */
  public final static Direction DEFAULT_DIRECTION = Direction.LAST_TO_FIRST;
  /** Default number of coarse passes. */
  public final static int DEFAULT_COUNT1 = 1;
  /** Default number of fine passes. */
  public final static int DEFAULT_COUNT2 = 1;
  /** Minimum count value. */
  public final static int MIN_COUNT = 0;
  /** Maximum count value. */
  public final static int MAX_COUNT = 10;
  /** Default depth per coarse pass. */
  public final static double DEFAULT_DEPTH1 = 0.020;
  /** Default depth per fine pass. */
  public final static double DEFAULT_DEPTH2 = 0.005;
  /** Minimum depth. */
  public final static double MIN_DEPTH = 0.000;
  /** Maximum depth. */
  public final static double MAX_DEPTH = 0.100;

  /** Direction of cutting. */
  public enum Direction {

    /** Last (top) to first (bottom). */
    LAST_TO_FIRST("Last to First"),
    /** First (bottom) to last (top). */
    FIRST_TO_LAST("First to Last");

    private final String text;

    private Direction(String s) {
      this.text = s;
    }
    
    public String getText() {
      return text;
    }
  }

  /** Step size. */
  private double step = DEFAULT_STEP;
  /** Back-off distance from the shape outline. */
  private double backoff = DEFAULT_BACKOFF;
  /** Direction of cutting. */
  private Direction direction = DEFAULT_DIRECTION;
  /** Number of rough passes. */
  private int count1 = DEFAULT_COUNT1;
  /** Depth of cut on each rough pass. */
  private double depth1 = DEFAULT_DEPTH1;
  /** Number of fine passes. */
  private int count2 = DEFAULT_COUNT2;
  /** Depth of cut on each fine pass. */
  private double depth2 = DEFAULT_DEPTH2;

  /**
   * Construct a new CutCurve object with default values.
   */
  public CutCurve() {
    // do nothing -- use defaults
  }

  /**
   * Construct a new CutCurve object.
   *
   * @param element XML DOM Element
   */
  public CutCurve(Element element) {
    this.step = CLUtilities.getDouble(element, "step", DEFAULT_STEP);
    this.backoff = CLUtilities.getDouble(element, "backoff", DEFAULT_BACKOFF);
    this.direction = CLUtilities.getEnum(element, "direction", Direction.class, DEFAULT_DIRECTION);
    this.count1 = CLUtilities.getInteger(element, "count1", DEFAULT_COUNT1);
    this.depth1 = CLUtilities.getDouble(element, "depth1", DEFAULT_DEPTH1);
    this.count2 = CLUtilities.getInteger(element, "count2", DEFAULT_COUNT2);
    this.depth2 = CLUtilities.getDouble(element, "depth2", DEFAULT_DEPTH2);
  }

  @Override
  public String toString() {
    return "CutCurve:" + F3.format(step) + " BackOff:" + F3.format(backoff) + " d=" + F3.format(count1*depth1 + count2*depth2);
  }

  /**
   * Get the step size.
   *
   * @return step size
   */
  public double getStep() {
    return step;
  }

  /**
   * Set the step size.
   *
   * @param step step size
   */
  public void setStep(double step) {
    double old = this.step;
    this.step = Math.min(Math.max(MIN_STEP, step), MAX_STEP);
    pcs.firePropertyChange(PROP_STEP, old, this.step);
  }

  /**
   * Get the distance to back off of the shape when beginning cuts.
   *
   * @return back-off distance
   */
  public double getBackoff() {
    return backoff;
  }

  /**
   * Set the distance to back off of the shape when beginning cuts.
   *
   * @param backoff back-off distance
   */
  public void setBackoff(double backoff) {
    double old = this.backoff;
    this.backoff = Math.max(MIN_BACKOFF, backoff);
    pcs.firePropertyChange(PROP_DEPTH, old, this.backoff);
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
   * Set the direction of the cut.
   *
   * @param direction direction of the cut
   */
  public void setDirection(Direction direction) {
    Direction old = this.direction;
    this.direction = direction;
    pcs.firePropertyChange(PROP_DIRECTION, old, direction);
  }

  /**
   * Get the number of coarse passes to make.
   *
   * @return number of coarse passes
   */
  public int getCount1() {
    return count1;
  }

  /**
   * Set the number of coarse passes to make.
   *
   * @param count1 number of coarse passes
   */
  public void setCount1(int count1) {
    int old = this.count1;
    this.count1 = Math.min(Math.max(MIN_COUNT, count1), MAX_COUNT);
    pcs.firePropertyChange(PROP_COUNT, old, this.count1);
  }

  /**
   * Get the depth of each coarse pass.
   *
   * @return depth of each coarse pass
   */
  public double getDepth1() {
    return depth1;
  }

  /**
   * Set the depth of each coarse pass.
   *
   * @param depth1 depth of each coarse pass
   */
  public void setDepth1(double depth1) {
    double old = this.depth1;
    this.depth1 = Math.min(Math.max(MIN_DEPTH, depth1), MAX_DEPTH);
    pcs.firePropertyChange(PROP_DEPTH, old, this.depth1);
  }

  /**
   * Get the number of fine passes to make.
   *
   * @return number of fine passes
   */
  public int getCount2() {
    return count2;
  }

  /**
   * Set the number of fine passes to make.
   *
   * @param count2 number of fine passes
   */
  public void setCount2(int count2) {
    int old = this.count2;
    this.count2 = Math.min(Math.max(MIN_COUNT, count2), MAX_COUNT);
    pcs.firePropertyChange(PROP_COUNT, old, this.count2);
  }

  /**
   * Get the depth of each fine pass.
   *
   * @return depth of each fine pass
   */
  public double getDepth2() {
    return depth2;
  }

  /**
   * Set the depth of each fine pass.
   *
   * @param depth2 depth of each fine pass
   */
  public void setDepth2(double depth2) {
    double old = this.depth2;
    this.depth2 = Math.min(Math.max(MIN_DEPTH, depth2), MAX_DEPTH);
    pcs.firePropertyChange(PROP_DEPTH, old, this.depth2);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<CutCurve"
        + " step='" + F4.format(step) + "'"
        + " backoff='" + F4.format(backoff) + "'"
        + " direction='" + direction.toString() + "'"
        + " count1='" + count1 + "'"
        + " depth1='" + F4.format(depth1) + "'"
        + " count2='" + count2 + "'"
        + " depth2='" + F4.format(depth2) + "'"
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("CutCurve.propertyChange Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
