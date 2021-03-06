package com.billooms.cutters;

import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * Locations identifying where the cutter is located.
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
public enum Location {

  /** Front side of the lathe inside the shape. */
  FRONT_INSIDE("FrontInside.png"),
  /** Front side of the lathe
   * outside the shape. */
  FRONT_OUTSIDE("FrontOutside.png"),
  /** Back side of the lathe
   * inside the shape. */
  BACK_INSIDE("BackInside.png"),
  /** Back side of the lathe
   * outside the shape. */
  BACK_OUTSIDE("BackOutside.png");

  /** Icon showing the cutter location relative to a bowl */
  private final ImageIcon icon;

  Location(String file) {
    icon = new ImageIcon(ImageUtilities.loadImage("com/billooms/cutters/" + file));
  }

  /**
   * Get the display icon.
   *
   * @return display icon
   */
  public ImageIcon getIcon() {
    return icon;
  }

  /**
   * Is the location on the inside?
   *
   * @return true=inside; false=outside
   */
  public boolean isInside() {
    switch (this) {
      case FRONT_INSIDE:
      case BACK_INSIDE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Is the location on the outside?
   *
   * @return true=outside; false=inside
   */
  public boolean isOutside() {
    return !isInside();
  }

  /**
   * Is the location on the front?
   *
   * @return true=front; false=back
   */
  public boolean isFront() {
    switch (this) {
      case FRONT_INSIDE:
      case FRONT_OUTSIDE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Is the location on the back?
   *
   * @return true=back; false=front
   */
  public boolean isBack() {
    return !isFront();
  }

  /**
   * Is the location on the front inside or back outside?
   *
   * @return true=FRONT_INSIDE or BACK_OUTSIDE
   */
  public boolean isFrontInOrBackOut() {
    switch (this) {
      case FRONT_INSIDE:
      case BACK_OUTSIDE:
        return true;
      default:
        return false;
    }
  }

}
