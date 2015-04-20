package com.billooms.comment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * Node for the Comment.
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
public class CommentNode extends AbstractNode implements PropertyChangeListener {

  private final Comment comment;

  /**
   * Create a new PointNode from the given point.
   *
   * @param com Comment
   */
  public CommentNode(Comment com) {
    super(Children.LEAF);
    this.comment = com;
    setName("Comment");
    setDisplayName(com.toString());
    setIconBaseWithExtension("com/billooms/comment/Comment16.png");

    com.addPropertyChangeListener((PropertyChangeListener) this);	  // listen for changes in the point
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Comment Properties");
    try {
      Property<String> textProp = new PropertySupport.Reflection<>(comment, String.class, "text");
      textProp.setName("Text");
      textProp.setShortDescription("Text to be added to the xml file");
      set.put(textProp);
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // When a Comment changes, just update the DisplayName
    setDisplayName(comment.toString());
  }

}
