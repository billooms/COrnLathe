package com.billooms.cutters;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Node for Cutters.
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
public class CuttersNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the cutter manager. */
  private final Cutters cutterMgr;

  /**
   * Create a new CuttersNode for the given cutterMgr.
   *
   * @param cutterMgr profile manager
   */
  public CuttersNode(Cutters cutterMgr) {
    super(Children.create(new CuttersChildFactory(cutterMgr), true), Lookups.singleton(cutterMgr));
    this.cutterMgr = cutterMgr;
    this.setName("Cutters");
    this.setDisplayName(cutterMgr.toString());
    this.setIconBaseWithExtension("com/billooms/cutters/Cutter16.png");

    cutterMgr.addPropertyChangeListener((PropertyChangeListener) this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CuttersNode.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When profiles change, simply update the DisplayName
    this.setDisplayName(cutterMgr.toString());
  }

  @Override
  public Action[] getActions(boolean context) {
    Action[] defaults = super.getActions(context);
    int numAdd = 1;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new AddAction();
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
    return newActions;
  }

  /** Nested inner class for action adding a cutter. */
  private class AddAction extends AbstractAction {

    /** Create the AddAction */
    public AddAction() {
      putValue(NAME, "Add Cutter");
    }

    /**
     * Copy the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      Cutters mgr = getLookup().lookup(Cutters.class);
      String str = (String) JOptionPane.showInputDialog(
          null,
          "Enter display name for new cutter:",
          "New Cutter Dialog",
          JOptionPane.PLAIN_MESSAGE,
          null,
          null,
          "New Name");
      if ((str != null) && (str.length() > 0)) {
        mgr.add(new Cutter(str, cutterMgr.getProfileMgr()));
      }
    }
  }
}
