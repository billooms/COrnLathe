package com.billooms.rosette;

import com.billooms.clclass.CLUtilities;
import com.billooms.drawables.simple.Circle;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.simple.Plus;
import com.billooms.patterns.Patterns;
import static com.billooms.rosette.BasicRosette.SOLID_LINE;
import com.billooms.rosette.Combine.CombineType;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A Rose Engine rosette made from multiple simple rosettes. 
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
public class CompoundRosette extends BasicRosette {

  /** Property name used for changing a rosette. */
  public final static String PROP_ROSETTE = PROP_PREFIX + "Rosette";
  
  /** Default size (currently set to 3) */
  public final static int DEFAULT_SIZE = 3;
  
  /** An array of available rosettes to combine. */
  private final Rosette[] rosettes;
  /** An array of ways to combine adjacent rosettes. */
  private Combine[] combiners;
  /** Maximum deflection of the combination of the rosettes. */
  double maxDeflection = -1.0;
  
  /**
   * Construct a new CompoundRosette. 
   * 
   * @param patMgr Pattern manager
   */
  public CompoundRosette(Patterns patMgr) {
    this(patMgr, DEFAULT_SIZE);
    repeat = 1;       // repeat is always 1 for CompoundRosette
  }
  
  /**
   * Construct a new CompoundRosette. 
   * 
   * @param patMgr Pattern manager
   * @param size the number of rosettes to use
   */
  public CompoundRosette(Patterns patMgr, int size) {
    super(patMgr);    // assigns patternManager
    rosettes = new Rosette[size];
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i] = new Rosette(patMgr);
    }
    combiners = new Combine[size-1];
    for (int i = 0; i < combiners.length; i++) {
      combiners[i] = new Combine();
    }
    calculateMax();
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i].addPropertyChangeListener(this);
    }
    for (int i = 0; i < combiners.length; i++) {
      combiners[i].addPropertyChangeListener(this);
    }
  }
  
  /**
   * Construct a new CompoundRosette from the given CompoundRosette.
   * 
   * @param cRosette given CompoundRosette
   */
  public CompoundRosette(CompoundRosette cRosette) {
    super(cRosette);  // copies patternManager, pToP, repeat, phase, int
    repeat = 1;       // repeat is always 1 for CompoundRosette
    rosettes = new Rosette[cRosette.size()];    // copies of the rosettes
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i] = new Rosette(cRosette.getRosette(i));
    }
    combiners = new Combine[cRosette.size()-1]; // copies of the combiners
    for (int i = 0; i < combiners.length; i++) {
      combiners[i] = new Combine(cRosette.getCombine(i));
    }
    calculateMax();
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i].addPropertyChangeListener(this);
    }
    for (int i = 0; i < combiners.length; i++) {
      combiners[i].addPropertyChangeListener(this);
    }
  }

  /**
   * Define a new CompoundRosette from DOM Element.
   *
   * @param element DOM Element
   * @param patMgr pattern manager with available patterns
   */
  public CompoundRosette(Element element, Patterns patMgr) {
    super(element, patMgr);   // stores patternMgr, reads pToP, phase, invert
    repeat = 1;       // repeat is always 1 for CompoundRosette
    int size = CLUtilities.getInteger(element, "size", DEFAULT_SIZE);
    rosettes = new Rosette[size];
    NodeList rosNodes = element.getElementsByTagName("Rosette");
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i] = new Rosette((Element) rosNodes.item(i), patMgr);
    }
    combiners = new Combine[size - 1];
    NodeList cNodes = element.getElementsByTagName("Combine");
    for (int i = 0; i < rosettes.length - 1; i++) {
      combiners[i] = new Combine((Element) cNodes.item(i));
    }
    calculateMax();
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i].addPropertyChangeListener(this);
    }
    for (int i = 0; i < combiners.length; i++) {
      combiners[i].addPropertyChangeListener(this);
    }
  }

  @Override
  public String toString() {
    String str = "";
    for (int i = 0; i < rosettes.length; i++) {
      str += rosettes[i].getPattern().getName() + rosettes[i].getRepeat();
      if (i < (rosettes.length - 1)) {
        str += " " + combiners[i].getType().toString() + " ";
      }
    }
    return str;
  }

  /**
   * Set the number of repeats on the rosette. 
   * For a CompoundRosette this will always be one.
   *
   * @param n number of repeats
   */
  @Override
  public void setRepeat(int n) {
    repeat = 1;
  }
  
  /**
   * Get the number of rosettes being used.
   * 
   * @return number of rosettes
   */
  public int size() {
    return rosettes.length;
  }
  
  /**
   * Get the rosette with the given index.
   * 
   * @param idx index
   * @return rosette (or null if index is out of range)
   */
  public Rosette getRosette(int idx) {
    if ((idx >= rosettes.length) || (idx < 0)) {
      return null;
    }
    return rosettes[idx];
  }
  
  /**
   * Set a new rosette with the given index.
   * 
   * This fires a PROP_ROSETTE propertyChange with the old and new rosettes.
   * 
   * @param idx index
   * @param newRosette new rosette
   */
  public void setRosette(int idx, Rosette newRosette) {
    if ((idx < rosettes.length) && (idx >= 0)) {
      if (rosettes[idx] != null) {
        rosettes[idx].removePropertyChangeListener(this);
      }
      Rosette old = rosettes[idx];
      rosettes[idx] = newRosette;
      calculateMax();
      rosettes[idx].addPropertyChangeListener(this);
      this.pcs.firePropertyChange(PROP_ROSETTE, old, newRosette);
    }
  }
  
  /**
   * Get the Combine for the given index.
   * 
   * @param idx index
   * @return Combine
   */
  public Combine getCombine(int idx) {
    if ((idx >= combiners.length) || (idx < 0)) {
      return null;
    }
    return combiners[idx];
  }
  
  /**
   * Get the combiner type for the given index. 
   * 
   * @param idx index
   * @return combiner type
   */
  public CombineType getCombineType(int idx) {
    if ((idx >= combiners.length) || (idx < 0)) {
      return Combine.DEFAULT_COMBINE;
    }
    return combiners[idx].getType();
  }
  
  /**
   * Set the combiner type for the given index.
   * 
   * @param idx index
   * @param newCombine new Combine type
   */
  public void setCombineType(int idx, CombineType newCombine) {
    if ((idx < combiners.length) && (idx >= 0)) {
      combiners[idx].setType(newCombine);   // fires a propertyChange
    }
  }
  

  /**
   * The only thing this does is remove any CustomPattern
   * propertyChangeListener from the rosettes.
   */
  @Override
  public void clear() {
    for (Rosette rosette : rosettes) {
      rosette.clear();
    }
  }

  /**
   * Calculate the maximum deflection of the combinations of rosettes.
   *
   * @return maximum deflection of the combinations of rosettes
   */
  private double calculateMax() {
    // TODO: This is crude, but it's the best I can think of for now.
    maxDeflection = 0.0;
    for (int i = 0; i < NUM_POINTS; i++) {
      maxDeflection = Math.max(maxDeflection, deflectionAt((double) i));
    }
    return maxDeflection;
  }

  /**
   * Get the deflection of the combined rosettes at a given
   * angle in degrees. A returned value of zero means zero deflection from its
   * nominal radius.
   *
   * @param ang Angle in degrees around the rosette
   * @return amplitude which will be a positive number from 0.0 to pToP
   */
  public double deflectionAt(double ang) {
    double[] deflections = new double[rosettes.length];
    for (int i = 0; i < deflections.length; i++) {
      deflections[i] = rosettes[i].getAmplitudeAt(ang);
    }
    double deflection = deflections[0];
    for (int i = 0; i < combiners.length; i++) {
      deflection = combiners[i].getType().combine(deflection, deflections[i+1]);
    }
    return deflection;
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
      return getPToP() - getAmplitudeAt(ang);
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
    return deflectionAt(ang) / maxDeflection * pToP;
  }
  
  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<CompoundRosette"
        + " amp='" + F3.format(pToP) + "'"
        + " phase='" + F1.format(phase) + "'"
        + " size='" + size() + "'"
        + ">");
    indentMore();
    for (int i = 0; i < rosettes.length; i++) {
      rosettes[i].writeXML(out);
    }
    for (int i = 0; i < combiners.length; i++) {
      combiners[i].writeXML(out);
    }
    indentLess();
    out.println(indent + "</CompoundRosette>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CompoundRosette.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
      
    // listening to Rosette and Combine
    calculateMax();
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

  }

  /**
   * Paint the object at the combined deflections, not scaled by pToP.
   * This is mainly used in the RosetteBuilder.
   *
   * @param g2d Graphics2D
   * @param nomRadius Nominal radius of the drawn rosette
   */
  public void paint2(Graphics2D g2d, double nomRadius) {
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
      r = nomRadius - deflectionAt((double) i);
      pts[i] = new Point2D.Double(r * Math.cos(rad), r * Math.sin(rad));
    }
    drawList.add(new Curve(pts, OUTLINE_COLOR, SOLID_LINE));
  }

}
