package com.billooms.cornfile;

import com.billooms.controls.Controls;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutters.Cutters;
import com.billooms.outline.Outline;
import com.billooms.patterns.Patterns;
import com.billooms.profiles.Profiles;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * DataNode for a COrnLathe XML file.
 *
 * This is separate from the COrnFileDataObject so that it doesn't get generated
 * during browsing in the Favorites window. One must explicitly use the LoadData
 * action.
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
public class COrnDataNode extends DataNode {

  /** Local copy of the COrnFileDataObject. */
  private final COrnFileDataObject dObj;

  /**
   * Construct a new COrnDataNode from the given COrnFileDataObject.
   *
   * @param obj given COrnFileDataObject
   */
  public COrnDataNode(COrnFileDataObject obj) {
    super(obj, Children.create(new COrnChildFactory(obj), true),
        Lookups.fixed(obj, // can lookup the COrnFileDataObject
            obj.getPrimaryFile(),   // can lookup the fileobject
            (Controls) obj.getTopObject().getClass(Controls.class), // can lookup Controls
            (Patterns) obj.getTopObject().getClass(Patterns.class), // can lookup Patterns
            (Profiles) obj.getTopObject().getClass(Profiles.class), // can lookup Profiles
            (Cutters) obj.getTopObject().getClass(Cutters.class), // can lookup Cutters
            (Outline) obj.getTopObject().getClass(Outline.class), // can lookup Outline
            (CutPoints) obj.getTopObject().getClass(CutPoints.class)));	// can lookup CutPoints
    this.dObj = obj;
    setIconBaseWithExtension("com/billooms/cornfile/COrnLathe16.png");
    setDisplayName(this.dObj.getPrimaryFile().getNameExt());
  }

}
