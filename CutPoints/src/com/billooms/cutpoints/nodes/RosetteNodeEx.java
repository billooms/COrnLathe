package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.RosettePoint;
import com.billooms.cutpoints.RosettePoint.Motion;
import com.billooms.rosette.CompoundRosette;
import com.billooms.rosette.Rosette;
import com.billooms.rosette.RosetteNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;

/**
 * Extends RosetteNode with added actions.
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
public class RosetteNodeEx extends RosetteNode {

  public RosetteNodeEx(Rosette ros) {
    super(ros);
  }

  @Override
  public Action[] getActions(boolean context) {
    Action[] defaults = super.getActions(context);	// the default actions includes "Properties"
    int numAdd = 1;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new ChangeAction();
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
    return newActions;
  }

  /** Nested inner class for action changing to a CompoundRosette. */
  private class ChangeAction extends AbstractAction {

    /** Create the ChangeAction. */
    public ChangeAction() {
      putValue(NAME, "Change to CompoundRosette");
    }

    /**
     * Change to a CompoundRosette.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      RosettePoint rpt = RosetteNodeEx.this.getParentNode().getLookup().lookup(RosettePoint.class);
      if (rpt.getRosette().equals(ros)) {
        rpt.setRosette(new CompoundRosette(rpt.getRosette().getPatternMgr()));
      } else if (rpt.getMotion().equals(Motion.BOTH)) {
        if (rpt.getRosette2().equals(ros)) {
          rpt.setRosette2(new CompoundRosette(rpt.getRosette2().getPatternMgr()));
        }
      }
    }
  }
}
