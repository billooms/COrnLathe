package com.billooms.rosette;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * This is a Node wrapped around a CompoundRosette to provide property editing, tree
 * viewing, etc.
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
public class CompoundRosetteNode extends AbstractNode implements PropertyChangeListener {

  private static final COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  private final CompoundRosette cRosette;

  /**
   * Create a new CompoundRosetteNode for the given CompoundRosette
   *
   * @param cRos a CompoundRosette
   */
  public CompoundRosetteNode(CompoundRosette cRos) {
    super(Children.create(new CompoundRosetteChildFactory(cRos), true), Lookups.singleton(cRos));
    this.cRosette = cRos;
    this.setName("CompoundRosette");
    this.setDisplayName(cRos.toString());
    this.setIconBaseWithExtension("com/billooms/rosette/CompoundRosette16.png");

    cRos.addPropertyChangeListener(this);
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("CompoundRosette Properties");
    try {

      Property<Double> ampProp = new PropertySupport.Reflection<>(cRosette, double.class, "pToP");
      ampProp.setName("Amplitude");
      ampProp.setShortDescription("peak-to-peak amplitude");
      set.put(ampProp);

      Property<Double> phaseProp;
      if (prefs.isFracPhase()) {
        phaseProp = new PropertySupport.Reflection<>(cRosette, double.class, "ph");
        phaseProp.setShortDescription("Fractional phase (range 0.0 to 1.0)");
      } else {
        phaseProp = new PropertySupport.Reflection<>(cRosette, double.class, "phase");
        phaseProp.setShortDescription("Engineering phase (range 0.0 to 360.0)");
      }
      phaseProp.setName("Phase");
      set.put(phaseProp);

      Property<Boolean> invertProp = new PropertySupport.Reflection<>(cRosette, boolean.class, "invert");
      invertProp.setName("Invert");
      invertProp.setShortDescription("invert the pattern");
      set.put(invertProp);
      
      Property<Integer> sizeProp = new PropertySupport.Reflection<>(cRosette, int.class, "size", null);
      sizeProp.setName("Number of Rosettes");
      sizeProp.setShortDescription("Number of rosettes being combined");
      set.put(sizeProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setDisplayName(cRosette.toString());		// update the display name
  }
}
