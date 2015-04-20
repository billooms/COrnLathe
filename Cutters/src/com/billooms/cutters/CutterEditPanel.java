package com.billooms.cutters;

import com.billooms.profiles.Profile;
import com.billooms.profiles.Profiles;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;

/**
 * Panel for editing a cutter.
 *
 * Note: Scroll wheel direction is based on "natural" touch pad setting.
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
public class CutterEditPanel extends JPanel implements PropertyChangeListener {

  /** All CustomProfile property change names start with this prefix */
  public final static String PROP_PREFIX = "CutterEditPanel" + "_";
  /** Property name used when changing the name or display name */
  public final static String PROP_CUTTER = PROP_PREFIX + "Cutter";

  private Cutters cutMgr = null;    // Everything is initiated when a cutMgr is given (may be null)
  private Profiles profMgr = null;  // A profile manager is gotten from the cutMgr
  private Cutter cutter = null;	    // A cutter is selected from the cutMgr list

  protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /**
   * Creates new form CutterEditPanel
   */
  public CutterEditPanel() {
    initComponents();

    frameCombo.removeAllItems();
    for (Frame f : Frame.values()) {
      frameCombo.addItem(f.toString());
    }

    locationCombo.removeAllItems();
    for (Location loc : Location.values()) {
      locationCombo.addItem(loc.toString());
    }

    updateForm();
  }

  /**
   * Get the currently selected cutter.
   *
   * @return currently selected cutter
   */
  public Cutter getCutter() {
    return cutter;
  }

  /**
   * Set a new cutter manager (such as when the ExplorerManager changes its
   * RootNode). This will also set a new profile manager (gotten from the cutter
   * manager) and set the selected cutter to the first cutter in the new manager
   * (unless the cutter manager is null).
   *
   * @param newCutMgr new cutter manager
   */
  public synchronized void setCutMgr(Cutters newCutMgr) {
    if (cutMgr != null) {
      cutMgr.removePropertyChangeListener(this);  // quit listening to the old one
    }
    if (profMgr != null) {
      profMgr.removePropertyChangeListener(this);
    }

    this.cutMgr = newCutMgr;
    updateCutterCombo();

    if (cutMgr == null) {
      profMgr = null;
    } else {
      profMgr = cutMgr.getProfileMgr();
      profMgr.addPropertyChangeListener(this);
    }
    updateProfileCombo();

    if (cutMgr == null) {
      setCutter(null);
      profMgr = null;
    } else {
      setCutter(cutMgr.get(0));			// arbitrarily select the first one
      cutMgr.addPropertyChangeListener(this);	// start listening to the new one
    }
  }

  /**
   * Select a new cutter (which could be null).
   *
   * @param newCutter
   */
  private void setCutter(Cutter newCutter) {
    if (cutter != null) {
      cutter.removePropertyChangeListener(this);
    }
    Cutter old = this.cutter;
    this.cutter = newCutter;
    if (cutter != null) {
      cutterCombo.setSelectedIndex(cutMgr.indexOf(cutter.getName()));
      cutter.addPropertyChangeListener(this);
    }
    updateForm();
    pcs.firePropertyChange(PROP_CUTTER, old, cutter);
  }

  /**
   * Update the items in the cutterCombo based on all the current profiles in
   * the cutter manager. Note that this does not set the selection.
   */
  private void updateCutterCombo() {
    cutterCombo.removeAllItems();
    if (cutMgr != null) {
      cutMgr.getAllNames().stream().forEach((str) -> {
        cutterCombo.addItem(str);
      });
    }
    cutterCombo.setEnabled(cutMgr != null);
  }

  /**
   * Update the items in the profileCombo based on all the current profiles in
   * the profile manager. Note that this does not set the selection.
   */
  private void updateProfileCombo() {
    profileCombo.removeAllItems();
    if (profMgr != null) {
      profMgr.getAllDisplayNames().stream().forEach((str) -> {
        profileCombo.addItem(str);
      });
    }
    profileCombo.setEnabled(cutMgr != null);
  }

  /**
   * Update all fields on the form from cutter data.
   */
  private void updateForm() {
    if (cutter == null) {
      diaField.setEnabled(false);
      radiusField.setEnabled(false);
      angleUCFField.setEnabled(false);
      rotUCFField.setEnabled(false);
      return;
    }

    locationCombo.setSelectedIndex(cutter.getLocation().ordinal());
    dirPicture.setIcon(cutter.getLocation().getIcon());

    profileCombo.setSelectedIndex(profMgr.indexOf(cutter.getProfile().getName()));
    profilePanel.repaint();

    frameCombo.setSelectedIndex(cutter.getFrame().ordinal());
    radiusField.setValue(cutter.getRadius());
    diaField.setValue(cutter.getTipWidth());
    angleUCFField.setValue(cutter.getUCFAngle());
    rotUCFField.setValue(cutter.getUCFRotate());

    // Enable/Disable fields depending on cutter frame
    diaField.setEnabled(true);
    switch (cutter.getFrame()) {
      case HCF:
        radiusField.setEnabled(true);
        angleUCFField.setEnabled(false);
        rotUCFField.setEnabled(false);
        break;
      case UCF:
        radiusField.setEnabled(true);
        angleUCFField.setEnabled(true);
        rotUCFField.setEnabled(true);
        break;
      case Drill:
        radiusField.setEnabled(false);
        angleUCFField.setEnabled(true);
        rotUCFField.setEnabled(false);
        break;
      case ECF:
        radiusField.setEnabled(true);
        angleUCFField.setEnabled(true);
        rotUCFField.setEnabled(false);
        break;
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CutterEditPanel.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens to cutter and cutMgr and profMgr
    String prop = evt.getPropertyName();
    if (prop.startsWith(Cutters.PROP_PREFIX) || prop.equals(Cutter.PROP_NAME)) {	      // watch for cutter manager changes
      // when cutter manager changes, it must be because of Add/Delete cutter
      String oldCutter = (String) cutterCombo.getSelectedItem();
      updateCutterCombo();
      if (cutMgr.nameExists(oldCutter)) {
        setCutter(cutMgr.getCutter(oldCutter));
      } else {
        setCutter(cutMgr.get(0));
      }
    } else if (prop.startsWith(Cutter.PROP_PREFIX)) {	  // watch for cutter changes
      updateForm();
    } else if (prop.startsWith(Profiles.PROP_PREFIX)) {
      updateProfileCombo();
    }
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /** Nested inner class for a panel showing a small plot of the profile. */
  private class IconPanel extends JPanel {

    private final static int MARGIN = 1;	// 1px margin top, bottom, left
    private final static int RIGHT_MARGIN = 4;	// 4px margin right

    /** Create a new PlotPanel. */
    public IconPanel() {
      super();
      this.setPreferredSize(new Dimension(Profile.DEFAULT_WIDTH + MARGIN + RIGHT_MARGIN,
          Profile.DEFAULT_HEIGHT + 2 * MARGIN));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (profMgr != null) {
        Profile pro = profMgr.get(profileCombo.getSelectedIndex());
        g.drawImage(pro.getImage(), MARGIN, MARGIN, null);
      }
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    locationPanel = new javax.swing.JPanel();
    locationCombo = new javax.swing.JComboBox();
    dirPicture = new javax.swing.JLabel();
    profPanel = new javax.swing.JPanel();
    profilePanel = new IconPanel();
    profileCombo = new javax.swing.JComboBox();
    jLabel3 = new javax.swing.JLabel();
    cutterCombo = new javax.swing.JComboBox();
    jLabel4 = new javax.swing.JLabel();
    frameCombo = new javax.swing.JComboBox();
    jLabel1 = new javax.swing.JLabel();
    radiusField = new javax.swing.JFormattedTextField();
    jLabel2 = new javax.swing.JLabel();
    diaField = new javax.swing.JFormattedTextField();
    jLabel5 = new javax.swing.JLabel();
    angleUCFField = new javax.swing.JFormattedTextField();
    jLabel6 = new javax.swing.JLabel();
    rotUCFField = new javax.swing.JFormattedTextField();

    setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.border.title"))); // NOI18N

    locationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.locationPanel.border.title"))); // NOI18N

    locationCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    locationCombo.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.locationCombo.toolTipText")); // NOI18N
    locationCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeLocation(evt);
      }
    });

    dirPicture.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.dirPicture.text")); // NOI18N

    javax.swing.GroupLayout locationPanelLayout = new javax.swing.GroupLayout(locationPanel);
    locationPanel.setLayout(locationPanelLayout);
    locationPanelLayout.setHorizontalGroup(
      locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(locationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGroup(locationPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(dirPicture))
    );
    locationPanelLayout.setVerticalGroup(
      locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(locationPanelLayout.createSequentialGroup()
        .addComponent(locationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(dirPicture))
    );

    profPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.profPanel.border.title"))); // NOI18N

    profilePanel.setBackground(new java.awt.Color(255, 255, 255));

    javax.swing.GroupLayout profilePanelLayout = new javax.swing.GroupLayout(profilePanel);
    profilePanel.setLayout(profilePanelLayout);
    profilePanelLayout.setHorizontalGroup(
      profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 64, Short.MAX_VALUE)
    );
    profilePanelLayout.setVerticalGroup(
      profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 21, Short.MAX_VALUE)
    );

    profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ideal" }));
    profileCombo.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.profileCombo.toolTipText")); // NOI18N
    profileCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeProfile(evt);
      }
    });

    javax.swing.GroupLayout profPanelLayout = new javax.swing.GroupLayout(profPanel);
    profPanel.setLayout(profPanelLayout);
    profPanelLayout.setHorizontalGroup(
      profPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(profileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addComponent(profilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );
    profPanelLayout.setVerticalGroup(
      profPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(profPanelLayout.createSequentialGroup()
        .addComponent(profileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(profilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    jLabel3.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.jLabel3.text")); // NOI18N

    cutterCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    cutterCombo.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.cutterCombo.toolTipText")); // NOI18N
    cutterCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeCutter(evt);
      }
    });

    jLabel4.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.jLabel4.text")); // NOI18N

    frameCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    frameCombo.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.frameCombo.toolTipText")); // NOI18N
    frameCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeFrame(evt);
      }
    });

    jLabel1.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.jLabel1.text")); // NOI18N

    radiusField.setColumns(4);
    radiusField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    radiusField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    radiusField.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.radiusField.toolTipText")); // NOI18N
    radiusField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    radiusField.setValue(Cutter.DEFAULT_RADIUS);
    radiusField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollCutRadius(evt);
      }
    });
    radiusField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeCutRadius(evt);
      }
    });

    jLabel2.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.jLabel2.text")); // NOI18N

    diaField.setColumns(4);
    diaField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0000"))));
    diaField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    diaField.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.diaField.toolTipText")); // NOI18N
    diaField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    diaField.setValue(Cutter.DEFAULT_DIAMETER);
    diaField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollDiameter(evt);
      }
    });
    diaField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeDiameter(evt);
      }
    });

    jLabel5.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.jLabel5.text")); // NOI18N

    angleUCFField.setColumns(4);
    angleUCFField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0"))));
    angleUCFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    angleUCFField.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.angleUCFField.toolTipText")); // NOI18N
    angleUCFField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    angleUCFField.setValue(Cutter.DEFAULT_UCF_ANGLE);
    angleUCFField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollUCFAngle(evt);
      }
    });
    angleUCFField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeUCFAngle(evt);
      }
    });

    jLabel6.setText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.jLabel6.text")); // NOI18N

    rotUCFField.setColumns(4);
    rotUCFField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0"))));
    rotUCFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    rotUCFField.setToolTipText(org.openide.util.NbBundle.getMessage(CutterEditPanel.class, "CutterEditPanel.rotUCFField.toolTipText")); // NOI18N
    rotUCFField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    rotUCFField.setValue(Cutter.DEFAULT_UCF_ROTATE);
    rotUCFField.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        scrollUCFRotation(evt);
      }
    });
    rotUCFField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeUCFRotation(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(locationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(profPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cutterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(frameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(radiusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(diaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(angleUCFField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(rotUCFField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(155, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(locationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addComponent(profPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(cutterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel4)
          .addComponent(frameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(radiusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel2)
          .addComponent(diaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(jLabel6)
            .addComponent(rotUCFField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(jLabel5)
            .addComponent(angleUCFField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
    );
  }// </editor-fold>//GEN-END:initComponents

private void changeProfile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeProfile
  if (profileCombo.isFocusOwner()) {
    if (profMgr != null) {
      cutter.setProfile(profMgr.get(profileCombo.getSelectedIndex()));
    }
  }
}//GEN-LAST:event_changeProfile

private void changeLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeLocation
  if (locationCombo.isFocusOwner()) {
    if (cutter != null) {
      cutter.setLocation(Location.values()[locationCombo.getSelectedIndex()]);
    }
  }
}//GEN-LAST:event_changeLocation

private void changeFrame(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeFrame
  if (frameCombo.isFocusOwner()) {
    if (cutter != null) {
      cutter.setFrame(Frame.values()[frameCombo.getSelectedIndex()]);
    }
  }
}//GEN-LAST:event_changeFrame

private void scrollUCFAngle(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollUCFAngle
  if (angleUCFField.isFocusOwner() && (cutter != null)) {
    double ucfAngle = ((Number) angleUCFField.getValue()).doubleValue() + 1.0 * evt.getWheelRotation();
    ucfAngle = (Math.max(Math.min(ucfAngle, 180.0), -180.0));    // range -180 to 180
    cutter.setUCFAngle(ucfAngle);
  }
}//GEN-LAST:event_scrollUCFAngle

private void scrollUCFRotation(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollUCFRotation
  if (rotUCFField.isFocusOwner() && (cutter != null)) {
    double ucfRotate = ((Number) rotUCFField.getValue()).doubleValue() + 1.0 * evt.getWheelRotation();
    ucfRotate = (Math.max(Math.min(ucfRotate, 180.0), -180.0));       // range -180 to 180
    cutter.setUCFRotate(ucfRotate);
  }
}//GEN-LAST:event_scrollUCFRotation

private void scrollCutRadius(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollCutRadius
  if (radiusField.isFocusOwner() && (cutter != null)) {
    cutter.setRadius(Math.max(((Number) radiusField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation(), 0.0));       // Don'c go less than 0.0
  }
}//GEN-LAST:event_scrollCutRadius

private void scrollDiameter(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollDiameter
  if (diaField.isFocusOwner() && (cutter != null)) {
    double rodDiameter = ((Number) diaField.getValue()).doubleValue() + 0.001 * evt.getWheelRotation();
    rodDiameter = Math.max(rodDiameter, 0.0);       // Don'c go less than 0.0
    cutter.setTipWidth(rodDiameter);
  }
}//GEN-LAST:event_scrollDiameter

	private void changeCutRadius(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeCutRadius
      if (radiusField.isFocusOwner() && (cutter != null)) {
        cutter.setRadius(((Number) radiusField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeCutRadius

	private void changeUCFAngle(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeUCFAngle
      if (angleUCFField.isFocusOwner() && (cutter != null)) {
        cutter.setUCFAngle(((Number) angleUCFField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeUCFAngle

	private void changeDiameter(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeDiameter
      if (diaField.isFocusOwner() && (cutter != null)) {
        cutter.setTipWidth(((Number) diaField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeDiameter

	private void changeUCFRotation(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeUCFRotation
      if (rotUCFField.isFocusOwner() && (cutter != null)) {
        cutter.setUCFRotate(((Number) rotUCFField.getValue()).doubleValue());
      }
	}//GEN-LAST:event_changeUCFRotation

  private void changeCutter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeCutter
    if (cutterCombo.isFocusOwner()) {
      if (cutMgr != null) {
        setCutter(cutMgr.get(cutterCombo.getSelectedIndex()));
      }
    }
  }//GEN-LAST:event_changeCutter

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFormattedTextField angleUCFField;
  private javax.swing.JComboBox cutterCombo;
  private javax.swing.JFormattedTextField diaField;
  private javax.swing.JLabel dirPicture;
  private javax.swing.JComboBox frameCombo;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JComboBox locationCombo;
  private javax.swing.JPanel locationPanel;
  private javax.swing.JPanel profPanel;
  private javax.swing.JComboBox profileCombo;
  private javax.swing.JPanel profilePanel;
  private javax.swing.JFormattedTextField radiusField;
  private javax.swing.JFormattedTextField rotUCFField;
  // End of variables declaration//GEN-END:variables
}
