package com.billooms.patterns;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * ChildFactory to create Nodes and keys for children of Patterns (which would
 * be a bunch of CustomPatterns).
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
public class PatternsChildFactory extends ChildFactory.Detachable<CustomPattern> implements PropertyChangeListener {

  /** Local copy of the pattern manager. */
  private final Patterns patMgr;

  /**
   * Construct a new PatternsChildFactory for the given Patterns.
   *
   * @param patMgr pattern manager
   */
  public PatternsChildFactory(Patterns patMgr) {
    this.patMgr = patMgr;
    patMgr.addPropertyChangeListener(this);  // listen for changes in the Patterns
  }

  @Override
  protected boolean createKeys(List<CustomPattern> list) {
    patMgr.getAllCustom().stream().forEach((p) -> {
      list.add(p);
    });
    return true;
  }

  @Override
  protected Node createNodeForKey(CustomPattern key) {
    return new CustomPatternNode(key);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PatternsChildFactory.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When Patterns change, refresh the child nodes
    refresh(true);
  }
}
