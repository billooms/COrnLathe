package com.billooms.profiles;

import com.billooms.clclass.CLclass;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.PiecedLine;
import com.billooms.drawables.simple.PolyLine;
import com.billooms.drawables.PtDefinedLine;
import com.billooms.drawables.SquarePt;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;

/**
 * Basic profile that can be extended without having to implement all the
 * methods.
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
public abstract class BasicProfile extends CLclass implements Profile {

  /** The name of this profile (upper case letters only). */
  protected String name = "";
  /** The display name of this profile. */
  protected String displayName = "";
  /** A small icon-like image of the profile. */
  protected BufferedImage image = null;
  /** Indicates if the profile is a built-in profile (cannot be deleted). */
  protected boolean builtIn = true;
  /** A line used for display in the profile editor. */
  protected PtDefinedLine line = null;

  /** The color of the profile * */
  public final static Color IMAGE_COLOR = new Color(65, 75, 126);	// dark blue/gray
  /** Number of points in the plot */
  private final int NUM_PTS = 24;

  /**
   * Create a new profile with a style of the given display name.
   *
   * The name will be a modified version of the display name: Convert everything
   * to uppercase, delete anything after the first blank, and remove anything
   * that is not an uppercase.
   *
   * @param displayName display name of the new style
   */
  public BasicProfile(String displayName) {
    this.displayName = displayName;
    this.name = filterName(displayName);
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Paint the object
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (line == null) {		// for custom profiles, line is already initialized
      makeLine();
    }
    line.paint(g2d);
  }

  /**
   * Make the line (for built-in profiles). For custom profiles, the line is
   * already initialized.
   */
  private synchronized void makeLine() {
    line = new PiecedLine(IMAGE_COLOR, SOLID_LINE);
    double x, y;
    for (int i = -NUM_PTS; i <= NUM_PTS; i++) {
      x = (double) i / (double) NUM_PTS;
      y = profileAt(x, 1.0);		// normalize to 1.0
      if (y == -1.0) {
        y = 2.0;		// for plotting purposes in case of "IDEAL"
      }
      SquarePt point = new SquarePt(new Point2D.Double(x, y), IMAGE_COLOR);
      line.insertPtX(point);
    }
  }

  /**
   * Get the name of this profile.
   *
   * @return name of this profile
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Get the display name of the style.
   *
   * @return display name
   */
  @Override
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Determine if the profile is built-in (can't be deleted).
   *
   * @return true=built-in
   */
  @Override
  public boolean isBuiltIn() {
    return builtIn;
  }

  /**
   * Get a BufferedImage of the profile.
   *
   * The image can be used like an icon. The tip of the cutter points to the
   * right. The size is DEFAULT_WIDTH x DEFAULT_HEIGHT.
   *
   * @return image
   */
  @Override
  public BufferedImage getImage() {
    if (image == null) {	// make the image
      image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = image.createGraphics();
      g2d.setColor(IMAGE_COLOR);
      g2d.setStroke(SOLID_LINE);
      int radius = DEFAULT_HEIGHT / 2;
      double r, p;
      int len, y;
      for (int i = -radius; i <= radius; i++) {
        r = (double) i;
        p = profileAt(r, (double) radius);
        if (p < 0.0) {
          len = DEFAULT_WIDTH - DEFAULT_HEIGHT;	// default
        } else {
          len = DEFAULT_WIDTH - (int) Math.round(p);
        }
        y = radius - i;
        g2d.drawLine(0, y, len, y);
      }
    }
    return image;
  }

  /**
   * Make a Drawable PolyLine representing this profile up to the given depth.
   *
   * The origin is at 0.0, 0.0 and the orientation prior to rotation is tip
   * pointing downward.
   *
   * @param pos Position
   * @param rodDiameter rodDiameter
   * @param angle angle of rotation -- plus is counter-clockwise
   * @param c Color
   * @param s BasicStroke
   * @return Drawable PolyLine
   */
  @Override
  public PolyLine getDrawable(Point2D.Double pos, double rodDiameter, double angle, Color c, BasicStroke s) {
    PolyLine pl = new PolyLine(c, s);
    for (int i = -NUM_PTS; i <= NUM_PTS; i++) {
      double x = (double) i / (double) NUM_PTS;
      double y = profileAt(x, 1.0);		// normalize to 1.0
      if (y >= 0.0) {
        pl.add(new Point2D.Double(pos.x + x * rodDiameter / 2.0, pos.y + y * rodDiameter / 2.0));
      }
    }
    pl.rotate(angle, pos);
    return pl;
  }

  /**
   * Calculate the profile of the cutter at a given distance from the center.
   *
   * This is a positive number indicating the distance back from the tip of the
   * cutter, where the tip of the cutter is 0.0. Return -1.0 if the given
   * distance is greater then the radius of the cutter.
   *
   * @param d distance from the center of the cutter (allow for both positive
   * and negative)
   * @param rodRadius rodDiameter/2.0
   * @return profile at the given distance. Return -1.0 if beyond the diameter
   * of the cutter.
   */
  @Override
  public double profileAt(double d, double rodRadius) {
    if (d == 0.0) {
      return 0.0;
    } else {
      return -1.0;
    }
  }

  /**
   * Filter the display name to produce a profile name. Convert everything to
   * uppercase, delete anything after the first blank, and remove anything that
   * is not an uppercase alpha-numeric.
   *
   * @param n display name
   * @return profile name
   */
  protected final String filterName(String n) {
    String str = n.toUpperCase();   // convert to upper case
    if (str.contains(" ")) {	    // only go up to first blank
      str = str.substring(0, str.indexOf(" "));
    }
    for (int i = 0; i < str.length(); i++) {    // look for anything besides uppercase letters
      if (((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z')) || // keep alpha-numerics
          ((str.charAt(i) >= '0') && (str.charAt(i) <= '9'))) {
      } else {
        str = str.replace(str.charAt(i), '-');
      }
    }
    str = str.replace("-", "");     // and strip them out
    if ("".equals(str)) {
      str = "NEW";
    }
    return str;
  }

  @Override
  public void writeXML(PrintWriter out) {
    // Do nothing for built-in profiles. Override for CustomProfile
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // Do nothing for built-in profiles. Override for CustomProfile
  }
}
