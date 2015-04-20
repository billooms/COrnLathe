package com.billooms.cutlist;

/**
 * Various types of instructions.
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
public enum Type {

  /** Proportional X & Z movement scaled with one stage at max velocity. */
  GO_XZ_FAST("Go XZ Fast"),
  /** Proportional
   * X & Z movement scaled with one stage at set velocity. */
  GO_XZ_VEL("Go XZ at Velocity"),
  /** Proportional
   * X & Z movement scaled with one stage at max velocity, maximum spindle. */
  GO_XZC_FAST("Go XZC Fast"),
  /** Proportional X, Z, and C, scaled for set RPM. */
  GO_XZC_RPM("Go XZC at RPM"),
  /** Proportional X, Z, and C, scaled for set velocity. */
  GO_XZC_VEL("Go XZC at Velocity"),
  /** Turn to C degrees at set RPM, no wrap check. */
  TURN("Turn"),
  /** Wrap-around check: set spindle between -180 and +180 degrees. */
  SPINDLE_WRAP_CHECK("Spindle Wrap Check"),
  /** No action -- comment in CutList. */
  COMMENT("//");

  /** Text used for display. */
  public String text;

  Type(String str) {
    this.text = str;
  }

}
