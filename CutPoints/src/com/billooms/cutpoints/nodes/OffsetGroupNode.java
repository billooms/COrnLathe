package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.OffsetGroup;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around an OffsetGroup to provide property editing,
 * tree viewing, etc.
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
public class OffsetGroupNode extends OffsetCutNode {

  /**
   * Construct a new OffsetGroupNode for the given OffsetGroup.
   *
   * @param cpt OffsetGroup
   * @param cptMgr CutPoint manager
   */
  public OffsetGroupNode(OffsetGroup cpt, CutPoints cptMgr) {
    super(Children.create(new OffsetGroupChildFactory(cpt, cptMgr), true), cpt, cptMgr);
    this.setName("OffsetGroup");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/Offset16.png");
  }
	@Override
	protected Sheet createSheet() {
		super.createSheet();
		set.setDisplayName("OffsetGroup Properties");

        sheet.put(set);
		return sheet;
	}

}
