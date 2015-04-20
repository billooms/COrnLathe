package com.billooms.drawables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * A Drawable 2D grid of lines.
 *
 * Defined by bottom, left (in inches) and width, height (in inches).
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class Grid implements Drawable {

  private final static Color GRID_COLOR = Color.GRAY;

  /** The color of the grid lines. */
  private Color color = GRID_COLOR;
  /** Flag indicating if the grid is visible or not. */
  private boolean visible = true;
  /** Lower left, width, and height. */
  private final double x, y, w, h;

  /**
   * A drawable 2D grid of lines with no text.
   *
   * @param x Left edge of grid in inches
   * @param y Bottom edge of grid in inches
   * @param w width of grid in inches
   * @param h width of grid in inches
   */
  public Grid(double x, double y, double w, double h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public void setColor(Color c) {
    this.color = c;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void setVisible(boolean v) {
    this.visible = v;
  }

  /**
   * Paint the object.
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (visible) {
      g2d.setColor(color);
      if ((w <= 0.0) || (h <= 0.0)) {
        return;
      }
      float scale = (float) g2d.getTransform().getScaleX();
      if (scale < 0.0) {
        return;		// not sure why this happens sometimes
      }
      float width = 1.0f / scale;
      BasicStroke solid = new BasicStroke(width);
      BasicStroke dotted = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{1 / scale, 5 / scale}, 0);
      for (int xx = (int) x; xx < (x + w); xx++) {	// draw vertical lines
        if (xx == 0) {
          g2d.setStroke(solid);		// line at zero is solid
        } else {
          g2d.setStroke(dotted);
        }
        g2d.draw(new Line2D.Double((double) xx, y, (double) xx, y + h));
      }
      for (int yy = (int) y; yy < (y + h); yy++) {	// draw horizontal lines
        if (yy == 0) {
          g2d.setStroke(solid);		// line at zero is solid
        } else {
          g2d.setStroke(dotted);
        }
        g2d.draw(new Line2D.Double(x, (double) yy, x + w, (double) yy));
      }
    }
  }
}
