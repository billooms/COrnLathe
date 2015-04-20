package com.billooms.profileeditor;

import com.billooms.drawables.Grid;
import com.billooms.drawables.SquarePt;
import com.billooms.profiles.BasicProfile;
import com.billooms.profiles.CustomProfile;
import com.billooms.profiles.CustomProfile.CustomStyle;
import com.billooms.profiles.Profile;
import com.billooms.profiles.Profiles;
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
 * Top component for editing custom profiles.
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
    dtd = "-//com.billooms.profileeditor//ProfileEditor//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "ProfileEditorTopComponent",
    iconBase = "com/billooms/profileeditor/Profile16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.billooms.profileeditor.ProfileEditorTopComponent")
@ActionReference(path = "Menu/Window", position = 600)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ProfileEditorAction",
    preferredID = "ProfileEditorTopComponent"
)
@Messages({
  "CTL_ProfileEditorAction=ProfileEditor",
  "CTL_ProfileEditorTopComponent=ProfileEditor Window",
  "HINT_ProfileEditorTopComponent=This is a ProfileEditor window"
})
public final class ProfileEditorTopComponent extends TopComponent implements PropertyChangeListener {

  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final DisplayPanel display;	      // panel for displaying the graphics
  private Profile selected = null;	      // Profile being edited
  private Profiles profileMgr = null;	      // Profile Manager

  public ProfileEditorTopComponent() {
    initComponents();
    setName(Bundle.CTL_ProfileEditorTopComponent());
    setToolTipText(Bundle.HINT_ProfileEditorTopComponent());

    display = new DisplayPanel();
    this.add(display, BorderLayout.CENTER);

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();

    putClientProperty("print.printable", Boolean.TRUE);	// this can be printed
  }

