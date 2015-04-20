package com.billooms.clclass;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * This defines a class that is always defined by an XML DOM Element.
 *
 * All CLclass objects should have a constructor that takes a DOM Element.
 * CLclass requires a means of writing the object information to an XML file.
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
public abstract class CLclass implements PropertyChangeListener {

  /** Formatter for single digit after the decimal place. */
  protected final static DecimalFormat F1 = new DecimalFormat("0.0");
  /** Formatter for double digit after the decimal place. */
  protected final static DecimalFormat F2 = new DecimalFormat("0.00");
  /** Formatter for three digits after the decimal place. */
  protected final static DecimalFormat F3 = new DecimalFormat("0.000");
  /** Formatter for four digits after the decimal place. */
  protected final static DecimalFormat F4 = new DecimalFormat("0.0000");

  /** Indentation is shared by all classes and used for formatting writeXML */
  public static String indent = "";

  /** All CLclass objects can fire propertyChanges. */
  protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /**
   * Increase indentation by 2 spaces.
   */
  public static synchronized void indentMore() {
    indent += "  ";
  }

  /**
   * Decrease indentation by 2 spaces.
   */
  public static synchronized void indentLess() {
    if (indent.length() >= 2) {
      indent = indent.substring(2);
    } else {
      indent = "";
    }
  }

  /**
   * Write the data to an xml file.
   *
   * @param out output stream for writing
   */
  public abstract void writeXML(PrintWriter out);

  /**
   * Add the given listener to this object.
   *
   * @param listener
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Remove the given listener to this object.
   *
   * @param listener
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

}
