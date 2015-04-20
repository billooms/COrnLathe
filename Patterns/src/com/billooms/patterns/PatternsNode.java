package com.billooms.patterns;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Node for Patterns.
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
public class PatternsNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the pattern manager. */
  private final Patterns patMgr;

  /**
   * Create a new PatternsNode for the given PatternMgr.
   *
   * @param patMgr pattern manager
   */
  public PatternsNode(Patterns patMgr) {
    super(Children.create(new PatternsChildFactory(patMgr), true), Lookups.singleton(patMgr));
    this.patMgr = patMgr;
    this.setName("Patterns");
    this.setDisplayName(patMgr.toString());
    this.setIconBaseWithExtension("com/billooms/patterns/Pattern16.png");

    patMgr.addPropertyChangeListener((PropertyChangeListener) this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PatternsNode.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When patterns change, simply update the DisplayName
    this.setDisplayName(patMgr.toString());
  }

}
