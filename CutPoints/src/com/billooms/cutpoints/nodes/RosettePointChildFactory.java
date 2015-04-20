package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.RosettePoint;
import com.billooms.cutpoints.RosettePoint.Motion;
import com.billooms.rosette.Rosette;
import com.billooms.rosette.RosetteNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Generate children for the RosettePoint (which will be one or two rosettes).
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
public class RosettePointChildFactory extends ChildFactory.Detachable<Rosette> implements PropertyChangeListener {

  /** Local copy of the RosettePoint. */
  private final RosettePoint rpt;

  /**
   * Construct a new OutlineChildFactory for the given RosettePoint.
   *
   * @param pt RosettePoint
   */
  public RosettePointChildFactory(RosettePoint pt) {
    this.rpt = pt;
    rpt.addPropertyChangeListener(this);    // listen for changes in the RosettePoint
  }

  @Override
  protected boolean createKeys(List<Rosette> list) {
    list.add(rpt.getRosette());
    if (rpt.getMotion().equals(Motion.BOTH)) {
      list.add(rpt.getRosette2());
    }
    return true;
  }

  @Override
  protected Node createNodeForKey(Rosette key) {
    return new RosetteNode(key);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When RosettePoint changes, refresh the child nodes
    refresh(true);
  }

}
