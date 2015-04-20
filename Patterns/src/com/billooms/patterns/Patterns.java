package com.billooms.patterns;

import com.billooms.clclass.CLclass;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Patterns is a manager which finds all available rosette patterns and
 * maintains a list of those patterns.
 *
 * Note: No check is made for duplicates.
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
public class Patterns extends CLclass {

  /** Default pattern. */
  public final static String DEFAULT_PROFILE = "SINE";

  /** All Patterns property change names start with this prefix. */
  public final static String PROP_PREFIX = "Patterns" + "_";
  /** Property name used when adding a custom pattern. */
  public final static String PROP_ADD = PROP_PREFIX + "Add";
  /** Property name used when deleting a custom pattern. */
  public final static String PROP_DELETE = PROP_PREFIX + "Delete";

  /** All available patterns. */
  private static ArrayList<Pattern> builtIns = null;
  /** All custom patterns. */
  private final ArrayList<CustomPattern> customs = new ArrayList<>();
  /** Default pattern */
  private final Pattern defaultPattern;

  /**
   * Construct a Patterns manager and find all defined patterns.
   */
  public Patterns() {
    if (builtIns == null) {   // only do this once
      builtIns = new ArrayList<>();
      findBuiltIns();
    }
    defaultPattern = getPattern(DEFAULT_PROFILE);
  }

  /**
   * Construct a Patterns manager, find all defined patterns, and add the
   * CustomPatterns from the given DOM Element.
   *
   * @param element DOM Element
   */
  public Patterns(Element element) {
    this();
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i) instanceof Element) {
        Element profElement = (Element) childNodes.item(i);
        if (profElement.getTagName().equals("CustomPattern")) {
          CustomPattern pat = new CustomPattern(profElement);
          customs.add(pat);
          pat.addPropertyChangeListener(this);
        }
      }
    }
  }

  /**
   * Find all the built-in patterns.
   */
  private static void findBuiltIns() {
    Lookup.Result<Pattern> result = Lookup.getDefault().lookupResult(Pattern.class);

    // This is helpful for assuring that the pattern manager finds user extensions
    System.out.println("Pattern Manager: " + result.allInstances().size() + " patterns found");
    result.allInstances().stream().forEach((p) -> {
      System.out.println("  " + p.getDisplayName());
    });

    result.allInstances().stream().forEach((p) -> {
      builtIns.add(p);
    });
  }

  @Override
  public String toString() {
    return "Patterns: " + customs.size() + " customs";
  }

  /**
   * Get a list of all CustomPatterns.
   *
   * @return list of all CustomPatterns
   */
  public ArrayList<CustomPattern> getAllCustom() {
    return customs;
  }

  /**
   * Get a list of names of all patterns.
   *
   * @return list of names
   */
  public ArrayList<String> getAllNames() {
    ArrayList<String> names = new ArrayList<>();
    builtIns.stream().forEach((p) -> {
      names.add(p.getName());
    });
    customs.stream().forEach((p) -> {
      names.add(p.getName());
    });
    return names;
  }

  /**
   * Get a list of display names of all patterns.
   *
   * @return list of names
   */
  public ArrayList<String> getAllDisplayNames() {
    ArrayList<String> names = new ArrayList<>();
    builtIns.stream().forEach((p) -> {
      names.add(p.getDisplayName());
    });
    customs.stream().forEach((p) -> {
      names.add(p.getDisplayName());
    });
    return names;
  }

  /**
   * Get the pattern for the given index starting with built-ins and then
   * customs.
   *
   * @param idx index
   * @return pattern (or default if out of range)
   */
  public Pattern get(int idx) {
    if (idx < 0 || idx >= (builtIns.size() + customs.size())) {
      return defaultPattern;
    }
    if (idx < builtIns.size()) {
      return builtIns.get(idx);
    } else {
      return customs.get(idx - builtIns.size());
    }
  }

  /**
   * Get the index of the pattern with the given name starting with the
   * built-ins and then the customs.
   *
   * @param name pattern name
   * @return index (or -1 if not found)
   */
  public int indexOf(String name) {
    for (Pattern p : builtIns) {
      if (p.getName().equals(name)) {
        return builtIns.indexOf(p);
      }
    }
    for (Pattern p : customs) {
      if (p.getName().equals(name)) {
        return customs.indexOf(p) + builtIns.size();
      }
    }
    return -1;
  }

  /**
   * Get the pattern for the given pattern name.
   *
   * @param name pattern name
   * @return matching pattern (or the default if not found)
   */
  public final Pattern getPattern(String name) {
    for (Pattern p : builtIns) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    for (Pattern p : customs) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return defaultPattern;
  }

  /**
   * Get the default Pattern (currently set to "IDEAL").
   *
   * @return the default pattern
   */
  public Pattern getDefaultPattern() {
    return defaultPattern;
  }

  /**
   * Add the given custom pattern.
   *
   * Fires a PROP_ADD property change with the new name.
   *
   * @param newPattern new pattern
   */
  public synchronized void add(CustomPattern newPattern) {
    customs.add(newPattern);
    newPattern.addPropertyChangeListener(this);
    pcs.firePropertyChange(PROP_ADD, null, newPattern.getName());
  }

  /**
   * Delete the given custom pattern.
   *
   * Fires a PROP_DELETE property change with the old name.
   *
   * @param prof pattern to delete
   */
  public synchronized void delete(CustomPattern prof) {
    prof.clearAll();	// so the pattern quits listening to its line
    customs.remove(prof);
    prof.removePropertyChangeListener(this);
    pcs.firePropertyChange(PROP_DELETE, prof.getName(), null);
  }

  /**
   * Determine if there is a pattern of this name.
   *
   * @param name given name
   * @return true=name exists
   */
  public boolean nameExists(String name) {
    if (builtIns.stream().anyMatch((p) -> (p.getName().equals(name)))) {
      return true;
    }
    return customs.stream().anyMatch((p) -> (p.getName().equals(name)));
  }

  @Override
  public void writeXML(PrintWriter out) {
    if (!customs.isEmpty()) {
      out.println(indent + "<Patterns>");
      indentMore();
      customs.stream().forEach((pat) -> {
        pat.writeXML(out);    // CustomPatterns can write out their own xml
      });
      indentLess();
      out.println(indent + "</Patterns>");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Patterns.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    if (evt.getPropertyName().equals(CustomPattern.PROP_REQ_DELETE)) {
      if (evt.getSource() instanceof CustomPattern) {
        delete((CustomPattern) evt.getSource());
      }
    } else {
      // pass the info through
      pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

}
