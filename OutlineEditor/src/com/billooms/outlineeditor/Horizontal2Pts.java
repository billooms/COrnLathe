package com.billooms.outlineeditor;

import com.billooms.cutpoints.CutPoints;
import com.billooms.outline.Outline;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Action to make an outline with two points horizontal.
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
    id = "com.billooms.outlineeditor.Horizontal2Pts"
)
@ActionRegistration(
    iconBase = "com/billooms/outlineeditor/icons/Horizontal16.png",
    displayName = "#CTL_Horizontal2Pts"
)
@ActionReference(path = "Menu/Outline", position = 200)
@Messages("CTL_Horizontal2Pts=2Pts: Horizontal")
public final class Horizontal2Pts implements ActionListener {

  @Override
  public void actionPerformed(ActionEvent e) {
    OutlineEditorTopComponent outEd = (OutlineEditorTopComponent) WindowManager.getDefault().findTopComponent("OutlineEditorTopComponent");
    if (outEd != null) {
      Outline outline = outEd.getLookup().lookup(Outline.class);
      if (outline != null) {
        outline.set2PtsHorizontal();
      }
      CutPoints cutPtMgr = outEd.getLookup().lookup(CutPoints.class);
      if (cutPtMgr != null) {
        cutPtMgr.snapAllCutPoints();
      }
    }
  }
}
