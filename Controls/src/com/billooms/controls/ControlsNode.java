package com.billooms.controls;

import com.billooms.controls.Controls.Kind;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for Controls.
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
public class ControlsNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy. */
  private final Controls controls;

  /**
   * Construct a new ControlsNode.
   *
   * @param controls Controls object
   */
  public ControlsNode(Controls controls) {
    super(Children.create(new ControlsChildFactory(controls), true), Lookups.singleton(controls));
    this.controls = controls;
    setName("Controls");
    setDisplayName(controls.toString());
    setIconBaseWithExtension("com/billooms/controls/icons/GControl16.png");

    controls.addPropertyChangeListener(this);	// listen for Controls changes
  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Controls Properties");
    try {
      Property<Kind> kindProp = new PropertySupport.Reflection<>(controls, Kind.class, "Kind");
      kindProp.setName("Kind of cuts");
      kindProp.setShortDescription("Kind of cutting selects which controls are active");
      set.put(kindProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When Controls changes, simply update the DisplayName
    setDisplayName(controls.toString());
  }

}
