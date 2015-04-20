package com.billooms.cutpoints.nodes;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.CutPoint;
import com.billooms.cutpoints.DownAction;
import com.billooms.cutpoints.UpAction;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.CutterEditorInplace;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * This is a Node wrapped around a CutPoint to provide property editing, tree
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
public class CutPointNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the CutPoint. */
  protected final CutPoint cpt;
  /** Local copy of the CutPoint manager. */
  private final CutPoints cptMgr;
  /** Retain a copy so that extensions can modify. */
  protected Sheet sheet;
  /** Retain a copy so that extensions can modify. */
  protected Sheet.Set set;

  /**
   * Create a new CutPointNode for the given CutPoint
   *
   * @param ch Children
   * @param cpt a CutPoint
   * @param cptMgr CutPoint manager
   */
  public CutPointNode(Children ch, CutPoint cpt, CutPoints cptMgr) {
    super(ch, Lookups.fixed(cpt, cptMgr));
    this.cpt = cpt;
    this.cptMgr = cptMgr;
    this.setName("CutPoint");
    this.setDisplayName(cpt.toString());
    this.setIconBaseWithExtension("com/billooms/cutpoints/icons/CutPoint16.png");

    cpt.addPropertyChangeListener((PropertyChangeListener) this);
  }

  /**
   * Initialize a property sheet
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    sheet = Sheet.createDefault();
    set = Sheet.createPropertiesSet();
    set.setDisplayName("CutPoint Properties");
    try {
      Property<Integer> numProp = new PropertySupport.Reflection<>(cpt, int.class, "getNum", null);
      numProp.setName("Number");
      numProp.setShortDescription("CutPoint number");
      set.put(numProp);

      Property<Double> xProp = new PropertySupport.Reflection<>(cpt, double.class, "X");
      xProp.setName("X-location");
      set.put(xProp);

      Property<Double> zProp = new PropertySupport.Reflection<>(cpt, double.class, "Z");
      zProp.setName("Z-location");
      set.put(zProp);

      Property<Boolean> snapProp = new PropertySupport.Reflection<>(cpt, boolean.class, "snap");
      snapProp.setName("Snap");
      snapProp.setShortDescription("Snap the CutPoint to the cutCurve");
      set.put(snapProp);

      Property<Boolean> visibleProp = new PropertySupport.Reflection<>(cpt, boolean.class, "visible");
      visibleProp.setName("Visible");
      visibleProp.setShortDescription("Show or hide the CutPoint");
      set.put(visibleProp);

      PropertySupport.Reflection<Cutter> cutterProp = new PropertySupport.Reflection<>(cpt, Cutter.class, "Cutter");
      cutterProp.setName("Cutter");
      cutterProp.setShortDescription("Cutter for the CutPoint");
      cutterProp.setPropertyEditorClass(CutterEditorInplace.class);
      set.put(cutterProp);

      Property<Double> depthProp = new PropertySupport.Reflection<>(cpt, double.class, "depth");
      depthProp.setName("Cut Depth");
      depthProp.setShortDescription("Depth of the cut");
      set.put(depthProp);

    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    this.setDisplayName(cpt.toString());		// update the display name
  }

  @Override
  public Action[] getActions(boolean context) {
    Action[] defaults = super.getActions(context);	// the default actions includes "Properties"
    int numAdd = 4;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new DeleteAction();
    newActions[1] = new DuplicateAction();
    newActions[2] = new UpAction();
    newActions[3] = new DownAction();
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
    return newActions;
  }

  /** Nested inner class for action deleting a point. */
  private class DeleteAction extends AbstractAction {

    /** Create the DeleteAction. */
    public DeleteAction() {
      putValue(NAME, "Delete");
    }

    /**
     * Remove the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      CutPoint pt = getLookup().lookup(CutPoint.class);
      CutPoints mgr = getLookup().lookup(CutPoints.class);
      mgr.removeCut(pt);
    }
  }

  /** Nested inner class for action copying a point. */
  private class DuplicateAction extends AbstractAction {

    /** Create the DuplicateAction. */
    public DuplicateAction() {
      putValue(NAME, "Duplicate");
    }

    /**
     * Copy the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      CutPoint pt = getLookup().lookup(CutPoint.class);
      CutPoints mgr = getLookup().lookup(CutPoints.class);
      mgr.duplicateCut(pt);
    }
  }

}
