package com.billooms.rosettebuilder;

import com.billooms.drawables.Grid;
import com.billooms.patterns.Patterns;
import com.billooms.rosette.Combine.CombineType;
import com.billooms.rosette.CompoundRosette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Top component for building custom rosettes from other patterns.
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
@ConvertAsProperties(
    dtd = "-//com.billooms.rosettebuilder//RosetteBuilder//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "RosetteBuilderTopComponent",
    iconBase = "com/billooms/rosettebuilder/RosetteBuilder16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.billooms.rosettebuilder.RosetteBuilderTopComponent")
@ActionReference(path = "Menu/Window", position = 550)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_RosetteBuilderAction",
    preferredID = "RosetteBuilderTopComponent"
)
@Messages({
  "CTL_RosetteBuilderAction=RosetteBuilder",
  "CTL_RosetteBuilderTopComponent=RosetteBuilder Window",
  "HINT_RosetteBuilderTopComponent=This is a RosetteBuilder window"
})
public final class RosetteBuilderTopComponent extends TopComponent implements PropertyChangeListener {

  private CompoundRosette cRosette = null;
  private final DisplayPanel display;
  private static ExplorerManager em = null;   // all instances share one ExplorerManager

  public RosetteBuilderTopComponent() {
    initComponents();
    
    display = new DisplayPanel();
    centerPanel.add(display, BorderLayout.CENTER);
    
    comboBox12.removeAllItems();	  // set the hiLoCombo values
    for (CombineType c : CombineType.values()) {
      comboBox12.addItem(c.toString());
    }
    comboBox123.removeAllItems();	  // set the hiLoCombo values
    for (CombineType c : CombineType.values()) {
      comboBox123.addItem(c.toString());
    }
    
    setName(Bundle.CTL_RosetteBuilderTopComponent());
    setToolTipText(Bundle.HINT_RosetteBuilderTopComponent());

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();

    putClientProperty("print.printable", Boolean.TRUE);	// this can be printed
  }

