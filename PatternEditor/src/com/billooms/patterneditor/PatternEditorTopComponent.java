package com.billooms.patterneditor;

import com.billooms.drawables.Grid;
import com.billooms.drawables.Pt;
import com.billooms.patterns.BasicPattern;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.CustomPattern.CustomStyle;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component for editing custom patterns.
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
    dtd = "-//com.billooms.patterneditor//PatternEditor//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "PatternEditorTopComponent",
    iconBase = "com/billooms/patterneditor/Pattern16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.billooms.patterneditor.PatternEditorTopComponent")
@ActionReference(path = "Menu/Window", position = 500)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_PatternEditorAction",
    preferredID = "PatternEditorTopComponent"
)
@Messages({
  "CTL_PatternEditorAction=PatternEditor",
  "CTL_PatternEditorTopComponent=PatternEditor Window",
  "HINT_PatternEditorTopComponent=This is a PatternEditor window"
})
public final class PatternEditorTopComponent extends TopComponent implements PropertyChangeListener {

  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final DisplayPanel display;	      // panel for displaying the graphics
  private Pattern selected = null;	      // Pattern being edited
  private Patterns patternMgr = null;	      // Pattern Manager

  public PatternEditorTopComponent() {
    initComponents();
    setName(Bundle.CTL_PatternEditorTopComponent());
    setToolTipText(Bundle.HINT_PatternEditorTopComponent());

    display = new DisplayPanel();
    this.add(display, BorderLayout.CENTER);

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();

    putClientProperty("print.printable", Boolean.TRUE);	// this can be printed
  }

