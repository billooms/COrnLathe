package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.RosettePoint;
import com.billooms.cutpoints.RosettePoint.Motion;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a RosettePoint to provide property editing, tree
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
public class RosettePointNode extends CutPointNode {

  /**
   * Construct a new RosettePointNode for the given RosettePoint and CutPoint
   * manager.
   *
   * @param cpt RosettePoint
   * @param cptMgr CutPoint manager
   */
  public RosettePointNode(RosettePoint cpt, CutPoints cptMgr) {
    super(Children.create(new RosettePointChildFactory(cpt), true), cpt, cptMgr);
    this.setName("RosettePoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/Rosette16.png");
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("RosettePoint Properties");
    try {
      Property<Motion> motionProp = new PropertySupport.Reflection<>(cpt, Motion.class, "Motion");
      motionProp.setName("Motion");
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
