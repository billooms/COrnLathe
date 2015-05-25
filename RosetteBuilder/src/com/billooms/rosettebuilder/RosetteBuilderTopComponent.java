package com.billooms.rosettebuilder;

import com.billooms.drawables.Grid;
import com.billooms.drawables.simple.Circle;
import com.billooms.drawables.simple.Curve;
import com.billooms.patterns.Patterns;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

  /** Different ways to combine rosettes. */
  public static enum Combine {
    /** Use the first one and ignore the second one. */
    NONE,
    /** Use the Minimum of the two at any given point. */
    MIN,
    /** Use the Maximum of the two at any given point. */
    MAX, 
    /** Add the two at any given point. */
    ADD;
    
    public double combine(double n1, double n2) {
      switch (Combine.this) {
        default:
        case NONE:
          return n1;
        case MIN:
          return Math.min(n1, n2);
        case MAX:
          return Math.max(n1, n2);
        case ADD:
          return n1+n2;
      }
    }
  }
  
  private final DisplayPanel display;
  private static ExplorerManager em = null;   // all instances share one ExplorerManager

  public RosetteBuilderTopComponent() {
    initComponents();
    
    display = new DisplayPanel();
    centerPanel.add(display, BorderLayout.CENTER);
    
    comboBox12.removeAllItems();	  // set the hiLoCombo values
    for (Combine c : Combine.values()) {
      comboBox12.addItem(c.toString());
    }
    comboBox123.removeAllItems();	  // set the hiLoCombo values
    for (Combine c : Combine.values()) {
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
      rosetteEditPanel1.setPatternMgr(null);
      rosetteEditPanel2.setPatternMgr(null);
      rosetteEditPanel3.setPatternMgr(null);
    } else {
      setName(Bundle.CTL_RosetteBuilderTopComponent() + ": " + rootNode.getDisplayName());
      rosetteEditPanel1.setPatternMgr(rootNode.getLookup().lookup(Patterns.class));
      rosetteEditPanel2.setPatternMgr(rootNode.getLookup().lookup(Patterns.class));
      rosetteEditPanel3.setPatternMgr(rootNode.getLookup().lookup(Patterns.class));
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
    rosetteEditPanel1 = new com.billooms.rosette.RosetteEditPanel();
    rosetteEditPanel2 = new com.billooms.rosette.RosetteEditPanel();
    rosetteEditPanel3 = new com.billooms.rosette.RosetteEditPanel();
    centerPanel = new javax.swing.JPanel();
    contolPanel = new javax.swing.JPanel();
    label1 = new javax.swing.JLabel();
    comboBox12 = new javax.swing.JComboBox();
    label2 = new javax.swing.JLabel();
    comboBox123 = new javax.swing.JComboBox();
    label3 = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());

    topPanel.setLayout(new java.awt.GridLayout(3, 1));

    rosetteEditPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    topPanel.add(rosetteEditPanel1);

    rosetteEditPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    topPanel.add(rosetteEditPanel2);

    rosetteEditPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    topPanel.add(rosetteEditPanel3);

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

    javax.swing.GroupLayout contolPanelLayout = new javax.swing.GroupLayout(contolPanel);
    contolPanel.setLayout(contolPanelLayout);
    contolPanelLayout.setHorizontalGroup(
      contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addGroup(contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(comboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(comboBox123, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 107, Short.MAX_VALUE))
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(label1)
          .addComponent(label2)
          .addComponent(label3))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    contolPanelLayout.setVerticalGroup(
      contolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(contolPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(label1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(comboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(label2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(comboBox123, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(label3)
        .addContainerGap(56, Short.MAX_VALUE))
    );

    centerPanel.add(contolPanel, java.awt.BorderLayout.LINE_START);

    add(centerPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void comboBox12Changed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox12Changed
    display.repaint();
  }//GEN-LAST:event_comboBox12Changed

  private void comboBox123Changed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBox123Changed
    display.repaint();
  }//GEN-LAST:event_comboBox123Changed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel centerPanel;
  private javax.swing.JComboBox comboBox12;
  private javax.swing.JComboBox comboBox123;
  private javax.swing.JPanel contolPanel;
  private javax.swing.JLabel label1;
  private javax.swing.JLabel label2;
  private javax.swing.JLabel label3;
  private com.billooms.rosette.RosetteEditPanel rosetteEditPanel1;
  private com.billooms.rosette.RosetteEditPanel rosetteEditPanel2;
  private com.billooms.rosette.RosetteEditPanel rosetteEditPanel3;
  private javax.swing.JPanel topPanel;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>RosetteBuilderTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>RosetteBuilderTopComponent.componentOpened em=" + em);
    updatePatternMgr();
    em.addPropertyChangeListener(this);
    rosetteEditPanel1.addPropertyChangeListener(display);
    rosetteEditPanel2.addPropertyChangeListener(display);
    rosetteEditPanel3.addPropertyChangeListener(display);
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>PatternEditorTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
    rosetteEditPanel1.removePropertyChangeListener(display);
    rosetteEditPanel2.removePropertyChangeListener(display);
    rosetteEditPanel3.removePropertyChangeListener(display);
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
    
  /* Information for drawing */
    private final BasicStroke SOLID_LINE = new BasicStroke(1.0f);
    private final BasicStroke DOT_LINE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{3, 3}, 0);
    private final Color OUTLINE_COLOR = Color.BLACK;
    private final Color RADIUS_COLOR = Color.BLUE;
    private final int NUM_POINTS = 720;	    // draw a point every 1/2 degree
    private final Point2D.Double center = new Point2D.Double(0.0, 0.0);   // center of the rosette is always 0.0, 0.0
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
      new Circle(new Point2D.Double(0.0, 0.0), ROSETTE_RADIUS, RADIUS_COLOR, DOT_LINE).paint(g2d);

      if (rosetteEditPanel1.getRosette() != null) {       // make sure there are rosettes
        double[] deflection = new double[NUM_POINTS + 1];     // add 1 for wrap-around
        for (int i = 0; i <= NUM_POINTS; i++) {
          deflection[i] = rosetteEditPanel1.getRosette().getAmplitudeAt((double) i);
        }
        for (int i = 0; i <= NUM_POINTS; i++) {
          deflection[i] = Combine.values()[comboBox12.getSelectedIndex()].combine(
              deflection[i], 
              rosetteEditPanel2.getRosette().getAmplitudeAt((double) i));
        }
        for (int i = 0; i <= NUM_POINTS; i++) {
          deflection[i] = Combine.values()[comboBox123.getSelectedIndex()].combine(
              deflection[i], 
              rosetteEditPanel3.getRosette().getAmplitudeAt((double) i));
        }
        // convert radius to xy points
        Point2D.Double[] pts = new Point2D.Double[NUM_POINTS + 1];     // add 1 for wrap-around
        for (int i = 0; i <= NUM_POINTS; i++) {
          double radius = ROSETTE_RADIUS - deflection[i];
          // Add PI so that the pattern starts on the left side.
          // Minus sign so that pattern goes clockwise such that 
          // a positive spindle rotation brings the feature to the left side.
          double angelRad = -Math.toRadians((double) i) + Math.PI;
          pts[i] = new Point2D.Double(radius * Math.cos(angelRad), radius * Math.sin(angelRad));
        }
        new Curve(pts, OUTLINE_COLOR, SOLID_LINE).paint(g2d);
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
