package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.GoToPoint;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a GoToPoint to provide property editing, tree
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
public class GoToPointNode extends CutPointNode {

  /**
   * Construct a new GoToPointNode for the given GoToPoint and CutPoint manager.
   *
   * @param cpt GoToPoint
   * @param cptMgr CutPoint manager
   */
  public GoToPointNode(GoToPoint cpt, CutPoints cptMgr) {
    super(Children.LEAF, cpt, cptMgr);
    this.setName("GoToPoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/GoTo16.png");
  }
  /**
   * Construct a new GoToPointNode for the given GoToPoint and CutPoint manager.
   *
   * @param cpt GoToPoint
   * @param cptMgr CutPoint manager
   * @param show true=show cutter, false=don't show it
   */
  public GoToPointNode(GoToPoint cpt, CutPoints cptMgr, boolean show) {
    this(cpt, cptMgr);
    this.showCutter = show;
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("GoToPoint Properties");

    set.remove("Snap");
    // GoToPoints need to have cutter information for the situation of multiple cutters
    // and we want to filter only those points that belong to the cutter.
//    set.remove("Cutter");
    set.remove("Cut Depth");

    sheet.put(set);
    return sheet;
  }
}
