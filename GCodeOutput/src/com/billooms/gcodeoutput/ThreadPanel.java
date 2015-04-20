package com.billooms.gcodeoutput;

import com.billooms.controls.Threads;
import com.billooms.cutlist.CutList;
import static com.billooms.cutlist.Speed.*;
import com.billooms.cutters.Cutter;
import com.billooms.outline.Outline;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/**
 * Control Panel data for threads.
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
public class ThreadPanel extends JPanel implements PropertyChangeListener {

  private final static double SIN60 = Math.sqrt(3.0) / 2.0;
  private final static Color ENABLED_COLOR = new Color(153, 153, 153);
  private final static Color DISABLED_COLOR = new Color(204, 204, 204);

  /** Back-off distance from threads when moving. */
  private final static double BACKOFF = 0.010;

  /** Threads object holds all the information. */
  private Threads threads = null;
  /** Depth of cut for threads. */
  private double cutDepth;
  /** Keep track of whether ID or OD was changed last. */
  private boolean IDChanged = true;

  /** Creates new form ThreadPanel. */
  public ThreadPanel() {
    initComponents();

    updateAll();
  }

  /**
   * Associate a new Threads object with this control panel.
   *
   * @param newThreads new FeedRate object
   */
  public void setThreads(Threads newThreads) {
    if (threads == newThreads) {
      return;
    }
    if (threads != null) {
      threads.removePropertyChangeListener(this);  // quit listening to the old one.
    }
    this.threads = newThreads;
    if (threads != null) {
      setEnabled(true);
      updateAll();
      threads.addPropertyChangeListener(this);     // listen for changes in the new one.
    } else {
      setEnabled(false);
    }
  }

  /**
   * Update all fields with the current information.
   */
  private void updateAll() {
    tpiField.setValue(getTpi());
    startsCombo.setSelectedIndex(getStarts() - 1);
    percentCombo.setSelectedItem(Integer.toString(getPercent()));

    percentLabel.setText(getPercent() + "% Depth:");
    double pitch = 1.0 / (double) getTpi();
    pitchField.setValue(pitch);
    double height = SIN60 * pitch;
    heightField.setValue(height);
    double delta = height * (double) getPercent() / 100.0;
    deltaField.setValue(delta);
    cutDepth = (height + delta) / 2.0;
    cutDepthField.setValue(cutDepth);
    if (IDChanged) {
      ODField.setValue(((Number) IDField.getValue()).doubleValue() + 2.0 * delta);
    } else {
      IDField.setValue(((Number) ODField.getValue()).doubleValue() - 2.0 * delta);
    }
  }

  /**
   * Enable (or disable) input fields.
   *
   * @param en true=enable; false=disable
   */
  @Override
  public void setEnabled(boolean en) {
    tpiField.setEnabled(en);
    startsCombo.setEnabled(en);
    percentCombo.setEnabled(en);
    ODField.setEnabled(en);
    IDField.setEnabled(en);
    if (en) {
      this.setBackground(ENABLED_COLOR);
    } else {
      this.setBackground(DISABLED_COLOR);
    }
  }

  /**
   * Get the number of threads per inch (independent of the number of starts).
   *
   * @return threads per inch
   */
  public int getTpi() {
    if (threads != null) {
      return threads.getTpi();
    } else {
      return Threads.DEFAULT_TPI;
    }
  }

  /**
   * Get the number of starts.
   *
   * @return number of starts
   */
  public int getStarts() {
    if (threads != null) {
      return threads.getStarts();
    } else {
      return Threads.DEFAULT_STARTS;
    }
  }

  /**
   * Get the percent value of the thread engagement (range of 0 to 100).
   *
   * @return thread percent
   */
  public int getPercent() {
    if (threads != null) {
      return threads.getPercent();
    } else {
      return Threads.DEFAULT_PERCENT;
    }
  }

  /**
   * Get the total depth of the threads.
   *
   * @return depth of threads
   */
  public double getCutDepth() {
    return cutDepth;
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
    double depth = cutDepth;
    double backOff = BACKOFF;
    switch (cutter.getLocation()) {
      case FRONT_OUTSIDE:
      case BACK_INSIDE:
        depth = -depth;
        break;
      case FRONT_INSIDE:
      case BACK_OUTSIDE:
        backOff = -backOff;
        break;
    }

    Point2D.Double[] cutCurvePts = outline.getCutterPathCurve().getPoints();
    Point2D.Double p0 = cutCurvePts[0];
    Point2D.Double p1 = cutCurvePts[cutCurvePts.length - 1];
    double dy = p1.y - p0.y;		// normally a positive value
    // <<<<<<< TODO Option to cut one direction or the other?

    double rot = -360.0 * dy * (double) getTpi() / (double) getStarts();		// negative sign for right hand threads

    for (int i = 0; i < getStarts(); i++) {
      double angOffset = 360.0 * (double) i / (double) getStarts();
      cutList.goToXZC(FAST, p0.x + backOff, p0.y, angOffset);
      cutList.goToXZ(VELOCITY, p0.x + 0.707 * depth, p0.y);			// first cut is 0.707*depth (1/2 the material)
      cutList.goToXZC(RPM, p1.x + 0.707 * depth, p1.y, rot + angOffset);
      cutList.goToXZ(VELOCITY, p1.x + backOff, p1.y);
      cutList.spindleWrapCheck();
      cutList.goToXZC(FAST, p0.x + backOff, p0.y, angOffset);
      cutList.goToXZ(VELOCITY, p0.x + depth, p0.y);					// second cut is full depth
      cutList.goToXZC(RPM, p1.x + depth, p1.y, rot + angOffset);
      cutList.goToXZ(VELOCITY, p1.x + backOff, p1.y);
      cutList.spindleWrapCheck();
    }
    cutList.goToXZC(FAST, p1.x + backOff, p1.y, 0.0);			// Always go to C=0 at the end
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

    jLabel1 = new javax.swing.JLabel();
    tpiField = new javax.swing.JFormattedTextField();
    jLabel5 = new javax.swing.JLabel();
    pitchField = new javax.swing.JFormattedTextField();
    jLabel4 = new javax.swing.JLabel();
    startsCombo = new javax.swing.JComboBox();
    jLabel7 = new javax.swing.JLabel();
    percentCombo = new javax.swing.JComboBox();
    jLabel6 = new javax.swing.JLabel();
    heightField = new javax.swing.JFormattedTextField();
    percentLabel = new javax.swing.JLabel();
    deltaField = new javax.swing.JFormattedTextField();
    jLabel9 = new javax.swing.JLabel();
    IDField = new javax.swing.JFormattedTextField();
    jLabel8 = new javax.swing.JLabel();
    ODField = new javax.swing.JFormattedTextField();
    jLabel10 = new javax.swing.JLabel();
    cutDepthField = new javax.swing.JFormattedTextField();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Threads"));

    jLabel1.setText("TPI:");

    tpiField.setColumns(3);
    tpiField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
    tpiField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
    tpiField.setToolTipText("Threads per inch");
    tpiField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    tpiField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollTPI(evt);
      }
    });
    tpiField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeTPI(evt);
      }
    });

    jLabel5.setText("Pitch:");

    pitchField.setEditable(false);
    pitchField.setColumns(4);
    pitchField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    pitchField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    pitchField.setToolTipText("Pitch in inches");

    jLabel4.setText("Starts:");

    startsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));
    startsCombo.setToolTipText("Number of thread starts");
    startsCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeStarts(evt);
      }
    });

    jLabel7.setText("%:");

    percentCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "50", "55", "60", "65", "70", "75", "80" }));
    percentCombo.setSelectedIndex(2);
    percentCombo.setToolTipText("Percent thread engagement");
    percentCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changePercent(evt);
      }
    });

    jLabel6.setText("100% Depth:");

    heightField.setEditable(false);
    heightField.setColumns(4);
    heightField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    heightField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    heightField.setToolTipText("Full depth of threads");

    percentLabel.setText("70% Depth:");

    deltaField.setEditable(false);
    deltaField.setColumns(4);
    deltaField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    deltaField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    deltaField.setToolTipText("Depth of threads at the percentage");

    jLabel9.setText("Female ID:");

    IDField.setColumns(4);
    IDField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    IDField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    IDField.setToolTipText("Set Inside Diameter (calculate Outside Diameter)");
    IDField.setFocusLostBehavior(javax.swing.JFormattedTextField.REVERT);
    IDField.setValue(1.0);
    IDField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeID(evt);
      }
    });

    jLabel8.setText("Male OD:");

    ODField.setColumns(4);
    ODField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    ODField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    ODField.setToolTipText("Set Outside Diameter (calculate Inside Diameter)");
    ODField.setFocusLostBehavior(javax.swing.JFormattedTextField.REVERT);
    ODField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeOD(evt);
      }
    });

    jLabel10.setText("Cut Depth:");

    cutDepthField.setEditable(false);
    cutDepthField.setColumns(4);
    cutDepthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    cutDepthField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    cutDepthField.setToolTipText("Actual depth of the cut");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pitchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(startsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel7)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(percentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(tpiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jLabel10)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cutDepthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jLabel8)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ODField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jLabel9)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(IDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(percentLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(deltaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jLabel6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(heightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(tpiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(pitchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(startsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(percentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel7)))
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(heightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel6))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(deltaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(percentLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(IDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel9))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(ODField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel8))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cutDepthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel10)))
    );
  }// </editor-fold>//GEN-END:initComponents

	private void changePercent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePercent
      if (threads != null) {
        threads.setPercent(Integer.valueOf((String) percentCombo.getSelectedItem()));
      }
}//GEN-LAST:event_changePercent

	private void scrollTPI(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollTPI
      if (tpiField.isFocusOwner() && (threads != null)) {
        threads.setTpi(((Number) tpiField.getValue()).intValue() + evt.getWheelRotation());
      }
}//GEN-LAST:event_scrollTPI

	private void changeStarts(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeStarts
      if (threads != null) {
        threads.setStarts(startsCombo.getSelectedIndex() + 1);
      }
	}//GEN-LAST:event_changeStarts

	private void changeTPI(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeTPI
      if (tpiField.isFocusOwner() && (threads != null)) {
        threads.setTpi(((Number) tpiField.getValue()).intValue());
      }
	}//GEN-LAST:event_changeTPI

	private void changeOD(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeOD
      IDChanged = false;
      IDField.setValue(((Number) ODField.getValue()).doubleValue() - 2.0 * ((Number) deltaField.getValue()).doubleValue());
	}//GEN-LAST:event_changeOD

	private void changeID(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeID
      IDChanged = true;
      ODField.setValue(((Number) IDField.getValue()).doubleValue() + 2.0 * ((Number) deltaField.getValue()).doubleValue());
	}//GEN-LAST:event_changeID


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFormattedTextField IDField;
  private javax.swing.JFormattedTextField ODField;
  private javax.swing.JFormattedTextField cutDepthField;
  private javax.swing.JFormattedTextField deltaField;
  private javax.swing.JFormattedTextField heightField;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JComboBox percentCombo;
  private javax.swing.JLabel percentLabel;
  private javax.swing.JFormattedTextField pitchField;
  private javax.swing.JComboBox startsCombo;
  private javax.swing.JFormattedTextField tpiField;
  // End of variables declaration//GEN-END:variables

}