  private synchronized void updateProfileMgr() {
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_ProfileEditorTopComponent() + ": (no profiles)");
      profileMgr = null;
      setSelected(null);
    } else {
      setName(Bundle.CTL_ProfileEditorTopComponent() + ": " + rootNode.getDisplayName());
      profileMgr = rootNode.getLookup().lookup(Profiles.class);
      if (profileMgr.getAllCustom().size() > 0) {
        setSelected(profileMgr.getAllCustom().get(0));    // set to first custom pattern
      } else {
        setSelected(profileMgr.getDefaultProfile());      // else the default
      }
    }
  }

  /**
   * Set the selected profile to the given profile and update the display.
   *
   * @param profile new profile
   */
  private synchronized void setSelected(Profile profile) {
    if ((selected != null) && (!selected.isBuiltIn())) {    // quit listening to the old one
      ((CustomProfile) selected).removePropertyChangeListener(display);
    }
    this.selected = profile;
    profileCombo.removeAllItems();    // update the profileCombo control
    if (profileMgr != null) {
      profileMgr.getAllDisplayNames().stream().forEach((s) -> {
        profileCombo.addItem(s);
      });
    }
    if (selected == null || selected.isBuiltIn()) {
      newButton.setEnabled(selected != null);
      clearButton.setEnabled(false);
      deleteButton.setEnabled(false);
      normalButton.setEnabled(false);
      mirrorButton.setEnabled(false);
    } else {
      newButton.setEnabled(true);
      clearButton.setEnabled(true);
      deleteButton.setEnabled(true);
      normalButton.setEnabled(true);
      mirrorButton.setEnabled(true);
      ((CustomProfile) selected).addPropertyChangeListener(display);  // listen for changes in the profile
    }
    if (selected != null) {
      nameLabel.setText(selected.getName());
      if (selected.isBuiltIn()) {
        msgLabel.setText("Built-In profile cannot be edited");
      } else if (((CustomProfile) selected).needToNormalize()) {
        msgLabel.setText("Extent must be -1.0 to 1.0 in x and minimum y = 0.0!");
      } else {
        msgLabel.setText("");
      }
      profileCombo.setSelectedIndex(profileMgr.indexOf(selected.getName()));
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
//    System.out.println("ProfileEditorTopComponent.propertyChange: " + evt.getSource().getClass().getName()
//	    + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Refresh the profileMgr when rootContext changes on the ExplorerManager
    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
      updateProfileMgr();
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jPanel1 = new javax.swing.JPanel();
    profilePanel = new javax.swing.JPanel();
    profileCombo = new javax.swing.JComboBox();
    iconPanel = new IconPanel();
    nameLabel = new javax.swing.JLabel();
    msgLabel = new javax.swing.JLabel();
    newButton = new javax.swing.JButton();
    coordsLabel = new javax.swing.JLabel();
    normalButton = new javax.swing.JButton();
    deleteButton = new javax.swing.JButton();
    clearButton = new javax.swing.JButton();
    mirrorButton = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    profilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.profilePanel.border.title"))); // NOI18N

    profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "(empty)" }));
    profileCombo.setToolTipText(org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.profileCombo.toolTipText")); // NOI18N
    profileCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        profileCombochangeProfile(evt);
      }
    });

    iconPanel.setBackground(new java.awt.Color(255, 255, 255));

    javax.swing.GroupLayout iconPanelLayout = new javax.swing.GroupLayout(iconPanel);
    iconPanel.setLayout(iconPanelLayout);
    iconPanelLayout.setHorizontalGroup(
      iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 102, Short.MAX_VALUE)
    );
    iconPanelLayout.setVerticalGroup(
      iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 32, Short.MAX_VALUE)
    );

    org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.nameLabel.text")); // NOI18N

    javax.swing.GroupLayout profilePanelLayout = new javax.swing.GroupLayout(profilePanel);
    profilePanel.setLayout(profilePanelLayout);
    profilePanelLayout.setHorizontalGroup(
      profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(profilePanelLayout.createSequentialGroup()
        .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(profileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(profilePanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(nameLabel))
          .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(37, Short.MAX_VALUE))
    );
    profilePanelLayout.setVerticalGroup(
      profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(profilePanelLayout.createSequentialGroup()
        .addComponent(profileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(4, 4, 4)
        .addComponent(nameLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(iconPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    org.openide.awt.Mnemonics.setLocalizedText(msgLabel, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.msgLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.newButton.text")); // NOI18N
    newButton.setEnabled(false);
    newButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newButtonaddProfile(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(coordsLabel, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.coordsLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(normalButton, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.normalButton.text")); // NOI18N
    normalButton.setEnabled(false);
    normalButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        normalButtonnormalize(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.deleteButton.text")); // NOI18N
    deleteButton.setEnabled(false);
    deleteButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteButtondelete(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(clearButton, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.clearButton.text")); // NOI18N
    clearButton.setEnabled(false);
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearButtonclear(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(mirrorButton, org.openide.util.NbBundle.getMessage(ProfileEditorTopComponent.class, "ProfileEditorTopComponent.mirrorButton.text")); // NOI18N
    mirrorButton.setEnabled(false);
    mirrorButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        mirrorButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(profilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addComponent(coordsLabel)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(newButton))
            .addGroup(jPanel1Layout.createSequentialGroup()
              .addComponent(msgLabel)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 330, Short.MAX_VALUE)
              .addComponent(normalButton)))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addComponent(mirrorButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(deleteButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(clearButton))))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(newButton)
          .addComponent(coordsLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(normalButton)
          .addComponent(msgLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(clearButton)
          .addComponent(deleteButton)
          .addComponent(mirrorButton)))
      .addComponent(profilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    add(jPanel1, java.awt.BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents

  private void profileCombochangeProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileCombochangeProfile
    if (profileCombo.isFocusOwner()) {
      setSelected(profileMgr.get(profileCombo.getSelectedIndex()));
    }
  }//GEN-LAST:event_profileCombochangeProfile

  private void newButtonaddProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonaddProfile
    AddProfilePanel panel = new AddProfilePanel();
    DialogDescriptor dd = new DialogDescriptor(
        panel,
        "Add Cutter Profile",
        true,
        DialogDescriptor.OK_CANCEL_OPTION,
        DialogDescriptor.OK_OPTION,
        null);
    Object result = DialogDisplayer.getDefault().notify(dd);
    if (result == DialogDescriptor.OK_OPTION) {
      String name = panel.nameField.getText();
      CustomProfile newProf;
      if (panel.straightButton.isSelected()) {
        newProf = new CustomProfile(name, CustomStyle.STRAIGHT);
      } else {
        newProf = new CustomProfile(name, CustomStyle.CURVE);
      }
      if (profileMgr.nameExists(newProf.getName())) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Profile Name " + newProf.getName() + " already exists!",
            NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
      } else {
        profileMgr.add(newProf);
        setSelected(newProf);
      }
    }
  }//GEN-LAST:event_newButtonaddProfile

  private void normalButtonnormalize(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normalButtonnormalize
    if (!selected.isBuiltIn()) {
      ((CustomProfile) selected).normalize();
    }
  }//GEN-LAST:event_normalButtonnormalize

  private void deleteButtondelete(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtondelete
    if (!selected.isBuiltIn()) {
      profileMgr.delete((CustomProfile) selected);
      setSelected(profileMgr.getDefaultProfile());
    }
  }//GEN-LAST:event_deleteButtondelete

  private void clearButtonclear(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonclear
    if (!selected.isBuiltIn()) {
      ((CustomProfile) selected).clear();
    }
  }//GEN-LAST:event_clearButtonclear

  private void mirrorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirrorButtonActionPerformed
    if (!selected.isBuiltIn()) {
      ((CustomProfile) selected).mirror();
    }
  }//GEN-LAST:event_mirrorButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearButton;
  private javax.swing.JLabel coordsLabel;
  private javax.swing.JButton deleteButton;
  private javax.swing.JPanel iconPanel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JButton mirrorButton;
  private javax.swing.JLabel msgLabel;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JButton newButton;
  private javax.swing.JButton normalButton;
  private javax.swing.JComboBox profileCombo;
  private javax.swing.JPanel profilePanel;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>ProfileEditorTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>ProfileEditorTopComponent.componentOpened em=" + em);
    updateProfileMgr();
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
//    System.out.println("  >>ProfileEditorTopComponent.componentClosed");
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
   * Nested inner class for a panel showing a small plot of the profile.
   */
  private class IconPanel extends JPanel {

    private final static int MARGIN = 1;	// 1px margin top, bottom, left
    private final static int RIGHT_MARGIN = 4;	// 4px margin right

    /** Construct a new PlotPanel. */
    public IconPanel() {
      super();
      this.setPreferredSize(new Dimension(Profile.DEFAULT_WIDTH + MARGIN + RIGHT_MARGIN,
          Profile.DEFAULT_HEIGHT + 2 * MARGIN));
    }

    /**
     * Paints each of the components in this container.
     *
     * @param g Graphics g
     */
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (profileMgr != null) {
        Profile pro = profileMgr.get(profileCombo.getSelectedIndex());
        g.drawImage(pro.getImage(), MARGIN, MARGIN, null);
      }
    }
  }

  /**
   * Nested Class -- Panel for displaying a Rosette
   *
   * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
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

    /** Dots per inch. */
    private double dpi = INITIAL_DPI;
    /** Location of zero. */
    private Point zeroPix = INITIAL_ZPIX;   // initial value is arbitrary
    /** The grid for the plot. */
    private final Grid grid;

    /** Point being moved/dragged. */
    private SquarePt grabbedPt;
    /** Flag indicating that we are measuring. */
    private boolean measuring = false;
    /** Original position of a point before dragging. */
    private Point2D.Double savedPos;

    /** Creates new DisplayPanel */
    public DisplayPanel() {
      setBackground(DEFAULT_BACKGROUND);
      setCursor(EDIT_CURSOR);
      grid = new Grid(-1.01, 0.0, 2.02, 1.01);	// slightly larger than 1.0 so we see top and right lines
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      dpi = (int) Math.min((WINDOW_PERCENT * this.getWidth()) / 2.0,
          WINDOW_PERCENT * this.getHeight());
      zeroPix = new Point(getWidth() / 2, (int) (getHeight() - dpi) / 2 + (int) dpi);	// centered

      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(zeroPix.x, zeroPix.y);
      g2d.scale(dpi, -dpi);	// positive y is up

      // Paint the grid
      grid.paint(g2d);

      if (selected == null) {
        return;
      }

      if (selected.isBuiltIn()) {
        msgLabel.setText("Built-In profile cannot be edited");
      } else if (((CustomProfile) selected).needToNormalize()) {
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
      if (evt.getPropertyName().equals(CustomProfile.PROP_REQ_DELETE)) {
        if (evt.getSource() instanceof CustomProfile) {
          setSelected(profileMgr.getDefaultProfile());	// also removes this as propListener
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
          ((CustomProfile) selected).addPt(pt);
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
          grabbedPt = (SquarePt) ((CustomProfile) selected).getLine().closestPt(pt, (double) CLOSEST / dpi);
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
        grabbedPt.setColor(BasicProfile.IMAGE_COLOR);
        grabbedPt = null;
        setCursor(EDIT_CURSOR);
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      if (grabbedPt != null) {
        ((CustomProfile) selected).deletePt(grabbedPt);
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
      return new Point2D.Double((double) (p.x - zeroPix.x) / dpi,
          (double) (zeroPix.y - p.y) / dpi);
    }
  }
}
