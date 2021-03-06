package com.billooms.cornlatheprefs;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbPreferences;

/**
 * Preference panel.
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
final class COrnLathePanel extends JPanel {
  
  /** File Extension used */
  public final static String EXTENSION = "xml";

  private final COrnLatheOptionsPanelController controller;

  COrnLathePanel(COrnLatheOptionsPanelController controller) {
    this.controller = controller;
    initComponents();
    // TODO listen to changes in form fields and call controller.changed()
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    buttonGroup1 = new javax.swing.ButtonGroup();
    phaseShiftPanel = new javax.swing.JPanel();
    fracPhaseButton = new javax.swing.JRadioButton();
    engPhaseButton = new javax.swing.JRadioButton();
    cutterLibPanel = new javax.swing.JPanel();
    useLibCheck = new javax.swing.JCheckBox();
    browseButton = new javax.swing.JButton();
    filePathLabel = new javax.swing.JLabel();

    phaseShiftPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.phaseShiftPanel.border.title"))); // NOI18N

    buttonGroup1.add(fracPhaseButton);
    org.openide.awt.Mnemonics.setLocalizedText(fracPhaseButton, org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.fracPhaseButton.text")); // NOI18N

    buttonGroup1.add(engPhaseButton);
    engPhaseButton.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(engPhaseButton, org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.engPhaseButton.text")); // NOI18N

    javax.swing.GroupLayout phaseShiftPanelLayout = new javax.swing.GroupLayout(phaseShiftPanel);
    phaseShiftPanel.setLayout(phaseShiftPanelLayout);
    phaseShiftPanelLayout.setHorizontalGroup(
      phaseShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(engPhaseButton)
      .addComponent(fracPhaseButton)
    );
    phaseShiftPanelLayout.setVerticalGroup(
      phaseShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(phaseShiftPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(engPhaseButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(fracPhaseButton)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    cutterLibPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.cutterLibPanel.border.title"))); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(useLibCheck, org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.useLibCheck.text")); // NOI18N
    useLibCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeLibCheck(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.browseButton.text")); // NOI18N
    browseButton.setEnabled(false);
    browseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        browseFile(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(filePathLabel, org.openide.util.NbBundle.getMessage(COrnLathePanel.class, "COrnLathePanel.filePathLabel.text")); // NOI18N

    javax.swing.GroupLayout cutterLibPanelLayout = new javax.swing.GroupLayout(cutterLibPanel);
    cutterLibPanel.setLayout(cutterLibPanelLayout);
    cutterLibPanelLayout.setHorizontalGroup(
      cutterLibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(cutterLibPanelLayout.createSequentialGroup()
        .addGroup(cutterLibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(cutterLibPanelLayout.createSequentialGroup()
            .addComponent(useLibCheck)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(cutterLibPanelLayout.createSequentialGroup()
            .addComponent(browseButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(filePathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap())
    );
    cutterLibPanelLayout.setVerticalGroup(
      cutterLibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(cutterLibPanelLayout.createSequentialGroup()
        .addComponent(useLibCheck)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(cutterLibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(browseButton)
          .addComponent(filePathLabel)))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(phaseShiftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cutterLibPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 147, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(phaseShiftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cutterLibPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 87, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void changeLibCheck(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeLibCheck
    browseButton.setEnabled(useLibCheck.isSelected());
  }//GEN-LAST:event_changeLibCheck

  private void browseFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFile
    File home = new File(System.getProperty("user.home"));	//The default dir to use if no value is stored
    File file = new FileChooserBuilder("libfile") // "libfile" is key for NbPreferences
    .setTitle("Open xml file for cutter library...")
    .setDefaultWorkingDirectory(home)
    .setApproveText("Save")
    .setFileFilter(new FileNameExtensionFilter(EXTENSION + " files", EXTENSION))
    .showSaveDialog();
    if (file == null) {
      filePathLabel.setText("");
    } else {
      if (!(file.toString()).endsWith("." + EXTENSION)) {
        file = new File(file.toString() + "." + EXTENSION);
      }
      filePathLabel.setText(file.getAbsolutePath());
    }
  }//GEN-LAST:event_browseFile

  void load() {
    fracPhaseButton.setSelected(NbPreferences.forModule(COrnLathePanel.class).getBoolean("fracPhase", false));
    
    useLibCheck.setSelected(NbPreferences.forModule(COrnLathePanel.class).getBoolean("useLib", false));
    filePathLabel.setText(NbPreferences.forModule(COrnLathePanel.class).get("libpath", ""));
    browseButton.setEnabled(useLibCheck.isSelected());
  }

  void store() {
    NbPreferences.forModule(COrnLathePanel.class).putBoolean("fracPhase", fracPhaseButton.isSelected());
    
    NbPreferences.forModule(COrnLathePanel.class).putBoolean("useLib", useLibCheck.isSelected());
    NbPreferences.forModule(COrnLathePanel.class).put("libpath", filePathLabel.getText());
    browseButton.setEnabled(useLibCheck.isSelected());
  }

  boolean valid() {
    // TODO check whether form is consistent and complete
    return true;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton browseButton;
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JPanel cutterLibPanel;
  private javax.swing.JRadioButton engPhaseButton;
  private javax.swing.JLabel filePathLabel;
  private javax.swing.JRadioButton fracPhaseButton;
  private javax.swing.JPanel phaseShiftPanel;
  private javax.swing.JCheckBox useLibCheck;
  // End of variables declaration//GEN-END:variables
}
