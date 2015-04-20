package com.billooms.profiles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * ChildFactory to create Nodes and keys for children of Profiles (which would
 * be a bunch of CustomProfiles).
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
public class ProfilesChildFactory extends ChildFactory.Detachable<CustomProfile> implements PropertyChangeListener {

  /** Local copy of the profile manager. */
  private final Profiles profMgr;

  /**
   * Construct a new ProfilesChildFactory for the given Profiles.
   *
   * @param profMgr profile manager
   */
  public ProfilesChildFactory(Profiles profMgr) {
    this.profMgr = profMgr;
    profMgr.addPropertyChangeListener(this);  // listen for changes in the Profiles
  }

  @Override
  protected boolean createKeys(List<CustomProfile> list) {
    profMgr.getAllCustom().stream().forEach((p) -> {
      list.add(p);
    });
    return true;
  }

  @Override
  protected Node createNodeForKey(CustomProfile key) {
    return new CustomProfileNode(key);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("ProfilesChildFactory.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When Profiles change, refresh the child nodes
    refresh(true);
  }
}
