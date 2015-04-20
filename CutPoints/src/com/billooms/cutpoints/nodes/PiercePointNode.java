package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.PiercePoint;
import com.billooms.cutpoints.PiercePoint.Direction;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a PiercePoint to provide property editing, tree
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
public class PiercePointNode extends CutPointNode {

  /**
   * Construct a new PiercePointNode for the given PiercePoint and CutPoint
   * manager.
   *
   * @param cpt PiercePoint
   * @param cptMgr CutPoint manager
   */
  public PiercePointNode(PiercePoint cpt, CutPoints cptMgr) {
    super(Children.LEAF, cpt, cptMgr);
    this.setName("PiercePoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/Pierce16.png");
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("PiercePoint Properties");
    try {
      Property<Direction> motionProp = new PropertySupport.Reflection<>(cpt, Direction.class, "Direction");
      motionProp.setName("Direction");
      motionProp.setShortDescription("Direction of cutting motion");
      set.put(motionProp);

      Property<Double> cutWProp = new PropertySupport.Reflection<>(cpt, double.class, "getWidthAtMax", null);
      cutWProp.setName("Cut Width");
      cutWProp.setShortDescription("Width of cut at the maximum cut depth");
      set.put(cutWProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

}
