package com.billooms.outlineeditor;

import com.billooms.cutpoints.CutPoint;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.GoToPoint;
import com.billooms.cutters.CutterEditPanel;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.BoundingBox;
import com.billooms.drawables.Grid;
import com.billooms.drawables.simple.Curve;
import com.billooms.outline.Outline;
import com.billooms.outline.OutlinePt;
import com.billooms.outline.SafePt;
import com.billooms.cutpoints.CutPointPaletteFactory;
import com.billooms.cutpoints.OffsetGroup;
import com.billooms.cutpoints.SpiralCut;
import com.billooms.cutters.Cutter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.spi.palette.PaletteController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Outline Editor for viewing and editing the Outline.
 *
 * The current outline is made available via AbstractLookup.
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
    dtd = "-//com.billooms.outlineeditor//OutlineEditor//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "OutlineEditorTopComponent",
    iconBase = "com/billooms/outlineeditor/icons/OutEd16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "com.billooms.outlineeditor.OutlineEditorTopComponent")
@ActionReference(path = "Menu/Window", position = 100)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_OutlineEditorAction",
    preferredID = "OutlineEditorTopComponent"
)
@Messages({
  "CTL_OutlineEditorAction=OutlineEditor",
  "CTL_OutlineEditorTopComponent=OutlineEditor Window",
  "HINT_OutlineEditorTopComponent=This is a OutlineEditor window"
})
public final class OutlineEditorTopComponent extends TopComponent implements PropertyChangeListener {

  private final static DecimalFormat F3 = new DecimalFormat("0.000");

  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final InstanceContent ic = new InstanceContent();   // will contain outline and cutMgr
  private final PaletteController palette = CutPointPaletteFactory.getCutPointPalette();
  private final DropTarget dropT;

  /** Panel for selecting a cutter. */
  private final CutterEditPanel cutEditPanel;
  /** Panel where the drawing is done. */
  private final DrawPanel drawPanel;

  /** Local copy of the outline. */
  private Outline outline = null;
  /** CutPoint manager. */
  private CutPoints cutPtMgr = null;

  public OutlineEditorTopComponent() {
    initComponents();
    setName(Bundle.CTL_OutlineEditorTopComponent());
    setToolTipText(Bundle.HINT_OutlineEditorTopComponent());

    cutEditPanel = new CutterEditPanel();
    this.add(cutEditPanel, BorderLayout.SOUTH);

    drawPanel = new DrawPanel();
    dropT = new DropTarget(drawPanel, drawPanel);   // drawPanel is a DropTargetListener
    drawPanel.setDropTarget(dropT);
    drawPanel.putClientProperty("print.printable", Boolean.TRUE);	// this can be printed
    outlineEditPanel.add(drawPanel, BorderLayout.CENTER);

    if (em == null) {
      em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
    }
//    System.out.println(">>>OutlineEditorTopComponent.constructor em=" + em);

    updateAll();

    this.associateLookup(new AbstractLookup(ic));   // will contain outline and cutMgr
    ic.add(palette);    // this brings up the palette with CutPoints
  }

