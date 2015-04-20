package com.billooms.controls;

import com.billooms.controls.CoarseFine.Rotation;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for CoarseFine.
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
public class CoarseFineNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy. */
  private final CoarseFine coarseFine;

  /**
   * Construct a new CoarseFineNode.
   *
   * @param coarseFine CoarseFine object
   */
  public CoarseFineNode(CoarseFine coarseFine) {
    super(Children.LEAF, Lookups.singleton(coarseFine));
    this.coarseFine = coarseFine;
    setName("CoarseFine");
    setDisplayName(coarseFine.toString());
    setIconBaseWithExtension("com/billooms/controls/icons/Rosette16.png");

    coarseFine.addPropertyChangeListener(this);	// listen for CoarseFine changes
  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("CoarseFine Properties");
    try {
      Property<Double> passDepthProp = new PropertySupport.Reflection<>(coarseFine, double.class, "passDepth");
      passDepthProp.setName("Pass Depth");
      passDepthProp.setShortDescription("Depth of cuts on coarse cut passes.");
      set.put(passDepthProp);

      Property<Integer> passStepProp = new PropertySupport.Reflection<>(coarseFine, int.class, "passStep");
      passStepProp.setName("Pass Step");
      passStepProp.setShortDescription("Number of micro-steps per movement on coarse cut passes");
      set.put(passStepProp);

      Property<Double> lastDepthProp = new PropertySupport.Reflection<>(coarseFine, double.class, "lastDepth");
      lastDepthProp.setName("Last Depth");
      lastDepthProp.setShortDescription("Depth of cuts on last pass.");
      set.put(lastDepthProp);

      Property<Integer> lastStepProp = new PropertySupport.Reflection<>(coarseFine, int.class, "lastStep");
      lastStepProp.setName("Last Step");
      lastStepProp.setShortDescription("Number of micro-steps per movement on last pass");
      set.put(lastStepProp);

      Property<Rotation> rotProp = new PropertySupport.Reflection<>(coarseFine, Rotation.class, "Rotation");
      rotProp.setName("Rotation");
      rotProp.setShortDescription("Direction of spindle rotation");
      set.put(rotProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When CoarseFine changes, simply update the DisplayName
    setDisplayName(coarseFine.toString());
  }

}
