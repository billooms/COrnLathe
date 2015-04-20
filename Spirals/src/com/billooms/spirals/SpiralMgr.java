package com.billooms.spirals;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Spiral manager finds all available spiral styles and maintains a list of
 * spirals and styles. Note: No check is made for duplicates.
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
@ServiceProvider(service = SpiralMgr.class)
public class SpiralMgr {

  /** Default spiral. */
  public final static String DEFAULT_SPIRAL = "UNIFORMD";

  /** All available spirals. */
  private static List<SpiralStyle> builtIns = null;
  /** Default spiral. */
  private static SpiralStyle defaultSpiral;

  /**
   * Construct a new SpiralMgr.
   */
  public SpiralMgr() {
    if (builtIns == null) {
      builtIns = new ArrayList<>();
      findBuiltIns();
    }
    defaultSpiral = getSpiral(DEFAULT_SPIRAL);		// should be "UNIFORM_D"
  }

  /**
   * Find all the built-in spirals.
   */
  private static void findBuiltIns() {
    Lookup.Result<SpiralStyle> result = Lookup.getDefault().lookupResult(SpiralStyle.class);

    // This is helpful for assuring that the spiral manager finds any user extensions
    System.out.println("Spiral Manger: " + result.allInstances().size() + " spirals found");
    result.allInstances().stream().forEach((p) -> {
      System.out.println("  " + p.getDisplayName());
    });

    result.allInstances().stream().forEach((p) -> {
      builtIns.add(p);
    });
  }

  @Override
  public String toString() {
    return "Spirals: " + builtIns.size() + " built-in";
  }

  /**
   * Get a list of names of all spirals.
   *
   * @return list of names
   */
  public ArrayList<String> getAllNames() {
    ArrayList<String> names = new ArrayList<>();
    builtIns.stream().forEach((p) -> {
      names.add(p.getName());
    });
    return names;
  }

  /**
   * Get a list of display names of all spirals.
   *
   * @return list of names
   */
  public ArrayList<String> getAllDisplayNames() {
    ArrayList<String> names = new ArrayList<>();
    builtIns.stream().forEach((p) -> {
      names.add(p.getDisplayName());
    });
    return names;
  }

  /**
   * Get the spiral for the given index.
   *
   * @param idx index
   * @return spiral (or default if out of range)
   */
  public SpiralStyle get(int idx) {
    if (idx < 0 || idx >= builtIns.size()) {
      return defaultSpiral;
    }
    return builtIns.get(idx);
  }

  /**
   * Get the index of the spiral with the given name.
   *
   * @param name spiral name
   * @return index (or -1 if not found)
   */
  public int indexOf(String name) {
    for (SpiralStyle p : builtIns) {
      if (p.getName().equals(name)) {
        return builtIns.indexOf(p);
      }
    }
    return -1;
  }

  /**
   * Get the spiral for the given spiral name.
   *
   * @param name spiral name
   * @return matching spiral (or the default if not found)
   */
  public final SpiralStyle getSpiral(String name) {
    for (SpiralStyle p : builtIns) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return defaultSpiral;
  }

  /**
   * Get the default Spiral (currently set to "UNIFORM_D").
   *
   * @return the default spiral
   */
  public SpiralStyle getDefaultSpiral() {
    return defaultSpiral;
  }
}
