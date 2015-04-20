package com.billooms.gcodeoutput;

import com.billooms.controls.CutCurve;
import com.billooms.controls.CutCurve.Direction;
import com.billooms.cutlist.CutList;
import static com.billooms.cutlist.Speed.*;
import com.billooms.cutters.Cutter;
import com.billooms.drawables.Pt;
import com.billooms.drawables.simple.Curve;
import com.billooms.outline.Outline;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Panel for data controlling cutting the curve.
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
public class CutCurvePanel extends JPanel implements PropertyChangeListener {

  private final static DecimalFormat F3 = new DecimalFormat("0.000");
  private final static Color ENABLED_COLOR = new Color(153, 153, 153);
  private final static Color DISABLED_COLOR = new Color(204, 204, 204);

  /** CutCurve object holds all the information. */
  private CutCurve cutCurve = null;

  /** Creates new form CutCurvePanel. */
  public CutCurvePanel() {
    initComponents();

    stepSlider.setLabelTable(new SliderLabels(0, 100, 50, 0.0, CutCurve.MAX_STEP / 2.0));
    directionCombo.removeAllItems();
    for (Direction d : Direction.values()) {
      directionCombo.addItem(d.getText());
    }

    updateAll();
  }

  /**
   * Associate a new CoarseFine object with this control panel.
   *
   * @param newCutCurve new CutCurve object
   */
  public void setCutCurve(CutCurve newCutCurve) {
    if (cutCurve == newCutCurve) {
      return;
    }
    if (cutCurve != null) {
      cutCurve.removePropertyChangeListener(this);  // quit listening to the old one.
    }
    this.cutCurve = newCutCurve;
    if (cutCurve != null) {
      setEnabled(true);
      updateAll();
      cutCurve.addPropertyChangeListener(this);     // listen for changes in the new one.
    } else {
      setEnabled(false);
    }
  }

  /**
   * Enable (or disable) input fields.
   *
   * @param en true=enable; false=disable
   */
  @Override
  public void setEnabled(boolean en) {
    stepField.setEnabled(en);
    stepSlider.setEnabled(en);
    directionCombo.setEnabled(en);
    deepField1.setEnabled(en);
    deepField2.setEnabled(en);
    backField.setEnabled(en);
    countSpinner1.setEnabled(en);
    countSpinner2.setEnabled(en);
    if (en) {
      this.setBackground(ENABLED_COLOR);
      stepSizePanel.setBackground(ENABLED_COLOR);
    } else {
      this.setBackground(DISABLED_COLOR);
      stepSizePanel.setBackground(DISABLED_COLOR);
    }
  }

  /**
   * Update all display fields with current data.
   */
  private void updateAll() {
    stepField.setValue(getStep());
    stepSlider.setValue((int) (100.0 * getStep() / CutCurve.MAX_STEP));
    directionCombo.setSelectedIndex(getDirection().ordinal());
    backField.setValue(getBackoff());
    countSpinner1.setValue(getCount1());
    countSpinner2.setValue(getCount2());
    deepField1.setValue(getDepth1());
    deepField2.setValue(getDepth2());
    double total = getCount1() * getDepth1() + getCount2() * getDepth2() - getBackoff();
    totalLabel.setText((getCount1() + getCount2()) + " passes, total = " + F3.format(total));
  }

  /**
   * Get the step size.
   *
   * @return step size
   */
  public double getStep() {
    if (cutCurve != null) {
      return cutCurve.getStep();
    } else {
      return CutCurve.DEFAULT_STEP;
    }
  }

  /**
   * Get the back-off amount (amount to back away from curve prior to cutting).
   *
   * @return back-off amount
   */
  public double getBackoff() {
    if (cutCurve != null) {
      return cutCurve.getBackoff();
    } else {
      return CutCurve.DEFAULT_BACKOFF;
    }
  }

  /**
   * Get number of rough passes.
   *
   * @return number of rough passes
   */
  public int getCount1() {
    if (cutCurve != null) {
      return cutCurve.getCount1();
    } else {
      return CutCurve.DEFAULT_COUNT1;
    }
  }

  /**
   * Get number of fine passes.
   *
   * @return number of fine passes
   */
  public int getCount2() {
    if (cutCurve != null) {
      return cutCurve.getCount2();
    } else {
      return CutCurve.DEFAULT_COUNT2;
    }
  }

