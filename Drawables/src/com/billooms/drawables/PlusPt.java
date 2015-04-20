package com.billooms.drawables;

import com.billooms.drawables.simple.Plus;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import org.w3c.dom.Element;

/**
 * An extension to Pt which draws a plus.
 *
 * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class PlusPt extends Pt {

  private final Plus plus = new Plus();

  /**
   * A drawable square point defined by inch location and color.
   *
   * @param pos square point location in inches
   * @param c Color
   */
  public PlusPt(Point2D.Double pos, Color c) {
    super(pos.x, pos.y, Style.XY);
    plus.setColor(c);
  }

  /**
   * Construct a new SquarePt from the given DOM Element.
   *
   * @param element given DOM Element
   * @param c color
   */
  public PlusPt(Element element, Color c) {
    super(element, Style.XY);
    plus.setColor(c);
  }

  @Override
  public synchronized void setVisible(boolean v) {
    super.setVisible(v);
    plus.setVisible(v);
  }

  @Override
  public synchronized void setColor(Color c) {
    super.setColor(c);
    plus.setColor(c);
  }

  /**
   * Paint the object
   *
   * @param g2d Graphics2D
   */
  @Override
  public void paint(Graphics2D g2d) {
    if (isVisible()) {
      plus.setXY(getX(), getY());
      plus.paint(g2d);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("SquarePt.propertyChange(): Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
