package com.billooms.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for FeedRate.
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
public class FeedRateNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy. */
  private final FeedRate feedrate;

  /**
   * Construct a new FeedRateNode.
   *
   * @param feedrate FeedRate object
   */
  public FeedRateNode(FeedRate feedrate) {
    super(Children.LEAF, Lookups.singleton(feedrate));
    this.feedrate = feedrate;
    setName("FeedRate");
    setDisplayName(feedrate.toString());
    setIconBaseWithExtension("com/billooms/controls/icons/GControl16.png");

    feedrate.addPropertyChangeListener(this);	// listen for FeedRate changes
  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("FeedRate Properties");
    try {
      Property<Double> rpmProp = new PropertySupport.Reflection<>(feedrate, double.class, "Rpm");
      rpmProp.setName("RPM");
      rpmProp.setShortDescription("Maximum RPM");
      set.put(rpmProp);

      Property<Double> velProp = new PropertySupport.Reflection<>(feedrate, double.class, "Velocity");
      velProp.setName("Velocity");
      velProp.setShortDescription("Maximum Velocity");
      set.put(velProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When FeedRate changes, simply update the DisplayName
    setDisplayName(feedrate.toString());
  }

}