  /**
   * Get rough cut depth.
   *
   * @return rough cut depth
   */
  public double getDepth1() {
    if (cutCurve != null) {
      return cutCurve.getDepth1();
    } else {
      return CutCurve.DEFAULT_DEPTH1;
    }
  }

  /**
   * Get fine cut depth.
   *
   * @return fine cut depth
   */
  public double getDepth2() {
    if (cutCurve != null) {
      return cutCurve.getDepth2();
    } else {
      return CutCurve.DEFAULT_DEPTH2;
    }
  }

  /**
   * Get the total cut depth.
   *
   * @return total cut depth
   */
  public double getTotal() {
    return getCount1() * getDepth1() + getCount2() * getDepth2() - getBackoff();
  }

  /**
   * Get the direction of cutting.
   *
   * @return Direction
   */
  public Direction getDirection() {
    if (cutCurve != null) {
      return cutCurve.getDirection();
    } else {
      return CutCurve.DEFAULT_DIRECTION;
    }
  }

  /**
   * Make instructions in the CutList for following the curve. Note that in
   * order for this to remain a NetBean object, you can't look things up so you
   * have to pass them as arguments.
   *
   * @param cutList CutList for instructions
   * @param cutter Cutter to use
   * @param outline the outline
   */
  public void makeInstructions(CutList cutList, Cutter cutter, Outline outline) {
    Curve followCurve = outline.getCutterPathCurve(cutter);
    followCurve.reSample(getStep());

    double depth = -getBackoff();
    for (int i = 0; i < getCount1(); i++) {
      depth = depth + getDepth1();
      makeCutCurve(cutList, cutter, outline, followCurve, depth);
    }
    for (int i = 0; i < getCount2(); i++) {
      depth = depth + getDepth2();
      makeCutCurve(cutList, cutter, outline, followCurve, depth);		// Always + angle only
    }
  }

