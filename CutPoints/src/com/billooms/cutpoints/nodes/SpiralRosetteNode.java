package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.SpiralRosette;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a SpiralRosette to provide property editing, tree
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
public class SpiralRosetteNode extends SpiralCutNode {

  /** Local copy. */
  private final SpiralRosette spiralCut;

  /**
   * Construct a new SpiralRosetteNode for the given SpiralRosette and CutPoint manager.
   * Note that this doesn't look for the Spiral child -- do that in extensions
   * to this.
   *
   * @param cpt SpiralRosette
   * @param cptMgr CutPoint manager
   */
  public SpiralRosetteNode(SpiralRosette cpt, CutPoints cptMgr) {
    super(cpt, cptMgr);
    this.spiralCut = cpt;
    this.setName("SpiralRosette");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/SpiralRos16.png");
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("SpiralRosette Properties");
    return sheet;
  }
}
