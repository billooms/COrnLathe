package com.billooms.cornfile;

import com.billooms.clclass.CLclass;
import com.billooms.comment.Comment;
import com.billooms.comment.CommentNode;
import com.billooms.controls.Controls;
import com.billooms.controls.ControlsNode;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.nodes.CutPointsNode;
import com.billooms.cutters.Cutters;
import com.billooms.cutters.CuttersNode;
import com.billooms.outline.Outline;
import com.billooms.outline.OutlineNode;
import com.billooms.patterns.Patterns;
import com.billooms.patterns.PatternsNode;
import com.billooms.profiles.Profiles;
import com.billooms.profiles.ProfilesNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Create children for a COrnDataNode, all of which are top elements from the
 * xml file and are all extensions of CLclass.
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
class COrnChildFactory extends ChildFactory.Detachable<CLclass> implements PropertyChangeListener {

  /** Local copy of the COrnFileDataObject. */
  private final COrnFileDataObject obj;

  /**
   * Construct a new COrnChildFactory.
   *
   * @param obj given COrnFileDataObject
   */
  public COrnChildFactory(COrnFileDataObject obj) {
    this.obj = obj;
    obj.addPropertyChangeListener(this);
  }

  @Override
  protected boolean createKeys(List<CLclass> list) {
    list.addAll(obj.getTopObjects());
    return true;
  }

  @Override
  protected Node createNodeForKey(CLclass key) {
    if (key instanceof Comment) {
      return new CommentNode((Comment) key);
    } else if (key instanceof Controls) {
      return new ControlsNode((Controls) key);
    } else if (key instanceof Patterns) {
      return new PatternsNode((Patterns) key);
    } else if (key instanceof Profiles) {
      return new ProfilesNode((Profiles) key);
    } else if (key instanceof Cutters) {
      return new CuttersNode((Cutters) key);
    } else if (key instanceof Outline) {
      return new OutlineNode((Outline) key);
    } else if (key instanceof CutPoints) {
      return new CutPointsNode((CutPoints) key);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("COrnChildFactory.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    refresh(true);
  }

}