  /**
   * Make instructions for cutting along the curve.
   *
   * @param cutList CutList for instructions
   * @param cutter Cutter to use
   * @param outline the outline
   * @param curve Curve with points spaced as desired
   * @param depth depth of cut
   */
  private void makeCutCurve(CutList cutList, Cutter cutter, Outline outline, Curve curve, double depth) {
    ArrayList<Pt> safePts = new ArrayList<>();  // empty list if there is no SafePath
    if (outline.getSafePath() != null) {
      safePts = outline.getSafePath().getPoints();
    }
    
    Point2D.Double[] cutPts;
    if (cutter.getLocation().isFrontInOrBackOut()) {
        cutPts = curve.ptsOffset(depth);
    } else {
        cutPts = curve.ptsOffset(-depth);
    }
    if (cutPts.length == 0) {   // do nothing if there are no points
      return;
    }

    if (getDirection() == Direction.LAST_TO_FIRST) {  // cutting Rim to center
//      for (int i = cutPts.length - 1; i >= 0; i--) {
//        Point2D.Double pt = cutPts[i];                // assumes it's safe to go to 1st point!
//        cutList.goToXZ(VELOCITY, pt.x, pt.y);
//        cutList.turn(360.0);
//        cutList.spindleWrapCheck();
//      }
      
      // cut a full circle on the first point
      double cAngle = 360.0;
      Point2D.Double pt = cutPts[cutPts.length - 1];
      cutList.goToXZ(VELOCITY, pt.x, pt.y);     // assumes it's safe to go to 1st point!
      cutList.turn(cAngle);
      cAngle += 360.0;
      
      // cut through all points in a spiral
      for (int i = cutPts.length - 2; i >= 0; i--) {
        pt = cutPts[i];
        if ((cutter.getLocation().isBack() && (pt.x > 0.0)) ||
            (cutter.getLocation().isFront() && (pt.x < 0.0))) {
          break;    // quits if crosses over to the other side
        }
        cutList.goToXZC(RPM, pt.x, pt.y, cAngle);
        cAngle += 360.0;
      }
      
      // cut a full turn at the last point
      cutList.turn(cAngle);
      cutList.spindleWrapCheck();
      
      // go throught the safe points back to the beginning
      if (safePts.size() > 0) {
        for (Pt spt : safePts) {
          cutList.goToXZ(FAST, spt.getX(), spt.getY());
        }
      }
    } else {                                      // cutting center to Rim
//      for (Point2D.Double pt : cutPts) { 
//        cutList.goToXZ(VELOCITY, pt.x, pt.y);     // assumes it's safe to go to 1st point!
//        cutList.turn(360.0);
//      }
      
      // cut a full circle on the first point
      double cAngle = 360.0;
      Point2D.Double pt = cutPts[0];
      cutList.goToXZ(VELOCITY, pt.x, pt.y);     // assumes it's safe to go to 1st point!
      cutList.turn(cAngle);
      cAngle += 360.0;
      
      // cut through all points in a spiral
      for (int i = 1; i < cutPts.length; i++) {
        pt = cutPts[i];
        if ((cutter.getLocation().isBack() && (pt.x > 0.0)) ||
            (cutter.getLocation().isFront() && (pt.x < 0.0))) {
          break;    // quits if crosses over to the other side
        }
        cutList.goToXZC(RPM, pt.x, pt.y, cAngle);
        cAngle += 360.0;
      }
      
      // cut a full turn at the last point
      cutList.turn(cAngle);
      cutList.spindleWrapCheck();
        
      // go throught the safe points back to the beginning
      if (safePts.size() > 0) {
        for (int i = safePts.size() - 1; i >= 0; i--) {
          Pt spt = safePts.get(i);
          cutList.goToXZ(FAST, spt.getX(), spt.getY());
        }
      }
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    updateAll();
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    stepSizePanel = new javax.swing.JPanel();
    stepField = new javax.swing.JFormattedTextField();
    stepSlider = new javax.swing.JSlider();
    jLabel2 = new javax.swing.JLabel();
    backField = new javax.swing.JFormattedTextField();
    directionCombo = new javax.swing.JComboBox();
    countSpinner1 = new javax.swing.JSpinner();
    jLabel14 = new javax.swing.JLabel();
    deepField1 = new javax.swing.JFormattedTextField();
    jLabel15 = new javax.swing.JLabel();
    countSpinner2 = new javax.swing.JSpinner();
    jLabel16 = new javax.swing.JLabel();
    deepField2 = new javax.swing.JFormattedTextField();
    jLabel17 = new javax.swing.JLabel();
    totalLabel = new javax.swing.JLabel();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Cut Curve Data"));

    stepSizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Step Size"));

    stepField.setColumns(4);
    stepField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    stepField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    stepField.setToolTipText("Set the spindle RPM for simple cutting");
    stepField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    stepField.setValue(CutCurve.DEFAULT_STEP);
    stepField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollStep(evt);
      }
    });
    stepField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeStep(evt);
      }
    });

    stepSlider.setMajorTickSpacing(10);
    stepSlider.setPaintLabels(true);
    stepSlider.setPaintTicks(true);
    stepSlider.setToolTipText("Set the spindle RPM for simple cutting");
    stepSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        slideStep(evt);
      }
    });

    javax.swing.GroupLayout stepSizePanelLayout = new javax.swing.GroupLayout(stepSizePanel);
    stepSizePanel.setLayout(stepSizePanelLayout);
    stepSizePanelLayout.setHorizontalGroup(
      stepSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(stepSizePanelLayout.createSequentialGroup()
        .addComponent(stepField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(stepSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    stepSizePanelLayout.setVerticalGroup(
      stepSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(stepField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addComponent(stepSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    jLabel2.setText("Backoff:");

    backField.setColumns(4);
    backField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    backField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    backField.setToolTipText("Negative offset prior to cutting curves");
    backField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    backField.setValue(CutCurve.DEFAULT_BACKOFF);
    backField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollBackoff(evt);
      }
    });
    backField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeBackoff(evt);
      }
    });

    directionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Center to Rim", "Rim to Center" }));
    directionCombo.setSelectedIndex(1);
    directionCombo.setToolTipText("Direction for cutting curve");
    directionCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeDirection(evt);
      }
    });

    countSpinner1.setModel(new javax.swing.SpinnerNumberModel(1, 0, 10, 1));
    countSpinner1.setToolTipText("Number of rough cuts");
    countSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeCount1(evt);
      }
    });

    jLabel14.setText("cuts");

    deepField1.setColumns(4);
    deepField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    deepField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    deepField1.setToolTipText("Rough cut depth");
    deepField1.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    deepField1.setValue(CutCurve.DEFAULT_DEPTH1);
    deepField1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollDeep1(evt);
      }
    });
    deepField1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeDepth1(evt);
      }
    });

    jLabel15.setText("deep");

    countSpinner2.setModel(new javax.swing.SpinnerNumberModel(1, 0, 10, 1));
    countSpinner2.setToolTipText("Number of fine cuts");
    countSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeCount2(evt);
      }
    });

    jLabel16.setText("cuts");

    deepField2.setColumns(4);
    deepField2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    deepField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    deepField2.setToolTipText("Fine cut depth");
    deepField2.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    deepField2.setValue(CutCurve.DEFAULT_DEPTH2);
    deepField2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollDeep2(evt);
      }
    });
    deepField2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeDepth2(evt);
      }
    });

    jLabel17.setText("deep");

    totalLabel.setText("total from curve is:");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(stepSizePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(backField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(directionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(countSpinner2, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(countSpinner1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jLabel16)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(deepField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel17))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel14)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(deepField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel15))))
      .addComponent(totalLabel)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(stepSizePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(directionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(backField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(countSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(countSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel14)
              .addComponent(deepField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel15))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel16)
              .addComponent(deepField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel17))))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(totalLabel))
    );
  }// </editor-fold>//GEN-END:initComponents

	private void scrollDeep2(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollDeep2
      if (deepField2.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setDepth2(((Number) deepField2.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
}//GEN-LAST:event_scrollDeep2

	private void scrollDeep1(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollDeep1
      if (deepField1.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setDepth1(((Number) deepField1.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
}//GEN-LAST:event_scrollDeep1

	private void scrollStep(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollStep
      if (stepField.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setStep(((Number) stepField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
	}//GEN-LAST:event_scrollStep

	private void slideStep(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slideStep
      if (stepSlider.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setStep((double) stepSlider.getValue() / 100.0 * CutCurve.MAX_STEP);
      }
	}//GEN-LAST:event_slideStep

	private void changeCount1(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeCount1
      if (cutCurve != null) {
        cutCurve.setCount1(((Number) countSpinner1.getValue()).intValue());
      }
	}//GEN-LAST:event_changeCount1

	private void changeCount2(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeCount2
      if (cutCurve != null) {
        cutCurve.setCount2(((Number) countSpinner2.getValue()).intValue());
      }
	}//GEN-LAST:event_changeCount2

	private void changeStep(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeStep
      if (stepField.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setStep(((Number) stepField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeStep

	private void changeDepth1(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeDepth1
      if (deepField1.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setDepth1(((Number) deepField1.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeDepth1

	private void changeDepth2(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeDepth2
      if (deepField2.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setDepth2(((Number) deepField2.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeDepth2

	private void changeBackoff(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeBackoff
      if (backField.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setBackoff(((Number) backField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeBackoff

	private void scrollBackoff(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollBackoff
      if (backField.isFocusOwner() && (cutCurve != null)) {
        cutCurve.setBackoff(((Number) backField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
	}//GEN-LAST:event_scrollBackoff

	private void changeDirection(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeDirection
      if (directionCombo.hasFocus() && (cutCurve != null)) {
        cutCurve.setDirection(Direction.values()[directionCombo.getSelectedIndex()]);
      }
	}//GEN-LAST:event_changeDirection


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFormattedTextField backField;
  private javax.swing.JSpinner countSpinner1;
  private javax.swing.JSpinner countSpinner2;
  private javax.swing.JFormattedTextField deepField1;
  private javax.swing.JFormattedTextField deepField2;
  private javax.swing.JComboBox directionCombo;
  private javax.swing.JLabel jLabel14;
  private javax.swing.JLabel jLabel15;
  private javax.swing.JLabel jLabel16;
  private javax.swing.JLabel jLabel17;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JFormattedTextField stepField;
  private javax.swing.JPanel stepSizePanel;
  private javax.swing.JSlider stepSlider;
  private javax.swing.JLabel totalLabel;
  // End of variables declaration//GEN-END:variables

}
