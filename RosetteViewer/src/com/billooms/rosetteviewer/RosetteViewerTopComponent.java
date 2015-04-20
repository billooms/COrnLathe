package com.billooms.rosetteviewer;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.drawables.Grid;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import com.billooms.rosette.Rosette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
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

  private final static COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);

  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final DisplayPanel display;	      // panel for displaying the graphics
  private Rosette rosette = null;	      // A local rosette (not one from a CutPoint) for display
  private Patterns patternMgr = null;	      // Pattern Manager for available patterns from ExplorerManager

  public RosetteViewerTopComponent() {
    initComponents();
    setName(Bundle.CTL_RosetteViewerTopComponent());
    setToolTipText(Bundle.HINT_RosetteViewerTopComponent());

    display = new DisplayPanel();
    this.add(display, BorderLayout.CENTER);

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();

    hiLoCombo.removeAllItems();	  // set the hiLoCombo values
    for (Rosette.Mask hl : Rosette.Mask.values()) {
      hiLoCombo.addItem(hl.toString());
    }
    ampSlider.setLabelTable(new SliderLabels(0, 100, 25, 0.0, 0.25));	  // labels for slider
    if (prefs.isFracPhase()) {
      phaseSlider.setLabelTable(new SliderLabels(0, 180, 45, 0.0, 0.25)); // fractional labels for slider
    } else {
      phaseSlider.setLabelTable(new SliderLabels(0, 180, 45, 0, 90));	  // engineering labels for slider
    }
    updateForm();

    putClientProperty("print.printable", Boolean.TRUE);	// this can be printed
  }

  /**
   * Update the pattern manager when the ExplorerManager rootContext changes.
   */
  private synchronized void updatePatternMgr() {
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_RosetteViewerTopComponent() + ": (no patterns)");
      patternMgr = null;
      patternCombo.removeAllItems();    // update the patternCombo control
