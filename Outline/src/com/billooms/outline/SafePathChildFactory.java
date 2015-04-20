package com.billooms.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * ChildFactory to create Nodes and keys for children of SafePath (which would
 * be a bunch of Points).
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
public class SafePathChildFactory extends ChildFactory.Detachable<SafePt> implements PropertyChangeListener {

  /** Local copy of the SafePath. */
  private final SafePath safePath;

  /**
   * Construct a new SafePathChildFactory for the given SafePath.
   *
   * @param safePath given SafePath
   */
  public SafePathChildFactory(SafePath safePath) {
    this.safePath = safePath;
    safePath.addPropertyChangeListener(this);  // listen for changes in the SafePath
  }

  @Override
  protected boolean createKeys(List<SafePt> list) {
    safePath.getPoints().stream().forEach((p) -> {
      list.add((SafePt) p);	// Assumes all Points are SafePt
    });
    return true;
  }

  @Override
  protected Node createNodeForKey(SafePt key) {
    return new SafePtNode(key);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When SafePath changes, refresh the child nodes
    refresh(true);
  }

}
