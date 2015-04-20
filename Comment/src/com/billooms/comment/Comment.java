package com.billooms.comment;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Comment at the beginning of an XML file.
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
public class Comment extends CLclass {

  /** All Comment property change names start with this prefix */
  public final static String PROP_PREFIX = "Comment" + "_";
  /** Property name used for changing the text */
  public final static String PROP_TEXT = PROP_PREFIX + "Text";

  private String text = "";

  /**
   * Create a new Comment with an empty string.
   */
  public Comment() {
    this.text = "";
  }

  /**
   * Define a new Comment from DOM Element.
   *
   * @param element DOM Element
   */
  public Comment(Element element) {
    this.text = CLUtilities.getString(element, "text", "");
  }

  @Override
  public String toString() {
    return text;
  }

  /**
   * Get the text of the comment.
   *
   * @return text
   */
  public String getText() {
    return text;
  }

  /**
   * Set the text of the comment.
   *
   * This fires a PROP_TEXT PropertyChange with the old and new values.
   *
   * @param str new text
   */
  public void setText(String str) {
    String old = this.text;
    if (str == null) {
      this.text = "";
    } else {
      this.text = str.replace("'", "");  // no single quotes are allowed
    }
    this.pcs.firePropertyChange(PROP_TEXT, old, text);
  }

  /**
   * Determine if the comment is empty.
   *
   * @return true: empty comment
   */
  public boolean isEmpty() {
    return text.isEmpty();
  }

  @Override
  public void writeXML(PrintWriter out) {
    if (!isEmpty()) {
      out.println(indent + "<Comment text='" + text + "'/>");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("Comment.propertyChange Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
