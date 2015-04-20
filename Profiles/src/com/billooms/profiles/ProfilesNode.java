package com.billooms.profiles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Node for Profiles.
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
public class ProfilesNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the profile manager. */
  private final Profiles profMgr;

  /**
   * Create a new ProfilesNode for the given ProfileMgr.
   *
   * @param profMgr profile manager
   */
  public ProfilesNode(Profiles profMgr) {
    super(Children.create(new ProfilesChildFactory(profMgr), true), Lookups.singleton(profMgr));
    this.profMgr = profMgr;
    this.setName("Profiles");
    this.setDisplayName(profMgr.toString());
    this.setIconBaseWithExtension("com/billooms/profiles/Profile16.png");

    profMgr.addPropertyChangeListener((PropertyChangeListener) this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("ProfilesNode.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When profiles change, simply update the DisplayName
    this.setDisplayName(profMgr.toString());
  }

}
