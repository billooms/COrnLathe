package com.billooms.drawables;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node for Pt which can be XYZ, XY, or XZ style.
 *
 * A DeleteAction is defined for PointNodes which will call the Points
 * requestDelete() method. Any listeners to the Pt will need to take the
 * appropriate action.
 *
 * @author Bill Ooms Copyright 2014 Studio of Bill Ooms. All rights reserved.
 */
public class PtNode extends AbstractNode implements PropertyChangeListener {

  /** Local copy of the point. */
  private final Pt pt;

  /**
   * Create a new PointNode from the given point.
   *
   * @param point given point
   */
  public PtNode(Pt point) {
    super(Children.LEAF, Lookups.singleton(point));
    this.pt = point;
    setName("Point");
    setDisplayName(point.toString());
    setIconBaseWithExtension("com/billooms/drawables/Point16.png");

    pt.addPropertyChangeListener(this);	  // listen for changes in the point
  }

  /**
   * Initialize a property sheet.
   *
   * @return property sheet
   */
  @Override
  protected Sheet createSheet() {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set set = Sheet.createPropertiesSet();
    set.setDisplayName("Point Properties");
    try {
      Property<Double> xProp = new PropertySupport.Reflection<>(pt, double.class, "X");
      xProp.setName("x-location");
      set.put(xProp);

      if (pt.getStyle() == Pt.Style.XY || pt.getStyle() == Pt.Style.XYZ || pt.getStyle() == Pt.Style.XYY) {
        Property<Double> yProp = new PropertySupport.Reflection<>(pt, double.class, "Y");
        yProp.setName("y-location");
        set.put(yProp);
      }
      if (pt.getStyle() == Pt.Style.XZ || pt.getStyle() == Pt.Style.XYZ) {
        Property<Double> yProp = new PropertySupport.Reflection<>(pt, double.class, "Z");
        yProp.setName("z-location");
        set.put(yProp);
      }
      if (pt.getStyle() == Pt.Style.XYY) {
        Property<Double> y2Prop = new PropertySupport.Reflection<>(pt, double.class, "Y2");
        y2Prop.setName("y2-location");
        set.put(y2Prop);
      }
    } catch (NoSuchMethodException ex) {
      ErrorManager.getDefault();
    }
    sheet.put(set);
    return sheet;
  }

  @Override
  public Action[] getActions(boolean context) {
    Action[] defaults = super.getActions(context);
    int numAdd = 1;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new DeleteAction();
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
    return newActions;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PtNode.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When a Pt changes, just update the DisplayName
    setDisplayName(pt.toString());
  }

  /**
   * Nested inner class for action deleting a point.
   */
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
      Pt pt = getLookup().lookup(Pt.class);
      pt.requestDelete();   // request deletion
    }
  }
}
