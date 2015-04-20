package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * DataNode for CutPoints manager.
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
public class CutPointsNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the CutPoints manager. */
  private final CutPoints cptMgr;

  /**
   * Construct a new CutPointsNode for the given CutPoints manager
   *
   * @param cpts CutPoints manager
   */
  public CutPointsNode(CutPoints cpts) {
    super(Children.create(new CutPointsChildFactory(cpts), true), Lookups.singleton(cpts));
    this.cptMgr = cpts;
    this.setDisplayName(cptMgr.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/CutPoint16.png");

    cptMgr.addPropertyChangeListener((PropertyChangeListener) this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CutPointsNode.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When CutPoints change, simply update the DisplayName
    this.setDisplayName(cptMgr.toString());
  }
}
