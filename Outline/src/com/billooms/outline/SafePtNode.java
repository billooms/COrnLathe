package com.billooms.outline;

import com.billooms.drawables.PtNode;
import org.openide.nodes.Sheet;

/**
 * The only difference between PointNode and SafePtNode is that we've changed
 * the DisplayName. Other changed may occur in the future.
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
public class SafePtNode extends PtNode {

  /**
   * Construct a new SafePtNode.
   *
   * @param point point
   */
  public SafePtNode(SafePt point) {
    super(point);
    setName("SafePath Point");

  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = super.createSheet();
    Sheet.Set props = sheet.get("properties");
    props.setDisplayName("SafePathPoint Properties");
    return sheet;
  }
}
