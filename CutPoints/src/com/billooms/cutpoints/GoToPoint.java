package com.billooms.cutpoints;

import static com.billooms.clclass.CLclass.indent;
import com.billooms.controls.CoarseFine;
import static com.billooms.cutlist.Speed.*;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.outline.Outline;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.openide.text.*;
import org.w3c.dom.Element;

/**
 * A point to go to between two other CutPoints to avoid hitting the surface.
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
@PaletteItemRegistration(paletteid = "MyPalette",
    category = "CutPoints",
    itemid = "GoToPoint",
    icon16 = "com/billooms/cutpoints/icons/GoTo16.png",
    icon32 = "com/billooms/cutpoints/icons/GoTo32.png",
    name = "GoToPoint",
    body = "GoToPoint body",
    tooltip = "Go to this point between cuts to avoid hitting the surface.")
public class GoToPoint extends CutPoint implements ActiveEditorDrop {

  /** Color for GoToPoints. */
  protected final static Color GOTO_COLOR = Color.RED;

  /**
   * Construct a new GoToPoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   */
  public GoToPoint(Element element, Cutters cutMgr, Outline outline) {
    super(element, cutMgr, outline);
    snap = false;   // GoToPoints never snap
    makeDrawables();
  }

  /**
   * Construct a new GoToPoint at the given position with information from the
   * given GoToPoint. This is primarily used when duplicating a GoToPoint.
   *
   * @param pos new position
   * @param cpt GoToPoint to copy from
   */
  public GoToPoint(Point2D.Double pos, GoToPoint cpt) {
    super(pos, cpt);
    snap = false;   // GoToPoints never snap
    makeDrawables();
  }

  /**
   * Construct a new GoToPoint at the given position with default values. This
   * is primarily used when adding a first GoToPoint from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline Outline
   */
  public GoToPoint(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
    snap = false;   // GoToPoints never snap
    makeDrawables();
  }

  @Override
  public String toString() {
    return num + ": " + F3.format(pt.getX()) + " " + F3.format(pt.getZ()); // no depth
  }

  @Override
  public void setSnap(boolean s) {
    super.setSnap(false);   // don't let a GoToPoint get snapped
  }

  /**
   * Update the CutPoint primitives to the desired color.
   */
  @Override
  protected final void makeDrawables() {
    super.makeDrawables();
    pt.setColor(GOTO_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(GOTO_COLOR);	  // the text
      drawList.remove(1);     // for now, don't draw cutter
//      drawList.get(1).setColor(GOTO_COLOR);	  // the cutter
    }
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<GoToPoint"
        + " n='" + num + "'" // no depth or cutter
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    indentLess();
    out.println(indent + "</GoToPoint>");
  }

  @Override
  public void cutSurface(Surface surface) {
    // do nothing for a GoToPoint
  }

  @Override
  protected void make3DLines() {
    // do nothing for a GoToPoint
  }

  @Override
  public void makeInstructions(double passDepth, int passStep, double lastDepth, int lastStep, int stepsPerRot, CoarseFine.Rotation rotation) {
    cutList.comment("GoToPoint " + num);
    cutList.spindleWrapCheck();
    cutList.goToXZC(FAST, getX(), getZ(), 0.0);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("GoToPoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  @Override
  public boolean handleTransfer(JTextComponent targetComponent) {
    throw new UnsupportedOperationException("GoToPoint.handleTransfer Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
