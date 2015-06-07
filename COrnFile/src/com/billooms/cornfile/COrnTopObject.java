package com.billooms.cornfile;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import static com.billooms.clclass.CLclass.indent;
import com.billooms.comment.Comment;
import com.billooms.controls.Controls;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutters.Cutters;
import com.billooms.outline.Outline;
import com.billooms.patterns.Patterns;
import com.billooms.profiles.Profiles;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Top object for the data structure.
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
public final class COrnTopObject extends CLclass {

//  /** List of all the top CLclass objects. */
//  private final static Class[] TOP_CLASSES = new Class[]{Comment.class, Patterns.class, Profiles.class, Cutters.class, Outline.class};
//  /** Indicates if the class is required in the xml file. */
//  private final static boolean[] REQUIRED = new boolean[]{false, false, false, true, true};
  /** A list of all top objects from the XML file. */
  private final ArrayList<CLclass> topObjects = new ArrayList<>();
  private final double fileVersion;	  // Note: this isn't used.

  /**
   * Construct the COrnTopObject from an XML DOM Element.
   *
   * @param root XML DOM Element
   */
  public COrnTopObject(Element root) {
    this.fileVersion = CLUtilities.getDouble(root, "version", 99.0);	  // large number for default

    // Read Comment first
    NodeList comList = root.getElementsByTagName("Comment");	// find the <Comment> element
    if (comList.getLength() == 0) {      // if none in xml file, use a default Comment
      topObjects.add(new Comment());
    } else {
      topObjects.add(new Comment((Element) comList.item(0)));
    }

    // Read Controls next
    NodeList contList = root.getElementsByTagName("Controls");	// find the <Controls> element
    topObjects.add(new Controls((Element) contList.item(0)));   // if there is none, let it throw an error

    // Read Patterns before CutPoints
    NodeList patList = root.getElementsByTagName("Patterns");	// find the <Patterns> element
    if (patList.getLength() == 0) {      // if none in xml file, use a default Patterns manager for built-ins
      topObjects.add(new Patterns());
    } else {
      topObjects.add(new Patterns((Element) patList.item(0)));
    }

    // Read Profiles before Cutters
    NodeList profList = root.getElementsByTagName("Profiles");	// find the <Profiles> element
    if (profList.getLength() == 0) {      // if none in xml file, use a default Profiles manager for built-ins
      topObjects.add(new Profiles());
    } else {
      topObjects.add(new Profiles((Element) profList.item(0)));
    }

    NodeList topNodes = root.getChildNodes();
    for (int i = 0; i < topNodes.getLength(); i++) {
      if (topNodes.item(i) instanceof Element) {
        Element topElement = (Element) topNodes.item(i);
        switch (topElement.getNodeName()) {
//	  case "Comment":
//	    topObjects.add(new Comment(topElement));
//	    break;
//          case "Controls":
//            topObjects.add(new Controls(topElement));
//            break;
//	  case "Patterns":
//	    topObjects.add(new Patterns(topElement));
//	    break;
//	  case "Profiles":
//	    topObjects.add(new Profiles(topElement));
//	    break;
          case "Cutters":
            topObjects.add(new Cutters(topElement, (Profiles) getClass(Profiles.class)));
            break;
          case "Outline":
            topObjects.add(new Outline(topElement, (Cutters) getClass(Cutters.class)));
            break;
        }
      }
    }

    // Read CutPoints last
    NodeList cpList = root.getElementsByTagName("CutPoints");	// find the <CutPoints> element
    if (cpList.getLength() == 0) {      // if none in xml file, use a default CutPoints manager so you can add some
      topObjects.add(new CutPoints((Cutters) getClass(Cutters.class), (Outline) getClass(Outline.class), (Patterns) getClass(Patterns.class)));
    } else {
      topObjects.add(new CutPoints((Element) cpList.item(0), (Cutters) getClass(Cutters.class), (Outline) getClass(Outline.class), (Patterns) getClass(Patterns.class)));
    }
  }

  /**
   * Get the list of all top objects.
   *
   * @return list of top objects
   */
  public ArrayList<CLclass> getTopObjects() {
    return topObjects;
  }

  /**
   * Get the top CLclass object that matches the given class.
   *
   * @param <T> extends CLclass
   * @param clazz some CLclass
   * @return CLclass object that matches the given class
   */
  public <T extends CLclass> CLclass getClass(Class<T> clazz) {
    for (CLclass c : topObjects) {
      if (c.getClass().equals(clazz)) {
        return c;
      }
    }
    return null;
  }

  @Override
  public void writeXML(PrintWriter out) {
    indent = "";    // reset it just to make sure
    double thisVersion = Double.parseDouble(NbBundle.getMessage(COrnTopObject.class, "COrnLathe_Version"));
    out.println("<?xml version=\"1.0\"?>");
    out.println("<!DOCTYPE COrnLathe PUBLIC \"-//IDN billooms.com//DTD COrnLathe file format 3.1//EN\" \"http://www.billooms.com/dtds/cornlathe3_1.dtd\">");
    out.println("<COrnLathe"
        + " version='" + F1.format(thisVersion) + "'"
        + ">");
    indentMore();
    topObjects.stream().forEach((item) -> {
      item.writeXML(out);
    });
    indentLess();
    out.println("</COrnLathe>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    throw new UnsupportedOperationException("COrnTopObject.propertyChange: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
