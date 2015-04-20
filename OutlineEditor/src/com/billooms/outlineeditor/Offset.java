package com.billooms.outlineeditor;

import com.billooms.cutpoints.CutPoints;
import com.billooms.drawables.BoundingBox;
import com.billooms.outline.Outline;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Action to offset the curves vertically.
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
    id = "com.billooms.outlineeditor.Offset"
)
@ActionRegistration(
    iconBase = "com/billooms/outlineeditor/icons/TopZero16.png",
    displayName = "#CTL_Offset"
)
@ActionReference(path = "Menu/Outline", position = 300)
@Messages("CTL_Offset=Offset Curve...")
public final class Offset implements ActionListener {

  private static OffsetPanel panel = null;	// keeps the values for the next use

  @Override
  public void actionPerformed(ActionEvent e) {
    if (panel == null) {
      panel = new OffsetPanel();
    }

    DialogDescriptor dd = new DialogDescriptor(
        panel,
        "Offset Curves",
        true,
        DialogDescriptor.OK_CANCEL_OPTION,
        DialogDescriptor.OK_OPTION,
        null);
    Object result = DialogDisplayer.getDefault().notify(dd);
    if (result == DialogDescriptor.OK_OPTION) {
      OutlineEditorTopComponent outEd = (OutlineEditorTopComponent) WindowManager.getDefault().findTopComponent("OutlineEditorTopComponent");
      if (outEd != null) {
        Outline outline = outEd.getLookup().lookup(Outline.class);
        CutPoints cutPtMgr = outEd.getLookup().lookup(CutPoints.class);
        if (outline != null) {
          BoundingBox bb;
          switch (panel.dirCombo.getSelectedIndex()) {
            case 0:			// vertical offset
              double current;
              switch (panel.curveCombo.getSelectedIndex()) {	// get BoundingBox for selected curve
                case 0:   // Inside Curve 
                  bb = outline.getInsideCurve().getBoundingBox();
                  break;
                case 1:   // Outside Curve
                  bb = outline.getOutsideCurve().getBoundingBox();
                  break;
                case 2:   // Cutter Curve
                default:
                  bb = outline.getCutterPathCurve().getBoundingBox();
                  break;
              }
              switch (panel.topBottomCombo.getSelectedIndex()) {  // Top or Bottom?
                case 0:     // Top
                  current = bb.max.y;
                  break;
                case 1:     // Bottom
                default:
                  current = bb.min.y;
                  break;
              }
              double target = ((Number) panel.posField.getValue()).doubleValue();
              outline.offsetVertical(current - target);		// update the outline first
              cutPtMgr.offsetVertical(current - target);	// then the cutpoints
              break;
            case 1:			// perpendicular offset
              double offset = ((Number) panel.posField.getValue()).doubleValue();
              if (offset != 0.0) {
                outline.offsetDotCurve(offset);		// offset the dotCurve
                cutPtMgr.snapAllCutPoints();        // then snap all cuts
              }
              break;
          }
          outEd.zoomToFit();
        }
      }
    }
  }
}
