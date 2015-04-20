package com.billooms.outline;

import com.billooms.drawables.Pt;
import com.billooms.drawables.simple.Plus;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import org.w3c.dom.Element;

/**
 * SafePt extends Pt and uses only XY coordinates. It draws itself with a red
 * plus.
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
public class SafePt extends Pt {

  /** Default color for a SafePt. */
  public final static Color DEFAULT_COLOR = Color.RED;

  /** SafePts are designated by a Plus. */
  private final Plus plus = new Plus();

  /**
   * Construct a new SafePt from the given Point2D.
   *
   * @param pt coordinates for new point
   */
  public SafePt(Point2D.Double pt) {
    super(pt.x, pt.y, Style.XY);
    plus.setColor(DEFAULT_COLOR);
  }

  /**
   * Construct a new SafePt from the given DOM Element.
   *
   * @param element given DOM Element
   */
  public SafePt(Element element) {
    super(element, Style.XY);
    plus.setColor(DEFAULT_COLOR);
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

  @Override
  public void clear() {
    // there should be no propertyChangeListeners to clear
    // SafePt should not be listening to anything
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // SafePt should not be listening to anything
    throw new UnsupportedOperationException("SafePt.propertyChange: Not supported yet.");
  }

  @Override
  public void paint(Graphics2D g2d) {
    if (isVisible()) {
      plus.setXY(getX(), getY());
      plus.paint(g2d);
    }
  }
}
