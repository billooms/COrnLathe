package com.billooms.controls;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.controls.Controls.PROP_PREFIX;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Information on feed rate used by all kinds of cuts.
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
public class FeedRate extends CLclass {

  /** Property name used when changing the RPM. */
  public final static String PROP_RPM = PROP_PREFIX + "RPM";
  /** Property name used when changing the velocity. */
  public final static String PROP_VELOCITY = PROP_PREFIX + "Velocity";

  /** Default RPM. */
  public final static double DEFAULT_RPM = 3.0;
  /** Maximum RPM. */
  public final static double MAX_RPM = 10.0;
  /** Minimum RPM. */
  public final static double MIN_RPM = 0.1;
  /** Default velocity. */
  public final static double DEFAULT_VELOCITY = 4.0;
  /** Maximum velocity. */
  public final static double MAX_VEL = 15.0;
  /** Minimum velocity. */
  public final static double MIN_VEL = 0.1;

  /** Maximum RPM to be used when cutting. */
  private double rpm = DEFAULT_RPM;
  /** Maximum velocity to be used when cutting. */
  private double velocity = DEFAULT_VELOCITY;

  /**
   * Construct a new FeedRate object with default values.
   */
  public FeedRate() {
    // do nothing -- use defaults
  }

  /**
   * Construct a new FeedRate object.
   *
   * @param element XML DOM Element
   */
  public FeedRate(Element element) {
    this.rpm = CLUtilities.getDouble(element, "rpm", DEFAULT_RPM);
    this.velocity = CLUtilities.getDouble(element, "velocity", DEFAULT_VELOCITY);
  }

  @Override
  public String toString() {
    return "FeedRate rpm:" + F1.format(rpm) + " vel:" + F1.format(velocity);
  }

  /**
   * Get the RPM.
   *
   * @return RPM
   */
  public double getRpm() {
    return rpm;
  }

  /**
   * Set the RPM.
   *
   * @param rpm new value of RPM
   */
  public void setRpm(double rpm) {
    double old = this.rpm;
    this.rpm = Math.min(Math.max(MIN_RPM, rpm), MAX_RPM);
    pcs.firePropertyChange(PROP_RPM, old, this.rpm);
  }

  /**
   * Get the velocity.
   *
   * @return velocity
   */
  public double getVelocity() {
    return velocity;
  }

  /**
   * Set the velocity.
   *
   * @param velocity new velocity
   */
  public void setVelocity(double velocity) {
    double old = this.velocity;
    this.velocity = velocity;
    this.velocity = Math.min(Math.max(MIN_VEL, velocity), MAX_VEL);
    pcs.firePropertyChange(PROP_VELOCITY, old, this.velocity);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<FeedRate"
        + " rpm='" + F2.format(rpm) + "'"
        + " velocity='" + F2.format(velocity) + "'"
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("FeedRate.propertyChange Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
