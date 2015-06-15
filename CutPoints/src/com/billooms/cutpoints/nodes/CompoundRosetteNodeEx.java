package com.billooms.cutpoints.nodes;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.cutpoints.RosettePoint;
import com.billooms.rosette.CompoundRosette;
import com.billooms.rosette.CompoundRosetteNode;
import com.billooms.rosette.Rosette;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Extends CompoundRosetteNode with added actions.
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
public class CompoundRosetteNodeEx extends CompoundRosetteNode {

  public CompoundRosetteNodeEx(CompoundRosette cRos) {
    super(cRos);
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
      putValue(NAME, "Change to Simple Rosette");
    }

    /**
     * Change to a simple Rosette.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      RosettePoint rpt = CompoundRosetteNodeEx.this.getParentNode().getLookup().lookup(RosettePoint.class);
      if (rpt.getRosette().equals(cRosette)) {
        rpt.setRosette(new Rosette(rpt.getRosette().getPatternMgr()));
      } else if (rpt.getMotion().equals(RosettePoint.Motion.BOTH)) {
        if (rpt.getRosette2().equals(cRosette)) {
          rpt.setRosette2(new Rosette(rpt.getRosette2().getPatternMgr()));
        }
      }
    }
  }
}
