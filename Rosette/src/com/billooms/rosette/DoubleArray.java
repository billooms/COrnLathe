package com.billooms.rosette;

import java.text.DecimalFormat;

/**
 * An array of double that can be entered and displayed as a comma delimited 
 * text string. 
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
public class DoubleArray {

  /** Formatter for three digits after the decimal place. */
  protected final static DecimalFormat F3 = new DecimalFormat("0.000");
  
  private double[] data = null;   // the data
  
  /**
   * Construct a new DoubleArray with null data.
   */
  public DoubleArray() {
  }
  
  /**
   * Construct a new DoubleArray with the given initial data. 
   * 
   * @param initialData initial data
   */
  public DoubleArray(double[] initialData) {
    this.data = initialData;
  }
  
  /**
   * Construct a new DoubleArray with values from the given comma delimited string. 
   * 
   * @param str comma delimited string
   */
  public DoubleArray(String str) {
    this.data = parseString(str);
  }
  
  /**
   * Construct a new DoubleArray with copies of values from the given DoubleArray.
   * 
   * @param da given DoubleAray (may be null)
   */
  public DoubleArray(DoubleArray da) {
    if ((da == null) || (da.getData() == null)) {
      this.data = null;
    } else {
      this.data = new double[da.size()];
      System.arraycopy(da.getData(), 0, data, 0, da.size());
    }
  }

  @Override
  public String toString() {
    return toString(F3);
  }
  
  /**
   * Convert the data to comma delimited string using the given format.
   * 
   * @param format format
   * @return String (which could be empty if data is null)
   */
  public String toString(DecimalFormat format) {
    String str = "";
    if (data == null) {
      str = "(none)";
    } else {
      for (int i = 0; i < data.length; i++) {
        if (i > 0) {
          str += ", ";
        }
        str += format.format(data[i]);
      }
    }
    return str;
  }
  
  /**
   * Get the data array (which could be null).
   * 
   * @return data array (or null)
   */
  public double[] getData() {
    return data;
  }
  
  /**
   * Set the data with the given array (which could be null).
   * 
   * @param newData new data (or null)
   */
  public void setData(double[] newData) {
    this.data = newData;
  }
  
  /**
   * Set the data with values from the given comma delimited string.
   * 
   * @param str comma delimited string
   */
  public void setData(String str) {
    this.data = parseString(str);
  }
  
  /**
   * Get the size of the data array.
   * 
   * @return size of the data array
   */
  public int size() {
    if (data == null) {
      return 0;
    }
    return data.length;
  }
  
  /**
   * Parse the given string for numbers separated by commas.
   * 
   * An empty string will result in a null array being returned.
   * 
   * @param str string of numbers separated by commas
   * @return double array (or null)
   */
  private double[] parseString(String str) {
    if ((str == null) || (str.length() == 0)) {
      return null;
    }
    String[] strs = str.split(",");
    if (strs.length == 0) {
      return null;
    }
    data = new double[strs.length];
    for (int i = 0; i < strs.length; i++) {
      try {
        data[i] = Double.parseDouble(strs[i]);
      } catch (NullPointerException | NumberFormatException ex) {
        data[i] = 0.0;
      }
    }
    return data;
  }
  
}
