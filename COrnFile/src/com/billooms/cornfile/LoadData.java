package com.billooms.cornfile;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to load the XML file associated with a given COrnFileDataObject and
 * create a new COrnDataNode.
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
@ActionID(
    category = "File",
    id = "com.billooms.cornfile.LoadData"
)
@ActionRegistration(
    displayName = "#CTL_LoadData"
)
@ActionReference(path = "Loaders/text/cornlathe+xml/Actions", position = 0)
@Messages("CTL_LoadData=Load Data")
public final class LoadData implements ActionListener {

  /** Local copy of the COrnFileDataObject. */
  private final COrnFileDataObject obj;

  /**
   * Construct the LoadData ActionListener for the given object.
   *
   * @param obj given COrnFileDataObject
   */
  public LoadData(COrnFileDataObject obj) {
    this.obj = obj;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    obj.buildNewDataNode(false);    // don't re-parse the file if it's already loaded
  }
}
