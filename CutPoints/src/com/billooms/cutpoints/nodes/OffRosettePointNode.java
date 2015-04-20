package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.OffRosettePoint;
import org.openide.nodes.Sheet;

/**
 * This is a Node wrapped around a OffRosettePoint to provide property editing,
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
public class OffRosettePointNode extends RosettePointNode {

  /**
   * Construct a new OffRosettePointNode for the given OffRosettePoint and
   * CutPoint manager.
   *
   * @param cpt OffRosettePoint
   * @param cptMgr CutPoint manager
   */
  public OffRosettePointNode(OffRosettePoint cpt, CutPoints cptMgr) {
    super(cpt, cptMgr);   // The RosettePointNode constructor creates the RosetteNode children
    this.setName("OffRosettePoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/Rosette16.png");
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("OffRosettePoint Properties");

    sheet.put(set);
    return sheet;
  }

}
