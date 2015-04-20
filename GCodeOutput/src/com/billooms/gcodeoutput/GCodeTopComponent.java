package com.billooms.gcodeoutput;

import com.billooms.controls.Controls;
import com.billooms.controls.FeedRate;
import com.billooms.cutlist.CutList;
import com.billooms.cutlist.Inst;
import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.OffsetCut;
import com.billooms.cutters.Cutter;
import com.billooms.drawables.Pt;
import com.billooms.gcodeoutput.hardwareprefs.HardwarePrefs;
import com.billooms.outline.Outline;
import com.billooms.outlineeditor.OutlineEditorTopComponent;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component with controls for g-code parameters.
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
    dtd = "-//com.billooms.gcodeoutput//GCode//EN",
    autostore = false)
@TopComponent.Description(
    preferredID = "GCodeTopComponent",
    iconBase = "com/billooms/gcodeoutput/icons/GControl16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "com.billooms.gcodeoutput.GCodeTopComponent")
@ActionReference(path = "Menu/Window", position = 750)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_GCodeAction",
    preferredID = "GCodeTopComponent")
@NbBundle.Messages({
  "CTL_GCodeAction=GCode",
  "CTL_GCodeTopComponent=GCode Window",
  "HINT_GCodeTopComponent=This is a GCode window"
})
public final class GCodeTopComponent extends TopComponent implements PropertyChangeListener {

  private final static DecimalFormat F2 = new DecimalFormat("0.00");
  private final static DecimalFormat F5 = new DecimalFormat("0.00000");
  
  private final HardwarePrefs prefs = Lookup.getDefault().lookup(HardwarePrefs.class);
  private static ExplorerManager em = null;   // all instances share one ExplorerManager

  /** This is where all the controls are located. */
  private final GControls controlPanel;
  /** This is where the data is kept. */
  private Controls controls = null;
  /** Root node from the DataNavigator. */
  private Node rootNode;

  public GCodeTopComponent() {
    initComponents();
    setName(Bundle.CTL_GCodeTopComponent());
    setToolTipText(Bundle.HINT_GCodeTopComponent());

    controlPanel = new GControls();
    this.add(controlPanel, BorderLayout.CENTER);

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();

    this.associateLookup(Lookups.singleton(controlPanel));
  }

