package com.billooms.rosette;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.drawables.Grid;
import com.billooms.patterns.Pattern;
import com.billooms.patterns.Patterns;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;
import org.openide.util.Lookup;

/**
 * Panel for editing a rosette.
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
public class RosetteEditPanel extends JPanel implements PropertyChangeListener {
  
  /** All RosetteEditPanel property change names start with this prefix. */
  public final static String PROP_PREFIX = "RosetteEditPanel" + "_";
  /** Property name used when changing the rosette. */
  public final static String PROP_ROSETTE = PROP_PREFIX + "Rosette";
  
  private final static COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);
  
  private Patterns patternMgr = null;   // Pattern manager
  private Rosette rosette = null;	    // The rosette being edited

  protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /**
   * Create a new RosetteEditPanel. 
   */
  public RosetteEditPanel() {
    initComponents();
    
    hiLoCombo.removeAllItems();	  // set the hiLoCombo values
    for (Rosette.Mask hl : Rosette.Mask.values()) {
      hiLoCombo.addItem(hl.toString());
    }
    ampSlider.setLabelTable(new SliderLabels(0, 100, 25, 0.0, 0.25));	  // labels for slider
    if ((prefs == null) || !prefs.isFracPhase()) {    // (test for null so that drag & drop works)
      phaseSlider.setLabelTable(new SliderLabels(0, 180, 45, 0, 90));	  // engineering labels for slider
    } else {
      phaseSlider.setLabelTable(new SliderLabels(0, 180, 45, 0.0, 0.25)); // fractional labels for slider
    }
    
    updateForm();
  }
  
  /**
   * Get the current pattern manager.
   * 
   * @return pattern manager
   */
  public Patterns getPatternMgr() {
    return patternMgr;
  }
  
  /**
   * Get the rosette being edited by this panel.
   * 
   * @return rosette
   */
  public Rosette getRosette() {
    return rosette;
  }
  
  /**
   * Set the current pattern manager.
   * 
   * @param newPatternMgr new pattern manager
   */
  public void setPatternMgr(Patterns newPatternMgr) {
    if (patternMgr != null) {
      patternMgr.removePropertyChangeListener(this);
    }
    if (rosette != null) {
      rosette.removePropertyChangeListener((PropertyChangeListener) viewPanel);
    }
    
    this.patternMgr = newPatternMgr;
    updatePatternCombo();
    
    if (patternMgr == null) {
      setRosette(null);
    } else {
      setRosette(new Rosette(patternMgr));
      rosette.addPropertyChangeListener((PropertyChangeListener) viewPanel);
      if (patternMgr.getAllCustom().size() > 0) {
        rosette.setPattern(patternMgr.getAllCustom().get(0));    // set to first custom pattern
      } else {
        rosette.setPattern(patternMgr.getDefaultPattern());      // else the default
      }
      patternMgr.addPropertyChangeListener(this);
    }
  }
  
  /**
   * Set the rosette being edited by this panel.
   * 
   * @param newRosette new rosette.
   */
  public void setRosette(Rosette newRosette) {
    if (rosette != null) {
      rosette.removePropertyChangeListener(this);
    }
    Rosette old = this.rosette;
    this.rosette = newRosette;
    if (rosette != null) {
      rosette.addPropertyChangeListener(this);
    }
    updateForm();
    pcs.firePropertyChange(PROP_ROSETTE, old, newRosette);
  }
  
  /** 
   * Update the patternCombo items.
   */
  private void updatePatternCombo() {
    patternCombo.removeAllItems();    // update the patternCombo control
    if (patternMgr != null) {
      patternMgr.getAllDisplayNames().stream().forEach((s) -> {
        patternCombo.addItem(s);
      });
    }
    patternCombo.setEnabled(patternMgr != null);
  }

  /**
   * Update all the form elements.
   */
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
      symAmpText.setText(rosette.getSymAmpStr());
      symWidText.setText(rosette.getSymWidStr());
      iconPanel.repaint();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("RosetteEditPanel.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens to rosette and patternMgr
    String prop = evt.getPropertyName();
    if (prop.startsWith(Patterns.PROP_PREFIX)) {	      // watch for pattern manager changes
      // when pattern manager changes, it must be because of Add/Delete cutter
      String oldPattern = (String) patternCombo.getSelectedItem();
      updatePatternCombo();
      if (patternMgr.nameExists(oldPattern)) {
        rosette.setPattern(patternMgr.getPattern(oldPattern));
      } else {
        rosette.setPattern(patternMgr.get(0));
      }
    } else if (prop.startsWith(Rosette.PROP_PREFIX)) {	  // watch for rosette changes
      updateForm();
    }
    // pass the info through
    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
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
  
  /** Nested inner class for a panel showing a small plot of the rosette. */
  private class ViewPanel extends JPanel implements PropertyChangeListener {
    
    private final double INITIAL_DPI = 100.0;	  // for the first time the window comes up
    private final double WINDOW_PERCENT = 0.95;	  // use 95% of the window for the rosette
    private final Point INITIAL_ZPIX = new Point(150, 200);   // artibrary
    private double dpi = INITIAL_DPI;           // Dots per inch for zooming in/out
    private Point zeroPix = INITIAL_ZPIX;       // Location of 0.0, 0.0 in pixels
    private final double ROSETTE_RADIUS = 1.0;  // The radius of the Rosette for drawing purposes
    
    /** Creates new ViewPanel */
    public ViewPanel() {
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

      if (rosette != null) {
        rosette.paint(g2d, ROSETTE_RADIUS);		// paint the rosette
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

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    stylePanel = new javax.swing.JPanel();
    patternCombo = new javax.swing.JComboBox();
    repeatSpinner = new javax.swing.JSpinner();
    invertCheck = new javax.swing.JCheckBox();
    iconPanel = new IconPanel();
    n2Spinner = new javax.swing.JSpinner();
    jLabel1 = new javax.swing.JLabel();
    amp2Field = new javax.swing.JFormattedTextField();
    jLabel2 = new javax.swing.JLabel();
    viewPanel = new ViewPanel();
    maskPanel = new javax.swing.JPanel();
    jLabel4 = new javax.swing.JLabel();
    maskField = new javax.swing.JTextField();
    hiLoCombo = new javax.swing.JComboBox();
    jLabel5 = new javax.swing.JLabel();
    maskPhaseField = new javax.swing.JFormattedTextField();
    jLabel3 = new javax.swing.JLabel();
    symAmpText = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    symWidText = new javax.swing.JTextField();
    ampPanel = new javax.swing.JPanel();
    ampSlider = new javax.swing.JSlider();
    ampField = new javax.swing.JFormattedTextField();
    phasePanel = new javax.swing.JPanel();
    phaseSlider = new javax.swing.JSlider();
    phaseField = new javax.swing.JFormattedTextField();

    stylePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.stylePanel.border.title"))); // NOI18N

    patternCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    patternCombo.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.patternCombo.toolTipText")); // NOI18N
    patternCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changePattern(evt);
      }
    });

    repeatSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
    repeatSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.repeatSpinner.toolTipText")); // NOI18N
    repeatSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeRepeat(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(invertCheck, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.invertCheck.text")); // NOI18N
    invertCheck.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.invertCheck.toolTipText")); // NOI18N
    invertCheck.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    invertCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeInvert(evt);
      }
    });

    iconPanel.setBackground(new java.awt.Color(255, 255, 255));
    iconPanel.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.iconPanel.toolTipText")); // NOI18N
    iconPanel.setPreferredSize(new java.awt.Dimension(100, 50));

    javax.swing.GroupLayout iconPanelLayout = new javax.swing.GroupLayout(iconPanel);
    iconPanel.setLayout(iconPanelLayout);
    iconPanelLayout.setHorizontalGroup(
      iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );
    iconPanelLayout.setVerticalGroup(
      iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 50, Short.MAX_VALUE)
    );

    n2Spinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(3), Integer.valueOf(1), null, Integer.valueOf(1)));
    n2Spinner.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.n2Spinner.toolTipText")); // NOI18N
    n2Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeN2(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.jLabel1.text")); // NOI18N

    amp2Field.setColumns(4);
    amp2Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    amp2Field.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    amp2Field.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.amp2Field.toolTipText")); // NOI18N
    amp2Field.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    amp2Field.setValue(0.0);
    amp2Field.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeAmp2(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.jLabel2.text")); // NOI18N

    javax.swing.GroupLayout stylePanelLayout = new javax.swing.GroupLayout(stylePanel);
    stylePanel.setLayout(stylePanelLayout);
    stylePanelLayout.setHorizontalGroup(
      stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stylePanelLayout.createSequentialGroup()
        .addComponent(patternCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(repeatSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(invertCheck))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stylePanelLayout.createSequentialGroup()
        .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stylePanelLayout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(n2Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stylePanelLayout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(amp2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
    );
    stylePanelLayout.setVerticalGroup(
      stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(stylePanelLayout.createSequentialGroup()
        .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(patternCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(repeatSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(invertCheck))
        .addGap(0, 0, 0)
        .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(stylePanelLayout.createSequentialGroup()
            .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(n2Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel1))
            .addGap(0, 0, 0)
            .addGroup(stylePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(amp2Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel2)))
          .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );

    viewPanel.setBackground(new java.awt.Color(255, 255, 255));
    viewPanel.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.viewPanel.toolTipText")); // NOI18N

    javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
    viewPanel.setLayout(viewPanelLayout);
    viewPanelLayout.setHorizontalGroup(
      viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    viewPanelLayout.setVerticalGroup(
      viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );

    maskPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.maskPanel.border.title"))); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.jLabel4.text")); // NOI18N

    maskField.setColumns(5);
    maskField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maskField.setText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.maskField.text")); // NOI18N
    maskField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.maskField.toolTipText")); // NOI18N
    maskField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeMask(evt);
      }
    });

    hiLoCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "HIGH", "LOW" }));
    hiLoCombo.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.hiLoCombo.toolTipText")); // NOI18N
    hiLoCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeHiLo(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.jLabel5.text")); // NOI18N

    maskPhaseField.setColumns(5);
    maskPhaseField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    maskPhaseField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    maskPhaseField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.maskPhaseField.toolTipText")); // NOI18N
    maskPhaseField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    maskPhaseField.setValue(0.0);
    maskPhaseField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        changeMaskPhase(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.jLabel3.text")); // NOI18N

    symAmpText.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.symAmpText.toolTipText")); // NOI18N
    symAmpText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeSymAmp(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.jLabel6.text")); // NOI18N

    symWidText.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.symWidText.toolTipText")); // NOI18N
    symWidText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        changeSymWid(evt);
      }
    });

    javax.swing.GroupLayout maskPanelLayout = new javax.swing.GroupLayout(maskPanel);
    maskPanel.setLayout(maskPanelLayout);
    maskPanelLayout.setHorizontalGroup(
      maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(maskPanelLayout.createSequentialGroup()
        .addComponent(jLabel4)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(maskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(hiLoCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel5)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(maskPhaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGroup(maskPanelLayout.createSequentialGroup()
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(symAmpText, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel6)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(symWidText))
    );
    maskPanelLayout.setVerticalGroup(
      maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(maskPanelLayout.createSequentialGroup()
        .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(maskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(hiLoCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel5)
          .addComponent(maskPhaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, 0)
        .addGroup(maskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(symAmpText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel6)
          .addComponent(symWidText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );

    ampPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.ampPanel.border.title"))); // NOI18N

    ampSlider.setMajorTickSpacing(10);
    ampSlider.setMinorTickSpacing(5);
    ampSlider.setPaintLabels(true);
    ampSlider.setPaintTicks(true);
    ampSlider.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.ampSlider.toolTipText")); // NOI18N
    ampSlider.setValue(0);
    ampSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changeAmpSlider(evt);
      }
    });

    ampField.setColumns(5);
    ampField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    ampField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    ampField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.ampField.toolTipText")); // NOI18N
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

    phasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.phasePanel.border.title"))); // NOI18N

    phaseSlider.setMajorTickSpacing(10);
    phaseSlider.setMaximum(180);
    phaseSlider.setMinorTickSpacing(5);
    phaseSlider.setPaintLabels(true);
    phaseSlider.setPaintTicks(true);
    phaseSlider.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.phaseSlider.toolTipText")); // NOI18N
    phaseSlider.setValue(0);
    phaseSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        changePhaseSlider(evt);
      }
    });

    phaseField.setColumns(5);
    phaseField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    phaseField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    phaseField.setText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.phaseField.text")); // NOI18N
    phaseField.setToolTipText(org.openide.util.NbBundle.getMessage(RosetteEditPanel.class, "RosetteEditPanel.phaseField.toolTipText")); // NOI18N
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

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(layout.createSequentialGroup()
            .addComponent(stylePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0)
            .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(maskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(ampPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(phasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(stylePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addComponent(maskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGroup(layout.createSequentialGroup()
        .addComponent(ampPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0)
        .addComponent(phasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

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

  private void changeSymAmp(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSymAmp
    if (rosette != null & symAmpText.isFocusOwner()) {
      rosette.setSymAmpStr(symAmpText.getText());
      updateForm();   // in case of format problem
    }
  }//GEN-LAST:event_changeSymAmp

  private void changeSymWid(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSymWid
    if (rosette != null & symWidText.isFocusOwner()) {
      rosette.setSymWidStr(symWidText.getText());
      updateForm();   // in case of format problem
    }
  }//GEN-LAST:event_changeSymWid


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFormattedTextField amp2Field;
  private javax.swing.JFormattedTextField ampField;
  private javax.swing.JPanel ampPanel;
  private javax.swing.JSlider ampSlider;
  private javax.swing.JComboBox hiLoCombo;
  private javax.swing.JPanel iconPanel;
  private javax.swing.JCheckBox invertCheck;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
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
  private javax.swing.JTextField symAmpText;
  private javax.swing.JTextField symWidText;
  private javax.swing.JPanel viewPanel;
  // End of variables declaration//GEN-END:variables
}
