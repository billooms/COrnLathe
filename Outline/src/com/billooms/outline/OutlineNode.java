package com.billooms.outline;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for Outline.
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
public class OutlineNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the outline. */
  private final Outline outline;

  /**
   * Create a new OutlineNode for the given Outline.
   *
   * @param outline given Outline
   */
  public OutlineNode(Outline outline) {
    super(Children.create(new OutlineChildFactory(outline), true), Lookups.singleton(outline));
    this.outline = outline;
    setName("Outline");
    setDisplayName(outline.toString());
    setIconBaseWithExtension("com/billooms/outline/Outline16.png");

    outline.addPropertyChangeListener(this);	// listen for Outline changes
  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Outline Properties");
    try {
//      Property locProp = new PropertySupport.Reflection<>(outline, Outline.Location.class, "getDotLocation", null);
      Property<Location> locProp = new PropertySupport.Reflection<>(outline, Location.class, "DotLocation");
      locProp.setName("Dot Location");
      locProp.setShortDescription("Location of digitized dots");
      set.put(locProp);

      Property<Double> thickProp = new PropertySupport.Reflection<>(outline, double.class, "Thickness");
      thickProp.setName("Thickness");
      thickProp.setShortDescription("Thickness of shape");
      set.put(thickProp);

      Property<Double> resProp = new PropertySupport.Reflection<>(outline, double.class, "Resolution");
      resProp.setName("Resolution");
      resProp.setShortDescription("Resolution of curves");
      set.put(resProp);

      Property<Color> resColor = new PropertySupport.Reflection<>(outline, Color.class, "Color");
      resColor.setName("Color");
      resColor.setShortDescription("Color of the outer surface of the shape");
      set.put(resColor);

      Property<Double> layer1Prop = new PropertySupport.Reflection<>(outline, double.class, "Layer1");
      layer1Prop.setName("Layer1");
      layer1Prop.setShortDescription("Thickness of the surface layer, or zero if layers are not used");
      set.put(layer1Prop);

      Property<Color> color1Prop = new PropertySupport.Reflection<>(outline, Color.class, "Color1");
      color1Prop.setName("Color1");
      color1Prop.setShortDescription("Color of the first layer under the surface");
      set.put(color1Prop);

      Property<Double> layer2Prop = new PropertySupport.Reflection<>(outline, double.class, "Layer2");
      layer2Prop.setName("Layer2");
      layer2Prop.setShortDescription("Thickness of the center layer, or zero if layers are not used.");
      set.put(layer2Prop);

      Property<Color> color2Prop = new PropertySupport.Reflection<>(outline, Color.class, "Color2");
      color2Prop.setName("Color2");
      color2Prop.setShortDescription("Color of the second layer under the surface");
      set.put(color2Prop);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("OutlineNode.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When Outline changes, simply update the DisplayName
    setDisplayName(outline.toString());
  }

}