  /**
   * Update the pattern manager when the ExplorerManager rootContext changes.
   */
  private synchronized void updatePatternMgr() {
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_PatternEditorTopComponent() + ": (no patterns)");
      patternMgr = null;
      setSelected(null);
    } else {
      setName(Bundle.CTL_PatternEditorTopComponent() + ": " + rootNode.getDisplayName());
      patternMgr = rootNode.getLookup().lookup(Patterns.class);
      if (patternMgr.getAllCustom().size() > 0) {
        setSelected(patternMgr.getAllCustom().get(0));    // set to first custom pattern
      } else {
        setSelected(patternMgr.getDefaultPattern());      // else the default
      }
    }
  }

  /**
   * Set the selected pattern to the given pattern and update the display.
   *
   * @param pattern new pattern
   */
  private synchronized void setSelected(Pattern pattern) {
    if ((selected != null) && (!selected.isBuiltIn())) {    // quit listening to the old one
      ((CustomPattern) selected).removePropertyChangeListener(display);
    }
    this.selected = pattern;
    patternCombo.removeAllItems();    // update the patternCombo control
    if (patternMgr != null) {
      patternMgr.getAllDisplayNames().stream().forEach((s) -> {
        patternCombo.addItem(s);
      });
    }
    if (selected == null || selected.isBuiltIn()) {
      newButton.setEnabled(selected != null);
      clearButton.setEnabled(false);
      deleteButton.setEnabled(false);
      normalButton.setEnabled(false);
      mirrorButton.setEnabled(false);
      invertButton.setEnabled(false);
    } else {
      newButton.setEnabled(true);
      clearButton.setEnabled(true);
      deleteButton.setEnabled(true);
      normalButton.setEnabled(true);
      mirrorButton.setEnabled(true);
      invertButton.setEnabled(true);
      ((CustomPattern) selected).addPropertyChangeListener(display);  // listen for changes in the pattern
    }
    if (selected != null) {
      nameLabel.setText(selected.getName());
      if (selected.isBuiltIn()) {
        msgLabel.setText("Built-In pattern cannot be edited");
      } else if (((CustomPattern) selected).needToNormalize()) {
        msgLabel.setText("Extent must be -1.0 to 1.0 in x and minimum y = 0.0!");
      } else {
        msgLabel.setText("");
      }
      patternCombo.setSelectedIndex(patternMgr.indexOf(selected.getName()));
    } else {
      nameLabel.setText("");
      msgLabel.setText("");
    }
    display.repaint();		// does plotPanel.repaint() too
  }

  /**
   * Handle the addition/deletion of a pattern.
   *
   * @param evt
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("PatternEditorTopComponent.propertyChange: " + evt.getSource().getClass().getName()
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

    jPanel1 = new javax.swing.JPanel();
    patternPanel = new javax.swing.JPanel();
    patternCombo = new javax.swing.JComboBox();
    iconPanel = new IconPanel();
    nameLabel = new javax.swing.JLabel();
    coordsLabel = new javax.swing.JLabel();
    msgLabel = new javax.swing.JLabel();
    newButton = new javax.swing.JButton();
    normalButton = new javax.swing.JButton();
    deleteButton = new javax.swing.JButton();
    clearButton = new javax.swing.JButton();
    mirrorButton = new javax.swing.JButton();
    invertButton = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    patternPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.patternPanel.border.title"))); // NOI18N

    patternCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BigSmall Inverse" }));
    patternCombo.setToolTipText(org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.patternCombo.toolTipText")); // NOI18N
    patternCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        patternCombochangePattern(evt);
      }
    });

    iconPanel.setBackground(new java.awt.Color(255, 255, 255));

    javax.swing.GroupLayout iconPanelLayout = new javax.swing.GroupLayout(iconPanel);
    iconPanel.setLayout(iconPanelLayout);
    iconPanelLayout.setHorizontalGroup(
      iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 60, Short.MAX_VALUE)
    );
    iconPanelLayout.setVerticalGroup(
      iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 40, Short.MAX_VALUE)
    );

    org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.nameLabel.text")); // NOI18N

    javax.swing.GroupLayout patternPanelLayout = new javax.swing.GroupLayout(patternPanel);
    patternPanel.setLayout(patternPanelLayout);
    patternPanelLayout.setHorizontalGroup(
      patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(patternPanelLayout.createSequentialGroup()
        .addGroup(patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(patternCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(patternPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(nameLabel))
          .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(37, Short.MAX_VALUE))
    );
    patternPanelLayout.setVerticalGroup(
      patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(patternPanelLayout.createSequentialGroup()
        .addComponent(patternCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(4, 4, 4)
        .addComponent(nameLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    org.openide.awt.Mnemonics.setLocalizedText(coordsLabel, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.coordsLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(msgLabel, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.msgLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.newButton.text")); // NOI18N
    newButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newButtonaddPattern(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(normalButton, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.normalButton.text")); // NOI18N
    normalButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        normalButtonnormalize(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.deleteButton.text")); // NOI18N
    deleteButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteButtondelete(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(clearButton, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.clearButton.text")); // NOI18N
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearButtonclear(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(mirrorButton, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.mirrorButton.text")); // NOI18N
    mirrorButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        mirrorButtonmirror(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(invertButton, org.openide.util.NbBundle.getMessage(PatternEditorTopComponent.class, "PatternEditorTopComponent.invertButton.text")); // NOI18N
    invertButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        invertButtoninvert(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(patternPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(coordsLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(newButton))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(msgLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(normalButton))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(0, 135, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(invertButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteButton))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(mirrorButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearButton))))))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(patternPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(newButton)
          .addComponent(coordsLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(normalButton)
          .addComponent(msgLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(deleteButton)
          .addComponent(invertButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(clearButton)
          .addComponent(mirrorButton)))
    );

    add(jPanel1, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

  private void patternCombochangePattern(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patternCombochangePattern
    if (patternCombo.isFocusOwner()) {
      setSelected(patternMgr.get(patternCombo.getSelectedIndex()));
    }
  }//GEN-LAST:event_patternCombochangePattern

  private void newButtonaddPattern(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonaddPattern
    AddPatternPanel panel = new AddPatternPanel();
    DialogDescriptor dd = new DialogDescriptor(
        panel,
        "Add Custom Pattern",
        true,
        DialogDescriptor.OK_CANCEL_OPTION,
        DialogDescriptor.OK_OPTION,
        null);
    Object result = DialogDisplayer.getDefault().notify(dd);
    if (result == DialogDescriptor.OK_OPTION) {
      String name = panel.nameField.getText();
      CustomPattern newProf;
      if (panel.straightButton.isSelected()) {
        newProf = new CustomPattern(name, CustomStyle.STRAIGHT);
      } else if (panel.arcButton.isSelected()) {
        newProf = new CustomPattern(name, CustomStyle.ARCS);
      } else if (panel.trigButton.isSelected()) {
        newProf = new CustomPattern(name, CustomStyle.TRIG);
      } else {
        newProf = new CustomPattern(name, CustomStyle.CURVE);
      }
      if (patternMgr.nameExists(newProf.getName())) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Pattern Name " + newProf.getName() + " already exists!",
            NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
      } else {
        patternMgr.add(newProf);
        setSelected(newProf);
      }
    }
  }//GEN-LAST:event_newButtonaddPattern

  private void normalButtonnormalize(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normalButtonnormalize
    if (!selected.isBuiltIn()) {
      ((CustomPattern) selected).normalize();
    }
  }//GEN-LAST:event_normalButtonnormalize

  private void deleteButtondelete(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtondelete
    if (!selected.isBuiltIn()) {
      patternMgr.delete((CustomPattern) selected);
      setSelected(patternMgr.getDefaultPattern());
    }
  }//GEN-LAST:event_deleteButtondelete

  private void clearButtonclear(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonclear
    if (!selected.isBuiltIn()) {
      ((CustomPattern) selected).clear();
    }
  }//GEN-LAST:event_clearButtonclear

  private void mirrorButtonmirror(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirrorButtonmirror
    if (!selected.isBuiltIn()) {
      ((CustomPattern) selected).mirror();
    }
  }//GEN-LAST:event_mirrorButtonmirror

  private void invertButtoninvert(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertButtoninvert
    if (!selected.isBuiltIn()) {
      ((CustomPattern) selected).invert();
    }
  }//GEN-LAST:event_invertButtoninvert

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearButton;
  private javax.swing.JLabel coordsLabel;
  private javax.swing.JButton deleteButton;
  private javax.swing.JPanel iconPanel;
  private javax.swing.JButton invertButton;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JButton mirrorButton;
  private javax.swing.JLabel msgLabel;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JButton newButton;
  private javax.swing.JButton normalButton;
  private javax.swing.JComboBox patternCombo;
  private javax.swing.JPanel patternPanel;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>PatternEditorTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>PatternEditorTopComponent.componentOpened em=" + em);
    updatePatternMgr();
    em.addPropertyChangeListener(this);

//    // TODO: Problem when Reset Windows -- listeners are added again!
//    // so for now, remove them all first if they are present
//    removeMouseListener(display);
//    removeMouseMotionListener(display);
    // Then add them again
    addMouseListener(display);
    addMouseMotionListener(display);
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>PatternEditorTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
    removeMouseListener(display);
    removeMouseMotionListener(display);
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
   * Nested inner class for a panel showing a small plot of the pattern.
   */
  private class IconPanel extends JPanel {

    /** Construct a new PlotPanel. */
    public IconPanel() {
      super();
      this.setPreferredSize(new Dimension(Pattern.DEFAULT_WIDTH, Pattern.DEFAULT_HEIGHT));
    }

    /**
     * Paints each of the components in this container.
     *
     * @param g Graphics g
     */
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (selected == null) {
        return;
      }
      if (selected.needsOptions()) {
        g.drawImage(selected.getImage(5, 4, 0.2), 0, 0, null);	// Use dummy values for a representative picture
      } else if (selected.needsRepeat()) {
        g.drawImage(selected.getImage(5), 0, 0, null);	// just use 5 for repeat
      } else {
        g.drawImage(selected.getImage(), 0, 0, null);
      }
    }
  }

  /**
   * Nested Class -- Panel for displaying a custom pattern
   *
   * @author Bill Ooms. Copyright 2014 Studio of Bill Ooms. All rights reserved.
   */
  private class DisplayPanel extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener {

    private final DecimalFormat F3 = new DecimalFormat("0.000");

    private final Color DEFAULT_BACKGROUND = Color.WHITE;
    private final double WINDOW_PERCENT = 0.95;	  // use 95% of the window for the plot
    private final double INITIAL_DPI = 100.0;	  // for the first time the window comes up
    private final Point INITIAL_ZPIX = new Point(150, 200);   // arbitrary
    private final int CLOSEST = 7;		// Grab point within 7 pixels

    private final Cursor EDIT_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);

    /** Dots per inch in X and Y. */
    private final Point2D.Double dpi = new Point2D.Double();
    /** Location of zero. */
    private Point zeroPix = INITIAL_ZPIX;   // initial value is arbitrary
    /** The grid for the plot. */
    private final Grid grid;

    /** Point being moved/dragged. */
    private Pt grabbedPt;
    /** Flag indicating that we are measuring. */
    private boolean measuring = false;
    /** Original position of a point before dragging. */
    private Point2D.Double savedPos;

    /** Creates new DisplayPanel */
    public DisplayPanel() {
      setBackground(DEFAULT_BACKGROUND);
      setCursor(EDIT_CURSOR);
      grid = new Grid(0.0, 0.0, 1.01, 1.01);	// slightly larger than 1.0 so we see top and right lines
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      dpi.x = WINDOW_PERCENT * this.getWidth();
      dpi.y = WINDOW_PERCENT * this.getHeight() * 0.8;    // leave 20% below the line for negative
      zeroPix = new Point((int) (getWidth() - dpi.x) / 2, (int) (getHeight() - dpi.y) / 2 + (int) (dpi.y * 0.9)); // no room on top, 20% on bottom

      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(zeroPix.x, zeroPix.y);
      g2d.scale(dpi.x, -dpi.y);	// positive y is up

      // Paint the grid
      grid.paint(g2d);

      if (selected == null) {
        return;
      }

      if (selected.isBuiltIn()) {
        msgLabel.setText("Built-In pattern cannot be edited");
      } else if (((CustomPattern) selected).needToNormalize()) {
        msgLabel.setText("Extent must be -1.0 to 1.0 in x and minimum y = 0.0!");
      } else {
        msgLabel.setText("");
      }
      selected.paint(g2d);
      iconPanel.repaint();
    }

    /**
     * Listen for changes and repaint
     *
     * @param evt event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(CustomPattern.PROP_REQ_DELETE)) {
        if (evt.getSource() instanceof CustomPattern) {
          setSelected(patternMgr.getDefaultPattern());	// also removes this as propListener
        }
      } else {
        repaint();		// when things change, just repaint
      }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() >= 2) {
        Point2D.Double pt = scalePixToInch(new Point(e.getX(), e.getY()));
        if (!selected.isBuiltIn()) {
          ((CustomPattern) selected).addPt(pt);
        }
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
      if (e.getClickCount() == 1) {
        Point2D.Double pt = scalePixToInch(new Point(e.getX(), e.getY()));
        measuring = true;		// enable measuring
        savedPos = scalePixToInch(e.getPoint());
        if (!selected.isBuiltIn()) {
          grabbedPt = ((CustomPattern) selected).getLine().closestPt(pt, (double) CLOSEST / dpi.x); // arbitrarily choose dpi.x
          if (grabbedPt != null) {
            setCursor(MOVE_CURSOR);
            savedPos = grabbedPt.getPoint2D();
          }
        }
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      Point p = new Point(e.getX(), e.getY());
      Point2D.Double pt = scalePixToInch(p);
      if (measuring) {
        double d = Math.hypot(pt.x - savedPos.x, pt.y - savedPos.y);
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y) + ", distance: " + F3.format(d));
      } else {
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y));
      }
      if (grabbedPt != null) {
        grabbedPt.drag(pt);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      Point p = new Point(e.getX(), e.getY());
      Point2D.Double pt = scalePixToInch(p);
      if (measuring) {
        double d = Math.hypot(pt.x - savedPos.x, pt.y - savedPos.y);
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y) + ", distance: " + F3.format(d));
        measuring = false;
      } else {
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y));
      }
      if (grabbedPt != null) {
        grabbedPt.move(pt);
        grabbedPt.setColor(BasicPattern.IMAGE_COLOR);
        grabbedPt = null;
        setCursor(EDIT_CURSOR);
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      if (grabbedPt != null) {
        ((CustomPattern) selected).deletePt(grabbedPt);
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      Point2D.Double pt = scalePixToInch(new Point(e.getX(), e.getY()));
      coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Convert a point in pixels to inches
     *
     * @param p point in pixels
     * @return point in inches
     */
    private Point2D.Double scalePixToInch(Point p) {
      return new Point2D.Double((double) (p.x - zeroPix.x) / dpi.x,
          (double) (zeroPix.y - p.y) / dpi.y);
    }
  }
}
