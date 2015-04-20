package com.billooms.cutters;

import com.billooms.clclass.CLclass;
import com.billooms.profiles.Profiles;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Cutters is a manager which maintains a list of available cutters. Note that
 * the manager keeps track of a profile manager which contains all the possible
 * profiles.
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
public class Cutters extends CLclass {

  /** All Cutters property change names start with this prefix. */
  public final static String PROP_PREFIX = "Cutters" + "_";
  /** Property name used when adding a cutter. */
  public final static String PROP_ADD = PROP_PREFIX + "Add";
  /** Property name used when deleting a cutter. */
  public final static String PROP_DELETE = PROP_PREFIX + "Delete";

  /** Saved copy of the profile manager. */
  private final Profiles profMgr;

  private final ArrayList<Cutter> cutters = new ArrayList<>();

  /**
   * Construct a Cutters manager and add the Cutters from the given DOM Element.
   *
   * @param element DOM Element
   * @param profMgr profile manager with available profiles for the cutters
   */
  public Cutters(Element element, Profiles profMgr) {
    this.profMgr = profMgr;
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i) instanceof Element) {
        Element profElement = (Element) childNodes.item(i);
        if (profElement.getTagName().equals("Cutter")) {
          Cutter cut = new Cutter(profElement, profMgr);
          cutters.add(cut);
          cut.addPropertyChangeListener(this);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "Cutters: " + cutters.size() + " cutters";
  }

  /**
   * Get a list of all Cutters.
   *
   * @return list of all Cutters
   */
  public ArrayList<Cutter> getAllCutters() {
    return cutters;
  }

  /**
   * Get a list of names of all cutters.
   *
   * @return list of names
   */
  public ArrayList<String> getAllNames() {
    ArrayList<String> names = new ArrayList<>();
    cutters.stream().forEach((c) -> {
      names.add(c.getName());
    });
    return names;
  }

  /**
   * Get the cutter for the given index.
   *
   * @param idx index
   * @return cutter (or null if out of range)
   */
  public Cutter get(int idx) {
    if (idx < 0 || idx > (cutters.size() - 1)) {
      return null;
    }
    return cutters.get(idx);
  }

  /**
   * Get the index of the cutter with the given name.
   *
   * @param name cutter name
   * @return index (or -1 if not found)
   */
  public int indexOf(String name) {
    for (Cutter c : cutters) {
      if (c.getName().equals(name)) {
        return cutters.indexOf(c);
      }
    }
    return -1;
  }

  /**
   * Get the cutter for the given cutter name.
   *
   * @param name cutter name
   * @return matching cutter (or the null if not found)
   */
  public final Cutter getCutter(String name) {
    for (Cutter c : cutters) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  /**
   * Get the profile manager that was saved with cutters were first read from
   * the DOM Element.
   *
   * @return profile manager
   */
  protected Profiles getProfileMgr() {
    return profMgr;
  }

  /**
   * Add the given cutter.
   *
   * Fires a PROP_ADD property change with the new name.
   *
   * @param newCutter new cutter
   */
  public synchronized void add(Cutter newCutter) {
    cutters.add(newCutter);
    newCutter.addPropertyChangeListener(this);
    pcs.firePropertyChange(PROP_ADD, null, newCutter.getName());
  }

  /**
   * Delete the given cutter.
   *
   * Fires a PROP_DELETE property change with the old name.
   *
   * @param cut cutter to delete
   */
  public synchronized void delete(Cutter cut) {
    if (cutters.size() == 1) {
      return;	// don't remove the last one -- must always have one
    }
    cut.clear();  // tell the old cutter to quit listening to CustomProfiles
    cutters.remove(cut);
    cut.removePropertyChangeListener(this);
    pcs.firePropertyChange(PROP_DELETE, cut.getName(), null);
  }

  /**
   * Determine if there is a cutter of this name.
   *
   * @param name given name
   * @return true=name exists
   */
  public boolean nameExists(String name) {
    return cutters.stream().anyMatch((c) -> (c.getName().equals(name)));
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<Cutters>");
    indentMore();
    cutters.stream().forEach((pat) -> {
      pat.writeXML(out);    // Cutters can write out their own xml
    });
    indentLess();
    out.println(indent + "</Cutters>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Cutters.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

}
