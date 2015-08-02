package com.billooms.gcodeoutput;

import com.billooms.controls.CoarseFine;
import com.billooms.controls.CoarseFine.Rotation;
import com.billooms.gcodeoutput.hardwareprefs.HardwarePrefs;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.openide.util.Lookup;

/**
 * Coarse/Fine cut settings.
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
public class CoarseFinePanel extends JPanel implements PropertyChangeListener {

  private final static DecimalFormat F2 = new DecimalFormat("0.00");
  private final static Color ENABLED_COLOR = new Color(153, 153, 153);
  private final static Color DISABLED_COLOR = new Color(204, 204, 204);

  private final HardwarePrefs prefs = Lookup.getDefault().lookup(HardwarePrefs.class);

  /** CoarseFine object holds all the information. */
  private CoarseFine coarseFine = null;

  /** Creates new form CoarseFinePanel */
  public CoarseFinePanel() {
    initComponents();

    dirCombo.removeAllItems();
    for (Rotation d : Rotation.values()) {
      dirCombo.addItem(d.text);
    }
  }

  /**
   * Get the CoarseFine object associated with this control panel.
   * 
   * @return CoarseFine object
   */
  public CoarseFine getCoarseFine() {
    return coarseFine;
  }

  /**
   * Associate a new CoarseFine object with this control panel.
   *
   * @param newCoarseFine new CoarseFine object
   */
  public void setCoarseFine(CoarseFine newCoarseFine) {
    if (coarseFine == newCoarseFine) {
      return;
    }
    if (coarseFine != null) {
      coarseFine.removePropertyChangeListener(this);  // quit listening to the old one.
    }
    this.coarseFine = newCoarseFine;
    if (coarseFine != null) {
      setEnabled(true);
      updateAll();
      coarseFine.addPropertyChangeListener(this);     // listen for changes in the new one.
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
    passDepthField.setEnabled(en);
    lastDepthField.setEnabled(en);
    passStepSpin.setEnabled(en);
    lastStepSpin.setEnabled(en);
    softLiftCheck.setEnabled(en);
    softLiftField.setEnabled(en && isSoftLift());
    softLiftSpin.setEnabled(en && isSoftLift());
    dirCombo.setEnabled(en);
    if (en) {
      this.setBackground(ENABLED_COLOR);
    } else {
      this.setBackground(DISABLED_COLOR);
    }
  }

  /**
   * Get the depth per pass (rough cuts).
   *
   * @return depth per pass
   */
  public double getPassDepth() {
    if (coarseFine != null) {
      return coarseFine.getPassDepth();
    } else {
      return CoarseFine.DEFAULT_PASS_DEPTH;
    }
  }

  /**
   * Get the depth for the last cut.
   *
   * @return last cut depth
   */
  public double getLastDepth() {
    if (coarseFine != null) {
      return coarseFine.getLastDepth();
    } else {
      return CoarseFine.DEFAULT_LAST_DEPTH;
    }
  }

  /**
   * Get the number of spindle steps on rough passes.
   *
   * @return spindle steps
   */
  public int getPassStep() {
    if (coarseFine != null) {
      return coarseFine.getPassStep();
    } else {
      return CoarseFine.DEFAULT_PASS_STEP;
    }
  }

  /**
   * Get the number of spindle steps on last pass.
   *
   * @return spindle steps
   */
  public int getLastStep() {
    if (coarseFine != null) {
      return coarseFine.getLastStep();
    } else {
      return CoarseFine.DEFAULT_LAST_STEP;
    }
  }

  /**
   * Is the optional soft lift selected?
   * 
   * @return true: optional soft lift is selected
   */
  public boolean isSoftLift() {
    if (coarseFine != null) {
      return coarseFine.isSoftLift();
    } else {
      return false;
    }
  }

  /**
   * Get the height of the optional soft lift-off.
   * 
   * @return height of the soft lift-off
   */
  public double getSoftLiftHeight() {
    if (coarseFine != null) {
      return coarseFine.getSoftLiftHeight();
    } else {
      return CoarseFine.DEFAULT_SOFT_LIFT;
    }
  }

  /**
   * Set the rotation angle for the optional soft lift-off.
   * 
   * @return rotation angle for the soft lift-off
   */
  public double getSoftLiftDeg() {
    if (coarseFine != null) {
      return coarseFine.getSoftLiftDeg();
    } else {
      return CoarseFine.DEFAULT_SOFT_DEG;
    }
  }

  /**
   * Get the direction for rotation.
   *
   * @return NEG_LAST, PLUS_ALWAYS, NEG_ALWAYS
   */
  public Rotation getRotation() {
    if (coarseFine != null) {
      return coarseFine.getRotation();
    } else {
      return CoarseFine.DEFAULT_ROTATION;
    }
  }

  /**
   * Update all display fields with current data.
   */
  public void updateAll() {
    passDepthField.setValue(getPassDepth());
    lastDepthField.setValue(getLastDepth());
    passStepSpin.setValue(getPassStep());
    double deg = 360.0 * (double) getPassStep() / (double) prefs.getStepsPerRotation();
    passDegreeLabel.setText(F2.format(deg) + " Deg/step");
    lastStepSpin.setValue(getLastStep());
    deg = 360.0 * (double) getLastStep() / (double) prefs.getStepsPerRotation();
    lastDegreeLabel.setText(F2.format(deg) + " Deg/step");
    softLiftCheck.setSelected(isSoftLift());
    softLiftField.setEnabled(softLiftCheck.isSelected());
    softLiftField.setValue(getSoftLiftHeight());
    softLiftSpin.setEnabled(softLiftCheck.isSelected());
    softLiftSpin.setValue(getSoftLiftDeg());
    dirCombo.setSelectedIndex(getRotation().ordinal());
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

    jLabel19 = new javax.swing.JLabel();
    passDepthField = new javax.swing.JFormattedTextField();
    passStepSpin = new javax.swing.JSpinner();
    passDegreeLabel = new javax.swing.JLabel();
    jLabel20 = new javax.swing.JLabel();
    lastDepthField = new javax.swing.JFormattedTextField();
    lastStepSpin = new javax.swing.JSpinner();
    lastDegreeLabel = new javax.swing.JLabel();
    softLiftCheck = new javax.swing.JCheckBox();
    softLiftField = new javax.swing.JFormattedTextField();
    softLiftSpin = new javax.swing.JSpinner();
    jLabel1 = new javax.swing.JLabel();
    dirCombo = new javax.swing.JComboBox();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Coarse/Fine Cut Controls"));

    jLabel19.setText("Depth per Pass:");

    passDepthField.setColumns(4);
    passDepthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    passDepthField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    passDepthField.setToolTipText("Coarse cutting depth per pass");
    passDepthField.setValue(CoarseFine.DEFAULT_PASS_DEPTH);
    passDepthField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollPassDepth(evt);
      }
    });
    passDepthField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changePassDepth(evt);
      }
    });

    passStepSpin.setModel(new javax.swing.SpinnerNumberModel(5, 0, 20, 1));
    passStepSpin.setToolTipText("Set size of each spindle step for coarse cutting");
    passStepSpin.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changePassStep(evt);
      }
    });

    passDegreeLabel.setText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.passDegreeLabel.text")); // NOI18N

    jLabel20.setText("Final Cut Depth:");

    lastDepthField.setColumns(4);
    lastDepthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    lastDepthField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    lastDepthField.setToolTipText("Depth of last cut");
    lastDepthField.setValue(CoarseFine.DEFAULT_LAST_DEPTH);
    lastDepthField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollLastDepth(evt);
      }
    });
    lastDepthField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeLastDepth(evt);
      }
    });

    lastStepSpin.setModel(new javax.swing.SpinnerNumberModel(1, 1, 20, 1));
    lastStepSpin.setToolTipText("Set size of each spindle step for fine cutting");
    lastStepSpin.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeLastStep(evt);
      }
    });

    lastDegreeLabel.setText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.lastDegreeLabel.text")); // NOI18N

    softLiftCheck.setText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.softLiftCheck.text")); // NOI18N
    softLiftCheck.setToolTipText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.softLiftCheck.toolTipText")); // NOI18N
    softLiftCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeSoftCheck(evt);
      }
    });

    softLiftField.setColumns(4);
    softLiftField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    softLiftField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    softLiftField.setToolTipText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.softLiftField.toolTipText")); // NOI18N
    softLiftField.setValue(CoarseFine.DEFAULT_SOFT_LIFT);
    softLiftField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollSoftLift(evt);
      }
    });
    softLiftField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeSoftLift(evt);
      }
    });

    softLiftSpin.setModel(new javax.swing.SpinnerNumberModel(10, 0, 20, 1));
    softLiftSpin.setToolTipText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.softLiftSpin.toolTipText")); // NOI18N
    softLiftSpin.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeSoftLiftHeight(evt);
      }
    });

    jLabel1.setText(org.openide.util.NbBundle.getMessage(CoarseFinePanel.class, "CoarseFinePanel.jLabel1.text")); // NOI18N

    dirCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Always + rotation", "Always - rotation", "+ on rough, - on final" }));
    dirCombo.setToolTipText("Direction of the spindle during cuts."); // NOI18N
    dirCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dirComboActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(dirCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(lastDepthField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(passDepthField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(layout.createSequentialGroup()
            .addGap(1, 1, 1)
            .addComponent(softLiftCheck)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(softLiftField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(softLiftSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel1))
          .addGroup(layout.createSequentialGroup()
            .addComponent(passStepSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(passDegreeLabel))
          .addGroup(layout.createSequentialGroup()
            .addComponent(lastStepSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lastDegreeLabel))))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(jLabel19)
          .addComponent(passDepthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(passStepSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(passDegreeLabel))
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(jLabel20)
          .addComponent(lastDepthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lastStepSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lastDegreeLabel))
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(softLiftCheck)
          .addComponent(softLiftSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1)
          .addComponent(softLiftField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, 0)
        .addComponent(dirCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

	private void scrollPassDepth(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollPassDepth
      if (passDepthField.isFocusOwner() && (coarseFine != null)) {
        coarseFine.setPassDepth(((Number) passDepthField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
	}//GEN-LAST:event_scrollPassDepth

	private void scrollLastDepth(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollLastDepth
      if (lastDepthField.isFocusOwner() && (coarseFine != null)) {
        coarseFine.setLastDepth(((Number) lastDepthField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
	}//GEN-LAST:event_scrollLastDepth

	private void changePassStep(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changePassStep
      if (coarseFine != null) {
        coarseFine.setPassStep(((Number) passStepSpin.getValue()).intValue());
      }
	}//GEN-LAST:event_changePassStep

	private void changeLastStep(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeLastStep
      if (coarseFine != null) {
        coarseFine.setLastStep(((Number) lastStepSpin.getValue()).intValue());
      }
	}//GEN-LAST:event_changeLastStep

	private void changePassDepth(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changePassDepth
      if (passDepthField.isFocusOwner() && (coarseFine != null)) {
        coarseFine.setPassDepth(((Number) passDepthField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changePassDepth

	private void changeLastDepth(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeLastDepth
      if (lastDepthField.isFocusOwner() && (coarseFine != null)) {
        coarseFine.setLastDepth(((Number) lastDepthField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeLastDepth

  private void dirComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dirComboActionPerformed
    if (coarseFine != null) {
      coarseFine.setRotation(Rotation.values()[dirCombo.getSelectedIndex()]);
    }
  }//GEN-LAST:event_dirComboActionPerformed

  private void scrollSoftLift(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollSoftLift
    if (softLiftField.isFocusOwner() && (coarseFine != null)) {
        coarseFine.setSoftLiftHeight(((Number) softLiftField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation());
      }
  }//GEN-LAST:event_scrollSoftLift

  private void changeSoftLift(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeSoftLift
    if (softLiftField.isFocusOwner() && (coarseFine != null)) {
        coarseFine.setSoftLiftHeight(((Number) softLiftField.getValue()).doubleValue());
      }
  }//GEN-LAST:event_changeSoftLift

  private void changeSoftLiftHeight(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeSoftLiftHeight
    if (coarseFine != null) {
        coarseFine.setSoftLiftDeg(((Number) softLiftSpin.getValue()).doubleValue());
      }
  }//GEN-LAST:event_changeSoftLiftHeight

  private void changeSoftCheck(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSoftCheck
    if (coarseFine != null) {
      coarseFine.setSoftLift(softLiftCheck.isSelected());
      softLiftField.setEnabled(softLiftCheck.isSelected());
      softLiftSpin.setEnabled(softLiftCheck.isSelected());
    }
  }//GEN-LAST:event_changeSoftCheck

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox dirCombo;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel19;
  private javax.swing.JLabel jLabel20;
  private javax.swing.JLabel lastDegreeLabel;
  private javax.swing.JFormattedTextField lastDepthField;
  private javax.swing.JSpinner lastStepSpin;
  private javax.swing.JLabel passDegreeLabel;
  private javax.swing.JFormattedTextField passDepthField;
  private javax.swing.JSpinner passStepSpin;
  private javax.swing.JCheckBox softLiftCheck;
  private javax.swing.JFormattedTextField softLiftField;
  private javax.swing.JSpinner softLiftSpin;
  // End of variables declaration//GEN-END:variables
}
