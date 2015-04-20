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
 * Node for Threads.
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
public class ThreadsNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy. */
  private final Threads threads;

  /**
   * Construct a new ThreadsNode.
   * @param threads Threads object
   */
  public ThreadsNode(Threads threads) {
    super(Children.LEAF, Lookups.singleton(threads));
    this.threads = threads;
    setName("Threads");
    setDisplayName(threads.toString());
    setIconBaseWithExtension("com/billooms/controls/icons/Threads16.png");

    threads.addPropertyChangeListener(this);	// listen for Threads changes
  }

  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Threads Properties");
    try {
      Property<Integer> tpiProp = new PropertySupport.Reflection<>(threads, int.class, "tpi");
      tpiProp.setName("TPI");
      tpiProp.setShortDescription("Threads per inch");
      set.put(tpiProp);
      
      Property<Integer> startsProp = new PropertySupport.Reflection<>(threads, int.class, "starts");
      startsProp.setName("Starts");
      startsProp.setShortDescription("Number of thread starts");
      set.put(startsProp);
      
      Property<Integer> percentProp = new PropertySupport.Reflection<>(threads, int.class, "percent");
      percentProp.setName("%");
      percentProp.setShortDescription("Percent thread engagement");
      set.put(percentProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When Threads changes, simply update the DisplayName
    setDisplayName(threads.toString());
  }

}
