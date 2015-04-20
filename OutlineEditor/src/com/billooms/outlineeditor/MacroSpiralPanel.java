package com.billooms.outlineeditor;

import com.billooms.cutpoints.CutPoint;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.SpiralCut;
import javafx.geometry.Point3D;
import javax.swing.JPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 * A panel to prompt for data to convert a spiral to individual CutPoints.
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
public class MacroSpiralPanel extends JPanel {

  /* Various ways of spacing the points. */
  private enum Spacing {

    /* Uniform in x-direction. */
    UNIFORM_X,
    /* Uniform in z-direction. */
    UNIFORM_Z,
    /* Uniform distance along the curve. */
    UNIFORM_D
  }

  /* Default spacing. */
  private final static double DEFAULT_SPACING = 0.1;
  /* Minimum spacing. */
  private final static double MIN_SPACE = 0.010;
  
  /** All instances share one ExplorerManager. */
  private static ExplorerManager em = null; 
  /** CutPoint manager. */
  private CutPoints cutPtMgr = null;
  
  /** The selected SpiralCut to expand. */
  private SpiralCut ePt;
  /** Array of Point3D with x,y from the original pts and z=twist. */
  private Point3D[] rzc;	//In lathe coordinates, this is actually radius, z, c(degrees)
  /** Total length in the chosen direction. */
  private double cumTotal = 0.0;
  /** Cumulative length at each point in the chosen direction. */
  private double[] cumLength;	
  /** number of additional points inserted (in addition to the start point). */
  private int nInserts;	

  /**
   * Creates new form MacroSpiralPanel.
   *
   * @throws java.lang.Exception
   */
  public MacroSpiralPanel() throws Exception {
    initComponents();
    
    if (em == null) {
      em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
    }
    Node rootNode = em.getRootContext();
    cutPtMgr = rootNode.getLookup().lookup(CutPoints.class);

    dirCombo.removeAllItems();
    for (Spacing s : Spacing.values()) {
      dirCombo.addItem(s.toString());
    }
    dirCombo.setSelectedIndex(Spacing.UNIFORM_D.ordinal());

    for (CutPoint cc : cutPtMgr.getAll()) {
      if (cc instanceof SpiralCut) {
        numberCombo.addItem(cc.getNum());
      }
    }
    if (numberCombo.getItemCount() == 0) {
      throw new Exception("No spiral cutpoints in list");
    }
    numberCombo.setSelectedIndex(0);
    ePt = (SpiralCut) cutPtMgr.get((Integer) numberCombo.getSelectedItem());

    rzc = ePt.getSurfaceTwist();	// twist is based on surface, not cut path
    if (rzc == null) {
      throw new Exception("No points on spiral curve");
    }

    update();
  }

  /**
   * Calculate new lengths along the curve and update display.
   */
  private void update() {
    cumLength = new double[rzc.length];
    cumLength[0] = 0.0;
    double xLength = 0.0;		// length in x-direction
    double zLength = 0.0;		// length in z-direction
    double curveLength = 0.0;	// length along the curve	
    for (int i = 1; i < rzc.length; i++) {    // calculate new lengths x, y, curve
      xLength += Math.abs(rzc[i].getX() - rzc[i - 1].getX());	// sum up each direction
      zLength += Math.abs(rzc[i].getY() - rzc[i - 1].getY());
      curveLength += Math.hypot(rzc[i].getX() - rzc[i - 1].getX(), rzc[i].getY() - rzc[i - 1].getY());
      switch (Spacing.values()[dirCombo.getSelectedIndex()]) {
        case UNIFORM_X:
          cumTotal = xLength;
          break;
        case UNIFORM_Z:
          cumTotal = zLength;
          break;
        case UNIFORM_D:
          cumTotal = curveLength;
          break;
      }
      cumLength[i] = cumTotal;
    }
    xField.setValue(xLength);
    zField.setValue(zLength);
    totalField.setValue(curveLength);

    nInserts = (int) Math.round(cumTotal / ((Number) spaceField.getValue()).doubleValue());
    nInserts = nInserts - 1;
    nPointsField.setValue(nInserts);
  }

  /**
   * Make the points and insert them in the cutList.
   */
  public void makePoints() {
    if ((nInserts == 0) || (cumTotal == 0.0)) {
      return;
    }
    cutPtMgr.spiralToPoints(ePt, nInserts, cumLength, cumTotal, rzc);
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        numberCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        xField = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        zField = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        totalField = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        spaceField = new javax.swing.JFormattedTextField();
        dirCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        nPointsField = new javax.swing.JFormattedTextField();

        jLabel6.setText("CutPoint #:");

        numberCombo.setToolTipText("Select which spiral to use");
        numberCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePoint(evt);
            }
        });

        jLabel1.setText("Length of Spiral: x:");

        xField.setColumns(5);
        xField.setEditable(false);
        xField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        jLabel4.setText("z:");

        zField.setColumns(5);
        zField.setEditable(false);
        zField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        jLabel5.setText("total:");

        totalField.setColumns(5);
        totalField.setEditable(false);
        totalField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        jLabel2.setText("Approximate Spacing:");

        spaceField.setColumns(5);
        spaceField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
        spaceField.setToolTipText("Set approximate spacing of inserted points");
        spaceField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
        spaceField.setValue(DEFAULT_SPACING);
        spaceField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollSpacing(evt);
            }
        });
        spaceField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                changeSpacing(evt);
            }
        });

        dirCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Spacing" }));
        dirCombo.setToolTipText("Set the spacing criteria");
        dirCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeDirection(evt);
            }
        });

        jLabel3.setText("Number of inserted points:");

        nPointsField.setEditable(false);
        nPointsField.setColumns(5);
        nPointsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spaceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dirCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nPointsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(numberCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(xField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(zField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(totalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spaceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dirCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nPointsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void changePoint(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePoint
      if (numberCombo.isFocusOwner()) {
        ePt = (SpiralCut) cutPtMgr.get((Integer) numberCombo.getSelectedItem());
        rzc = ePt.getSurfaceTwist();
        if (rzc == null) {
          try {
            throw new Exception("No points on spiral curve");		// should not happen
          } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
          }
        }
        update();
      }
}//GEN-LAST:event_changePoint

	private void changeDirection(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeDirection
      if (dirCombo.isFocusOwner()) {
        update();
      }
}//GEN-LAST:event_changeDirection

	private void scrollSpacing(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollSpacing
      double s = ((Number) spaceField.getValue()).doubleValue() - 0.01 * evt.getWheelRotation();
      s = Math.max(MIN_SPACE, s);
      spaceField.setValue(s);
	}//GEN-LAST:event_scrollSpacing

	private void changeSpacing(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeSpacing
      update();
	}//GEN-LAST:event_changeSpacing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dirCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JFormattedTextField nPointsField;
    private javax.swing.JComboBox numberCombo;
    private javax.swing.JFormattedTextField spaceField;
    private javax.swing.JFormattedTextField totalField;
    private javax.swing.JFormattedTextField xField;
    private javax.swing.JFormattedTextField zField;
    // End of variables declaration//GEN-END:variables

}
