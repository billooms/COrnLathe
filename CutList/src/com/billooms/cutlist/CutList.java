package com.billooms.cutlist;

import com.billooms.drawables.vecmath.Vector2d;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * A list of pseudo-instructions that can later be converted to g-code.
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
@ServiceProvider(service = CutList.class)
public class CutList {

  /** All CutList property change names start with this prefix. */
  public final static String PROP_PREFIX = "CutList" + "_";
  /** Property name used for clearing the list. */
  public final static String PROP_CLEAR = PROP_PREFIX + "Clear";
  /** Property name used to indicate many instructions have been added. */
  public final static String PROP_UPDATE = PROP_PREFIX + "Update";

  /** The list of instructions. */
  private final ArrayList<Inst> list = new ArrayList<>();

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /**
   * Construct a new empty CutList.
   */
  public CutList() {
    // do nothing because the list is already initialized above
  }

  /**
   * Clear the instruction list. This fires a PROP_CLEAR property change.
   */
  public void clear() {
    list.clear();
    pcs.firePropertyChange(PROP_CLEAR, null, null);
  }

  /**
   * This simply fires a PROP_UPDATE property change which usually is used to
   * indicate that a number of instructions have been added.
   */
  public void update() {
    pcs.firePropertyChange(PROP_UPDATE, null, null);
  }

  /**
   * Get the number instructions in the list.
   *
   * @return the number of instructions
   */
  public int length() {
    return list.size();
  }

  /**
   * Get all the instructions in an unmodifiable list.
   *
   * @return list of instructions
   */
  public List<Inst> getAll() {
    return Collections.unmodifiableList(list);
  }

  /**
   * Pop the first instruction from the list and remove it. Note: this does NOT
   * fire a PropertyChange!
   *
   * @return first instruction from the list (or null if there are none)
   */
  public Inst popInst() {
    if (list.isEmpty()) {
      return null;
    }
    Inst i = list.get(0);
    list.remove(i);
    return i;
  }

  /**
   * Go to the specified XZ position with one stage at some speed, and the other
   * stage at a proportional velocity so that travel time on both stages is the
   * same.
   *
   * @param speed speed
   * @param x x-coordinate
   * @param z z-coordinate
   */
  public void goToXZ(Speed speed, double x, double z) {
    switch (speed) {
      default:
      case VELOCITY:
      case RPM:
        list.add(new Inst(Type.GO_XZ_VEL, x, z));
        break;
      case FAST:
        list.add(new Inst(Type.GO_XZ_FAST, x, z));
        break;
    }
  }

  /**
   * Go to the specified XZC position with one stage at some speed, and the
   * other stages at a proportional velocity so that travel time on all stages
   * is the same.
   *
   * @param speed speed
   * @param x x-coordinate
   * @param z z-coordinate
   * @param c spindle rotation in degrees
   */
  public void goToXZC(Speed speed, double x, double z, double c) {
    switch (speed) {
      default:
      case VELOCITY:
        list.add(new Inst(Type.GO_XZC_VEL, x, z, c));
        break;
      case FAST:
        list.add(new Inst(Type.GO_XZC_FAST, x, z, c));
        break;
      case RPM:
        list.add(new Inst(Type.GO_XZC_RPM, x, z, c));
        break;
    }
  }

  /**
   * Go to the specified XZC position with one stage at some speed, and the
   * other stages at a proportional velocity so that travel time on all stages
   * is the same.
   *
   * @param speed speed
   * @param xz x and z coordinates
   * @param c spindle rotation in degrees
   */
  public void goToXZC(Speed speed, Vector2d xz, double c) {
    goToXZC(speed, xz.x, xz.y, c);
  }

  /**
   * Turn the spindle to the given c rotation in degrees from the present
   * location at the set RPM. Note: No wrap-checking is done.
   *
   * @param c rotation in degrees
   */
  public void turn(double c) {
    list.add(new Inst(Type.TURN, c));
  }

  /**
   * Make sure that c-rotation is between -180 and +180 degrees by
   * adding/subtraction 360 to the position.
   */
  public void spindleWrapCheck() {
    list.add(new Inst(Type.SPINDLE_WRAP_CHECK));
  }

  /**
   * Add a comment in the CutList (there is no action).
   *
   * @param s comment string
   */
  public void comment(String s) {
    list.add(new Inst(Type.COMMENT, s));
  }

  /**
   * Add a property change listener
   *
   * @param listener
   */
  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    this.pcs.addPropertyChangeListener(listener);
  }

  /**
   * Remove the given property change listener
   *
   * @param listener
   */
  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    this.pcs.removePropertyChangeListener(listener);
  }
}
