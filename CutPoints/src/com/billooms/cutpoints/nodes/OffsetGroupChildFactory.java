package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.CutPoint;
import com.billooms.cutpoints.OffRosettePoint;
import com.billooms.cutpoints.OffsetGroup;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Generate children for the OffsetGroup (which will be one or more OffPoints).
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
public class OffsetGroupChildFactory extends ChildFactory.Detachable<CutPoint> implements PropertyChangeListener {

  /** Local copy of the OffsetGroup. */
  private final OffsetGroup offGrp;
  /** Local copy of the CutPoints manager. */
  private final CutPoints cptMgr;

  /**
   * Construct a new OffsetGroupChildFactory for the given OffsetGroup.
   *
   * @param oGrp OffsetGroup
   * @param cptMgr CutPoint manager
   */
  public OffsetGroupChildFactory(OffsetGroup oGrp, CutPoints cptMgr) {
    this.offGrp = oGrp;
    this.cptMgr = cptMgr;
    offGrp.addPropertyChangeListener(this);    // listen for changes in the RosettePoint
  }

  @Override
  protected boolean createKeys(List<CutPoint> list) {
    list.addAll(offGrp.getAllCutPoints());
    return true;
  }

  @Override
  protected Node createNodeForKey(CutPoint key) {
    if (key instanceof OffRosettePoint) {
      return new OffRosettePointNode((OffRosettePoint)key, cptMgr);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When OffsetGroup changes, refresh the child nodes
    refresh(true);
  }

}
