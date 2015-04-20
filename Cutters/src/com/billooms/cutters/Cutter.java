package com.billooms.cutters;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.drawables.Drawable;
import static com.billooms.drawables.Drawable.LIGHT_DOT;
import com.billooms.drawables.simple.Circle;
import com.billooms.profiles.CustomProfile;
import com.billooms.profiles.Profile;
import com.billooms.profiles.Profiles;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import org.w3c.dom.Element;

/**
 * Cutter for ornamental lathe.
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
public class Cutter extends CLclass {

  /** All CustomProfile property change names start with this prefix */
  public final static String PROP_PREFIX = "Cutter" + "_";
  /** Property name used when changing the name or display name */
  public final static String PROP_NAME = PROP_PREFIX + "Name";
  /** Property name used for changing the cut radius */
  public final static String PROP_RADIUS = PROP_PREFIX + "Radius";
  /** Property name used for changing the cutter location */
  public final static String PROP_LOCATION = PROP_PREFIX + "Location";
  /** Property name used for changing the cutting frame */
  public final static String PROP_FRAME = PROP_PREFIX + "Frame";
  /** Property name used for changing the UCF angle */
  public final static String PROP_UCFANGLE = PROP_PREFIX + "UCFAngle";
  /** Property name used for changing the UCF rotation */
  public final static String PROP_UCFROTATE = PROP_PREFIX + "UCFRotate";
  /** Property name used for changing the cutter profile */
  public final static String PROP_PROFILE = PROP_PREFIX + "Profile";
  /** Property name used for changing the cutter diameter */
  public final static String PROP_DIAMETER = PROP_PREFIX + "Diameter";

  /** Default cutter radius (currently defaults to 0.5) */
  public final static double DEFAULT_RADIUS = 0.5;
  /** Default cutter location (currently set to FRONT_INSIDE) */
  public final static Location DEFAULT_LOCATION = Location.FRONT_INSIDE;
  /** Default cutting frame (currently set to UCF) */
  public final static Frame DEFAULT_FRAME = Frame.UCF;
  /** Default UCF angle (currently set to 0.0) */
  public final static double DEFAULT_UCF_ANGLE = 0.0;
  /** Default UCF rotation (currently set to 0.0) */
  public final static double DEFAULT_UCF_ROTATE = 0.0;
  /** Default cutter profile (currently set to "IDEAL") */
  public final static String DEFAULT_PROFILE = Profiles.DEFAULT_PROFILE;
  /** Default cutter tip width/diameter (currently set to 3/16") */
  public final static double DEFAULT_DIAMETER = 0.1875;

  /** The name of this cutter (upper case letters only). */
  private String name = "";
  /** The display name of this cutter. */
  private String displayName = "";
  /** Radius of the cutter (for UCF, HCF, ECF). */
  private double radius = DEFAULT_RADIUS;
  /** Location of the cutter. */
  private Location location = DEFAULT_LOCATION;
  /** Kind of cutting frame. */
  private Frame frame = DEFAULT_FRAME;
  /** Angle of the UCF relative to the axis of the lathe. */
  private double ucfAngle = DEFAULT_UCF_ANGLE;
  /** Rotation of the UCF on its axis. */
  private double ucfRotate = DEFAULT_UCF_ROTATE;
  /** Profile of the cutter tip. */
  private Profile profile;
  /** Width of the cutter tip (diameter for Drill). */
  private double tipWidth = DEFAULT_DIAMETER;

  /**
   * Construct a new cutter with default values.
   *
   * @param displayName display name
   * @param profMgr profile manager with names of profiles for the cutter
   */
  public Cutter(String displayName, Profiles profMgr) {
    this.displayName = displayName;
    this.name = filterName(displayName);
    profile = profMgr.getDefaultProfile();
  }

  /**
   * Construct a new cutter with information copied from the given cutter.
   *
   * @param copy cutter to copy
   */
  public Cutter(Cutter copy) {
    this.displayName = copy.displayName + "copy";
    this.name = copy.name + "COPY";
    this.radius = copy.radius;
    this.location = copy.location;
    this.frame = copy.frame;
    this.ucfAngle = copy.ucfAngle;
    this.ucfRotate = copy.ucfRotate;
    this.profile = copy.profile;
    this.tipWidth = copy.tipWidth;
  }

  /**
   * Construct a Cutter from the given DOM Element.
   *
   * @param element DOM Element
   * @param profMgr profile manager with names of profiles for the cutter
   */
  public Cutter(Element element, Profiles profMgr) {
    this.displayName = CLUtilities.getString(element, "displayName", "No Name");
    this.name = CLUtilities.getString(element, "name", "NONAME");   // in case it's different
    this.radius = CLUtilities.getDouble(element, "radius", DEFAULT_RADIUS);
    this.location = CLUtilities.getEnum(element, "location", Location.class, DEFAULT_LOCATION);
    this.frame = CLUtilities.getEnum(element, "frame", Frame.class, DEFAULT_FRAME);
    this.ucfAngle = CLUtilities.getDouble(element, "ucfAngle", DEFAULT_UCF_ANGLE);
    this.ucfRotate = CLUtilities.getDouble(element, "ucfRotate", DEFAULT_UCF_ROTATE);
    this.profile = profMgr.getProfile(CLUtilities.getString(element, "profile", DEFAULT_PROFILE));
    this.tipWidth = CLUtilities.getDouble(element, "tipWidth", DEFAULT_DIAMETER);
    if (profile instanceof CustomProfile) {
      ((CustomProfile) profile).addPropertyChangeListener(this);
    }
  }

  @Override
  public String toString() {
    String str = name + " " + getFrame().toString() + " " + location.toString();
    switch (frame) {
      case HCF:
      case UCF:
      case ECF:
        str += " R=" + F3.format(radius);
        break;
      case Drill:
        str += " Dia=" + F3.format(tipWidth);
        break;
    }
    return str;
  }

  /**
   * Clear the Cutter (mainly remove listeners).
   */
  public void clear() {
    if (profile instanceof CustomProfile) {   // quit listening to any old profile
      ((CustomProfile) profile).removePropertyChangeListener(this);
    }
  }

  /**
   * Get the name of the cutter.
   *
   * @return the name of the cutter
   */
  public String getName() {
    return name;
  }

  /**
   * Set a new name (all capital letters).
   *
   * Convert everything to uppercase, delete anything after the first blank, and
   * remove anything that is not an uppercase. This fires a PROP_NAME property
   * change with old an new names.
   *
   * @param newName new name
   */
  public synchronized void setName(String newName) {
    String old = this.name;
    this.name = filterName(newName);
    pcs.firePropertyChange(PROP_NAME, old, this.name);
  }

  /**
   * Get the displayName.
   *
   * @return displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set a new display name.
   *
   * This fires a PROP_NAME property change with old an new names.
   *
   * @param newName new display name
   */
  public synchronized void setDisplayName(String newName) {
    String old = this.displayName;
    this.displayName = newName;
    pcs.firePropertyChange(PROP_NAME, old, this.displayName);
  }

  /**
   * Filter the display name to produce a profile name. Convert everything to
   * uppercase, delete anything after the first blank, and remove anything that
   * is not an uppercase alpha-numeric.
   *
   * @param n display name
   * @return profile name
   */
  protected final String filterName(String n) {
    String str = n.toUpperCase();   // convert to upper case
    if (str.contains(" ")) {	    // only go up to first blank
      str = str.substring(0, str.indexOf(" "));
    }
    for (int i = 0; i < str.length(); i++) {    // look for anything besides uppercase letters
      if (((str.charAt(i) >= 'A') && (str.charAt(i) <= 'Z')) || // keep alpha-numerics
          ((str.charAt(i) >= '0') && (str.charAt(i) <= '9'))) {
      } else {
        str = str.replace(str.charAt(i), '-');
      }
    }
    str = str.replace("-", "");     // and strip them out
    if ("".equals(str)) {
      str = "NEW";
    }
    return str;
  }

  /**
   * Get the radius of the cutter (for HCF, UCF, ECF).
   *
   * @return radius of cut
   */
  public double getRadius() {
    return radius;
  }

  /**
   * Set the the radius of the cutter (for HCF, UCF, ECF). This fires a
   * PROP_RADIUS property change with the old and new values.
   *
   * @param radius new radius
   */
  public synchronized void setRadius(double radius) {
    double old = this.radius;
    this.radius = radius;
    pcs.firePropertyChange(PROP_RADIUS, old, radius);
  }

  /**
   * Get the location of the cutter.
   *
   * @return location of the cutter
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Set the location of the cutter. This fires a PROP_LOCATION property change
   * with the old and new locations.
   *
   * @param location new location of the cutter
   */
  public synchronized void setLocation(Location location) {
    Location old = this.location;
    this.location = location;
    pcs.firePropertyChange(PROP_LOCATION, old, location);
  }

  /**
   * Get the type of cutting frame.
   *
   * @return type of cutting frame
   */
  public Frame getFrame() {
    return frame;
  }

  /**
   * Set the type of cutting frame. This fires a PROP_FRAME property change with
   * the old and new frames.
   *
   * @param frame new type of cutting frame
   */
  public void setFrame(Frame frame) {
    Frame old = this.frame;
    this.frame = frame;
    switch (frame) {
      case HCF:
        setUCFRotate(0.0);	// not used for HCF
        setUCFAngle(0.0);
        break;
      case UCF:
        break;
      case Drill:
        setUCFRotate(0.0);	// not used for Drill
        setRadius(0.0);
        break;
      case ECF:
        setUCFRotate(0.0);	// not used for ECF
        break;
    }
    if (old == Frame.Drill) {	// changing from Drill to something else
      switch (frame) {
        case HCF:
          setRadius(DEFAULT_RADIUS);
          break;
        case UCF:
          setRadius(DEFAULT_RADIUS);
          break;
        case ECF:
          setRadius(DEFAULT_RADIUS);
          break;
      }
    }
    pcs.firePropertyChange(PROP_FRAME, old, frame);
  }

  /**
   * Get the angle of a UCF cutting frame around y-axis. Zero is along Z-axis, +
   * angle is rotate toward front.
   *
   * @return angle of a UCF cutting frame in degrees
   */
  public double getUCFAngle() {
    return ucfAngle;
  }

  /**
   * Set the angle of a UCF cutting frame around y-axis. Zero is along Z-axis, +
   * angle is rotate toward front. This fires a PROP_UCFANGLE property change
   * with the old and new values.
   *
   * @param ang new angle in degrees
   */
  public void setUCFAngle(double ang) {
    double old = this.ucfAngle;
    this.ucfAngle = ang;
    pcs.firePropertyChange(PROP_UCFANGLE, old, ang);
  }

  /**
   * Get the rotation of a UCF cutting frame on it's axis. Zero is horizontal, +
   * angle is CCW looking at the wood.
   *
   * @return rotation of a UCF cutting frame in degrees
   */
  public double getUCFRotate() {
    return ucfRotate;
  }

  /**
   * Set the rotation of a UCF cutting frame on it's axis. Zero is horizontal, +
   * angle is CCW looking at the wood. This fires a PROP_UCFROTATE property
   * change with the old and new values.
   *
   * @param ang new rotation in degrees
   */
  public void setUCFRotate(double ang) {
    double old = this.ucfRotate;
    this.ucfRotate = ang;
    pcs.firePropertyChange(PROP_UCFROTATE, old, ang);
  }

  /**
   * Get the profile of the cutter.
   *
   * @return cutter profile
   */
  public Profile getProfile() {
    return profile;
  }

  /**
   * Set the profile of the cutter. This fires a PROP_PROFILE property change
   * with the old and new profiles.
   *
   * @param p new cutter profile
   */
  public void setProfile(Profile p) {
    if (profile instanceof CustomProfile) {   // quit listening to any old profile
      ((CustomProfile) profile).removePropertyChangeListener(this);
    }
    Profile old = profile;
    this.profile = p;
    if (profile instanceof CustomProfile) {
      ((CustomProfile) profile).addPropertyChangeListener(this);
    }
    this.pcs.firePropertyChange(PROP_PROFILE, old, profile);
  }

  /**
   * Get the width of the cutting tip (diameter for Drill).
   *
   * @return width of cutting tip
   */
  public double getTipWidth() {
    return tipWidth;
  }

  /**
   * Set the width of the cutting tip (diameter for Drill). This fires a
   * PROP_DIAMETER property change with the old and new values.
   *
   * @param tipWidth new width of cutting tip
   */
  public void setTipWidth(double tipWidth) {
    double old = this.tipWidth;
    this.tipWidth = tipWidth;
    pcs.firePropertyChange(PROP_DIAMETER, old, tipWidth);
  }

  /**
   * Get the width of the cut for a given cut depth. For UCF & HCF this is
   * determined by the radius of the cutter. For Drill & ECF this is determined
   * by the profile of the cutter rod.
   *
   * @param d depth of cut
   * @return width of cut
   */
  public double getWidthOfCut(double d) {
    if (d <= 0.0) {
      return 0.0;
    }
    switch (frame) {
      case HCF:
      case UCF:
        if (d >= radius) {
          return 2.0 * radius;
        } else {
          return 2.0 * Math.sqrt(radius * radius - (radius - d) * (radius - d));
        }
      case Drill:
      case ECF:
      default:
        return profile.widthAtDepth(d, tipWidth);
    }
  }

  /**
   * Build a drawable shape for the cutter.
   *
   * @param pos position of the shape in XY space
   * @param c color
   * @return drawable shape
   */
  public Drawable getDrawable(Point2D.Double pos, Color c) {
    Drawable d = null;
    switch (frame) {
      case HCF:
      case UCF:
        d = new Circle(pos, radius, ucfRotate, ucfAngle, c, LIGHT_DOT);
        break;
      case Drill:
      case ECF:
        d = profile.getDrawable(pos, tipWidth, -ucfAngle, c, LIGHT_DOT);
        break;
    }
    return d;
  }

  @Override
  public void writeXML(PrintWriter out) {
    String opt = "";
    switch (frame) {
      case HCF:
        opt = opt + " radius='" + F3.format(radius) + "'";
        break;
      case UCF:
        opt = opt + " radius='" + F3.format(radius) + "'"
            + " ucfAngle='" + F1.format(ucfAngle) + "'"
            + " ucfRotate='" + F1.format(ucfRotate) + "'";
        break;
      case Drill:
        opt = opt + " ucfAngle='" + F1.format(ucfAngle) + "'";
        break;
      case ECF:
        opt = opt + " radius='" + F3.format(radius) + "'"
            + " ucfAngle='" + F1.format(ucfAngle) + "'";
        break;
    }
    out.println(indent + "<Cutter"
        + " name='" + getName() + "'"
        + " displayName='" + getDisplayName() + "'"
        + " frame='" + frame.toString() + "'"
        + " location='" + location.toString() + "'"
        + " profile='" + profile.getName() + "'"
        + " tipWidth='" + F4.format(tipWidth) + "'"
        + opt
        + "/>");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

}
