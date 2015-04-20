package com.billooms.outlineeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to convert a spiral CutPoint to a series of individual CutPoints.
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
@ActionID(category = "Edit",
    id = "com.billooms.outlineeditor.MacroSpiral")
@ActionRegistration(displayName = "#CTL_MacroSpiral")
@ActionReferences({
  @ActionReference(path = "Menu/Outline", position = 3100, separatorBefore = 3000)
})
@Messages("CTL_MacroSpiral=Spiral to CutPoints...")
public final class MacroSpiral extends AbstractAction implements ActionListener {

  @Override
  public void actionPerformed(ActionEvent e) {
    MacroSpiralPanel panel;
    try {
      panel = new MacroSpiralPanel();
    } catch (Exception ex) {
      NotifyDescriptor d = new NotifyDescriptor.Message(ex, NotifyDescriptor.ERROR_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
    DialogDescriptor dd = new DialogDescriptor(
        panel,
        "Convert Spiral to Rosette Cuts",
        true,
        DialogDescriptor.OK_CANCEL_OPTION,
        DialogDescriptor.OK_OPTION,
        null);
    Object result = DialogDisplayer.getDefault().notify(dd);
    if (result == DialogDescriptor.OK_OPTION) {
      panel.makePoints();
    }
  }
}