//      setPattern(null);   // just leave the old one
    } else {
      setName(Bundle.CTL_RosetteViewerTopComponent() + ": " + rootNode.getDisplayName());
      patternMgr = rootNode.getLookup().lookup(Patterns.class);
      patternCombo.removeAllItems();    // update the patternCombo control
      if (patternMgr != null) {
        patternMgr.getAllDisplayNames().stream().forEach((s) -> {
          patternCombo.addItem(s);
        });
      }
      if (rosette == null) {
        rosette = new Rosette(patternMgr);
        rosette.addPropertyChangeListener(display);  // listen for changes in the pattern
      }
      if (patternMgr.nameExists(rosette.getPattern().getName())) {
        setPattern(rosette.getPattern());   // try to keep the same pattern if it exists
      } else {
        setPattern(patternMgr.getDefaultPattern());   // else use the default
      }
    }
  }

  /**
   * Set the pattern to the given pattern and update the display.
   *
   * @param pattern new pattern
   */
  private synchronized void setPattern(Pattern pattern) {
    rosette.setPattern(pattern);
    patternCombo.setSelectedIndex(patternMgr.indexOf(pattern.getName()));
    updateForm();
  }

  private void updateForm() {
    if (rosette != null) {
      patternCombo.setSelectedItem(rosette.getPattern().getDisplayName());
      repeatSpinner.setValue(rosette.getRepeat());
      ampField.setValue(rosette.getPToP());
      ampSlider.setValue((int) (100.0 * rosette.getPToP()));
      if (prefs.isFracPhase()) {
        phaseField.setValue(rosette.getPhase() / 360.0);
        maskPhaseField.setValue(rosette.getMaskPhase() / 360.0);
      } else {
        phaseField.setValue(rosette.getPhase());
        maskPhaseField.setValue(rosette.getMaskPhase());
      }
      phaseSlider.setValue((int) (rosette.getPhase() / 2.0));
      if (rosette.getPattern().needsAmp2()) {
        amp2Field.setEnabled(true);
        amp2Field.setValue(rosette.getAmp2());
      } else {
        amp2Field.setEnabled(false);
      }
      if (rosette.getPattern().needsN2()) {
        n2Spinner.setEnabled(true);
        n2Spinner.setValue(rosette.getN2());
      } else {
        n2Spinner.setEnabled(false);
      }
      maskField.setText(rosette.getMask());
      invertCheck.setSelected(rosette.getInvert());
      hiLoCombo.setSelectedItem(rosette.getMaskHiLo());
      display.repaint();		// does plotPanel.repaint() too
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
        out.println(F4.format(degrees) + "\t" + F4.format(radius - rosette.getAmplitudeAt(degrees)));
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

    editPanel = new javax.swing.JPanel();
    phasePanel = new javax.swing.JPanel();
    phaseSlider = new javax.swing.JSlider();
    phaseField = new javax.swing.JFormattedTextField();
    ampPanel = new javax.swing.JPanel();
    ampSlider = new javax.swing.JSlider();
    ampField = new javax.swing.JFormattedTextField();
    stylePanel = new javax.swing.JPanel();
    patternCombo = new javax.swing.JComboBox();
    repeatSpinner = new javax.swing.JSpinner();
    iconPanel = new IconPanel();
    n2Spinner = new javax.swing.JSpinner();
    jLabel1 = new javax.swing.JLabel();
    amp2Field = new javax.swing.JFormattedTextField();
    jLabel2 = new javax.swing.JLabel();
    invertCheck = new javax.swing.JCheckBox();
    writeButton = new javax.swing.JButton();
    maskPanel = new javax.swing.JPanel();
    maskField = new javax.swing.JTextField();
    jLabel4 = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    maskPhaseField = new javax.swing.JFormattedTextField();
    hiLoCombo = new javax.swing.JComboBox();

    setLayout(new java.awt.BorderLayout());

    phasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.phasePanel.border.title"))); // NOI18N

    phaseSlider.setMajorTickSpacing(10);
    phaseSlider.setMaximum(180);
    phaseSlider.setMinorTickSpacing(5);
    phaseSlider.setPaintLabels(true);
    phaseSlider.setPaintTicks(true);
    phaseSlider.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.phaseSlider.toolTipText")); // NOI18N
    phaseSlider.setValue(0);
    phaseSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changePhaseSlider(evt);
      }
    });

    phaseField.setColumns(5);
    phaseField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    phaseField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    phaseField.setText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.phaseField.text")); // NOI18N
    phaseField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.phaseField.toolTipText")); // NOI18N
    phaseField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    phaseField.setValue(0.0);
    phaseField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changePhase(evt);
      }
    });

    javax.swing.GroupLayout phasePanelLayout = new javax.swing.GroupLayout(phasePanel);
    phasePanel.setLayout(phasePanelLayout);
    phasePanelLayout.setHorizontalGroup(
      phasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(phasePanelLayout.createSequentialGroup()
        .addComponent(phaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(phaseSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    phasePanelLayout.setVerticalGroup(
      phasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(phaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addComponent(phaseSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    ampPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.ampPanel.border.title"))); // NOI18N

    ampSlider.setMajorTickSpacing(10);
    ampSlider.setMinorTickSpacing(5);
    ampSlider.setPaintLabels(true);
    ampSlider.setPaintTicks(true);
    ampSlider.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.ampSlider.toolTipText")); // NOI18N
    ampSlider.setValue(0);
    ampSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeAmpSlider(evt);
      }
    });

    ampField.setColumns(5);
    ampField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    ampField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    ampField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.ampField.toolTipText")); // NOI18N
    ampField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    ampField.setValue(0.0);
    ampField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeAmp(evt);
      }
    });

    javax.swing.GroupLayout ampPanelLayout = new javax.swing.GroupLayout(ampPanel);
    ampPanel.setLayout(ampPanelLayout);
    ampPanelLayout.setHorizontalGroup(
      ampPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(ampPanelLayout.createSequentialGroup()
        .addComponent(ampField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(ampSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    ampPanelLayout.setVerticalGroup(
      ampPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(ampField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addComponent(ampSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    stylePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.stylePanel.border.title"))); // NOI18N

    patternCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    patternCombo.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.patternCombo.toolTipText")); // NOI18N
    patternCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changePattern(evt);
      }
    });

    repeatSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
    repeatSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.repeatSpinner.toolTipText")); // NOI18N
    repeatSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeRepeat(evt);
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
      .addGap(0, 25, Short.MAX_VALUE)
    );

    n2Spinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(3), Integer.valueOf(1), null, Integer.valueOf(1)));
    n2Spinner.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.n2Spinner.toolTipText")); // NOI18N
    n2Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeN2(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.jLabel1.text")); // NOI18N

    amp2Field.setColumns(4);
    amp2Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    amp2Field.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    amp2Field.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.amp2Field.toolTipText")); // NOI18N
    amp2Field.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    amp2Field.setValue(0.0);
    amp2Field.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeAmp2(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.jLabel2.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(invertCheck, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.invertCheck.text")); // NOI18N
    invertCheck.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    invertCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeInvert(evt);
      }
    });

    javax.swing.GroupLayout stylePanelLayout = new javax.swing.GroupLayout(stylePanel);
    stylePanel.setLayout(stylePanelLayout);
    stylePanelLayout.setHorizontalGroup(
      stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stylePanelLayout.createSequentialGroup()
        .addComponent(patternCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(repeatSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(invertCheck))
      .addGroup(stylePanelLayout.createSequentialGroup()
        .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(n2Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(6, 6, 6)
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(amp2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    stylePanelLayout.setVerticalGroup(
      stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(stylePanelLayout.createSequentialGroup()
        .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(repeatSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(patternCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(invertCheck))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(jLabel1)
            .addComponent(n2Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(amp2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel2))))
    );

    org.openide.awt.Mnemonics.setLocalizedText(writeButton, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.writeButton.text")); // NOI18N
    writeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        writeData(evt);
      }
    });

    maskPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.maskPanel.border.title"))); // NOI18N

    maskField.setColumns(5);
    maskField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maskField.setText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.maskField.text")); // NOI18N
    maskField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeMask(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.jLabel4.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.jLabel5.text")); // NOI18N

    maskPhaseField.setColumns(5);
    maskPhaseField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    maskPhaseField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maskPhaseField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteViewerTopComponent.class, "RosetteViewerTopComponent.maskPhaseField.toolTipText")); // NOI18N
    maskPhaseField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    maskPhaseField.setValue(0.0);
    maskPhaseField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeMaskPhase(evt);
      }
    });

    hiLoCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "HIGH", "LOW" }));
    hiLoCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeHiLo(evt);
      }
    });

    javax.swing.GroupLayout maskPanelLayout = new javax.swing.GroupLayout(maskPanel);
    maskPanel.setLayout(maskPanelLayout);
    maskPanelLayout.setHorizontalGroup(
      maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(maskPanelLayout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, maskPanelLayout.createSequentialGroup()
            .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(maskField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(maskPhaseField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addComponent(hiLoCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    maskPanelLayout.setVerticalGroup(
      maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(maskPanelLayout.createSequentialGroup()
        .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(maskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel4))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(hiLoCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(maskPhaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel5)))
    );

    javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
    editPanel.setLayout(editPanelLayout);
    editPanelLayout.setHorizontalGroup(
      editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editPanelLayout.createSequentialGroup()
        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(editPanelLayout.createSequentialGroup()
            .addComponent(stylePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(maskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(writeButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(ampPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(phasePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    editPanelLayout.setVerticalGroup(
      editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editPanelLayout.createSequentialGroup()
        .addComponent(stylePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(writeButton))
      .addGroup(editPanelLayout.createSequentialGroup()
        .addComponent(ampPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(phasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addComponent(maskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    add(editPanel, java.awt.BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents

  private void changePhaseSlider(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changePhaseSlider
    if (rosette != null & phaseSlider.isFocusOwner()) {
      double ph = 2.0 * phaseSlider.getValue();
      rosette.setPhase(ph);
      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changePhaseSlider

  private void changePhase(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changePhase
    if (rosette != null && phaseField.isFocusOwner()) {
      double n = ((Number) phaseField.getValue()).doubleValue();
      if (prefs.isFracPhase()) {
        rosette.setPh(n);
      } else {
        rosette.setPhase(n);
      }
      phaseSlider.setValue((int) (rosette.getPhase() / 2.0));
    }
  }//GEN-LAST:event_changePhase

  private void changeAmpSlider(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeAmpSlider
    if (rosette != null && ampSlider.isFocusOwner()) {
      rosette.setPToP(ampSlider.getValue() / 100.0);
      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changeAmpSlider

  private void changeAmp(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeAmp
    if (rosette != null && ampField.isFocusOwner()) {
      double n = ((Number) ampField.getValue()).doubleValue();
      n = Math.max(0.0, n);
      ampSlider.setValue((int) (100 * n));
      rosette.setPToP(n);
    }
  }//GEN-LAST:event_changeAmp

  private void changePattern(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePattern
    if (rosette != null && patternCombo.isFocusOwner()) {
      rosette.setPattern(patternMgr.get(patternCombo.getSelectedIndex()));
      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changePattern

  private void changeRepeat(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeRepeat
    if (rosette != null) {
      rosette.setRepeat((Integer) repeatSpinner.getValue());
      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changeRepeat

  private void changeN2(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeN2
    if (rosette != null) {
      rosette.setN2((Integer) n2Spinner.getValue());
      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changeN2

  private void changeAmp2(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeAmp2
    if (rosette != null && amp2Field.isFocusOwner()) {
      rosette.setAmp2(((Number) amp2Field.getValue()).doubleValue());
//      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changeAmp2

  private void changeInvert(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeInvert
    if (rosette != null && invertCheck.isFocusOwner()) {
      rosette.setInvert(invertCheck.isSelected());
      updateForm();   // in case other values are changed
    }
  }//GEN-LAST:event_changeInvert

  private void writeData(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeData
    if (rosette != null) {
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

  private void changeMask(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeMask
    if (rosette != null) {
      rosette.setMask(maskField.getText());
      updateForm();
    }
  }//GEN-LAST:event_changeMask

  private void changeMaskPhase(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_changeMaskPhase
    if (rosette != null) {
      if (prefs.isFracPhase()) {
        rosette.setMaskPh(((Number) maskPhaseField.getValue()).doubleValue());
      } else {
        rosette.setMaskPhase(((Number) maskPhaseField.getValue()).doubleValue());
      }
//      updateForm();
    }
  }//GEN-LAST:event_changeMaskPhase

  private void changeHiLo(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeHiLo
    if (rosette != null) {
      try {
        rosette.setMaskHiLo(Rosette.Mask.values()[hiLoCombo.getSelectedIndex()]);
      } catch (ArrayIndexOutOfBoundsException e) {
      }
      updateForm();
    }
  }//GEN-LAST:event_changeHiLo

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFormattedTextField amp2Field;
  private javax.swing.JFormattedTextField ampField;
  private javax.swing.JPanel ampPanel;
  private javax.swing.JSlider ampSlider;
  private javax.swing.JPanel editPanel;
  private javax.swing.JComboBox hiLoCombo;
  private javax.swing.JPanel iconPanel;
  private javax.swing.JCheckBox invertCheck;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JTextField maskField;
  private javax.swing.JPanel maskPanel;
  private javax.swing.JFormattedTextField maskPhaseField;
  private javax.swing.JSpinner n2Spinner;
  private javax.swing.JComboBox patternCombo;
  private javax.swing.JFormattedTextField phaseField;
  private javax.swing.JPanel phasePanel;
  private javax.swing.JSlider phaseSlider;
  private javax.swing.JSpinner repeatSpinner;
  private javax.swing.JPanel stylePanel;
  private javax.swing.JButton writeButton;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>RosetteViewerTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>RosetteViewerTopComponent.componentOpened em=" + em);
    updatePatternMgr();
    em.addPropertyChangeListener(this);
    if (rosette != null) {
      rosette.addPropertyChangeListener(display);
    }
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>RosetteViewerTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
    if (rosette != null) {
      rosette.removePropertyChangeListener(display);
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

  /** Nested inner class for a panel showing a small plot of the pattern. */
  private class IconPanel extends JPanel {

    /** Create a new PlotPanel. */
    public IconPanel() {
      super();
      this.setPreferredSize(new Dimension(Pattern.DEFAULT_WIDTH, Pattern.DEFAULT_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (rosette == null) {
        return;
      }
      Pattern pat = rosette.getPattern();
      if (pat.needsOptions()) {
        g.drawImage(pat.getImage(rosette.getRepeat(), rosette.getN2(), rosette.getAmp2()), 0, 0, null);
      } else if (pat.needsRepeat()) {
        g.drawImage(pat.getImage(rosette.getRepeat()), 0, 0, null);
      } else {
        g.drawImage(pat.getImage(), 0, 0, null);
      }
    }
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
    private double dpi = INITIAL_DPI;		  // Dots per inch for zooming in/out
    private Point zeroPix = INITIAL_ZPIX;	  // Location of 0.0, 0.0 in pixels

    /** Creates new DisplayPanel */
    public DisplayPanel() {
      setBackground(DEFAULT_BACKGROUND);
      this.setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      dpi = (int) Math.min(WINDOW_PERCENT * this.getWidth() / (2 * Rosette.DEFAULT_RADIUS),
          WINDOW_PERCENT * this.getHeight() / (2 * Rosette.DEFAULT_RADIUS));
      zeroPix = new Point(getWidth() / 2, getHeight() / 2);	// zero is always in the center

      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(zeroPix.x, zeroPix.y);
      g2d.scale(dpi, -dpi);	// positive y is up

      // Paint the grid
      new Grid(-(double) zeroPix.x / dpi, -(double) (getHeight() - zeroPix.y) / dpi,
          (double) getWidth() / dpi, (double) getHeight() / dpi).paint(g2d);

      if (rosette != null) {
        rosette.paint(g2d);		// paint the rosette
      }

      iconPanel.repaint();			// repaint the icon for the pattern
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
