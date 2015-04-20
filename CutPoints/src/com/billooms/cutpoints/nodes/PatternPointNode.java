package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.cutpoints.PatternPoint;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.PatternEditorInplace;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 * This is a Node wrapped around an PatternPoint to provide property editing,
 * tree viewing, etc.
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
public class PatternPointNode extends OffsetCutNode {

  private static final COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  public PatternPointNode(PatternPoint cpt, CutPoints cptMgr) {
    super(Children.LEAF, cpt, cptMgr);
    this.setName("PatternPoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/PatternPt16.png");
  }

  @Override
  protected Sheet createSheet() {
    super.createSheet();
    set.setDisplayName("PatternPoint Properties");
    try {
      PropertySupport.Reflection<CustomPattern> patternProp = new PropertySupport.Reflection<>(cpt, CustomPattern.class, "Pattern");
      patternProp.setName("Pattern");
      patternProp.setShortDescription("Pattern for the rosette");
      patternProp.setPropertyEditorClass(PatternEditorInplace.class);
      set.put(patternProp);

      Property<Integer> patRepeatProp = new PropertySupport.Reflection<>(cpt, int.class, "patternRepeat");
      patRepeatProp.setName("PatternRepeat");
      patRepeatProp.setShortDescription("number of repetitions of pattern");
      set.put(patRepeatProp);

      Property<Double> phaseProp;
      if (prefs.isFracPhase()) {
        phaseProp = new PropertySupport.Reflection<>(cpt, double.class, "ph");
        phaseProp.setShortDescription("Fractional phase (range 0.0 to 1.0)");
      } else {
        phaseProp = new PropertySupport.Reflection<>(cpt, double.class, "phase");
        phaseProp.setShortDescription("Engineering phase (range 0.0 to 360.0)");
      }
      phaseProp.setName("Phase");
      set.put(phaseProp);

      Property<Boolean> optProp = new PropertySupport.Reflection<>(cpt, boolean.class, "optimize");
      optProp.setName("Optimize");
      optProp.setShortDescription("Optimize for curvature of the surface");
      set.put(optProp);
      
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

}
