package com.billooms.cornfile;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to forget any unsaved changes and reload the XML file associated with
 * a given COrnFileDataObject and create a new COrnDataNode.
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
    id = "com.billooms.cornfile.Revert"
)
@ActionRegistration(
//    iconBase = "com/billooms/cornfile/Revert.png",
    displayName = "#CTL_Revert"
)
//@ActionReferences({
//  @ActionReference(path = "Loaders/text/cornlathe+xml/Actions", position = 20),
//  @ActionReference(path = "Toolbars/File", position = 0)
//})
@ActionReference(path = "Loaders/text/cornlathe+xml/Actions", position = 20)
@Messages("CTL_Revert=Revert to Saved")
public final class Revert implements ActionListener {

  private final COrnFileDataObject context;

  public Revert(COrnFileDataObject context) {
    this.context = context;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    context.buildNewDataNode(true);
  }
}
