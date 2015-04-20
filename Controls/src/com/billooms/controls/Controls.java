package com.billooms.controls;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Top object for all g-code controls.
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
public class Controls extends CLclass {

  /** All Controls property change names start with this prefix. */
  public final static String PROP_PREFIX = "Controls" + "_";
  /** Property name used when changing the active control panel. */
  public final static String PROP_ACTIVE = PROP_PREFIX + "Active";

  /** Default active control panel. */
  private final static Kind DEFAULT_KIND = Kind.NORMAL;

  /** Various kinds of controls. */
  public enum Kind {

    /** Normal cutting of CutPoints. */
    NORMAL("Normal cuts"),
    /** Following the outline of a shape. */
    OUTLINE("Cut Outline"),
    /** Cutting threads. */
    THREADS("Threads");

    private final String text;

    Kind(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  }

  /** Which kind of control panel is active. */
  private Kind kind = DEFAULT_KIND;
  /** FeedRate information. */
  private FeedRate feedRate = null;
  /** Information for the active panel. */
  private CLclass active = null;

  /**
   * Construct a new Controls object.
   *
   * @param element XML DOM Element
   */
  public Controls(Element element) {
    this.kind = CLUtilities.getEnum(element, "active", Kind.class, DEFAULT_KIND);
    NodeList controlNodes = element.getChildNodes();
    for (int i = 0; i < controlNodes.getLength(); i++) {
      if (controlNodes.item(i) instanceof Element) {
        Element controlElement = (Element) controlNodes.item(i);
        if (controlElement.getTagName().equals("FeedRate")) {
          feedRate = new FeedRate(controlElement);
        }
        if (controlElement.getTagName().equals("CoarseFine")) {
          active = new CoarseFine(controlElement);
        }
        if (controlElement.getTagName().equals("CutCurve")) {
          active = new CutCurve(controlElement);
        }
        if (controlElement.getTagName().equals("Threads")) {
          active = new Threads(controlElement);
        }
      }
    }
    if (feedRate != null) {
      feedRate.addPropertyChangeListener(this);
    }
    if (active != null) {
      active.addPropertyChangeListener(this);
    }
  }

  @Override
  public String toString() {
    return "Controls " + kind.getText();
  }

  /**
   * Get the kind of control panel which is active.
   *
   * @return kind of control panel which is active
   */
  public Kind getKind() {
    return kind;
  }

  /**
   * Set which kind of control panel is active.
   *
   * @param newKind active control panel
   */
  public void setKind(Kind newKind) {
    if (newKind.equals(this.kind)) {
      return;
    }
    if (active != null) {
      active.removePropertyChangeListener(this);  // quit listening to the old one
    }
    Kind old = this.kind;
    this.kind = newKind;
    switch (newKind) {      // Use default values when changing Kind
      case NORMAL:
        active = new CoarseFine();
        break;
      case OUTLINE:
        active = new CutCurve();
        break;
      case THREADS:
        active = new Threads();
        break;
    }
    active.addPropertyChangeListener(this);   // listen to the new one
    pcs.firePropertyChange(PROP_ACTIVE, old, newKind);
  }

  /**
   * Get the feed rate object.
   *
   * @return feed rate object
   */
  public FeedRate getFeedRate() {
    return feedRate;
  }

  /**
   * Get the active information object.
   *
   * @return active information object
   */
  public CLclass getActive() {
    return active;
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<Controls"
        + " active='" + kind.toString() + "'"
        + ">");
    indentMore();
    feedRate.writeXML(out);
    active.writeXML(out);
    indentLess();
    out.println(indent + "</Controls>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    //    System.out.println("Controls.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // We're listening to feedRate and active
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

}