  /**
   * When required, get the latest Outline and CutPoint manager from the root of
   * the ExplorerManager.
   */
  private synchronized void updateRootNode() {
    if (outline != null) {
      cutEditPanel.removePropertyChangeListener(outline);   // old outline quit listening to cutter
      outline.removePropertyChangeListener(this);	    // this quits listening to old outline
      ic.remove(outline);
    }
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_OutlineEditorTopComponent() + ": (no outline)");
      outline = null;
      setCutPointMgr(null);
      dropT.setActive(false);
      cutEditPanel.setCutMgr(null);
      dropT.setActive(false);
    } else {
      setName(Bundle.CTL_OutlineEditorTopComponent() + ": " + rootNode.getDisplayName());
      outline = rootNode.getLookup().lookup(Outline.class);
      if (outline != null) {
        outline.addPropertyChangeListener(this);	  // this listens to outline
        cutEditPanel.addPropertyChangeListener(outline);  // Outline listens when cutter changed
        dropT.setActive(true);
        ic.add(outline);	    // make the outline availble to actions via lookup
//        zoomToFit();
      }
      setCutPointMgr(rootNode.getLookup().lookup(CutPoints.class));
      cutEditPanel.setCutMgr(rootNode.getLookup().lookup(Cutters.class));
    }
    updateAll();
  }

  /**
   * Set a new CutPoint manager.
   *
   * @param newMgr new CutPoint manager (which might be null);
   */
  protected synchronized void setCutPointMgr(CutPoints newMgr) {
    if (cutPtMgr != null) {
      cutPtMgr.removePropertyChangeListener(this);  // quit listening to the old CutPoint manager
      ic.remove(cutPtMgr);
    }
    this.cutPtMgr = newMgr;
    drawPanel.repaint();
    if (cutPtMgr != null) {
      if (cutPtMgr.isEmpty()) {
        editOutlineButton.setSelected(true);
      } else {
        editCutPointsButton.setSelected(true);
      }
      cutPtMgr.addPropertyChangeListener(this);	// listen for CutPoint changes
      ic.add(cutPtMgr);
    }
  }

  /**
   * Set the editing mode to "Edit Outline".
   */
  public void setEditPoints() {
    editOutlineButton.setSelected(true);
  }

  /**
   * Set the editing mode to "Edit CutPoints".
   */
  public void setEditCuts() {
    editCutPointsButton.setSelected(true);
  }

  /**
   * Zoom the drawPanel to fit the curves.
   */
  public void zoomToFit() {
    drawPanel.zoomToFit();
  }

  /**
   * Update the display fields and repaint.
   */
  private void updateAll() {
    if (outline == null) {
      thickField.setValue(Outline.DEFAULT_THICKNESS);
      resolutionField.setValue(Outline.DEFAULT_RESOLUTION);
      lengthLabel.setText("");
    } else {
      thickField.setValue(outline.getThickness());
      resolutionField.setValue(outline.getResolution());
      if (outline.getDotLocation().isInside()) {
        lengthLabel.setText("Inside length: " + F3.format(outline.getInsideCurve().getLength()));
      } else {
        lengthLabel.setText("Outside length: " + F3.format(outline.getOutsideCurve().getLength()));
      }
    }

    drawPanel.repaint();
  }

  public Cutter getSelectedCutter() {
    return cutEditPanel.getCutter();
  }

  /**
   * Add an externally probed point. The point might be either an outline point
   * or a CutPoint depending on the status of the editCombo.
   *
   * @param pt Point to add
   */
  public void probePt(Point2D.Double pt) {
    if (editCutPointsButton.isSelected()) {     // Editing CutPoints
      drawPanel.addFromPalette(pt);
    } else {									// Editing curve points
      outline.addPoint(new OutlinePt(pt));
      offsetButton.setEnabled(true);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("OutlineEditorTopComponent.propertyChange: " + evt.getSource().getClass().getName()
//	    + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // This listens to the ExplorerManager
    // Refresh the outline when rootContext changes on the ExplorerManager
    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
      updateRootNode();
    } else {
      // This listens to the Outline and the CutPoint manager
      // update everything and repaint
      updateAll();
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    buttonGroup1 = new javax.swing.ButtonGroup();
    outlineEditPanel = new javax.swing.JPanel();
    controlPanel = new javax.swing.JPanel();
    coordsLabel = new javax.swing.JLabel();
    lengthLabel = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();
    thickField = new javax.swing.JFormattedTextField();
    jLabel3 = new javax.swing.JLabel();
    resolutionField = new javax.swing.JFormattedTextField();
    editOutlineButton = new javax.swing.JToggleButton();
    editCutPointsButton = new javax.swing.JToggleButton();
    offsetButton = new javax.swing.JButton();
    allCutsCheckBox = new javax.swing.JCheckBox();

    setLayout(new java.awt.BorderLayout());

    outlineEditPanel.setBackground(new java.awt.Color(0, 0, 0));
    outlineEditPanel.setLayout(new java.awt.BorderLayout());

    org.openide.awt.Mnemonics.setLocalizedText(coordsLabel, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.coordsLabel.text")); // NOI18N
    coordsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.coordsLabel.toolTipText")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(lengthLabel, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.lengthLabel.text")); // NOI18N
    lengthLabel.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.lengthLabel.toolTipText")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.jLabel1.text")); // NOI18N

    thickField.setColumns(4);
    thickField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    thickField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    thickField.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.thickField.toolTipText")); // NOI18N
    thickField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    thickField.setValue(Outline.DEFAULT_THICKNESS);
    thickField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        thickFieldchangeThickness(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.jLabel3.text")); // NOI18N

    resolutionField.setColumns(4);
    resolutionField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
    resolutionField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    resolutionField.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.resolutionField.toolTipText")); // NOI18N
    resolutionField.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
    resolutionField.setValue(Outline.DEFAULT_RESOLUTION);
    resolutionField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        resolutionFieldchangeResoluttion(evt);
      }
    });

    buttonGroup1.add(editOutlineButton);
    editOutlineButton.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(editOutlineButton, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.editOutlineButton.text")); // NOI18N
    editOutlineButton.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.editOutlineButton.toolTipText")); // NOI18N

    buttonGroup1.add(editCutPointsButton);
    org.openide.awt.Mnemonics.setLocalizedText(editCutPointsButton, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.editCutPointsButton.text")); // NOI18N
    editCutPointsButton.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.editCutPointsButton.toolTipText")); // NOI18N

    offsetButton.setForeground(new java.awt.Color(255, 0, 0));
    org.openide.awt.Mnemonics.setLocalizedText(offsetButton, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.offsetButton.text")); // NOI18N
    offsetButton.setToolTipText("Offset the curve to compensate for the cutter radius after digitizing points."); // NOI18N
    offsetButton.setEnabled(false);
    offsetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        offsetCurveForCutter(evt);
      }
    });

    allCutsCheckBox.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(allCutsCheckBox, org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.allCutsCheckBox.text")); // NOI18N
    allCutsCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineEditorTopComponent.class, "OutlineEditorTopComponent.allCutsCheckBox.toolTipText")); // NOI18N
    allCutsCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        selectAllCutters(evt);
      }
    });

    javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
    controlPanel.setLayout(controlPanelLayout);
    controlPanelLayout.setHorizontalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlPanelLayout.createSequentialGroup()
        .addComponent(editOutlineButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(editCutPointsButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(coordsLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(lengthLabel)
        .addContainerGap())
      .addGroup(controlPanelLayout.createSequentialGroup()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(thickField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(resolutionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
        .addComponent(allCutsCheckBox)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(offsetButton))
    );
    controlPanelLayout.setVerticalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlPanelLayout.createSequentialGroup()
        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(editOutlineButton)
          .addComponent(editCutPointsButton)
          .addComponent(coordsLabel)
          .addComponent(lengthLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(thickField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3)
          .addComponent(resolutionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(offsetButton)
          .addComponent(allCutsCheckBox))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    outlineEditPanel.add(controlPanel, java.awt.BorderLayout.SOUTH);

    add(outlineEditPanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void thickFieldchangeThickness(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_thickFieldchangeThickness
    if (thickField.isFocusOwner()) {
      double t = Math.max(Outline.MIN_THICKNESS, ((Number) thickField.getValue()).doubleValue());
      if (outline != null) {
        outline.setThickness(t);
      }
    }
  }//GEN-LAST:event_thickFieldchangeThickness

  private void resolutionFieldchangeResoluttion(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_resolutionFieldchangeResoluttion
    if (resolutionField.isFocusOwner()) {
      double r = Math.max(Outline.MIN_RESOLUTION, ((Number) resolutionField.getValue()).doubleValue());
      if (outline != null) {
        outline.setResolution(r);
      }
    }
  }//GEN-LAST:event_resolutionFieldchangeResoluttion

  private void selectAllCutters(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllCutters
    drawPanel.repaint();
  }//GEN-LAST:event_selectAllCutters

  private void offsetCurveForCutter(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offsetCurveForCutter
    if (outline.getNumPts() < 2) {
      NotifyDescriptor d = new NotifyDescriptor.Message(
          "Must have more than 2 points!",
          NotifyDescriptor.WARNING_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
    outline.offsetForCutter();
    offsetButton.setEnabled(false);
  }//GEN-LAST:event_offsetCurveForCutter

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox allCutsCheckBox;
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JPanel controlPanel;
  private javax.swing.JLabel coordsLabel;
  private javax.swing.JToggleButton editCutPointsButton;
  private javax.swing.JToggleButton editOutlineButton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel lengthLabel;
  private javax.swing.JButton offsetButton;
  private javax.swing.JPanel outlineEditPanel;
  private javax.swing.JFormattedTextField resolutionField;
  private javax.swing.JFormattedTextField thickField;
  // End of variables declaration//GEN-END:variables
  @Override
  public void componentOpened() {
//    System.out.println("  >>OutlineEditorTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>OutlineEditorTopComponent.componentOpened em=" + em);
    updateRootNode();
    em.addPropertyChangeListener(this);

//    // TODO: Problem when Reset Windows -- listeners are added again!
//    // so for now, remove them all first if they are present
//    removeMouseListener(outlineEditPanel.drawPanel);
//    removeMouseMotionListener(outlineEditPanel.drawPanel);
//    removeMouseWheelListener(outlineEditPanel.drawPanel);
    // Then add them again
    addMouseListener(drawPanel);
    addMouseMotionListener(drawPanel);
    addMouseWheelListener(drawPanel);
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>OutlineEditorTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
    removeMouseMotionListener(drawPanel);
    removeMouseWheelListener(drawPanel);

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
   * Nested class for the drawing panel
   */
  public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, DropTargetListener {

    private final Color DEFAULT_BACKGROUND = Color.BLACK;
    private final double WINDOW_PERCENT = 0.90;		// use 90% of the window for the view
    private final double INITIAL_DPI = 150.0;		      // for the first time the window comes up
    private final Point INITIAL_ZPIX = new Point(150, 200);   // arbitrary
    private final int CLOSEST = 10;		// Grab point within 10 pixels	

    private final Font MESSAGE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private final Color MESSAGE_COLOR = Color.GREEN;
    private final static String OPEN_MSG = "Load a file by double clicking in Favorites window";

    private final Cursor EDIT_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);

    /** Dots per inch. */
    private double dpi = INITIAL_DPI;
    /** Location of zero. */
    private Point zeroPix = INITIAL_ZPIX;   // initial value is arbitrary

    /** Point being moved/dragged. */
    private OutlinePt grabbedPoint;
    /** Point being moved/dragged. */
    private SafePt grabbedSafePt;
    /** CutPoint being moved/dragged. */
    private CutPoint grabbedCut;
    /** Flag indicating that we are panning the view. */
    private boolean panning = false;
    /** Flag indicating that we are measuring. */
    private boolean measuring = false;
    /** Delta between clicked point and original zeroPix. */
    private int dzX, dzY;
    /** Original position of a point before dragging. */
    private Point2D.Double savedPos;

    /**
     * Create a new drawing panel
     */
    public DrawPanel() {
      setBackground(DEFAULT_BACKGROUND);
      setCursor(EDIT_CURSOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Graphics2D g2d = (Graphics2D) g;
      if (outline == null) {		// show opening message
        g2d.setColor(MESSAGE_COLOR);
        FontMetrics fm = g2d.getFontMetrics(MESSAGE_FONT);
        g2d.setFont(MESSAGE_FONT);
        g2d.drawString(OPEN_MSG, getWidth() / 2 - fm.stringWidth(OPEN_MSG) / 2, getHeight() / 2);
      } else {
        g2d.translate(zeroPix.x, zeroPix.y);
        g2d.scale(dpi, -dpi);	// positive y is up

        new Grid(-(double) zeroPix.x / dpi, -(double) (getHeight() - zeroPix.y) / dpi,
            (double) getWidth() / dpi, (double) getHeight() / dpi).paint(g2d);

        if (outline != null) {
          outline.paint(g2d);	// paint the curves
        }
        cutPtMgr.paint(g2d, (allCutsCheckBox.isSelected() ? null : cutEditPanel.getCutter()));	// paint the CutPoints
      }
    }

    /**
     * Scale so that the curves fit in the window.
     */
    private void zoomToFit() {
      BoundingBox bb = outline.getBoundingBox();		// bounding box for the curves
      dpi = (int) Math.min(WINDOW_PERCENT * getWidth() / bb.getWidth(),
          WINDOW_PERCENT * getHeight() / bb.getHeight());
      zeroPix = new Point((getWidth() - (int) (bb.getWidth() * dpi)) / 2 - (int) (bb.min.x * dpi),
          (getHeight() - (int) (bb.getHeight() * dpi)) / 2 + (int) (bb.max.y * dpi));
      if (dpi < 1) {
        dpi = INITIAL_DPI;		// this could happen when zoomToFit() and window is not open
        zeroPix = INITIAL_ZPIX;
      }
      repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (outline == null) {
        return;
      }
      if (e.getClickCount() >= 2) {
        Point2D.Double pt = scalePixToInch(new Point(e.getX(), e.getY()));
        if (editCutPointsButton.isSelected()) {	  // editing CutPoints
          if (e.isMetaDown()) {		// right double click adds GoToPoin
            cutPtMgr.addCut(GoToPoint.class, pt, cutEditPanel.getCutter());
          } else {				  // left double click adds other CutPoint
            addFromPalette(onCutCurve(pt));
          }
        } else {				  // editing curve points
          if (e.isMetaDown()) {		// right click for safePath plus point
            outline.addSafePoint(new SafePt(pt));
          } else {
            outline.addPoint(new OutlinePt(pt));  // otherwise curve point
          }
        }
      }
    }

    /**
     * Add a CutPoint based on the palette selection at the given point (which
     * is on the cut curve).
     *
     * @param pt point on the cutCurve
     */
    private void addFromPalette(Point2D.Double pt) {
      Lookup selItem = palette.getSelectedItem();
      if (selItem == null) {
        // nothing selected in palette, so add one like the last
        cutPtMgr.addCut(pt, cutEditPanel.getCutter());
      } else {
        Node selNode = selItem.lookup(Node.class);
        if (selNode == null) {
          // nothing selected in palette, so add one like the last
          cutPtMgr.addCut(pt, cutEditPanel.getCutter());
        } else {    // palette has a selection
          CutPoint last = cutPtMgr.getLastCutPoint(cutEditPanel.getCutter());
          if (last == null || !last.getClass().getSimpleName().equals(selNode.getName())) {
            // no last, or palette selection is different
            try {           // so make one from palette selection
              Class<? extends CutPoint> clazz = (Class<? extends CutPoint>) Class.forName("com.billooms.cutpoints." + selNode.getName());
              cutPtMgr.addCut(clazz, pt, cutEditPanel.getCutter());
            } catch (ClassNotFoundException ex) {
              // this shouldn't happen, but add one like the last
              cutPtMgr.addCut(pt, cutEditPanel.getCutter());
            }
          } else {
            cutPtMgr.addCut(pt, cutEditPanel.getCutter());  // add one like the lase
          }
        }
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
      if (outline == null) {
        return;
      }
      if (e.getClickCount() == 1) {
        dzX = e.getX() - zeroPix.x;
        dzY = e.getY() - zeroPix.y;
        Point2D.Double pt = scalePixToInch(new Point(e.getX(), e.getY()));
        if (editCutPointsButton.isSelected()) {	// editing CutPoints
          if (e.isMetaDown()) {		// right click: pan
            panning = true;
          } else {
            measuring = true;		// enable measuring
            savedPos = scalePixToInch(e.getPoint());
            grabbedCut = cutPtMgr.closestCutPt(pt, (double) CLOSEST / dpi, cutEditPanel.getCutter());	// left click: drag the cutpoint
            if (grabbedCut != null) {	// dragging a cutpoint
              savedPos = grabbedCut.getPos2D();
            }
          }
        } else {				  // Editing curve points
          if (e.isMetaDown()) {		// right click for safePath plus
            grabbedSafePt = outline.getClosestSafePt(pt, (double) CLOSEST / dpi);
            if (grabbedSafePt != null) {
              setCursor(MOVE_CURSOR);
            } else {
              panning = true;
            }
          } else {
            measuring = true;		// enable measuring
            savedPos = scalePixToInch(e.getPoint());
            grabbedPoint = outline.getClosestPt(pt, (double) CLOSEST / dpi);
            if (grabbedPoint != null) {
              setCursor(MOVE_CURSOR);
            }
          }
        }
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (outline == null) {
        return;
      }
      Point p = new Point(e.getX(), e.getY());
      Point2D.Double pt = scalePixToInch(p);
      if (measuring) {
        double d = Math.hypot(pt.x - savedPos.x, pt.y - savedPos.y);
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y) + ", distance: " + F3.format(d));
      } else {
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y));
      }
      if (panning) {
        zeroPix = new Point(p.x - dzX, p.y - dzY);
      } else if (grabbedPoint != null) {
        grabbedPoint.drag(pt);
      } else if (grabbedSafePt != null) {
        grabbedSafePt.drag(pt);
      } else if (grabbedCut != null) {
        grabbedCut.drag(onCutCurve(pt));	// dragging should not render detail
      }
      repaint();
    }

    private Point2D.Double onCutCurve(Point2D.Double pt) {
      if ((grabbedCut instanceof GoToPoint)) {
        return pt;  // don't force GoToPoints onto the curve
      }
      if (grabbedCut != null && !grabbedCut.isSnap()) {
        return pt;  // don't force un-snapped point onto the curve
      }
      Curve cutCurve = outline.getCutterPathCurve(cutEditPanel.getCutter());
      return cutCurve.nearestPoint(pt);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (outline == null) {
        return;
      }
      Point p = new Point(e.getX(), e.getY());
      Point2D.Double pt = scalePixToInch(p);
      if (measuring) {
        double d = Math.hypot(pt.x - savedPos.x, pt.y - savedPos.y);
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y) + ", distance: " + F3.format(d));
        measuring = false;
      } else {
        coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y));
      }
      if (panning) {
        zeroPix = new Point(p.x - dzX, p.y - dzY);
        panning = false;
      } else if (grabbedPoint != null) {
        grabbedPoint.move(pt);     // if dragging, move the point here
        grabbedPoint = null;
        setCursor(EDIT_CURSOR);
      } else if (grabbedSafePt != null) {
        grabbedSafePt.move(pt);     // if dragging, move the point here
        grabbedSafePt = null;
        setCursor(EDIT_CURSOR);
      } else if ((grabbedCut != null)) {
        moveGrabbed(grabbedCut, pt);
        grabbedCut = null;
      }
      repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
      if (outline == null) {
        return;
      }
      if (panning) {
        panning = false;
      } else if (grabbedPoint != null) {
        outline.deletePoint(grabbedPoint);
      } else if (grabbedSafePt != null) {
        outline.deleteSafePt(grabbedSafePt);
      } else if (grabbedCut != null) {
        cutPtMgr.removeCut(grabbedCut);
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      if (outline == null) {
        return;
      }
      Point2D.Double pt = scalePixToInch(new Point(e.getX(), e.getY()));
      coordsLabel.setText(F3.format(pt.x) + ", " + F3.format(pt.y));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      if (outline == null) {
        return;
      }
      int notches = e.getWheelRotation();
      dpi = Math.max(dpi + notches, 1);	// don't go below 1
      repaint();			// force display to update
    }

    /**
     * Move the grabbed point to a new location.
     *
     * @param grabbed current grabbed point
     * @param pt new location
     */
    private void moveGrabbed(CutPoint grabbed, Point2D.Double pt) {
      if (grabbed instanceof GoToPoint) {		// Are we dropping a GoToPoint onto a SpiralCutGoTo?
        SpiralCut nearest = cutPtMgr.closestSpiralCut(pt, (double) CLOSEST / dpi);
        if (nearest != null) {
          grabbed.move(savedPos);					// put it back where it was
          cutPtMgr.dropGoTo((GoToPoint) grabbed, nearest);  // drop the GoToPoint onto the SpiralCut
          return;
        }
      } else if (!(grabbed instanceof OffsetGroup)) {		// Are we dropping a CutPoint onto a OffsetGroup?
//        OffsetGroup nearest = cutPtMgr.closestOffsetGroup(pt, (double) CLOSEST / dpi);
        OffsetGroup nearest = cutPtMgr.closestOffsetGroup(new Point2D.Double(grabbed.getX(), grabbed.getZ()), (double) CLOSEST / dpi);
        if (nearest != null) {
          grabbed.move(savedPos);					// put it back where it was
          cutPtMgr.dropOffPoint(grabbed, nearest);  // drop the OffPoint onto the OffsetGroup
          return;
        }
      }
      grabbed.move(onCutCurve(pt));
    }

    /**
     * Convert a point in pixels to inches.
     *
     * @param p point in pixels
     * @return point in inches
     */
    private Point2D.Double scalePixToInch(Point p) {
      return new Point2D.Double((double) (p.x - zeroPix.x) / dpi,
          (double) (zeroPix.y - p.y) / dpi);
    }

    @Override
    public void drop(DropTargetDropEvent dropEv) {
      setEditCuts();    // clearly we're editing cuts now
      dropEv.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      Node node = NodeTransfer.node(dropEv.getTransferable(), NodeTransfer.DND_COPY_OR_MOVE);
      dropEv.dropComplete(node != null);
      if (null != node) {
        Point2D.Double pt = scalePixToInch(dropEv.getLocation());
        if (!node.getName().equals("GoToPoint")) {    // don't snap GoToPoints to the curve
          pt = onCutCurve(pt);
        }
        addFromPalette(pt);   // duplicate the paramenters from the last (if applicable)
      }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }
  }
}