  /**
   * Update the pattern manager when the ExplorerManager rootContext changes.
   */
  private synchronized void updatePatternMgr() {
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_RosetteBuilderTopComponent() + ": (no patterns)");
      rosetteEditPanel0.setPatternMgr(null);
      rosetteEditPanel1.setPatternMgr(null);
      rosetteEditPanel2.setPatternMgr(null);
      cRosette = null;
    } else {
      setName(Bundle.CTL_RosetteBuilderTopComponent() + ": " + rootNode.getDisplayName());
      Patterns patternMgr = rootNode.getLookup().lookup(Patterns.class);
      cRosette = new CompoundRosette(patternMgr, 3);
      rosetteEditPanel0.setPatternMgr(patternMgr);    // set patternMgr first
      rosetteEditPanel1.setPatternMgr(patternMgr);
      rosetteEditPanel2.setPatternMgr(patternMgr);
      rosetteEditPanel0.setRosette(cRosette.getRosette(0));   // then set rosette so it's shared with cRosette
      rosetteEditPanel1.setRosette(cRosette.getRosette(1));
      rosetteEditPanel2.setRosette(cRosette.getRosette(2));
      cRosette.addPropertyChangeListener(display);
    }
  }
  
  private void setRosette(CompoundRosette cRos) {
    if (cRos.size() == 3) {
      if (this.cRosette != null) {
        cRosette.removePropertyChangeListener(display);
      }
      this.cRosette = cRos;
      if (cRosette != null) {
        rosetteEditPanel0.setRosette(cRosette.getRosette(0));   // then set rosette so it's shared with cRosette
        rosetteEditPanel1.setRosette(cRosette.getRosette(1));
        rosetteEditPanel2.setRosette(cRosette.getRosette(2));
        System.out.println(cRosette.getCombineType(0) + " " + cRosette.getCombineType(0).ordinal());
        System.out.println(cRosette.getCombineType(1) + " " + cRosette.getCombineType(1).ordinal());
        comboBox12.setSelectedIndex(cRosette.getCombineType(0).ordinal());
        comboBox123.setSelectedIndex(cRosette.getCombineType(1).ordinal());
        System.out.println("  " + comboBox12.getSelectedIndex() + " " + comboBox123.getSelectedIndex());
        cRosette.addPropertyChangeListener(display);
      }
    } else {
      JOptionPane.showMessageDialog(this,
          "RosetteBuilder requires a CompoundRosette with 3 rosettes",
          "Compatibility warning",
          JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * Handle the addition/deletion of a pattern.
   *
   * @param evt
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("RosetteBuilderTopComponent.propertyChange: " + evt.getSource().getClass().getName()
//	    + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Refresh the patternMgr when rootContext changes on the ExplorerManager
    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
      updatePatternMgr();
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    topPanel = new javax.swing.JPanel();
    rosetteEditPanel0 = new com.billooms.rosette.RosetteEditPanel();
    rosetteEditPanel1 = new com.billooms.rosette.RosetteEditPanel();
    rosetteEditPanel2 = new com.billooms.rosette.RosetteEditPanel();
    centerPanel = new javax.swing.JPanel();
    contolPanel = new javax.swing.JPanel();
    label1 = new javax.swing.JLabel();
    comboBox12 = new javax.swing.JComboBox();
    label2 = new javax.swing.JLabel();
    comboBox123 = new javax.swing.JComboBox();
    label3 = new javax.swing.JLabel();
    getButton = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    topPanel.setLayout(new java.awt.GridLayout(3, 1));

    rosetteEditPanel0.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    topPanel.add(rosetteEditPanel0);

    rosetteEditPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    topPanel.add(rosetteEditPanel1);

    rosetteEditPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    topPanel.add(rosetteEditPanel2);

    add(topPanel, java.awt.BorderLayout.PAGE_START);

    centerPanel.setLayout(new java.awt.BorderLayout());

    org.openide.awt.Mnemonics.setLocalizedText(label1, org.openide.util.NbBundle.getMessage(RosetteBuilderTopComponent.class, "RosetteBuilderTopComponent.label1.text")); // NOI18N

    comboBox12.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    comboBox12.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        comboBox12Changed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(label2, org.openide.util.NbBundle.getMessage(RosetteBuilderTopComponent.class, "RosetteBuilderTopComponent.label2.text")); // NOI18N

    comboBox123.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    comboBox123.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        comboBox123Changed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(label3, org.openide.util.NbBundle.getMessage(RosetteBuilderTopComponent.class, "RosetteBuilderTopComponent.label3.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(getButton, org.openide.util.NbBundle.getMessage(RosetteBuilderTopComponent.class, "RosetteBuilderTopComponent.getButton.text")); // NOI18N
    getButton.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteBuilderTopComponent.class, "RosetteBuilderTopComponent.getButton.toolTipText")); // NOI18N
    getButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        getButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout contolPanelLayout = new javax.swing.GroupLayout(contolPanel);
    contolPanel.setLayout(contolPanelLayout);
    contolPanelLayout.setHorizontalGroup(
      contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(contolPanelLayout.createSequentialGroup()
            .addComponent(label3)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(contolPanelLayout.createSequentialGroup()
            .addComponent(label1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
            .addComponent(getButton))))
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addGroup(contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(comboBox123, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(contolPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(label2)))
        .addGap(0, 0, Short.MAX_VALUE))
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addComponent(comboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, Short.MAX_VALUE))
    );
    contolPanelLayout.setVerticalGroup(
      contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(label1)
          .addComponent(getButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(comboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(label2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(comboBox123, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(label3)
        .addContainerGap(49, Short.MAX_VALUE))
    );

    centerPanel.add(contolPanel, java.awt.BorderLayout.LINE_START);

    add(centerPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void comboBox12Changed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox12Changed
    if (comboBox12.isFocusOwner() && (cRosette != null)) {
      cRosette.setCombineType(0, CombineType.values()[comboBox12.getSelectedIndex()]);
    }
  }//GEN-LAST:event_comboBox12Changed

  private void comboBox123Changed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox123Changed
    if (comboBox123.isFocusOwner() && (cRosette != null)) {
      cRosette.setCombineType(1, CombineType.values()[comboBox123.getSelectedIndex()]);
    }
  }//GEN-LAST:event_comboBox123Changed

  private void getButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getButtonActionPerformed
    if ((em.getSelectedNodes().length == 1)) {
      Node n = em.getSelectedNodes()[0];
      if (n.getLookup().lookup(CompoundRosette.class) != null) {
        setRosette(n.getLookup().lookup(CompoundRosette.class));
      } else {
        JOptionPane.showMessageDialog(this,
            "No CompoundRosette is selected in the DataNavigator",
            "Selection warning",
            JOptionPane.WARNING_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this,
          "Only one node can be selected in the DataNavigator",
          "Selection warning",
          JOptionPane.WARNING_MESSAGE);
    }
  }//GEN-LAST:event_getButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel centerPanel;
  private javax.swing.JComboBox comboBox12;
  private javax.swing.JComboBox comboBox123;
  private javax.swing.JPanel contolPanel;
  private javax.swing.JButton getButton;
  private javax.swing.JLabel label1;
  private javax.swing.JLabel label2;
  private javax.swing.JLabel label3;
  private com.billooms.rosette.RosetteEditPanel rosetteEditPanel0;
  private com.billooms.rosette.RosetteEditPanel rosetteEditPanel1;
  private com.billooms.rosette.RosetteEditPanel rosetteEditPanel2;
  private javax.swing.JPanel topPanel;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>RosetteBuilderTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>RosetteBuilderTopComponent.componentOpened em=" + em);
    updatePatternMgr();
    em.addPropertyChangeListener(this);
    if (cRosette != null) {
      cRosette.addPropertyChangeListener(display);
    }
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>PatternEditorTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
    if (cRosette != null) {
      cRosette.removePropertyChangeListener(display);
    }
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    // TODO store your settings
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    // TODO read your settings according to their version
  }

  /**
   * Nested Class -- Panel for displaying a Rosette
   *
   * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
   */
  private class DisplayPanel extends JPanel implements PropertyChangeListener {

    private final Color DEFAULT_BACKGROUND = Color.WHITE;
    private final double INITIAL_DPI = 100.0;	  // for the first time the window comes up
    private final double WINDOW_PERCENT = 0.95;	  // use 95% of the window for the rosette
    private final Point INITIAL_ZPIX = new Point(150, 200);   // artibrary
    private double dpi = INITIAL_DPI;           // Dots per inch for zooming in/out
    private Point zeroPix = INITIAL_ZPIX;       // Location of 0.0, 0.0 in pixels\
    private final double ROSETTE_RADIUS = 2.0;  // The radius of the Rosette for drawing purposes

    /** Creates new DisplayPanel */
    public DisplayPanel() {
      setBackground(DEFAULT_BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      dpi = (int) Math.min(WINDOW_PERCENT * this.getWidth() / (2 * ROSETTE_RADIUS),
          WINDOW_PERCENT * this.getHeight() / (2 * ROSETTE_RADIUS));
      zeroPix = new Point(getWidth() / 2, getHeight() / 2);	// zero is always in the center

      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(zeroPix.x, zeroPix.y);
      g2d.scale(dpi, -dpi);	// positive y is up

      // Paint the grid
      new Grid(-(double) zeroPix.x / dpi, -(double) (getHeight() - zeroPix.y) / dpi,
          (double) getWidth() / dpi, (double) getHeight() / dpi).paint(g2d);

      if (cRosette != null) {       // make sure there are rosettes
        cRosette.paint2(g2d, ROSETTE_RADIUS);		// paint the rosette without scaling to pToP
      }
    }

    /**
     * Listen for changes and repaint
     *
     * @param evt event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      repaint();		// when things change, just repaint
    }
  }
}
