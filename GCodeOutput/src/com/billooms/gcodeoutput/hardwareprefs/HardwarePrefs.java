package com.billooms.gcodeoutput.hardwareprefs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Interface for accessing the hardware preferences.
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
@ServiceProvider(service = HardwarePrefs.class)
public class HardwarePrefs {

  /** Property name used for changing hardware Steps per Rotation. */
  String PROP_STEPSPERROTATION = "stepsPerRotation";
  /** Property name used for changing hardware Steps per Inch. */
  String PROP_STEPSPERINCH = "stepsPerInch";

  /** Micro-stepping choices. */
  int[] microSteps = {1, 2, 4, 8, 10, 16};

  private int stepper;	// Stepper motor steps per rotation
  private int micro;		// Driver micro-stepping (2 = 1/2 microstepping)
  private int small;		// Number of teeth on small pulley (on motor)
  private int large;		// Number of teeth on large pulley (on spindle)
  private int stepsPerRotation;	// stepper microsteps per spindle rotation

  private int xzStepper;	// Stepper motor steps per rotation
  private int xzMicro;	// Driver micro-stepping (2 = 1/2 microstepping)
  private int xzTPI;		// lead screw turns per inch
  private int stepsPerInch;	// stepper microsteps per inch
  private final PropertyChangeSupport pcs;

  public HardwarePrefs() {
    pcs = new PropertyChangeSupport(this);
    update();
  }

  /**
   * Get the number of micro-steps per revolution of the spindle. This is a
   * function of the stepper, the driver, and the pulleys (gears).
   *
   * @return micro-steps per revolution
   */
  public int getStepsPerRotation() {
    return stepsPerRotation;
  }

  /**
   * Get the number of micro-steps per inch of the X and Z stages. This is a
   * function of the stepper, the driver, and the lead screw.
   *
   * @return micro-steps per inch
   */
  public int getStepsPerInch() {
    return stepsPerInch;
  }

  /**
   * Get the maximum number of g-code instructions per second. This is a
   * function of the CPU speed on the machine running the g-code.
   *
   * @return maximum g-code instructions per second
   */
  public int getMaxGPerSec() {
    return NbPreferences.forModule(HardwarePrefPanel.class).getInt("maxG", 100);
  }

  /**
   * Update all values from the last saved preferences
   */
  public final void update() {
    switch (NbPreferences.forModule(HardwarePrefPanel.class).getInt("stepper", 0)) {
      default:
      case 0:
        stepper = 200;
        break;
      case 1:
        stepper = 400;
        break;
    }
    micro = microSteps[NbPreferences.forModule(HardwarePrefPanel.class).getInt("micro", 1)];
    small = NbPreferences.forModule(HardwarePrefPanel.class).getInt("smallGear", 20);
    large = NbPreferences.forModule(HardwarePrefPanel.class).getInt("largeGear", 130);
    int old = stepsPerRotation;
    stepsPerRotation = stepper * micro * large / small;
    pcs.firePropertyChange(PROP_STEPSPERROTATION, old, stepsPerRotation);

    switch (NbPreferences.forModule(HardwarePrefPanel.class).getInt("XZStepper", 0)) {
      default:
      case 0:
        xzStepper = 200;
        break;
      case 1:
        xzStepper = 400;
        break;
    }
    xzMicro = microSteps[NbPreferences.forModule(HardwarePrefPanel.class).getInt("XZMicro", 1)];
    xzTPI = NbPreferences.forModule(HardwarePrefPanel.class).getInt("tpi", 10);
    old = stepsPerInch;
    stepsPerInch = xzStepper * xzMicro * xzTPI;
    pcs.firePropertyChange(PROP_STEPSPERINCH, old, stepsPerInch);
  }

  /*
   * Is the g-code file the same name and location as the xml file?
   * @return true=same name/location; false=specified file path.
   */
  public boolean isGSame() {
    return NbPreferences.forModule(HardwarePrefPanel.class).getBoolean("gSame", true);
  }

  /**
   * Get the path name for the g-code file
   *
   * @return full path name for the g-code file
   */
  public String getGPath() {
    return NbPreferences.forModule(HardwarePrefPanel.class).get("gpath", "");
  }

  /**
   * Should we open a telnet connection to LinuxCNCrsh?
   *
   * @return true=connect, false=don't
   */
  public boolean connectLinuxCNC() {
    return NbPreferences.forModule(HardwarePrefPanel.class).getBoolean("emc", false);
  }

  /**
   * Get the IP address of the LinuxCNC machine
   *
   * @return IP address as a string ###.###.###.###
   */
  public String linuxCNCIP() {
    return NbPreferences.forModule(HardwarePrefPanel.class).get("ip", "127.000.000.001");
  }

  /**
   * Add the given PropertyChangeListener to this object
   *
   * @param listener
   */
  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Remove the given PropertyChangeListener from this object
   *
   * @param listener
   */
  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

}
