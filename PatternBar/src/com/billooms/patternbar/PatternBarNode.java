package com.billooms.patternbar;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.PatternEditorInplace;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * This is a Node wrapped around a PatternBar to provide property editing, tree
 * viewing, etc.
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
public class PatternBarNode extends AbstractNode implements PropertyChangeListener {

  private static final COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  private final PatternBar pBar;
  private Sheet.Set set;
  private Property<Double> amp2Prop;
  private Property<Integer> n2Prop;

  /**
   * Create a new PatternBarNode for the given PatternBar
   *
   * @param pb a PatternBar
   */
  public PatternBarNode(PatternBar pb) {
    super(Children.LEAF, Lookups.singleton(pb));	// there will be no children
    this.pBar = pb;
    this.setName("PatternBar");
    this.setDisplayName(pb.toString());
    this.setIconBaseWithExtension("com/billooms/patternbar/PatternBar16.png");

    pb.addPropertyChangeListener((PropertyChangeListener) this);
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    set = Sheet.createPropertiesSet();
    set.setDisplayName("PatternBar Properties");
    try {
      PropertySupport.Reflection<Pattern> patternProp = new PropertySupport.Reflection<>(pBar, Pattern.class, "Pattern");
      patternProp.setName("Pattern");
      patternProp.setShortDescription("Pattern for the patternBar");
      patternProp.setPropertyEditorClass(PatternEditorInplace.class);
      set.put(patternProp);

      Property<Double> ampProp = new PropertySupport.Reflection<>(pBar, double.class, "pToP");
      ampProp.setName("Amplitude");
      ampProp.setShortDescription("Peak-to-peak amplitude");
      set.put(ampProp);

      Property<Double> perProp = new PropertySupport.Reflection<>(pBar, double.class, "period");
      perProp.setName("Period");
      perProp.setShortDescription("Period distance");
      set.put(perProp);

      Property<Double> phaseProp;
      if (prefs.isFracPhase()) {
        phaseProp = new PropertySupport.Reflection<>(pBar, double.class, "ph");
        phaseProp.setShortDescription("Fractional phase (range 0.0 to 1.0)");
      } else {
        phaseProp = new PropertySupport.Reflection<>(pBar, double.class, "phase");
        phaseProp.setShortDescription("Engineering phase (range 0.0 to 360.0)");
      }
      phaseProp.setName("Phase");
      set.put(phaseProp);

      Property<Boolean> invertProp = new PropertySupport.Reflection<>(pBar, boolean.class, "invert");
      invertProp.setName("Invert");
      invertProp.setShortDescription("invert the pattern");
      set.put(invertProp);

      n2Prop = new PropertySupport.Reflection<>(pBar, int.class, "n2");
      n2Prop.setName("N2");
      n2Prop.setShortDescription("Optional 2nd integer parameter");
      if (pBar.getPattern().needsN2()) {
        set.put(n2Prop);
      }

      amp2Prop = new PropertySupport.Reflection<>(pBar, double.class, "amp2");
      amp2Prop.setName("Amplitude2");
      amp2Prop.setShortDescription("Optional 2nd amplitude parameter");
      if (pBar.getPattern().needsAmp2()) {
        set.put(amp2Prop);
      }

    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setDisplayName(pBar.toString());		// update the display name
    if (pBar.getPattern().needsN2()) {
      if (set.get("n2") == null) {
        set.put(n2Prop);
      }
    } else {
      set.remove("n2");
    }
    if (pBar.getPattern().needsAmp2()) {
      if (set.get("amp2") == null) {
        set.put(amp2Prop);
      }
    } else {
      set.remove("amplitude2");
    }
  }
}
