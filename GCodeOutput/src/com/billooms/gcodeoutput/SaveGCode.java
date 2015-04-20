package com.billooms.gcodeoutput;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutpoints.OffsetCut;
import com.billooms.gcodeoutput.hardwareprefs.HardwarePrefs;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Action to save g-code to a file.
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
@ActionID(category = "File",
    id = "com.billooms.gcodeoutput.SaveGCode")
@ActionRegistration(displayName = "#CTL_SaveGCode")
@ActionReferences({
  @ActionReference(path = "Menu/File", position = 600)
})
@Messages("CTL_SaveGCode=Save g-code")
public final class SaveGCode implements ActionListener {

  /** Extension for g-code files. */
  private final static String EXTENSION = "ngc";

  /** All instances share one ExplorerManager. */
  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  /** Hardware preferences. */
  private final HardwarePrefs prefs = Lookup.getDefault().lookup(HardwarePrefs.class);

  @Override
  public void actionPerformed(ActionEvent e) {
    if (em == null) {
      em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
    }
    if (em == null) {
      return;
    }
    Node rootNode = em.getRootContext();     // get the current DataNavigator root node.
    if (rootNode == null) {
      return;   // no root node in the DataNavigator
    }
    CutPoints cutMgr = rootNode.getLookup().lookup(CutPoints.class);    // current CutPoint manager
    
    File gCodeFile;
    OffsetCut offPt = null;

    ArrayList<OffsetCut> offList = cutMgr.getOffsetCutPoints();  // are there any OffsetCutPoints?
    if (!offList.isEmpty()) {
      FilterOffsetPanel panel;
      try {
        panel = new FilterOffsetPanel(offList);
      } catch (Exception ex) {
        NotifyDescriptor d = new NotifyDescriptor.Message(ex, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        return;
      }
      DialogDescriptor dd = new DialogDescriptor(
          panel,
          "Filter g-code for offset point",
          true,
          DialogDescriptor.OK_CANCEL_OPTION,
          DialogDescriptor.OK_OPTION,
          null);
      Object result = DialogDisplayer.getDefault().notify(dd);
      if (result == DialogDescriptor.CANCEL_OPTION) {
        return;
      }
      offPt = panel.getSelection(offList);    // use this OffsetCutPoint for output
    }

    if (prefs.isGSame()) {
      FileObject fileObj = rootNode.getLookup().lookup(FileObject.class);
      if (fileObj != null) {
        String suffix = "";
        if (offPt != null) {
          suffix = "_" + Integer.toString(offPt.getNum());
        }
        gCodeFile = new File((fileObj.getPath()).replaceAll("." + fileObj.getExt(), suffix + "." + EXTENSION));
      } else {
        File home = new File(System.getProperty("user.home"));	//The default dir to use if no value is stored
        gCodeFile = new FileChooserBuilder("openfile")
            .setTitle("Save g-code File As...")
            .setDefaultWorkingDirectory(home)
            .setApproveText("save")
            .setFileFilter(new FileNameExtensionFilter(EXTENSION + " files", EXTENSION))
            .showSaveDialog();
      }
      if (gCodeFile == null) {
        return;
      }
      if (!(gCodeFile.toString()).endsWith("." + EXTENSION)) {
        gCodeFile = new File(gCodeFile.toString() + "." + EXTENSION);
      }
      if (gCodeFile.exists()) {					// Ask the user whether to replace the file.
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
            "The file " + gCodeFile.getName() + " already exists.\nDo you want to replace it?",
            "Overwrite File Check",
            NotifyDescriptor.YES_NO_OPTION,
            NotifyDescriptor.WARNING_MESSAGE);
        d.setValue(NotifyDescriptor.CANCEL_OPTION);
        Object result = DialogDisplayer.getDefault().notify(d);
        if (result != DialogDescriptor.YES_OPTION) {
          return;
        }
      }
    } else {
      gCodeFile = new File(prefs.getGPath());
    }

    StatusDisplayer.getDefault().setStatusText("Saving g-code File As: " + gCodeFile.getName());
    PrintWriter out;
    try {
      FileOutputStream stream = new FileOutputStream(gCodeFile);
      out = new PrintWriter(stream);
    } catch (Exception ex) {
      NotifyDescriptor d = new NotifyDescriptor.Message("Error while trying to open the g-code file:\n" + ex,
          NotifyDescriptor.ERROR_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return;
    }
//    try {
      ((GCodeTopComponent) WindowManager.getDefault().findTopComponent("GCodeTopComponent")).writeGCode(out, offPt);
//    } catch (Exception ex) {
//      NotifyDescriptor d = new NotifyDescriptor.Message("Error while trying to write the g-code file:\n" + ex,
//          NotifyDescriptor.ERROR_MESSAGE);
//      DialogDisplayer.getDefault().notify(d);
//    } finally {
      out.close();
//    }
  }
}
