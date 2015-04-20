package com.billooms.outline;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Node for SafePath. At present, there are no attributes for SafePath.
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
public class SafePathNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the SafePath. */
  private final SafePath safePath;

  /**
   * Create a new SafePathNode for the given SafePath.
   *
   * @param safePath given SafePath
   */
  public SafePathNode(SafePath safePath) {
    super(Children.create(new SafePathChildFactory(safePath), true), Lookups.singleton(safePath));
    this.safePath = safePath;
    setName("SafePath");
    setDisplayName(safePath.toString());
    setIconBaseWithExtension("com/billooms/outline/Safe16.png");

    safePath.addPropertyChangeListener(this);	// listen for SafePath changes
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When SafePath changes, simply update the DisplayName
    setDisplayName(safePath.toString());
  }

  /**
   * Add my own actions for the node.
   *
   * @param popup Find actions for context meaning or for the node itself.
   * @return List of all actions
   */
  @Override
  public Action[] getActions(boolean popup) {
    Action[] defaults = super.getActions(popup);	// the default actions includes "Properties"
    Action[] newActions = new Action[defaults.length + 1];
    newActions[0] = new DeleteAction();
    System.arraycopy(defaults, 0, newActions, 1, defaults.length);
    return newActions;
  }

  /**
   * Nested inner class for action deleting a point.
   */
  private class DeleteAction extends AbstractAction {

    /**
     * Create the DeleteAction
     */
    public DeleteAction() {
      putValue(NAME, "Delete");
    }

    /**
     * Remove the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      SafePath path = getLookup().lookup(SafePath.class);
      path.requestDelete();   // request deletion
    }
  }

}
