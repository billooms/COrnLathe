package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.clclass.CLclass;
import com.billooms.cutpoints.GoToPoint;
import com.billooms.cutpoints.IndexPoint;
import com.billooms.cutpoints.LinePoint;
import com.billooms.cutpoints.RosettePoint;
import com.billooms.cutpoints.SpiralCut;
import com.billooms.spirals.Spiral;
import com.billooms.spirals.SpiralNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Generate children for the SpiralCut (which will be a Spiral and another
 * CutPoint).
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
public class SpiralCutChildFactory extends ChildFactory.Detachable<CLclass> implements PropertyChangeListener {

  /** Local copy of the SpiralCut. */
  private final SpiralCut spCut;
  /** Local copy of the CutPoints manager. */
  private final CutPoints cptMgr;

  /**
   * Construct a new OutlineChildFactory for the given SpiralCut.
   *
   * @param cPt SpiralCut
   * @param cutPtMgr CutPoint manager
   */
  public SpiralCutChildFactory(SpiralCut cPt, CutPoints cutPtMgr) {
    this.spCut = cPt;
    this.cptMgr = cutPtMgr;
    spCut.addPropertyChangeListener(this);    // listen for changes in the SpiralCut
  }

  @Override
  protected boolean createKeys(List<CLclass> list) {
    list.add(spCut.getBeginPoint());
    list.add(spCut.getSpiral());
    spCut.getAllGoTos().stream().forEach((gpt) -> {
      list.add(gpt);
    });
    return true;
  }

  @Override
  protected Node createNodeForKey(CLclass key) {
    if (key instanceof Spiral) {
      return new SpiralNode((Spiral) key);
    }
    // don't allow editing of cutter for children
    if (key instanceof IndexPoint) {
      return new IndexPointNode((IndexPoint) key, cptMgr, false);
    }
    if (key instanceof RosettePoint) {
      return new RosettePointNode((RosettePoint) key, cptMgr, false);
    }
    if (key instanceof LinePoint) {
      return new LinePointNode((LinePoint) key, cptMgr, false);
    }
    if (key instanceof GoToPoint) {
      return new GoToPointNode((GoToPoint) key, cptMgr, false);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When SpiralCut changes, refresh the child nodes
    refresh(true);
  }

}
