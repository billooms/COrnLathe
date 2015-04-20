package com.billooms.clclass;

import java.awt.Color;
import org.w3c.dom.Element;

/**
 * Utilities for use in conjunction with CLclass objects.
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
public class CLUtilities {

  /**
   * Utility to get a String attribute from the given DOM Element.
   *
   * @param element DOM Element
   * @param name Name of the attribute
   * @param def Default value (in the event of an error)
   * @return value of the attribute (or default if an error)
   */
  public static String getString(Element element, String name, String def) {
    if (!element.hasAttribute(name)) {
      return def;
    }
    return element.getAttribute(name);
  }

  /**
   * Utility to get a Boolean attribute from the given DOM Element.
   *
   * @param element DOM Element
   * @param name Name of the attribute
   * @param def Default value (in the event of an error)
   * @return value of the attribute (or default if an error)
   */
  public static boolean getBoolean(Element element, String name, boolean def) {
    if (!element.hasAttribute(name)) {
      return def;
    }
    return Boolean.parseBoolean(element.getAttribute(name));
  }

  /**
   * Utility to get an Integer attribute from the given DOM Element.
   *
   * @param element DOM Element
   * @param name Name of the attribute
   * @param def Default value (in the event of an error)
   * @return value of the attribute (or default if an error)
   */
  public static int getInteger(Element element, String name, int def) {
    if (!element.hasAttribute(name)) {
      return def;
    }
    try {
      return Integer.parseInt(element.getAttribute(name));
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  /**
   * Utility to get a Double attribute from the given DOM Element.
   *
   * @param element DOM Element
   * @param name Name of the attribute
   * @param def Default value (in the event of an error)
   * @return value of the attribute (or default if an error)
   */
  public static double getDouble(Element element, String name, double def) {
    if (!element.hasAttribute(name)) {
      return def;
    }
    try {
      return Double.parseDouble(element.getAttribute(name));
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  /**
   * Utility to get a Color attribute the form "rgb(128,32,212)" from the given
   * DOM Element. Additional white space is permitted in the string.
   *
   * @param element DOM Element
   * @param name Name of the attribute
   * @param def Default value (in the event of an error)
   * @return the color
   */
  public static Color getColor(Element element, String name, Color def) {
    String str = element.getAttribute(name);
    try {
      if (str.startsWith("rgb(") && str.endsWith(")")) {
        String[] colors = str.substring(4, str.length() - 1).split(",");
        if (colors.length == 3) {
          return new Color(
              Integer.parseInt(colors[0].trim()),
              Integer.parseInt(colors[1].trim()),
              Integer.parseInt(colors[2].trim())
          );
        }
      }
      return def;
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  /**
   * Utility to get a enum attribute from the given DOM Element.
   *
   * @param <E> Some enum
   * @param element DOM Element
   * @param name Name of the attribute
   * @param clazz enum that we're looking for
   * @param def Default value (in the event of an error)
   * @return value of the attribute (or default if an error)
   */
  public static <E extends Enum<E>> E getEnum(Element element, String name, Class<E> clazz, E def) {
    if (!element.hasAttribute(name)) {
      return def;
    }
    E[] consts = clazz.getEnumConstants();
    String str = element.getAttribute(name);
    for (E e : consts) {
      if (e.toString().equals(str)) {
        return e;
      }
    }
    return def;
  }
}
