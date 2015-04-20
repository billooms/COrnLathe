package com.billooms.spirals;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.spirals.SpiralMgr.DEFAULT_SPIRAL;
import static com.billooms.spirals.SpiralStyle.DEFAULT_AMP;
import static com.billooms.spirals.SpiralStyle.DEFAULT_TWIST;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 * Object with information for specific spirals.
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
public class Spiral extends CLclass {

  /** All Spiral property change names start with this prefix. */
  public final static String PROP_PREFIX = "Spiral" + "_";
  /** Property name used for changing the style. */
  public final static String PROP_STYLE = PROP_PREFIX + "style";
  /** Property name used for changing the twist. */
  public final static String PROP_TWIST = PROP_PREFIX + "twist";
  /** Property name used for changing the optional amplitude. */
  public final static String PROP_AMP = PROP_PREFIX + "amp";

  private final static SpiralMgr spiralMgr = Lookup.getDefault().lookup(SpiralMgr.class);

  /** The style of spiral. */
  private SpiralStyle style;
  /** The total twist in degrees. */
  private double twist = DEFAULT_TWIST;
  /** Optional amplitude parameter. */
  private double amp = DEFAULT_AMP;

  /**
   * Construct a new Spiral.
   *
   * @param style Spiral style
   * @param twist total twist in degrees
   * @param amp optional amplitude parameter
   */
  public Spiral(SpiralStyle style, double twist, double amp) {
    this.style = style;
    this.twist = twist;
    this.amp = amp;
  }

  /**
   * Construct a new Spiral with default values.
   */
  public Spiral() {
    this(spiralMgr.getDefaultSpiral(), DEFAULT_TWIST, DEFAULT_AMP);
  }

  /**
   * Construct a new Spiral with data from the given spiral.
   *
   * @param sp spiral to copy
   */
  public Spiral(Spiral sp) {
    this(sp.getStyle(), sp.getTwist(), sp.getAmp());
  }

  /**
   * Construct a new Spiral from the given DOM element.
   *
   * @param element DOM element
   */
  public Spiral(Element element) {
    this(spiralMgr.getSpiral(CLUtilities.getString(element, "style", DEFAULT_SPIRAL)),
        CLUtilities.getDouble(element, "twist", DEFAULT_TWIST),
        CLUtilities.getDouble(element, "amp", DEFAULT_AMP));
  }

  @Override
  public String toString() {
    return style.getDisplayName() + " " + F2.format(twist) + " deg";
  }

  /**
   * Get the style of spiral.
   *
   * @return style of spiral
   */
  public SpiralStyle getStyle() {
    return style;
  }

  /**
   * Set the style of spiral
   *
   * @param newStyle new style
   */
  public void setStyle(SpiralStyle newStyle) {
    SpiralStyle old = this.style;
    this.style = newStyle;
    pcs.firePropertyChange(PROP_STYLE, old, newStyle);
  }

  /**
   * Get the total twist of the spiral in degrees.
   *
   * @return twist in degrees
   */
  public double getTwist() {
    return twist;
  }

  /**
   * Set the total twist of the spiral.
   *
   * @param deg total twist in degrees
   */
  public void setTwist(double deg) {
    double old = this.twist;
    this.twist = deg;
    pcs.firePropertyChange(PROP_TWIST, old, deg);
  }

  /**
   * Get the optional amplitude parameter.
   *
   * @return optional amplitude parameter
   */
  public double getAmp() {
    return amp;
  }

  /**
   * Set the optional amplitude parameter.
   *
   * @param newAmp new amplitude parameter
   */
  public void setAmp(double newAmp) {
    double old = this.amp;
    this.amp = newAmp;
    pcs.firePropertyChange(PROP_TWIST, old, newAmp);
  }

  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    if (style.needsAmplitude()) {
      opt = opt + " amp='" + F4.format(amp) + "'";
    }
    out.println(indent + "<Spiral"
        + " style='" + style.getName() + "'"
        + " twist='" + F2.format(twist) + "'"
        + opt
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("Spiral.propertyChange Not supported yet.");
  }

}
