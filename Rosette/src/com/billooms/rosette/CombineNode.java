package com.billooms.rosette;

import com.billooms.rosette.Combine.CombineType;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * This is a Node wrapped around a Combine to provide property editing, tree
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
public class CombineNode extends AbstractNode implements PropertyChangeListener {

  private final Combine combine;

  /**
   * Create a new CombineNode for the given Combine
   *
   * @param comb a Combine
   */
  public CombineNode(Combine comb) {
    super(Children.LEAF, Lookups.singleton(comb));	// there will be no children
    this.combine = comb;
    this.setName("Combine");
    this.setDisplayName(combine.toString());
    this.setIconBaseWithExtension("com/billooms/rosette/Combine16.png");

    combine.addPropertyChangeListener(this);
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
    set.setDisplayName("Combine Properties");
    try {

      Property<CombineType> typeProp = new PropertySupport.Reflection<>(combine, CombineType.class, "Type");
      typeProp.setName("Combine Type");
      typeProp.setShortDescription("Type of combination between two rosettes.");
      set.put(typeProp);

    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setDisplayName(combine.toString());		// update the display name
  }
}
