package com.billooms.cutters;

import com.billooms.profiles.Profile;
import com.billooms.profiles.ProfileEditorInplace;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for a Cutter.
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
public class CutterNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the cutter. */
  private final Cutter cutter;

  /**
   * Create a new CutterNode from the given cutter.
   *
   * @param cutter given cutter
   * @param cutterMgr cutter manager
   */
  public CutterNode(Cutter cutter, Cutters cutterMgr) {
    super(Children.LEAF, Lookups.fixed(cutter, cutterMgr));
    this.cutter = cutter;
    setName("Cutter");
    setDisplayName(cutter.toString());
    setIconBaseWithExtension("com/billooms/cutters/Cutter16.png");

    cutter.addPropertyChangeListener((PropertyChangeListener) this);	  // listen for changes in the point
  }

  /**
   * Initialize a property sheet.
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Point Properties");
    try {
      Property<String> nameProp = new PropertySupport.Reflection<>(cutter, String.class, "name");
      nameProp.setName("Name");
      nameProp.setShortDescription("Unique name (all capitals)");
      set.put(nameProp);

      Property<String> displayNameProp = new PropertySupport.Reflection<>(cutter, String.class, "displayName");
      displayNameProp.setName("Display name");
      displayNameProp.setShortDescription("Display name");
      set.put(displayNameProp);

      Property<Frame> frameProp = new PropertySupport.Reflection<>(cutter, Frame.class, "Frame");
      frameProp.setName("Cutting Frame");
      frameProp.setShortDescription("Type of cutting frame");
      set.put(frameProp);

      Property<Location> locProp = new PropertySupport.Reflection<>(cutter, Location.class, "Location");
      locProp.setName("Cut Location");
      locProp.setShortDescription("Position of the cutter relative to the surface");
      set.put(locProp);

      PropertySupport.Reflection<Profile> profileProp = new PropertySupport.Reflection<>(cutter, Profile.class, "Profile");
      profileProp.setName("Tip Profile");
      profileProp.setShortDescription("Profile of cutter tip");
      profileProp.setPropertyEditorClass(ProfileEditorInplace.class);
      set.put(profileProp);

      Property<Double> radiusProp = new PropertySupport.Reflection<>(cutter, double.class, "Radius");
      radiusProp.setName("Cut Radius");
      radiusProp.setShortDescription("Radius of the cut as the tip is rotating (for UCF, HCF, ECF)");
      set.put(radiusProp);

      Property<Double> rodProp = new PropertySupport.Reflection<>(cutter, double.class, "tipWidth");
      rodProp.setName("Tip width");
      rodProp.setShortDescription("Width of the cutter tip (diameter for Drill)");
      set.put(rodProp);

      Property<Double> angProp = new PropertySupport.Reflection<>(cutter, double.class, "UCFAngle");
      angProp.setName("UCF/Drill Angle");
      angProp.setShortDescription("Rotation of cutting frame around the y-axis. Zero is along the z-axis, + angle is rotate toward front.");
      set.put(angProp);

      Property<Double> rotProp = new PropertySupport.Reflection<>(cutter, double.class, "UCFRotate");
      rotProp.setName("UCF Rotation");
      rotProp.setShortDescription("Rotation of UCF on its axis. Zero is horizontal, + angle is CCW looking at the wood.");
      set.put(rotProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When a Pt changes, just update the DisplayName
    setDisplayName(cutter.toString());
  }

  @Override
  public Action[] getActions(boolean context) {
    Action[] defaults = super.getActions(context);
    int numAdd = 2;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new DeleteAction();
    newActions[1] = new DuplicateAction();
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
    return newActions;
  }

  /** Nested inner class for action deleting a cutter. */
  private class DeleteAction extends AbstractAction {

    /** Create the DeleteAction. */
    public DeleteAction() {
      putValue(NAME, "Delete");
    }

    /**
     * Remove the selected cutter.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      Cutter c = getLookup().lookup(Cutter.class);
      Cutters mgr = getLookup().lookup(Cutters.class);
      mgr.delete(c);
    }
  }

  /** Nested inner class for action copying a cutter. */
  private class DuplicateAction extends AbstractAction {

    /** Create the DuplicateAction */
    public DuplicateAction() {
      putValue(NAME, "Duplicate");
    }

    /**
     * Copy the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      Cutter c = getLookup().lookup(Cutter.class);
      Cutters mgr = getLookup().lookup(Cutters.class);
      mgr.add(new Cutter(c));
    }
  }
}
