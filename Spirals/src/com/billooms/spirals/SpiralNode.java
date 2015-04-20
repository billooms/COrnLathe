package com.billooms.spirals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * This is a Node wrapped around a Spiral to provide property editing, tree
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
public class SpiralNode extends AbstractNode implements PropertyChangeListener {

  private final Spiral spiral;
  private Sheet.Set set;
  private Property<Double> ampProp;

  /**
   * Create a new SpiralNode for the given Spiral
   *
   * @param spiral a Spiral
   */
  public SpiralNode(Spiral spiral) {
    super(Children.LEAF, Lookups.singleton(spiral));	// there will be no children
    this.spiral = spiral;
    this.setName("Spiral");
    this.setDisplayName(spiral.toString());
    this.setIconBaseWithExtension("com/billooms/spirals/Spiral16.png");

    spiral.addPropertyChangeListener((PropertyChangeListener) this);
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    set = Sheet.createPropertiesSet();
    set.setDisplayName("Spiral Properties");
    try {
      PropertySupport.Reflection<SpiralStyle> spiralProp = new PropertySupport.Reflection<>(spiral, SpiralStyle.class, "Style");
      spiralProp.setName("Spiral");
      spiralProp.setShortDescription("Style of spiral");
      spiralProp.setPropertyEditorClass(SpiralEditorInplace.class);
      set.put(spiralProp);

      Property<Double> twistProp = new PropertySupport.Reflection<>(spiral, double.class, "twist");
      twistProp.setName("Twist");
      twistProp.setShortDescription("total twist in degrees");
      set.put(twistProp);

      ampProp = new PropertySupport.Reflection<>(spiral, double.class, "amp");
      ampProp.setName("Amplitude");
      ampProp.setShortDescription("Optional amplitude parameter");
      if (spiral.getStyle().needsAmplitude()) {
        set.put(ampProp);
      }

    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setDisplayName(spiral.toString());		// update the display name
    if (spiral.getStyle().needsAmplitude()) {
      if (set.get("amplitude") == null) {
        set.put(ampProp);
      }
    } else {
      set.remove("amplitude");
    }
  }

}
