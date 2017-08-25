package com.billooms.profiles;

import com.billooms.profiles.CustomProfile.CustomStyle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * This is a Node wrapped around a CustomProfile to provide property editing,
 * tree viewing, etc.
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
public class CustomProfileNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the profile. */
  private final CustomProfile profile;

  /**
   * Create a new CustomProfileNode for the given profile
   *
   * @param prof profile
   */
  public CustomProfileNode(CustomProfile prof) {
    super(Children.create(new CustomProfileChildFactory(prof), true), Lookups.singleton(prof));
    this.profile = prof;
    this.setName("Profile");
    this.setDisplayName(profile.toString());
    this.setIconBaseWithExtension("com/billooms/profiles/Profile16.png");

    profile.addPropertyChangeListener((PropertyChangeListener) this);
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Profile Properties");
    try {
      Property<CustomStyle> styleProp = new PropertySupport.Reflection<>(profile, CustomStyle.class, "getCustomStyle", null);
      styleProp.setName("Style");
      styleProp.setShortDescription("Style of the custom profile");
      set.put(styleProp);

      Property<String> nameProp = new PropertySupport.Reflection<>(profile, String.class, "name");
      nameProp.setName("Name");
      nameProp.setShortDescription("Unique name (all capitals)");
      set.put(nameProp);

      Property<String> displayNameProp = new PropertySupport.Reflection<>(profile, String.class, "displayName");
      displayNameProp.setName("Display name");
      displayNameProp.setShortDescription("Display name");
      set.put(displayNameProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }

    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CustomProfileNode.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    this.setDisplayName(profile.toString());		// update the display name
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
    int numAdd = 1;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new DeleteAction();
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
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
      CustomProfile prof = getLookup().lookup(CustomProfile.class);
      prof.requestDelete();   // request deletion
    }
  }
}
