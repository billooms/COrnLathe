package com.billooms.rosette;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Class defining ways of combining simple rosettes. 
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
public class Combine extends CLclass {
  
  /** All Combine property change names start with this prefix */
  public final static String PROP_PREFIX = "Combine" + "_";
  /** Property name used for changing the type */
  public final static String PROP_TYPE = PROP_PREFIX + "Type";
  
  /** Default combiner type. */
  public final static CombineType DEFAULT_COMBINE = CombineType.NONE;

  /** Different ways to combine rosettes. */
  public static enum CombineType {
    /** Use the first one and ignore the next one. */
    NONE,
    /** Use the Minimum of the two at any given point. */
    MIN,
    /** Use the Maximum of the two at any given point. */
    MAX, 
    /** Add the two at any given point. */
    ADD, 
    /** Subtract the two at any given point. */
    SUB;
    
    /**
     * Perform the math on the two given values based on the type of combiner.
     * 
     * @param n1 first value
     * @param n2 second value
     * @return result
     */
    public double combine(double n1, double n2) {
      switch (CombineType.this) {
        default:
        case NONE:
          return n1;
        case MIN:
          return Math.min(n1, n2);
        case MAX:
          return Math.max(n1, n2);
        case ADD:
          return n1+n2;
        case SUB:
          return n1-n2;
      }
    }
  }
  
  /** The type of combiner. */
  private CombineType combine = DEFAULT_COMBINE;

  /**
   * Construct a new Combine with default values. 
   */
  public Combine() {
  }
  
  /**
   * Construct a new Combine which is the same as the given Combine.
   * 
   * @param c Combine to duplicate
   */
  public Combine(Combine c) {
    this.combine = c.getType();
  }
  
  /**
   * Construct a new Combine from the given DOM element.
   * 
   * @param element DOM Element
   */
  public Combine(Element element) {
    combine = CLUtilities.getEnum(element, "type", CombineType.class, CombineType.NONE);
  }

  @Override
  public String toString() {
    return combine.toString();
  }
  
  /**
   * Get the type of this Combine.
   * 
   * @return CombineType
   */
  public CombineType getType() {
    return combine;
  }
  
  /** 
   * Set the type of this Combine.
   * 
   * This fires a PROP_TYPE propertyChange with the old and new values.
   * 
   * @param newType new type
   */
  public void setType(CombineType newType) {
    CombineType old = this.combine;
    this.combine = newType;
    this.pcs.firePropertyChange(PROP_TYPE, old, newType);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<Combine"
        + " type='" + combine.toString() + "'"
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("Not supported yet."); // doesn't listen to anything
  }

}
