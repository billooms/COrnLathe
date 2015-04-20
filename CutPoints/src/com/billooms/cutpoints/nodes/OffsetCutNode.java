package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.OffsetCut;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around an OffsetCut to provide property editing, tree
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
public class OffsetCutNode extends CutPointNode {

  /**
   * Create a new OffsetCutNode for the given OffsetCut.
   *
   * @param ch Children of this OffsetCut
   * @param cpt OffsetCut
   * @param cptMgr CutPoint manager
   */
  public OffsetCutNode(Children ch, OffsetCut cpt, CutPoints cptMgr) {
    super(ch, cpt, cptMgr);
    this.setName("OffsetCut");
    this.setDisplayName(cpt.toString());
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("OffsetCutPoint Properties");
    try {

      Property<Integer> repeatProp = new PropertySupport.Reflection<>(cpt, int.class, "Repeat");
      repeatProp.setName("Repeat");
      repeatProp.setShortDescription("Number of cuts around perimeter");
      set.put(repeatProp);

      Property<Integer> offsetProp = new PropertySupport.Reflection<>(cpt, int.class, "IndexOffset");
      offsetProp.setName("IndexOffset");
      offsetProp.setShortDescription("Offset pattern by number of index holes");
      set.put(offsetProp);

      Property<Double> tanPointx = new PropertySupport.Reflection<>(cpt, double.class, "getTangentPointX", null);
      tanPointx.setName("Tangent Point X");
      tanPointx.setShortDescription("Tangent point on the surface");
      set.put(tanPointx);

      Property<Double> tanPointy = new PropertySupport.Reflection<>(cpt, double.class, "getTangentPointY", null);
      tanPointy.setName("Tangent Point Y");
      tanPointy.setShortDescription("Tangent point on the surface");
      set.put(tanPointy);

      Property<Double> tangent = new PropertySupport.Reflection<>(cpt, double.class, "getTangentAngle", null);
      tangent.setName("Tangent Angle");
      tangent.setShortDescription("Tangent angle relative to the flat top");
      set.put(tangent);

      Property<Double> distance = new PropertySupport.Reflection<>(cpt, double.class, "getDistanceToTop", null);
      distance.setName("Distance to top");
      distance.setShortDescription("Straight line distance from surface tangent point to top center");
      set.put(distance);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

}
