package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.LinePoint;
import com.billooms.patternbar.PatternBar;
import com.billooms.patternbar.PatternBarNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Generate children for the LinePoint (which will be one a PatternBar).
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
public class LinePointChildFactory extends ChildFactory.Detachable<PatternBar> implements PropertyChangeListener {

  /** Local copy of the LinePoint. */
  private final LinePoint lpt;

  /**
   * Construct a new OutlineChildFactory for the given LinePoint.
   *
   * @param pt LinePoint
   */
  public LinePointChildFactory(LinePoint pt) {
    this.lpt = pt;
    lpt.addPropertyChangeListener(this);    // listen for changes in the LinePoint
  }

  @Override
  protected boolean createKeys(List<PatternBar> list) {
    list.add(lpt.getPatternBar());
    return true;
  }

  @Override
  protected Node createNodeForKey(PatternBar key) {
    return new PatternBarNode(key);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When LinePoint changes, refresh the child nodes
    refresh(true);
  }

}
