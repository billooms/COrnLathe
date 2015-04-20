package com.billooms.controls;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.controls.Controls.PROP_PREFIX;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Object containing control information for cutting threads.
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
public class Threads extends CLclass {

  /** Property name used for changing the TPI. */
  public final static String PROP_TPI = PROP_PREFIX + "TPI";
  /** Property name used for changing the starts. */
  public final static String PROP_STARTS = PROP_PREFIX + "Starts";
  /** Property name used for changing the percent. */
  public final static String PROP_PERCENT = PROP_PREFIX + "Percent";

  /** Default TPI. */
  public final static int DEFAULT_TPI = 20;
  /** Default thread starts. */
  public final static int DEFAULT_STARTS = 1;
  /** Default percent thread engagement. */
  public final static int DEFAULT_PERCENT = 60;

  /** Threads per inch. */
  private int tpi = DEFAULT_TPI;
  /** Number of starts. */
  private int starts = DEFAULT_STARTS;
  /** Percent thread engagement. */
  private int percent = DEFAULT_PERCENT;

  /**
   * Construct a new Threads object with default values.
   */
  public Threads() {
    // do nothing -- use defaults
  }

  /**
   * Construct a new Threads object.
   *
   * @param element XML DOM Element
   */
  public Threads(Element element) {
    this.tpi = CLUtilities.getInteger(element, "tpi", DEFAULT_TPI);
    this.starts = CLUtilities.getInteger(element, "starts", DEFAULT_STARTS);
    this.percent = CLUtilities.getInteger(element, "percent", DEFAULT_PERCENT);
  }

  @Override
  public String toString() {
    return "Threads:" + tpi + " " + starts + "start";
  }

  /**
   * Get the number of threads per inch.
   *
   * @return threads per inch
   */
  public int getTpi() {
    return tpi;
  }

  /**
   * Set the number of threads per inch.
   *
   * @param tpi threads per inch
   */
  public void setTpi(int tpi) {
    int old = this.tpi;
    this.tpi = tpi;
    pcs.firePropertyChange(PROP_TPI, old, tpi);
  }

  /**
   * Get the number of starts.
   *
   * @return number of starts
   */
  public int getStarts() {
    return starts;
  }

  /**
   * Set the number of starts.
   *
   * @param starts number of starts
   */
  public void setStarts(int starts) {
    int old = this.starts;
    this.starts = starts;
    pcs.firePropertyChange(PROP_STARTS, old, starts);
  }

  /**
   * Get the percent thread engagement.
   *
   * @return percent
   */
  public int getPercent() {
    return percent;
  }

  /**
   * Set the percent thread engagement.
   *
   * @param percent percent
   */
  public void setPercent(int percent) {
    int old = this.percent;
    this.percent = percent;
    pcs.firePropertyChange(PROP_PERCENT, old, percent);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<Threads"
        + " tpi='" + tpi + "'"
        + " starts='" + starts + "'"
        + " percent='" + percent + "'"
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("Threads.propertyChange Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
