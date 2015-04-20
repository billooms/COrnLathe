package com.billooms.controls;

import com.billooms.controls.CutCurve.Direction;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for CutCurve.
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
public class CutCurveNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy. */
  private final CutCurve cutCurve;

  /**
   * Construct a new CutCurveNode.
   *
   * @param cutCurve CutCurve object
   */
  public CutCurveNode(CutCurve cutCurve) {
    super(Children.LEAF, Lookups.singleton(cutCurve));
    this.cutCurve = cutCurve;
    setName("CutCurve");
    setDisplayName(cutCurve.toString());
    setIconBaseWithExtension("com/billooms/controls/icons/CutCurve16.png");

    cutCurve.addPropertyChangeListener(this);	// listen for CoarseFine changes
  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("CoarseFine Properties");
    try {
      Property<Double> stepProp = new PropertySupport.Reflection<>(cutCurve, double.class, "step");
      stepProp.setName("Step size");
      stepProp.setShortDescription("Size of each step when cutting the curve.");
      set.put(stepProp);

      Property<Double> backoffProp = new PropertySupport.Reflection<>(cutCurve, double.class, "backoff");
      backoffProp.setName("Backoff");
      backoffProp.setShortDescription("Distance to back off from final shape when starting cuts.");
      set.put(backoffProp);

      Property<Direction> dirProp = new PropertySupport.Reflection<>(cutCurve, Direction.class, "direction");
      dirProp.setName("Rotation");
      dirProp.setShortDescription("Direction of spindle rotation");
      set.put(dirProp);

      Property<Integer> count1Prop = new PropertySupport.Reflection<>(cutCurve, int.class, "count1");
      count1Prop.setName("Course passes");
      count1Prop.setShortDescription("Number of coarse passes.");
      set.put(count1Prop);

      Property<Double> depth1Prop = new PropertySupport.Reflection<>(cutCurve, double.class, "depth1");
      depth1Prop.setName("Coarse Depth");
      depth1Prop.setShortDescription("Depth of each coarse pass.");
      set.put(depth1Prop);

      Property<Integer> count2Prop = new PropertySupport.Reflection<>(cutCurve, int.class, "count2");
      count2Prop.setName("Fine passes");
      count2Prop.setShortDescription("Number of fine passes.");
      set.put(count2Prop);

      Property<Double> depth2Prop = new PropertySupport.Reflection<>(cutCurve, double.class, "depth2");
      depth2Prop.setName("Fine Depth");
      depth2Prop.setShortDescription("Depth of each fine pass.");
      set.put(depth2Prop);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When CutCurve changes, simply update the DisplayName
    setDisplayName(cutCurve.toString());
  }

}
