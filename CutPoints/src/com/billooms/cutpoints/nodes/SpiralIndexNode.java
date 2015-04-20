package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.SpiralIndex;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a SpiralIndex to provide property editing, tree
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
public class SpiralIndexNode extends SpiralCutNode {

  /** Local copy. */
  private final SpiralIndex spiralCut;

  /**
   * Construct a new SpiralIndexNode for the given SpiralIndex and CutPoint manager.
   * Note that this doesn't look for the Spiral child -- do that in extensions
   * to this.
   *
   * @param cpt SpiralIndex
   * @param cptMgr CutPoint manager
   */
  public SpiralIndexNode(SpiralIndex cpt, CutPoints cptMgr) {
    super(cpt, cptMgr);
    this.spiralCut = cpt;
    this.setName("SpiralIndex");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/SpiralIdx16.png");
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("SpiralIndex Properties");
    return sheet;
  }
}
