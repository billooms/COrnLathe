package com.billooms.cornlatheprefs;

import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class to get the stored COrnLathe preferences.
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
@ServiceProvider(service = COrnLathePrefs.class)
public class COrnLathePrefs {

  /** Flag to indicate fractional phase (rather than degrees). */
  private boolean fracPhase = false;

  /** Construct a new COrnLathePrefs object. */
  public COrnLathePrefs() {
    update();
  }

  /**
   * Are preferences set for fractional phase?
   *
   * @return true=fractional phase, false=engineering phase
   */
  public boolean isFracPhase() {
    return fracPhase;
  }

  /**
   * Is the cutter library checkbox seleted?
   * 
   * @return true=use library
   */
  public boolean useLibrary() {
    return NbPreferences.forModule(COrnLathePanel.class).getBoolean("useLib", false);
  }

  /**
   * Get the path name for the cutter library file
   *
   * @return full path name for the cutter library file
   */
  public String getLibPath() {
    return NbPreferences.forModule(COrnLathePanel.class).get("libpath", "");
  }

  /**
   * Update all values from the last saved preferences.
   */
  private void update() {
    fracPhase = NbPreferences.forModule(COrnLathePanel.class).getBoolean("fracPhase", false);
  }
}
