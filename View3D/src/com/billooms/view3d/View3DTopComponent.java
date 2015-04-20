package com.billooms.view3d;

import com.billooms.cutpoints.surface.Surface;
import com.billooms.cornfile.COrnFileDataObject;
import com.billooms.cutpoints.CutPoints;
import com.billooms.outline.Outline;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component for displaying the 3D view.
 *
 * The current Surface is made available via AbstractLookup.
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
    dtd = "-//com.billooms.view3d//View3D//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "View3DTopComponent",
    iconBase = "com/billooms/view3d/view3D16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.billooms.view3d.View3DTopComponent")
@ActionReference(path = "Menu/Window", position = 200)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_View3DAction",
    preferredID = "View3DTopComponent"
)
@Messages({
  "CTL_View3DAction=View3D",
  "CTL_View3DTopComponent=View3D Window",
  "HINT_View3DTopComponent=This is a View3D window"
})
public final class View3DTopComponent extends TopComponent implements PropertyChangeListener {

  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final InstanceContent ic = new InstanceContent();
  private final View3DPanel view3DPanel;
  protected Surface surface = null;
  protected CutPoints cutPtMgr = null;
  protected String filePath = "";   // used for snapshot

  public View3DTopComponent() {
    initComponents();
    setName(Bundle.CTL_View3DTopComponent());
    setToolTipText(Bundle.HINT_View3DTopComponent());

    view3DPanel = new View3DPanel(this);
    this.add(view3DPanel, BorderLayout.CENTER);

    em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
//    System.out.println("***View3DTopComponent.constructor em=" + em);

    this.associateLookup(new AbstractLookup(ic));
  }

  /**
   * When required, get the latest Outline from the root of the ExplorerManager
   * and build a new surface.
   */
  private synchronized void rootNodeChanged() {
    if (surface != null) {
      surface.clear();	      // surface should quit listening to the old outline
      surface.removePropertyChangeListener(view3DPanel); // view3DPanel should quit listening to the old surface
      ic.remove(surface);    // remove the old surface from the lookup
    }
    Node rootNode = em.getRootContext();
    if (rootNode == Node.EMPTY) {
      setName(Bundle.CTL_View3DTopComponent() + ": (no outline)");
      filePath = "";
      view3DPanel.snapShotButton.setEnabled(false);
      surface = null;	// nothing to view
      cutPtMgr = null;
    } else {
      setName(Bundle.CTL_View3DTopComponent() + ": " + rootNode.getDisplayName());
      filePath = ((COrnFileDataObject) rootNode.getLookup().lookup(MultiDataObject.class)).getPrimaryFile().getPath();
      view3DPanel.snapShotButton.setEnabled(true);
      cutPtMgr = rootNode.getLookup().lookup(CutPoints.class);
      surface = new Surface(rootNode.getLookup().lookup(Outline.class), view3DPanel.inOutButton.isSelected(), cutPtMgr);
      if (isShowing()) {    // only do the 3D rendering if the View3D window is showing
        view3DPanel.updateAll();   // update the Bowl
        surface.addPropertyChangeListener(view3DPanel);   // view3DPanel listen for changes in the surface
      }
      ic.add(surface);	    // add the new surface to the lookup
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("View3DTopComponent.propertyChange: " + evt.getSource().getClass().getName()
//	    + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Refresh the outline when rootContext changes on the ExplorerManager
    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
      rootNodeChanged();
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
//    System.out.println("View3DTopComponent.componentOpened em=" + em);
    rootNodeChanged();
    em.addPropertyChangeListener(this);
  }

  @Override
  public void componentClosed() {
//    System.out.println("View3DTopComponent.componentClosed");
    if (em != null) {
      em.removePropertyChangeListener(this);
    }
  }

  @Override
  protected void componentHidden() {
    super.componentHidden();
//    System.out.println("View3DTopComponent.componentHidden");
    if (surface != null) {
      surface.removePropertyChangeListener(view3DPanel); // view3DPanel should quit listening to the old surface
      view3DPanel.renderButton.setSelected(false);
    }
  }

  @Override
  protected void componentShowing() {
    super.componentShowing();
//    System.out.println("View3DTopComponent.componentShowing");
    if (surface != null) {
      view3DPanel.updateAll();   // update the Bowl
      surface.addPropertyChangeListener(view3DPanel);	// view3DPanel listen for changes in the surface
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

}
