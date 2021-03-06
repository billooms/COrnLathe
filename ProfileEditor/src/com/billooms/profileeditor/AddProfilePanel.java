package com.billooms.profileeditor;

import javax.swing.JPanel;

/**
 * Panel for adding a new profile.
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
public class AddProfilePanel extends JPanel {

  /** Constructs new form AddProfilePanel */
  public AddProfilePanel() {
    initComponents();
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        nameField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        straightButton = new javax.swing.JRadioButton();
        curveButton = new javax.swing.JRadioButton();

        nameField.setText(org.openide.util.NbBundle.getMessage(AddProfilePanel.class, "AddProfilePanel.nameField.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddProfilePanel.class, "AddProfilePanel.jLabel1.text")); // NOI18N

        buttonGroup1.add(straightButton);
        straightButton.setSelected(true);
        straightButton.setText(org.openide.util.NbBundle.getMessage(AddProfilePanel.class, "AddProfilePanel.straightButton.text")); // NOI18N

        buttonGroup1.add(curveButton);
        curveButton.setText(org.openide.util.NbBundle.getMessage(AddProfilePanel.class, "AddProfilePanel.curveButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(straightButton)
                    .addComponent(curveButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(straightButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(curveButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    protected javax.swing.JRadioButton curveButton;
    private javax.swing.JLabel jLabel1;
    protected javax.swing.JTextField nameField;
    protected javax.swing.JRadioButton straightButton;
    // End of variables declaration//GEN-END:variables
}
