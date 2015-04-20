package com.billooms.profiles;

import com.billooms.clclass.CLclass;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Profiles is a manager which finds all available cutter profile styles and
 * maintains a list of profiles.
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
public class Profiles extends CLclass {

  /** Default profile */
  public final static String DEFAULT_PROFILE = "IDEAL";

  /** All Profiles property change names start with this prefix. */
  public final static String PROP_PREFIX = "Profiles" + "_";
  /** Property name used when adding a custom profile. */
  public final static String PROP_ADD = PROP_PREFIX + "Add";
  /** Property name used when deleting a custom profile. */
  public final static String PROP_DELETE = PROP_PREFIX + "Delete";

  /** All available profiles. */
  private static ArrayList<Profile> builtIns = null;
  /** All custom profiles. */
  private final ArrayList<CustomProfile> customs = new ArrayList<>();
  /** Default profile */
  private final Profile defaultProfile;

  /**
   * Construct a Profiles manager and find all defined profiles.
   */
  public Profiles() {
    if (builtIns == null) {   // only do this once
      builtIns = new ArrayList<>();
      findBuiltIns();
    }
    defaultProfile = getProfile(DEFAULT_PROFILE);
  }

  /**
   * Construct a Profiles manager, find all defined profiles, and add the
   * CustomProfiles from the given DOM Element.
   *
   * @param element DOM Element
   */
  public Profiles(Element element) {
    this();
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i) instanceof Element) {
        Element profElement = (Element) childNodes.item(i);
        if (profElement.getTagName().equals("CustomProfile")) {
          CustomProfile pat = new CustomProfile(profElement);
          customs.add(pat);
          pat.addPropertyChangeListener(this);
        }
      }
    }
  }

  /**
   * Find all the built-in profiles.
   */
  private static void findBuiltIns() {
    Lookup.Result<Profile> result = Lookup.getDefault().lookupResult(Profile.class);

    // This is helpful for assuring that the profile manager finds user extensions
    System.out.println("Profile Manager: " + result.allInstances().size() + " profiles found");
    result.allInstances().stream().forEach((p) -> {
      System.out.println("  " + p.getDisplayName());
    });

    result.allInstances().stream().forEach((p) -> {
      builtIns.add(p);
    });
  }

  @Override
  public String toString() {
    return "Profiles: " + customs.size() + " customs";
  }

  /**
   * Get a list of all CustomProfiles.
   *
   * @return list of all CustomProfiles
   */
  public ArrayList<CustomProfile> getAllCustom() {
    return customs;
  }

  /**
   * Get a list of names of all profiles.
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
   * Get a list of display names of all profiles.
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
   * Get the profile for the given index starting with built-ins and then
   * customs.
   *
   * @param idx index
   * @return profile (or default if out of range)
   */
  public Profile get(int idx) {
    if (idx < 0 || idx >= (builtIns.size() + customs.size())) {
      return defaultProfile;
    }
    if (idx < builtIns.size()) {
      return builtIns.get(idx);
    } else {
      return customs.get(idx - builtIns.size());
    }
  }

  /**
   * Get the index of the profile with the given name starting with the
   * built-ins and then the customs.
   *
   * @param name profile name
   * @return index (or -1 if not found)
   */
  public int indexOf(String name) {
    for (Profile p : builtIns) {
      if (p.getName().equals(name)) {
        return builtIns.indexOf(p);
      }
    }
    for (Profile p : customs) {
      if (p.getName().equals(name)) {
        return customs.indexOf(p) + builtIns.size();
      }
    }
    return -1;
  }

  /**
   * Get the profile for the given profile name.
   *
   * @param name profile name
   * @return matching profile (or the default if not found)
   */
  public final Profile getProfile(String name) {
    for (Profile p : builtIns) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    for (Profile p : customs) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return defaultProfile;
  }

  /**
   * Get the default Profile (currently set to "IDEAL").
   *
   * @return the default profile
   */
  public Profile getDefaultProfile() {
    return defaultProfile;
  }

  /**
   * Add the given custom profile.
   *
   * Fires a PROP_ADD property change with the new name.
   *
   * @param newProfile new profile
   */
  public synchronized void add(CustomProfile newProfile) {
    customs.add(newProfile);
    newProfile.addPropertyChangeListener(this);
    pcs.firePropertyChange(PROP_ADD, null, newProfile.getName());
  }

  /**
   * Delete the given custom profile.
   *
   * Fires a PROP_DELETE property change with the old name.
   *
   * @param prof profile to delete
   */
  public synchronized void delete(CustomProfile prof) {
    prof.clearAll();	// so the profile quits listening to its line
    customs.remove(prof);
    prof.removePropertyChangeListener(this);
    pcs.firePropertyChange(PROP_DELETE, prof.getName(), null);
  }

  /**
   * Determine if there is a profile of this name.
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
      out.println(indent + "<Profiles>");
      indentMore();
      customs.stream().forEach((pat) -> {
        pat.writeXML(out);    // CustomPatterns can write out their own xml
      });
      indentLess();
      out.println(indent + "</Profiles>");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Profiles.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    if (evt.getPropertyName().equals(CustomProfile.PROP_REQ_DELETE)) {
      if (evt.getSource() instanceof CustomProfile) {
        delete((CustomProfile) evt.getSource());
      }
    } else {
      // pass the info through
      pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

}
