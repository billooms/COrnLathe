package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.SpiralCut;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a SpiralCut to provide property editing, tree
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
public class SpiralCutNode extends CutPointNode {

  /** Local copy. */
  private final SpiralCut spiralCut;

  /**
   * Construct a new SpiralCutNode for the given SpiralCut and CutPoint manager.
   * Note that this doesn't look for the Spiral child -- do that in extensions
   * to this.
   *
   * @param cpt SpiralCut
   * @param cptMgr CutPoint manager
   */
  public SpiralCutNode(SpiralCut cpt, CutPoints cptMgr) {
    super(Children.create(new SpiralCutChildFactory(cpt, cptMgr), true), cpt, cptMgr);
    this.spiralCut = cpt;
    this.setName("SpiralCut");
    this.setDisplayName(cpt.toString());
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("SpiralCut Properties");
    try {
      Property<Double> endDepthProp = new PropertySupport.Reflection<>(spiralCut, double.class, "EndDepth");
      endDepthProp.setName("End Cut Depth");
      endDepthProp.setShortDescription("Depth of the cut at the end of the spiral");
      set.put(endDepthProp);

      // Don't display snap, cutter, depth -- get those from beginPoint
      set.remove("Snap");
      set.remove("Cutter");
      set.remove("Cut Depth");

    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }
}
