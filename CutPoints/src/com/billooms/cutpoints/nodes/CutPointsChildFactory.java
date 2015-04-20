package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.SpiralLine;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.CutPoint;
import com.billooms.cutpoints.GoToPoint;
import com.billooms.cutpoints.IndexPoint;
import com.billooms.cutpoints.OffsetGroup;
import com.billooms.cutpoints.PatternPoint;
import com.billooms.cutpoints.PiercePoint;
import com.billooms.cutpoints.RosettePoint;
import com.billooms.cutpoints.SpiralIndex;
import com.billooms.cutpoints.SpiralRosette;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Generate children for the CutPoints manager.
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
public class CutPointsChildFactory extends ChildFactory.Detachable<CutPoint> implements PropertyChangeListener {

  /** Local copy of the CutPoints manager. */
  private final CutPoints cptMgr;

  /**
   * Construct a new CutPointsChildFactory.
   *
   * @param cptMgr given CutPoints manager
   */
  public CutPointsChildFactory(CutPoints cptMgr) {
    this.cptMgr = cptMgr;
    cptMgr.addPropertyChangeListener(this);
  }

  @Override
  protected boolean createKeys(List<CutPoint> list) {
    list.addAll(cptMgr.getAll());
    return true;
  }

  @Override
  protected Node createNodeForKey(CutPoint key) {
    if (key instanceof GoToPoint) {
      return new GoToPointNode((GoToPoint) key, cptMgr);
    } else if (key instanceof IndexPoint) {
      return new IndexPointNode((IndexPoint) key, cptMgr);
    } else if (key instanceof PiercePoint) {
      return new PiercePointNode((PiercePoint) key, cptMgr);
    } else if (key instanceof RosettePoint) {
      return new RosettePointNode((RosettePoint) key, cptMgr);
    } else if (key instanceof SpiralIndex) {
      return new SpiralIndexNode((SpiralIndex) key, cptMgr);
    } else if (key instanceof SpiralRosette) {
      return new SpiralRosetteNode((SpiralRosette) key, cptMgr);
    } else if (key instanceof SpiralLine) {
      return new SpiralLineNode((SpiralLine) key, cptMgr);
    } else if (key instanceof PatternPoint) {
      return new PatternPointNode((PatternPoint) key, cptMgr);
    } else if (key instanceof OffsetGroup) {
      return new OffsetGroupNode((OffsetGroup) key, cptMgr);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CutPointsChildFactory.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    refresh(true);
  }

}
