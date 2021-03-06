package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.cutpoints.IndexPoint;
import com.billooms.cutpoints.IndexPoint.Direction;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 * This is a Node wrapped around a IndexPoint to provide property editing, tree
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
public class IndexPointNode extends CutPointNode {

  private static final COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  /**
   * Construct a new IndexPointNode for the given IndexPoint and CutPoint
   * manager.
   *
   * @param cpt IndexPoint
   * @param cptMgr CutPoint manager
   */
  public IndexPointNode(IndexPoint cpt, CutPoints cptMgr) {
    super(Children.LEAF, cpt, cptMgr);
    this.setName("IndexPoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/Index16.png");
  }
  /**
   * Construct a new IndexPointNode for the given IndexPoint and CutPoint
   * manager.
   *
   * @param cpt IndexPoint
   * @param cptMgr CutPoint manager
   * @param show true=show cutter, false=don't show it
   */
  public IndexPointNode(IndexPoint cpt, CutPoints cptMgr, boolean show) {
    this(cpt, cptMgr);
    this.showCutter = show;
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("IndexPoint Properties");
    try {
      Property<Direction> motionProp = new PropertySupport.Reflection<>(cpt, Direction.class, "Direction");
      motionProp.setName("Direction");
      motionProp.setShortDescription("Direction of cutting motion");
      set.put(motionProp);

      Property<Integer> repeatProp = new PropertySupport.Reflection<>(cpt, int.class, "Repeat");
      repeatProp.setName("Repeat");
      repeatProp.setShortDescription("Number of cuts around perimeter");
      set.put(repeatProp);

      Property<Double> phaseProp;
      if (prefs.isFracPhase()) {
        phaseProp = new PropertySupport.Reflection<>(cpt, double.class, "ph");
        phaseProp.setShortDescription("Fractional phase (range 0.0 to 1.0)");
      } else {
        phaseProp = new PropertySupport.Reflection<>(cpt, double.class, "phase");
        phaseProp.setShortDescription("Engineering phase (range 0.0 to 360.0)");
      }
      phaseProp.setName("Phase");
      set.put(phaseProp);

      Property<String> maskProp = new PropertySupport.Reflection<>(cpt, String.class, "Mask");
      maskProp.setName("Mask");
      maskProp.setShortDescription("Mask to select only certain index positions. 0 means skip, any other character means use.");
      set.put(maskProp);

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
