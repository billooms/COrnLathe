package com.billooms.cutlist;

import java.text.DecimalFormat;

/**
 * Instruction for the CutList. All instances of instructions are immutable. 
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
public class Inst {

  private final static DecimalFormat F2 = new DecimalFormat("0.00");
  private final static DecimalFormat F4 = new DecimalFormat("0.0000");

  /** Instruction type. */
  private final Type type;
  /** X-coordinate. */
  private final double x;
  /** Z-coordinate. */
  private final double z;
  /** C-coordinate (spindle). */
  private final double c;
  /** Text (for comments). */
  private final String text;

  /**
   * Construct an instruction with XZC coordinates.
   *
   * @param i Instruction type
   * @param x x-value
   * @param z z-value
   * @param c c-value 
   */
  public Inst(Type i, double x, double z, double c) {
    this.type = i;
    this.x = x;
    this.z = z;
    this.c = c;
    this.text = "";
  }

  /**
   * Construct an instruction with XZ coordinates.
   *
   * @param i Instruction type
   * @param x x-value
   * @param z z-value
   */
  public Inst(Type i, double x, double z) {
    this(i, x, z, 0.0);
  }

  /**
   * Construct an instruction with C coordinate.
   *
   * @param i Instruction type
   * @param c c-value 
   */
  public Inst(Type i, double c) {
    this(i, 0.0, 0.0, c);
  }

  /**
   * Construct an instruction with no coordinate.
   *
   * @param i Instruction type
   */
  public Inst(Type i) {
    this(i, 0.0, 0.0, 0.0);
  }

  /**
   * Create an instruction with text (comment).
   *
   * @param i Instruction type
   * @param s String
   */
  public Inst(Type i, String s) {
    this.type = i;
    this.text = s;
    this.x = 0.0;
    this.z = 0.0;
    this.c = 0.0;
  }

  /**
   * Get the instruction type.
   *
   * @return instruction type
   */
  public Type getType() {
    return type;
  }

  /**
   * Get the x coordinate.
   *
   * @return x coordinate
   */
  public double getX() {
    return x;
  }

  /**
   * Get the z coordinate.
   *
   * @return z coordinate
   */
  public double getZ() {
    return z;
  }

  /**
   * Get the c coordinate.
   *
   * @return c coordinate
   */
  public double getC() {
    return c;
  }

  /**
   * Get the text string.
   *
   * @return text string
   */
  public String getText() {
    return text;
  }

  /**
   * Return a string with displayable information about this instruction.
   *
   * @return a string
   */
  @Override
  public String toString() {
    String str;
    switch (type) {
      default:
      case GO_XZC_FAST:
      case GO_XZC_RPM:
      case GO_XZC_VEL:
        str = type.text + ": " + F4.format(x) + ", " + F4.format(z) + ", " + F2.format(c);
        break;
      case GO_XZ_VEL:
      case GO_XZ_FAST:
        str = type.text + ": " + F4.format(x) + ", " + F4.format(z);
        break;
      case TURN:
        str = type.text + ": " + F2.format(c);
        break;
      case SPINDLE_WRAP_CHECK:
        str = type.text;
        break;
      case COMMENT:
        str = "// " + text;
        break;
    }
    return str;
  }
}
