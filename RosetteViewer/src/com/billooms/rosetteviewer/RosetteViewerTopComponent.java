package com.billooms.rosetteviewer;

import com.billooms.drawables.Grid;
import com.billooms.patterns.Patterns;
import com.billooms.rosette.RosetteEditPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component to view rosettes.
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
    dtd = "-//com.billooms.rosetteviewer//RosetteViewer//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "RosetteViewerTopComponent",
    iconBase = "com/billooms/rosetteviewer/Rosette16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.billooms.rosetteviewer.RosetteViewerTopComponent")
@ActionReference(path = "Menu/Window", position = 400)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_RosetteViewerAction",
    preferredID = "RosetteViewerTopComponent"
)
@Messages({
  "CTL_RosetteViewerAction=RosetteViewer",
  "CTL_RosetteViewerTopComponent=RosetteViewer Window",
  "HINT_RosetteViewerTopComponent=This is a RosetteViewer window"
})
public final class RosetteViewerTopComponent extends TopComponent implements PropertyChangeListener {

  private final static String EXTENSION = "txt";
  private final static DecimalFormat F4 = new DecimalFormat("0.0000");
  
  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final DisplayPanel display;	      // panel for displaying the graphics
  
  private RosetteEditPanel editPanel = null;

  public RosetteViewerTopComponent() {
    initComponents();
    setName(Bundle.CTL_RosetteViewerTopComponent());
    setToolTipText(Bundle.HINT_RosetteViewerTopComponent());

    display = new DisplayPanel();
    this.add(display, BorderLayout.CENTER);
    
    editPanel = new RosetteEditPanel();
    bottomPanel.add(editPanel, BorderLayout.CENTER);

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();

    putClientProperty("print.printable", Boolean.TRUE);	// this can be printed
  }

  /**
   * Update the pattern manager when the ExplorerManager rootContext changes.
   */
  private synchronized void updatePatternMgr() {
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_RosetteViewerTopComponent() + ": (no patterns)");
      editPanel.setPatternMgr(null);
    } else {
      setName(Bundle.CTL_RosetteViewerTopComponent() + ": " + rootNode.getDisplayName());
      editPanel.setPatternMgr(rootNode.getLookup().lookup(Patterns.class));
    }
  }

  /**
   * Handle the addition/deletion of a pattern.
   *
   * @param evt
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("RosetteViewerTopComponent.propertyChange: " + evt.getSource().getClass().getName()
//	    + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Refresh the patternMgr when rootContext changes on the ExplorerManager
    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
      updatePatternMgr();
    }
  }

  private void writeRosetteData(double delta, double radius) {
    if ((delta <= 0.0) || (radius <= 0.0)) {
      return;
    }

    File textFile;
    File home = new File(System.getProperty("user.home"));	//The default dir to use if no value is stored
    textFile = new FileChooserBuilder("openfile")
        .setTitle("Save g-code File As...")
        .setDefaultWorkingDirectory(home)
        .setApproveText("save")
        .setFileFilter(new FileNameExtensionFilter(EXTENSION + " files", EXTENSION))
        .showSaveDialog();
    if (textFile == null) {
      return;
    }
    if (!(textFile.toString()).endsWith("." + EXTENSION)) {   // make sure there's a .txt extention
      textFile = new File(textFile.toString() + "." + EXTENSION);
    }
    if (textFile.exists()) {	// Ask the user whether to replace the file.
      NotifyDescriptor d = new NotifyDescriptor.Confirmation(
          "The file " + textFile.getName() + " already exists.\nDo you want to replace it?",
          "Overwrite File Check",
          NotifyDescriptor.YES_NO_OPTION,
          NotifyDescriptor.WARNING_MESSAGE);
      d.setValue(NotifyDescriptor.CANCEL_OPTION);
      Object result = DialogDisplayer.getDefault().notify(d);
      if (result != DialogDescriptor.YES_OPTION) {
        return;
      }
    }
    StatusDisplayer.getDefault().setStatusText("Saving Rosette File As: " + textFile.getName());

    PrintWriter out;
    try {
      FileOutputStream stream = new FileOutputStream(textFile);
      out = new PrintWriter(stream);
    } catch (FileNotFoundException e) {
      NotifyDescriptor d = new NotifyDescriptor.Message("Error while trying to open the text file:\n" + e,
          NotifyDescriptor.ERROR_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
    try {
      double degrees = 0.0;
      out.println("degrees\tradius");
      do {
        out.println(F4.format(degrees) + "\t" + F4.format(radius - editPanel.getRosette().getAmplitudeAt(degrees)));
        degrees += delta;
      } while (degrees < 360.0);

    } catch (Exception e) {
      NotifyDescriptor d = new NotifyDescriptor.Message("Error while trying to write the text file:\n" + e,
          NotifyDescriptor.ERROR_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
    } finally {
      out.close();
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    bottomPanel = new javax.swing.JPanel();
    buttonPanel = new javax.swing.JPanel();
    writeButton = new javax.swing.JButton();

    org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.jButton1.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.jButton2.text")); // NOI18N

    setLayout(new java.awt.BorderLayout());

    bottomPanel.setLayout(new java.awt.BorderLayout());

    buttonPanel.setLayout(new java.awt.BorderLayout());

    org.openide.awt.Mnemonics.setLocalizedText(writeButton, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.writeButton.text")); // NOI18N
    writeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        writeData(evt);
      }
    });
    buttonPanel.add(writeButton, java.awt.BorderLayout.WEST);

    bottomPanel.add(buttonPanel, java.awt.BorderLayout.PAGE_END);

    add(bottomPanel, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

  private void writeData(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeData
    if (editPanel != null) {
      WriteDataPanel panel = new WriteDataPanel();
      DialogDescriptor dd = new DialogDescriptor(
        panel,
        "Write Rosette Data",
        true,
        DialogDescriptor.OK_CANCEL_OPTION,
        DialogDescriptor.OK_OPTION,
        null);
      Object result = DialogDisplayer.getDefault().notify(dd);
      if (result != DialogDescriptor.OK_OPTION) {
        return;
      }
      double delta = ((Number) panel.degreeField.getValue()).doubleValue();
      double radius = ((Number) panel.radiusField.getValue()).doubleValue();

      writeRosetteData(delta, radius);
    }
  }//GEN-LAST:event_writeData

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel bottomPanel;
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton writeButton;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>RosetteViewerTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>RosetteViewerTopComponent.componentOpened em=" + em);
    updatePatternMgr();
    em.addPropertyChangeListener(this);
    if (editPanel != null) {
      editPanel.addPropertyChangeListener(display);
    }
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>RosetteViewerTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
    if (editPanel != null) {
      editPanel.removePropertyChangeListener(display);
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
    private Point zeroPix = INITIAL_ZPIX;       // Location of 0.0, 0.0 in pixels
    private final double ROSETTE_RADIUS = 3.5;  // The radius of the Rosette for drawing purposes

    /** Creates new DisplayPanel */
    public DisplayPanel() {
      setBackground(DEFAULT_BACKGROUND);
      this.setBackground(Color.WHITE);
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

      if (editPanel.getRosette() != null) {
        editPanel.getRosette().paint(g2d, ROSETTE_RADIUS);		// paint the rosette
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
