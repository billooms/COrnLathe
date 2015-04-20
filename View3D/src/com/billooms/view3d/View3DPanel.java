package com.billooms.view3d;

import com.billooms.cutpoints.surface.Line3D;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;

/**
 * JPanel for displaying the 3D view and associated controls. This listens for
 * changes in the Surface and updates the Bowl in the Scene.
 *
 * When this is a separate class (not private within View3DTopComponent), then
 * the JFXPanel seems to stretch to fit the JPanel. When this was a private
 * class within View3DTopComponent, then one had to specify the width and height
 * and it didn't stretch with the JPanel which was a pain.
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
public class View3DPanel extends JPanel implements PropertyChangeListener {

  private final static int FILE_IMAGE_WIDTH = 1000;   // for saved files
  private final static int FILE_IMAGE_HEIGHT = 800;
  private final static Color BACKGROUND_COLOR = Color.WHITE;

  // When View3DPanel is a separate class (not within View3DTopComponent) 
  // then the width and height don't seem to matter. 
  // The scene seems to adapt to fill the JPanel (which is nice). 
  private final static int SCENE_WIDTH = 800;
  private final static int SCENE_HEIGHT = 400;

  private Scene scene;
//  private static JFXPanel myFXPanel;
  private static myJFXPanel myFXPanel;
  private final Group root = new Group();	  // root will only contain world and camera
  private final Xform world = new Xform();	  // world holds everything and sets lathe coordinates
  private AxesGrid axes;
  private BowlShape bowl;
  private MultiLine3D lines;
  private final static double AXES_LENGTH = 4.0;

  private final PerspectiveCamera camera = new PerspectiveCamera(true);
  private final double cameraDistance = 10;
  private final Xform cameraXform = new Xform();
  private final Xform cameraXform2 = new Xform();

  private final static double MOUSE_ORBIT_SCALE = 0.3;	// determined by experiment
  private final static double MOUSE_SCROLL_SCALE = 0.01;
  private final static double MOUSE_PAN_SCALE = 0.02;
  private double mouseOldX;
  private double mouseOldY;

  private final View3DTopComponent parent;

  /**
   * Creates new form view3DPanel.
   *
   * @param parent TopComponent
   */
  public View3DPanel(View3DTopComponent parent) {
    this.parent = parent;
    initComponents();

//    myFXPanel = new JFXPanel();
    myFXPanel = new myJFXPanel();
    myFXPanel.putClientProperty("print.printable", Boolean.TRUE); // myFXPanel can be printed
    add(myFXPanel, BorderLayout.CENTER);

    Platform.setImplicitExit(false);  // keep running when window closed
    Platform.runLater(() -> {
      createScene(myFXPanel);
    });
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("View3DPanel.propertyChange: "  + evt.getSource().getClass().getSimpleName() + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listens to the surface
    // When the surface changes, update the bowl and lines and repaint
    updateAll();
  }

  /**
   * Update the bowl from the new shape. This runs on the JavaFX thread.
   */
  protected synchronized void updateAll() {
//    System.out.println("  View3DPanel.updateBowl" + System.currentTimeMillis());
    if (bowl != null) {
      Platform.runLater(() -> {
        bowl.refresh(parent.surface);
      });
    }
    if ((lines != null) && lineCheckBox.isSelected()) {
      Platform.runLater(() -> {
        lines.refresh(getAllLines());
      });
    }
  }

  /**
   * Create the scene. This method is invoked on JavaFX thread.
   *
   * @param fxPanel
   */
  private void createScene(JFXPanel fxPanel) {
    //  root
    //    light1, light2
    //    world
    //      axes
    //      bowl
    //    cameraXform
    //      cameraXform2
    //	camera
    root.getChildren().add(world);
    world.rx.setAngle(90.0);	// transform to X right, Y back, Z up for lathe

    scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, true);
    scene.setFill(BACKGROUND_COLOR);
    fxPanel.setScene(scene);

    buildCamera();
    scene.setCamera(camera);

    buildLights();

    axes = new AxesGrid(AXES_LENGTH);
    world.getChildren().add(axes);

    bowl = new BowlShape(parent.surface);
    world.getChildren().add(bowl);

    lines = new MultiLine3D(getAllLines());
    world.getChildren().add(lines);

    handleMouse(scene);
  }

  /**
   * Get all 3D lines that need to be drawn.
   */
  private List<Line3D> getAllLines() {
    if (parent.cutPtMgr != null) {
      return parent.cutPtMgr.getAll3DLines();
    }
    return new ArrayList<>();
  }

  private void buildLights() {
    AmbientLight ambientLight = new AmbientLight(Color.color(0.5, 0.5, 0.5));
    // Since this is attached to root, X is right, Y is down, Z is back
    PointLight light1 = new PointLight(Color.WHITE);
    light1.setTranslateX(20);     // right
    light1.setTranslateY(-20);    // up
    light1.setTranslateZ(-20);    // forward
    PointLight light2 = new PointLight(Color.color(0.6, 0.6, 0.6));
    light2.setTranslateX(-20);    // left
    light2.setTranslateY(-20);    // up
//    light2.setTranslateZ(-20);    // forward
//    PointLight light3 = new PointLight(Color.color(0.3, 0.3, 0.3));
//    light3.setTranslateY(20);     // bottom
    root.getChildren().addAll(ambientLight, light1, light2);
  }

  /**
   * Do whatever is required to setup the camera. Note that this also adds the
   * camera to root so that camera directions are the original JavaFX directions
   * (not lathe directions).
   */
  private void buildCamera() {
    root.getChildren().add(cameraXform);
    cameraXform.getChildren().add(cameraXform2);  // cameraXform is used for mouse orbiting
    cameraXform2.getChildren().add(camera);	  // cameraXform2 is used for mouse translation

    camera.setNearClip(0.1);
    camera.setFarClip(100.0);
    camera.setTranslateZ(-cameraDistance);
    cameraXform.ry.setAngle(-20.0);
    cameraXform.rx.setAngle(-20.0);
  }

  /** Handle mouse events for orbiting, panning, and zooming.
   *
   * @param scene
   * @param root
   */
  private void handleMouse(Scene scene) {
    scene.setOnMousePressed(event -> {
      if (event.getClickCount() == 2) {	  // Double click to reset the view to the default
        resetView();
        return;
      }
      mouseOldX = event.getSceneX();
      mouseOldY = event.getSceneY();
    });
    scene.setOnMouseDragged(event -> {
      double mousePosX = event.getSceneX();
      double mousePosY = event.getSceneY();
      double dx = (mousePosX - mouseOldX);
      double dy = (mousePosY - mouseOldY);
      if (event.isPrimaryButtonDown()) {
//	  world.ry.setAngle(world.ry.getAngle() - dx * MOUSE_ORBIT_SCALE);
//	  world.rx.setAngle(world.rx.getAngle() + dy * MOUSE_ORBIT_SCALE);
        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + dx * MOUSE_ORBIT_SCALE);
        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - dy * MOUSE_ORBIT_SCALE);
      } else if (event.isSecondaryButtonDown()) {
//	  world.setTx(world.t.getX() + dx * MOUSE_PAN_SCALE);
//	  world.setTy(world.t.getY() + dy * MOUSE_PAN_SCALE);
        cameraXform2.setTx(cameraXform2.t.getX() - dx * MOUSE_PAN_SCALE);
        cameraXform2.setTy(cameraXform2.t.getY() - dy * MOUSE_PAN_SCALE);
      }
      mouseOldX = mousePosX;
      mouseOldY = mousePosY;
    });
    scene.setOnScroll(event -> {
      camera.setTranslateZ(camera.getTranslateZ() + event.getDeltaY() * MOUSE_SCROLL_SCALE);
    });
  }

  /**
   * Reset the view to the default.
   */
  private void resetView() {
    cameraXform.reset();
    cameraXform2.reset();
    cameraXform.ry.setAngle(-20.0);
    cameraXform.rx.setAngle(-20.0);
    camera.setTranslateZ(-cameraDistance);
  }

  /**
   * Patch to fix the doubling problem.
   */
  class myJFXPanel extends JFXPanel {

    @Override
    public void removeNotify() {
      try {
        Field scaleFactor = JFXPanel.class.getDeclaredField("scaleFactor");
        scaleFactor.setAccessible(true);
        scaleFactor.setInt(this, 1);
      } catch (NoSuchFieldException | IllegalAccessException e) {
      }
      super.removeNotify();
    }
  }

  /** This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    controlPanel = new javax.swing.JPanel();
    gridButton = new javax.swing.JToggleButton();
    resetButton = new javax.swing.JButton();
    inOutButton = new javax.swing.JToggleButton();
    snapShotButton = new javax.swing.JButton();
    renderButton = new javax.swing.JToggleButton();
    lineCheckBox = new javax.swing.JCheckBox();

    setLayout(new java.awt.BorderLayout());

    gridButton.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(gridButton, org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.gridButton.text_1")); // NOI18N
    gridButton.setToolTipText(org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.gridButton.toolTipText_1")); // NOI18N
    gridButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        gridButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.resetButton.text_1")); // NOI18N
    resetButton.setToolTipText(org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.resetButton.toolTipText_1")); // NOI18N
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });

    inOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/billooms/view3d/Outside20.png"))); // NOI18N
    inOutButton.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(inOutButton, org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.inOutButton.text_1")); // NOI18N
    inOutButton.setToolTipText(org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.inOutButton.toolTipText_1")); // NOI18N
    inOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
    inOutButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/billooms/view3d/Inside20.png"))); // NOI18N
    inOutButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        inOutButtonActionPerformed(evt);
      }
    });

    snapShotButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/billooms/view3d/camera20.png"))); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(snapShotButton, org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.snapShotButton.text")); // NOI18N
    snapShotButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        snapShotButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(renderButton, org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.renderButton.text")); // NOI18N
    renderButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        renderButtonActionPerformed(evt);
      }
    });

    lineCheckBox.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(lineCheckBox, org.openide.util.NbBundle.getMessage(View3DPanel.class, "View3DPanel.lineCheckBox.text")); // NOI18N
    lineCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        lineCheckBoxActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
    controlPanel.setLayout(controlPanelLayout);
    controlPanelLayout.setHorizontalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlPanelLayout.createSequentialGroup()
        .addComponent(gridButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(inOutButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(renderButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lineCheckBox)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
        .addComponent(snapShotButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(resetButton))
    );
    controlPanelLayout.setVerticalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(gridButton)
        .addComponent(resetButton)
        .addComponent(inOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(snapShotButton)
        .addComponent(renderButton)
        .addComponent(lineCheckBox))
    );

    add(controlPanel, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

  private void gridButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridButtonActionPerformed
    Platform.runLater(() -> {	// run on the JavaFX thread
      axes.setVisible(gridButton.isSelected());
    });
  }//GEN-LAST:event_gridButtonActionPerformed

  private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
    Platform.runLater(() -> {	// run on the JavaFX thread
      resetView();
    });
  }//GEN-LAST:event_resetButtonActionPerformed

  private void inOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inOutButtonActionPerformed
    if (parent.surface != null) {
      parent.surface.setInOut(inOutButton.isSelected());    // also does a surface.rebuild()
    }
  }//GEN-LAST:event_inOutButtonActionPerformed

  private void snapShotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapShotButtonActionPerformed
    if (parent.filePath.isEmpty()) {
      return;	// don't save if there is no file open (shouldn't happen)
    }
    File saveFile = new File(parent.filePath.replaceAll(".xml", ".png"));
    JFileChooser chooser = new FileChooserBuilder("openfile")
        .setTitle("Save 3D Graphic View")
        .setApproveText("save")
        .createFileChooser();
    chooser.setSelectedFile(saveFile);
    int option = chooser.showSaveDialog(null);
    if (option != JFileChooser.APPROVE_OPTION) {
      return;	// User canceled or clicked the dialog's close box.
    }
    saveFile = chooser.getSelectedFile();
    if (saveFile == null) {
      return;	// null response
    }
    if (!(saveFile.toString()).endsWith(".png")) {	// make sure we have format right
      saveFile = new File(saveFile.toString() + ".png");
    }
    if (saveFile.exists()) {					// Ask the user whether to replace the file.
      NotifyDescriptor d = new NotifyDescriptor.Confirmation(
          "The file " + saveFile.getName() + " already exists.\nDo you want to replace it?",
          "Overwrite File Check",
          NotifyDescriptor.OK_CANCEL_OPTION,
          NotifyDescriptor.WARNING_MESSAGE);
      d.setValue(NotifyDescriptor.CANCEL_OPTION);
      Object result = DialogDisplayer.getDefault().notify(d);
      if (result != null && result == DialogDescriptor.CANCEL_OPTION) {
        return;	  // don't replace
      }
    }
    File pngFile = new File(saveFile.toString());   // must be effectively final for JavaFX thread
    StatusDisplayer.getDefault().setStatusText("Save 3D View to: " + pngFile.getName());

    Platform.runLater(() -> {	// run on the JavaFX thread
      WritableImage image = new WritableImage(FILE_IMAGE_WIDTH, FILE_IMAGE_HEIGHT);
//      WritableImage image = new WritableImage(this.getWidth(), this.getHeight());
      scene.snapshot(image);
      BufferedImage bufImage = SwingFXUtils.fromFXImage(image, null);
      try {
        ImageIO.write(bufImage, "png", pngFile);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    });
  }//GEN-LAST:event_snapShotButtonActionPerformed

  private void renderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renderButtonActionPerformed
    parent.surface.setRender(renderButton.isSelected());
  }//GEN-LAST:event_renderButtonActionPerformed

  private void lineCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineCheckBoxActionPerformed
    if (!lineCheckBox.isSelected()) {
      Platform.runLater(() -> {
        lines.clear();
      });
    }
    updateAll();
  }//GEN-LAST:event_lineCheckBoxActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel controlPanel;
  private javax.swing.JToggleButton gridButton;
  protected javax.swing.JToggleButton inOutButton;
  private javax.swing.JCheckBox lineCheckBox;
  protected javax.swing.JToggleButton renderButton;
  private javax.swing.JButton resetButton;
  protected javax.swing.JButton snapShotButton;
  // End of variables declaration//GEN-END:variables
}
