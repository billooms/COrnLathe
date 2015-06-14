package com.billooms.cutpoints.nodes;

import com.billooms.clclass.CLclass;
import com.billooms.rosette.Combine;
import com.billooms.rosette.CompoundRosette;
import com.billooms.rosette.Rosette;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Generate children for the CompoundRosette (which will be some number of Rosettes and Combines).
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
public class CompoundRosetteChildFactory extends ChildFactory.Detachable<CLclass> implements PropertyChangeListener {

  /** Local copy of the CompoundRosette. */
  private final CompoundRosette cRosette;

  /**
   * Construct a new CompoundRosetteChildFactory for the given CompoundRosette.
   *
   * @param cRos CompoundRosette
   */
  public CompoundRosetteChildFactory(CompoundRosette cRos) {
    this.cRosette = cRos;
    cRosette.addPropertyChangeListener(this);    // listen for changes in the CompoundRosette
  }

  @Override
  protected boolean createKeys(List<CLclass> list) {
    for (int i = 0; i < cRosette.size(); i++) {
      list.add(cRosette.getRosette(i));
      if (i < (cRosette.size() - 1)) {
        list.add(cRosette.getCombine(i));
      }
    }
    return true;
  }

  @Override
  protected Node createNodeForKey(CLclass key) {
    if (key instanceof Rosette) {
      return new RosetteNode((Rosette)key);
    }
    if (key instanceof Combine) {
      return new CombineNode((Combine)key);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When SpiralCut changes, refresh the child nodes
    refresh(true);
  }

}
