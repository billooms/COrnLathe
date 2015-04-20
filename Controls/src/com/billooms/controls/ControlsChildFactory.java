package com.billooms.controls;

import com.billooms.clclass.CLclass;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Children of Controls are objects with information on active control panels.
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
public class ControlsChildFactory extends ChildFactory.Detachable<CLclass> implements PropertyChangeListener {

  /** Local copy. */
  private final Controls controls;

  public ControlsChildFactory(Controls controls) {
    this.controls = controls;
    controls.addPropertyChangeListener(this);
  }

  @Override
  protected boolean createKeys(List<CLclass> list) {
    list.add(controls.getFeedRate());
    list.add(controls.getActive());
    return true;
  }

  @Override
  protected Node createNodeForKey(CLclass key) {
    if (key instanceof FeedRate) {
      return new FeedRateNode((FeedRate) key);
    }
    if (key instanceof CoarseFine) {
      return new CoarseFineNode((CoarseFine) key);
    }
    if (key instanceof CutCurve) {
      return new CutCurveNode((CutCurve) key);
    }
    if (key instanceof Threads) {
      return new ThreadsNode((Threads) key);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When Controls changes, refresh the child nodes
    refresh(true);
  }

}
