package com.billooms.cutpoints;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Action to move the selected CutPoint up in the list.
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
    id = "com.billooms.cutpoints.UpAction")
@ActionRegistration(displayName = "#CTL_UpAction")
@ActionReferences({
  @ActionReference(path = "Shortcuts", name = "D-UP")
})
@Messages("CTL_UpAction=Move Up")
public final class UpAction extends AbstractAction {

  private Lookup.Result<CutPoint> result;
  private CutPoints mgr;

  /** Construct a new UpAction. */
  public UpAction() {
    this(Utilities.actionsGlobalContext());
  }

  /**
   * Construct a new UpAction.
   *
   * @param lookup given lookup
   */
  public UpAction(Lookup lookup) {
    super("Move Up");
    this.result = lookup.lookupResult(CutPoint.class);
    this.mgr = lookup.lookup(CutPoints.class);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ((this.result != null) && (this.mgr != null) && (this.result.allInstances().size() > 0)) {
      result.allInstances().stream().forEach(c -> mgr.moveUp(c));
    }
  }
}
