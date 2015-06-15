package com.billooms.rosette;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.PatternEditorInplace;
import com.billooms.rosette.Rosette.Mask;
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
 * This is a Node wrapped around a Rosette to provide property editing, tree
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
public class RosetteNode extends AbstractNode implements PropertyChangeListener {

  private static final COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  protected final Rosette ros;
  private Sheet.Set set;
  private Property<Integer> n2Prop;
  private Property<Double> amp2Prop;

  /**
   * Create a new RosetteNode for the given Rosette
   *
   * @param ros a Rosette
   */
  public RosetteNode(Rosette ros) {
    super(Children.LEAF, Lookups.singleton(ros));	// there will be no children
    this.ros = ros;
    this.setName("Rosette");
    this.setDisplayName(ros.toString());
    this.setIconBaseWithExtension("com/billooms/rosette/Rosette16.png");

    ros.addPropertyChangeListener(this);
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
    set.setDisplayName("Rosette Properties");
    try {
      PropertySupport.Reflection<Pattern> patternProp = new PropertySupport.Reflection<>(ros, Pattern.class, "Pattern");
      patternProp.setName("Pattern");
      patternProp.setShortDescription("Pattern for the rosette");
      patternProp.setPropertyEditorClass(PatternEditorInplace.class);
      set.put(patternProp);

      Property<Double> ampProp = new PropertySupport.Reflection<>(ros, double.class, "pToP");
      ampProp.setName("Amplitude");
      ampProp.setShortDescription("peak-to-peak amplitude");
      set.put(ampProp);

      Property<Integer> repeatProp = new PropertySupport.Reflection<>(ros, int.class, "repeat");
      repeatProp.setName("Repeat");
      repeatProp.setShortDescription("number of repetitions of pattern");
      set.put(repeatProp);

      Property<Double> phaseProp;
      if (prefs.isFracPhase()) {
        phaseProp = new PropertySupport.Reflection<>(ros, double.class, "ph");
        phaseProp.setShortDescription("Fractional phase (range 0.0 to 1.0)");
      } else {
        phaseProp = new PropertySupport.Reflection<>(ros, double.class, "phase");
        phaseProp.setShortDescription("Engineering phase (range 0.0 to 360.0)");
      }
      phaseProp.setName("Phase");
      set.put(phaseProp);

      Property<Boolean> invertProp = new PropertySupport.Reflection<>(ros, boolean.class, "invert");
      invertProp.setName("Invert");
      invertProp.setShortDescription("invert the pattern");
      set.put(invertProp);

      Property<String> maskProp = new PropertySupport.Reflection<>(ros, String.class, "mask");
      maskProp.setName("Mask");
      maskProp.setShortDescription("Mask to select only certain repeat positions. 0 means skip, any other character means use.");
      set.put(maskProp);

      Property<Mask> maskHiLo = new PropertySupport.Reflection<>(ros, Mask.class, "maskHiLo");
      maskHiLo.setName("MaskHiLo");
      maskHiLo.setShortDescription("Flag indicating if masking causes rosette to be the highest or the lowest in the pattern.");
      set.put(maskHiLo);

      Property<Double> maskPhaseProp;
      if (prefs.isFracPhase()) {
        maskPhaseProp = new PropertySupport.Reflection<>(ros, double.class, "maskPh");
        maskPhaseProp.setShortDescription("Fractional mask phase (range 0.0 to 1.0)");
      } else {
        maskPhaseProp = new PropertySupport.Reflection<>(ros, double.class, "maskPhase");
        maskPhaseProp.setShortDescription("Engineering mask phase (range 0.0 to 360.0)");
      }
      maskPhaseProp.setName("MaskPhase");
      set.put(maskPhaseProp);

      n2Prop = new PropertySupport.Reflection<>(ros, int.class, "n2");
      n2Prop.setName("N2");
      n2Prop.setShortDescription("Optional 2nd integer parameter");
      if (ros.getPattern().needsN2()) {
        set.put(n2Prop);
      }

      amp2Prop = new PropertySupport.Reflection<>(ros, double.class, "amp2");
      amp2Prop.setName("Amplitude2");
      amp2Prop.setShortDescription("Optional 2nd amplitude parameter");
      if (ros.getPattern().needsAmp2()) {
        set.put(amp2Prop);
      }

      PropertySupport.Reflection<String> symAmp = new PropertySupport.Reflection<>(ros, String.class, "SymAmpStr");
      symAmp.setName("SymmetryAmplitudes");
      symAmp.setShortDescription("Optional Symmetry amplitudes separated by commas");
      set.put(symAmp);

      PropertySupport.Reflection<String> symWid = new PropertySupport.Reflection<>(ros, String.class, "SymWidStr");
      symWid.setName("SymmetryWidths");
      symWid.setShortDescription("Optional Symmetry widths separated by commas");
      set.put(symWid);

    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setDisplayName(ros.toString());		// update the display name
    if (ros.getPattern().needsN2()) {
      if (set.get("N2") == null) {
        set.put(n2Prop);
      }
    } else {
      if (set != null) {
        set.remove("N2");
      }
    }
    if (ros.getPattern().needsAmp2()) {
      if (set.get("Amplitude2") == null) {
        set.put(amp2Prop);
      }
    } else {
      if (set != null) {
        set.remove("Amplitude2");
      }
    }
  }
}