  /**
   * When required, get the latest Outline and CutPoint manager from the root of
   * the ExplorerManager.
   */
  private synchronized void updateRootNode() {
    rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_GCodeTopComponent() + ": (no file)");
      controls = null;
    } else {
      setName(Bundle.CTL_GCodeTopComponent() + ": " + rootNode.getDisplayName());
      controls = rootNode.getLookup().lookup(Controls.class);
    }
    controlPanel.setControls(controls);
  }

  /**
   * Make the pseudo-instructions in the CutList then convert to g-code
   *
   * @param cPt Output for a single OffsetCutPoint (or all others if this is
   * null)
   * @param out output
   */
  public void writeGCode(PrintWriter out, OffsetCut cPt) {
    if (rootNode == null) {
      return;
    }
    
	CutList cutList = Lookup.getDefault().lookup(CutList.class);
    Outline outline = rootNode.getLookup().lookup(Outline.class);
    CutPoints cutMgr = rootNode.getLookup().lookup(CutPoints.class);
    Cutter cutter = ((OutlineEditorTopComponent)WindowManager.getDefault().findTopComponent("OutlineEditorTopComponent")).getSelectedCutter();
    
    cutList.clear();
    if (outline.getDotCurve().getSize() < 2) {
      NotifyDescriptor d = new NotifyDescriptor.Message(
          "Traced outline must have at least 2 points.",
          NotifyDescriptor.WARNING_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
    if (controlPanel.cutRosetteButton.isSelected()) {
      cutMgr.makeInstructions(cPt, cutter,
          controlPanel.cfPanel.getPassDepth(), controlPanel.cfPanel.getPassStep(),
          controlPanel.cfPanel.getLastDepth(), controlPanel.cfPanel.getLastStep(),
          prefs.getStepsPerRotation(),
          controlPanel.cfPanel.getRotation());
    } else if (controlPanel.cutCurveButton.isSelected()) {
      controlPanel.cutCurvePanel.makeInstructions(cutList, cutter, outline);
    } else {      // threads
      if (outline.getDotCurve().getSize() != 2) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Traced outline must have only 2 points.",
            NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        return;
      }
      Pt p0 = outline.getDotCurve().getPt(0);
      Pt p1 = outline.getDotCurve().getPt(1);
      if (p0.getX() != p1.getX()) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Traced outline points must have same X.",
            NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        return;
      }
      controlPanel.threadPanel.makeInstructions(cutList, cutter, outline);
    }
    cutList.update();		// this will cause a re-draw of the display
    if (cutList.length() == 0) {
      return;
    }
    convertToG(out, cutList);
  }

  private void convertToG(PrintWriter out, CutList cutList) {
    out.println("; g-code generated by COrnLathe");
    out.println("g20 (units are inches)");
    out.println("g90 (absolute distance mode)");

    int maxInvF = 60 * prefs.getMaxGPerSec();       // Max inverse time speed based on max instructions per second
    int stepsPerRot = prefs.getStepsPerRotation();
    int stepsPerInch = prefs.getStepsPerInch();
    double rpm = controlPanel.feedPanel.getRpm();	// rotations per minute
    double vel = controlPanel.feedPanel.getVelocity();	// inches per minute

    long lastX = 0;		// Assume the stage is currently at 0,0,0
    long lastZ = 0;		// This might not be true, but there is no way of knowing.
    long lastC = 0;		// *** TODO Find some way around this! ***
    boolean firstPt = true;	// flag so that we always go to first point even if 0,0,0
    double x, z, c, xx, zz, cc, dx, dz, dc, time;
    long lx, lz, lc;
    for (Inst i : cutList.getAll()) {
      x = i.getX();
      z = i.getZ();
      c = i.getC();	// reminder: degrees (not rotation)
      lx = Math.round((x * (double) stepsPerInch));
      lz = Math.round((z * (double) stepsPerInch));
      lc = Math.round(c / 360.0 * (double) stepsPerRot);	// reminder: steps
      xx = lx / (double) stepsPerInch;
      zz = lz / (double) stepsPerInch;
      cc = lc * 360.0 / (double) stepsPerRot;
      dx = Math.abs((double) (lx - lastX) / (double) stepsPerInch);
      dz = Math.abs((double) (lz - lastZ) / (double) stepsPerInch);
      dc = Math.abs((double) (lc - lastC) / (double) stepsPerRot);	// reminder: rotation (not degrees)
      switch (i.getType()) {
        case COMMENT:
          out.println("; " + i.getText());
          break;
        case GO_XZ_FAST:	// Proportional X & Z movement scaled with one stage at max velocity
          if ((lx == lastX) && (lz == lastZ) && !firstPt) {
            break;		// no movement
          }
          // Inches per minute mode
          out.println("g94 g1"
              + " x" + F5.format(x)
              + " z" + F5.format(z)
              + " f" + F2.format(FeedRate.MAX_VEL));
          lastX = lx;
          lastZ = lz;
          firstPt = false;
          break;
        case GO_XZ_VEL:			// Proportional X & Z movement scaled with one stage at set velocity
          if ((lx == lastX) && (lz == lastZ) && !firstPt) {
            break;		// no movement
          }
          // Inches per minute mode
          out.println("g94 g1"
              + " x" + F5.format(x)
              + " z" + F5.format(z)
              + " f" + F2.format(vel));
          lastX = lx;
          lastZ = lz;
          firstPt = false;
          break;
        case GO_XZC_FAST:	// Proportional X & Z movement scaled with one stage at max velocity, maximum spindle
          if ((lx == lastX) && (lz == lastZ) && (lc == lastC)) {
            if (firstPt) {
              out.println("g0"
                  + " x" + F5.format(x)
                  + " z" + F5.format(z)
                  + " c" + F2.format(c));
              firstPt = false;
              break;
            } else {
              break;		// no movement
            }
          }
          time = Math.max(Math.max(dx / FeedRate.MAX_VEL, dz / FeedRate.MAX_VEL), dc / FeedRate.MAX_RPM);
          // Inverse Time mode using both velocity and rpm 
          // (whichever is the limit for the distance traveled)
          out.println("g93 g1"
              + " x" + F5.format(x)
              + " z" + F5.format(z)
              + " c" + F2.format(c)
              + " f" + F2.format(1.0 / time));
          lastX = lx;
          lastZ = lz;
          lastC = lc;
          firstPt = false;
          break;
        case GO_XZC_VEL:	// Proportional X, Z, and C, scaled for set velocity
          if ((lx == lastX) && (lz == lastZ) && (lc == lastC) && !firstPt) {
            break;		// no movement
          }
          if ((lx != lastX) || (lz != lastZ)) {	// either x or z is moving
            // Inches per minute mode limited by velocity
            out.println("g94 g1"
                + " x" + F5.format(x)
                + " z" + F5.format(z)
                + " c" + F2.format(c)
                + " f" + F2.format(vel));
          } else {								// only c is moving
            // Inverse Time mode using rpm as the limit
            out.println("g93 g1"
                + " c" + F2.format(c)
                + " f" + F2.format(rpm / dc));
          }
          lastX = lx;
          lastZ = lz;
          lastC = lc;
          firstPt = false;
          break;
        case GO_XZC_RPM:	// Proportional X, Z, and C, scaled for set RPM
          if ((lx == lastX) && (lz == lastZ) && (lc == lastC) && !firstPt) {
            break;		// no movement
          }
          // Don't go faster than MAX_VEL 
          // This is mainly used when following rosettes
          // Note we aren't using the panel feed rate, but the MAX
          time = Math.max(Math.max(dx / FeedRate.MAX_VEL, dz / FeedRate.MAX_VEL), dc / rpm);
          if (lc != lastC) {		// c is moving
            // Inverse Time mode using rpm as the limit
            // but don't exceed max instructions per second
            out.println("g93 g1"
                + " x" + F5.format(xx)
                + " z" + F5.format(zz)
                + " c" + F2.format(cc)
                + " f" + F2.format(Math.min(1.0 / time, maxInvF)));
          } else {				// c isn't moving, so x or z must be moving
            // Inches per minute mode limited by velocity
            out.println("g94 g1"
                + " x" + F5.format(x)
                + " z" + F5.format(z)
                + " f" + F2.format(vel));
          }
          lastX = lx;
          lastZ = lz;
          lastC = lc;
          firstPt = false;
          break;
        case TURN:			// Turn to C degrees at set RPM, no wrap check	
          if (lc == lastC) {
            break;		// no movement
          }
          // Inverse Time mode using rpm as the limit
          out.println("g93 g1"
              + " c" + F2.format(c)
              + " f" + F2.format(rpm / dc));
          lastC = lc;
          break;
        case SPINDLE_WRAP_CHECK:	// Wrap-around check : set spindle between -180 and +180 degrees
          long newC = lastC;
          while (newC > stepsPerRot / 2) {
            newC = newC - stepsPerRot;
          }
          while (newC < -stepsPerRot / 2) {
            newC = newC + stepsPerRot;
          }
          if (newC != lastC) {
            // Offset the c axis back to +/- 180 degrees
            out.println("g92 c" + ((double) newC * 360.0 / (double) stepsPerRot));
            lastC = newC;
          }
          break;
      }
    }
    out.println("g92.1 (clear offsets)");
    out.println("m2 (end of program)");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("GCodeTopComponent.propertyChange: " + evt.getSource().getClass().getName()
//	    + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // This listens to the ExplorerManager
    // Refresh the outline when rootContext changes on the ExplorerManager
    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
      updateRootNode();
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
	@Override
  public void componentOpened() {
//    System.out.println("  >>GCodeTopComponent.componentOpened");
    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("  >>GCodeTopComponent.componentOpened em=" + em);
    updateRootNode();
    em.addPropertyChangeListener(this);

    controlPanel.linuxCNCPanel.initialize();	// can't do this in the constructor
  }

  @Override
  public void componentClosed() {
//    System.out.println("  >>GCodeTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }

    controlPanel.linuxCNCPanel.close();
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
}
