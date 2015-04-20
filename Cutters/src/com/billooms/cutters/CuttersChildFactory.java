package com.billooms.cutters;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * ChildFactory to create Nodes and keys for children of Cutters (which would be
 * one or more Cutter).
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
public class CuttersChildFactory extends ChildFactory.Detachable<Cutter> implements PropertyChangeListener {

  /** Local copy of the cutter manager. */
  private final Cutters cutterMgr;

  /**
   * Construct a new ProfilesChildFactory for the given Profiles.
   *
   * @param cutterMgr Cutter manager
   */
  public CuttersChildFactory(Cutters cutterMgr) {
    this.cutterMgr = cutterMgr;
    cutterMgr.addPropertyChangeListener(this);  // listen for changes in the Cutters
  }

  @Override
  protected boolean createKeys(List<Cutter> list) {
    list.addAll(cutterMgr.getAllCutters());
    return true;
  }

  @Override
  protected Node createNodeForKey(Cutter key) {
    return new CutterNode(key, cutterMgr);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CuttersChildFactory.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When Profiles change, refresh the child nodes
    refresh(true);
  }

}
