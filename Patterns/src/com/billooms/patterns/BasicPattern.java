package com.billooms.patterns;

import com.billooms.clclass.CLclass;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.PiecedLine;
import com.billooms.drawables.PtDefinedLine;
import com.billooms.drawables.SquarePt;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;

/**
 * Basic pattern that can be extended without having to implement all the
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
public abstract class BasicPattern extends CLclass implements Pattern {

  /** The name of this pattern (upper case letters only). */
  protected String name = "";
  /** The display name of this pattern. */
  protected String displayName = "";
  /** A small icon-like image of the pattern. */
  protected BufferedImage image = null;
  /** The minimum allowable repeat for this pattern (generally 1). */
  protected int minRepeat = 1;
  /** Indicates if the pattern is a built-in pattern (cannot be deleted). */
  protected boolean builtIn = true;
  /** Indicates the pattern needs a repeat value. */
  protected boolean needsRepeat = false;
  /** Flag indicating if this pattern requires optional n2 parameter. */
  protected boolean needsN2 = false;
  /** Flag indicating if this pattern requires optional amp2 parameter. */
  protected boolean needsAmp2 = false;
  /** A line used for display in the pattern editor. */
  protected PtDefinedLine line = null;

  /** The repeat value (for those patterns that need it). */
  protected int repeat;
  /** The optional n2 value (for those patterns that need it). */
  protected int n2;
  /** The optional amp2 value (for those patterns that need it). */
  protected double amp2;

  /** The color of the plotted line. */
  public final static Color IMAGE_COLOR = new Color(255, 120, 0);	// dark orange
  /** Number of points in the plot. */
  private final int NUM_PTS = 48;

  /**
   * Create a new pattern with a style of the given display name.
   *
   * The name will be a modified version of the display name: Convert everything
   * to uppercase, delete anything after the first blank, and remove anything
   * that is not an uppercase.
   *
   * @param displayName display name of the new style
   */
  public BasicPattern(String displayName) {
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
    if (line == null) {		// for custom patterns, line is already initialized
      makeLine();
    }
    line.paint(g2d);
  }

  /**
   * Make the line (for built-in patterns). For custom patterns, the line is
   * already initialized.
   */
  private synchronized void makeLine() {
    line = new PiecedLine(IMAGE_COLOR, SOLID_LINE);
    double x, y;
    for (int i = 0; i <= NUM_PTS; i++) {
      x = (double) i / (double) NUM_PTS;
      if (needsOptions()) {
        y = getValue(x, repeat, n2, amp2);
      } else if (needsRepeat) {
        y = getValue(x, repeat);
      } else {
        y = getValue(x);
      }
      SquarePt point = new SquarePt(new Point2D.Double(x, y), IMAGE_COLOR);
      line.insertPtX(point);
    }
  }

  /**
   * Get the name of this pattern.
   *
   * @return name of this pattern
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
   * Get the repeat value.
   *
   * @return repeat value
   */
  @Override
  public int getRepeat() {
    return repeat;
  }

  /**
   * Find the minimum repeat allowed for this pattern.
   *
   * @return minimum repeat
   */
  @Override
  public int getMinRepeat() {
    return minRepeat;
  }

  /**
   * Determine if the pattern is built-in (can't be deleted).
   *
   * @return true=built-in
   */
  @Override
  public boolean isBuiltIn() {
    return builtIn;
  }

  /**
   * Determine if the pattern needs a repeat value (usually not the case).
   *
   * @return true=needs a repeat value for the pattern
   */
  @Override
  public boolean needsRepeat() {
    return needsRepeat;
  }

  /**
   * Determine if the pattern needs optional n2.
   *
   * @return true=needs optional n2 parameter
   */
  @Override
  public boolean needsN2() {
    return needsN2;
  }

  /**
   * Determine if the pattern needs optional parameter amp2.
   *
   * @return true=needs optional amp2 parameter
   */
  @Override
  public boolean needsAmp2() {
    return needsAmp2;
  }

  /**
   * Determine if the pattern needs optional parameter (n2 and/or amp2).
   *
   * @return true=needs one or more optional parameters
   */
  @Override
  public boolean needsOptions() {
    return needsN2 || needsAmp2;
  }

  /**
   * Get a BufferedImage of the pattern.
   *
   * The image can be used like an icon. The tip of the cutter points to the
   * right. The size is DEFAULT_WIDTH x DEFAULT_HEIGHT.
   *
   * @return image
   */
  @Override
  public BufferedImage getImage() {	  // TODO: Icon doesn't draw first time on ARCs and TRIGs
    if (image == null) {	// make the image
      image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = image.createGraphics();
      g2d.setColor(IMAGE_COLOR);
      g2d.setStroke(SOLID_LINE);
      Point[] pts = new Point[NUM_PTS + 1];
      double x, y;
      for (int i = 0; i <= NUM_PTS; i++) {
        x = (double) i / (double) NUM_PTS;
        y = getValue(x);
        pts[i] = new Point((int) (x * DEFAULT_WIDTH), DEFAULT_HEIGHT - (int) (y * DEFAULT_HEIGHT));
      }

      GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, pts.length);
      polyline.moveTo(pts[0].x, pts[0].y);
      for (Point pt : pts) {
        polyline.lineTo(pt.x, pt.y);
      }
      g2d.draw(polyline);
    }
    return image;
  }

  /**
   * Get a BufferedImage of a plot of the pattern. The image can be used like an
   * icon. The size is DEFAULT_WIDTH x DEFAULT_HEIGHT. This should only be used
   * for patterns that need to know the repeat. For all others, use getImage().
   *
   * @param r pattern repeat
   * @return image
   */
  @Override
  public BufferedImage getImage(int r) {
    if ((image == null) || (r != repeat)) {		// make the image again if repeat changes
      this.repeat = r;						// save if for comparison next time
      image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = image.createGraphics();
      g2d.setColor(IMAGE_COLOR);
      g2d.setStroke(SOLID_LINE);
      Point[] pts = new Point[NUM_PTS + 1];
      double x, y;
      for (int i = 0; i <= NUM_PTS; i++) {
        x = (double) i / (double) NUM_PTS;
        y = getValue(x, repeat);
        pts[i] = new Point((int) (x * DEFAULT_WIDTH), DEFAULT_HEIGHT - (int) (y * DEFAULT_HEIGHT));
      }

      GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, pts.length);
      polyline.moveTo(pts[0].x, pts[0].y);
      for (Point pt : pts) {
        polyline.lineTo(pt.x, pt.y);
      }
      g2d.draw(polyline);
    }
    return image;
  }

  /**
   * Get a BufferedImage of a plot of the pattern. The image can be used like an
   * icon. The size is DEFAULT_WIDTH x DEFAULT_HEIGHT. This should only be used
   * for patterns that need optional parameters. For all others, use
   * getImage(n).
   *
   * @param r pattern repeat
   * @param n optional 2nd integer parameter
   * @param a2 optional 2nd amplitude parameter
   * @return image
   */
  @Override
  public BufferedImage getImage(int r, int n, double a2) {
    if ((image == null) || (r != repeat)
        || (this.n2 != n) || (amp2 != a2)) {	// make the image again if something changes
      this.repeat = r;					// save if for comparison next time
      this.n2 = n;
      this.amp2 = a2;
      image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = image.createGraphics();
      g2d.setColor(IMAGE_COLOR);
      g2d.setStroke(SOLID_LINE);
      Point[] pts = new Point[NUM_PTS + 1];
      double x, y;
      for (int i = 0; i <= NUM_PTS; i++) {
        x = (double) i / (double) NUM_PTS;
        y = getValue(x, repeat, n2, amp2);
        pts[i] = new Point((int) (x * DEFAULT_WIDTH), DEFAULT_HEIGHT - (int) (y * DEFAULT_HEIGHT));
      }

      GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, pts.length);
      polyline.moveTo(pts[0].x, pts[0].y);
      for (Point pt : pts) {
        polyline.lineTo(pt.x, pt.y);
      }
      g2d.draw(polyline);
    }
    return image;
  }

  /**
   * Filter the display name to produce a pattern name. Convert everything to
   * uppercase, delete anything after the first blank, and remove anything that
   * is not an uppercase alpha-numeric.
   *
   * @param n display name
   * @return pattern name
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
    // Do nothing for built-in patterns. Override for CustomPattern
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // Do nothing for built-in patterns. Override for CustomPattern
  }
}
