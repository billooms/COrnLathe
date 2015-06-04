package com.billooms.outlineeditor;

import com.billooms.cutpoints.CutPoints;
import com.billooms.outline.Outline;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Action to scale a curve.
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
@ActionID(
    category = "Outline",
    id = "com.billooms.outlineeditor.Scale"
)
@ActionRegistration(
    iconBase = "com/billooms/outlineeditor/icons/Scale16.png",
    displayName = "#CTL_Scale"
)
@ActionReference(path = "Menu/Outline", position = 500)
@Messages("CTL_Scale=Scale...")
public final class Scale implements ActionListener {

  @Override
  public void actionPerformed(ActionEvent e) {
    String str = (String) JOptionPane.showInputDialog(
        null,
        "Coordinates will be multiplied by the scale factor",
        "Scale Factor",
        JOptionPane.QUESTION_MESSAGE,
        null,
        null,
        "1.0");
    if ((str != null) && (str.length() > 0)) {
      double factor = Double.parseDouble(str);
      if ((factor > 0.1) && (factor < 10.0) && (factor != 1.0)) {   // limit the range
        OutlineEditorTopComponent outEd = (OutlineEditorTopComponent) WindowManager.getDefault().findTopComponent("OutlineEditorTopComponent");
        if (outEd != null) {
          Outline outline = outEd.getLookup().lookup(Outline.class);
          if (outline != null) {
            outline.scale(factor);    // scale the outline
          }
          CutPoints cutPtMgr = outEd.getLookup().lookup(CutPoints.class);
          if (cutPtMgr != null) {
            cutPtMgr.scale(factor);   // and the cutpoint coordinates
          }
        }
      }
    }
  }
}
