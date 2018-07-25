package com.billooms.cutpoints;

import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.controls.CoarseFine;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.RotMatrix;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import com.billooms.patterns.Patterns;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ProgressMonitor;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This represents a group of offset CutPoints for use with an elliptical or
 * dome chuck. An OffsetGroup extends OffsetCut and adds a list of several
 * CutPoints at the same offset.
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
    itemid = "OffsetGroup",
    icon16 = "com/billooms/cutpoints/icons/Offset16.png",
    icon32 = "com/billooms/cutpoints/icons/Offset32.png",
    name = "OffsetGroup",
    body = "OffsetGroup body",
    tooltip = "A group of offset CutPoints")
public class OffsetGroup extends OffsetCut {

  /** Property name used when adding a CutPoint. */
  String PROP_ADD = PROP_PREFIX + "Add";
  /** Property name used when removing a CutPoint. */
  String PROP_REMOVE = PROP_PREFIX + "Remove";

  // OffsetGroup state variables
  protected ArrayList<CutPoint> cpList = new ArrayList<>();	// offset CutPoints

  /**
   * Construct a new OffsetGroup from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   * @param patMgr Pattern manager
   */
  public OffsetGroup(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    super(element, cutMgr, outline);

    NodeList node = element.getChildNodes();
    for (int k = 0; k < node.getLength(); k++) {		// read data from xml file
      if (node.item(k) instanceof Element) {
        Element cutElement = (Element) node.item(k);
        if (cutElement.getTagName().equals("RosettePoint")) {
          OffRosettePoint offRPt = new OffRosettePoint(cutElement, cutMgr, outline, patMgr, this);
          cpList.add(offRPt);
          offRPt.addPropertyChangeListener(this);
        }
      }
    }
  }

  /**
   * Construct a new OffsetGroup with information from the given OffsetGroup.
   *
   * @param pos new position
   * @param cpt OffsetCut to copy from
   */
  public OffsetGroup(Point2D.Double pos, OffsetGroup cpt) {
    super(pos, cpt);
    Vector2d offset = new Vector2d(pos.x - cpt.getX(), pos.y - cpt.getZ());
    for (CutPoint cp : cpt.getAllCutPoints()) {
      if (cp instanceof OffRosettePoint) {
        OffRosettePoint oRPt = new OffRosettePoint(pos, (OffRosettePoint) cp, this);
        cpList.add(oRPt);
        oRPt.move(cp.getX() + offset.x, cp.getZ() + offset.y);
        oRPt.snapToCurve();
        oRPt.addPropertyChangeListener(this);
      }
    }
  }

  /**
   * Construct a new OffsetGroup at the given position with default values. This
   * is primarily used when adding a first OffsetGroup from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public OffsetGroup(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
  }

  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);
    cpList.stream().forEach((cPt) -> {
      cPt.paint(g2d);
    });
  }

  /**
   * Clear the CutPoint of any propertyChangeListeners.
   */
  @Override
  public void clear() {
    super.clear();
    for (CutPoint cpt : cpList) {
      cpt.clear();
      cpt.removePropertyChangeListener(this);
    }
    cpList.clear();
  }

  // Override anything that moves this and move all the other CutPoints too
  @Override
  public synchronized void setX(double x) {
    double offset = x - this.getX();
    for (CutPoint cpt : cpList) {		// move the other points with it
      cpt.setX(cpt.getX() + offset);
      cpt.snapToCurve();
    }
    super.setX(x);
  }

  @Override
  public synchronized void setZ(double z) {
    double offset = z - this.getZ();
    for (CutPoint cpt : cpList) {		// move the other points with it
      cpt.setZ(cpt.getZ() + offset);
      cpt.snapToCurve();
    }
    super.setZ(z);
  }

  @Override
  public void move(Point2D.Double p) {
    this.move(p.x, p.y);
  }

  @Override
  public void move(double x, double z) {
    Vector2d offset = new Vector2d(x - this.getX(), z - this.getZ());
    for (CutPoint cpt : cpList) {		// move the other points with it
      cpt.move(cpt.getX() + offset.x, cpt.getZ() + offset.y);
      cpt.snapToCurve();
    }
    super.move(x, z);
  }

  @Override
  public void drag(Point2D.Double p) {
    Vector2d offset = new Vector2d(p.x - this.getX(), p.y - this.getZ());
    for (CutPoint cpt : cpList) {		// move the other points with it
      cpt.move(cpt.getX() + offset.x, cpt.getZ() + offset.y);
      cpt.snapToCurve();
    }
    super.drag(p);
  }

  @Override
  public void snapToCurve() {
    cpList.stream().forEach((cpt) -> {
      cpt.snapToCurve();
    });
    super.snapToCurve();
  }

  @Override
  public void setNum(int n) {
    super.setNum(n);	// fires PROP_NUM property change
    cpList.stream().forEach((cPt) -> {
      cPt.setNum(n);	// each fires PROP_NUM property change
    });
    makeDrawables();
  }

  /**
   * Add a CutPoint. This fires a PROP_ADD property change with the new point.
   *
   * @param cPt CutPoint
   */
  public void addOffPoint(CutPoint cPt) {
    if (containsCutPoint(cPt)) {
      return;		// don't add it again
    }
    if (cPt instanceof OffPoint) {
      cPt.setNum(this.num);	// make it the same number
      cpList.add(cPt);
      cPt.addPropertyChangeListener(this);
      pcs.firePropertyChange(PROP_ADD, null, cPt);
    } else if (cPt instanceof RosettePoint) {
      OffRosettePoint oPt = new OffRosettePoint((RosettePoint) cPt, this);
      oPt.setNum(this.num);
      cpList.add(oPt);
      oPt.addPropertyChangeListener(this);
      pcs.firePropertyChange(PROP_ADD, null, oPt);
    }
  }

  /**
   * Remove the given CutPoint from the list. This fires a PROP_REMOVE property
   * change with the old CutPoint.
   *
   * @param cPt CutPoint
   */
  public void removeCutPoint(CutPoint cPt) {
    if (cpList.contains(cPt)) {
      cPt.clear();
      cpList.remove(cPt);
      cPt.removePropertyChangeListener(this);
      pcs.firePropertyChange(PROP_REMOVE, cPt, null);
    }
  }

  /**
   * Get the number of CutPoints.
   *
   * @return number of CutPoints
   */
  public int getNumCutPoints() {
    return cpList.size();
  }

  /**
   * Get a specific CutPoint.
   *
   * @param n number
   * @return CutPoint (or null if it does not exist)
   */
  public CutPoint getCutPoint(int n) {
    if ((n >= cpList.size()) || (n < 0)) {
      return null;
    }
    return cpList.get(n);
  }

  /**
   * Get all the CutPoints in an editable list.
   *
   * @return list of CutPoints, an empty list if no points
   */
  public List<CutPoint> getAllCutPoints() {
    return cpList;
  }

  /**
   * Check if the given CutPoint is in the list.
   *
   * @param gPt CutPoint
   * @return true=contains the given CutPoint
   */
  public boolean containsCutPoint(CutPoint gPt) {
    return cpList.contains(gPt);
  }

  @Override
  protected void makeDrawables() {
    super.makeDrawables();    // text and cutter at rest
    cpList.stream().forEach((cp) -> {
      cp.makeDrawables();
    });
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    for (CutPoint cp : cpList) {
      if (cp instanceof OffRosettePoint) {
        Line3D line = ((OffRosettePoint) cp).make3DLine();
        line.rotate(RotMatrix.Axis.Y, getTangentAngle());     // rotate around Y axis
        line.translate(getTangentPointX(), 0.0, getTangentPointY()); // and offset to the tangent point
        if (indexOffset != 0) {
          double angleDeg = -360.0 * (double) indexOffset / (double) indexWheelHoles();
          line.rotate(RotMatrix.Axis.Z, angleDeg);
        }
        for (int i = 0; i < repeat; i++) {
          list3D.add(new Line3D(line.getPoints()));
          line.rotate(RotMatrix.Axis.Z, 360.0 / repeat);
        }
      }
    }
  }

  @Override
  public synchronized void cutSurface(Surface surface, ProgressMonitor monitor) {
    if (cpList.isEmpty() || (repeat <= 0)) {
      return;
    }
    double zRotation = indexOffsetDegrees();
    if (zRotation != 0.0) {
      surface.rotateZ(zRotation);			// initial phase rotation
    }
    outerloop:
    for (int i = 0; i < repeat; i++) {
      if (i > 0) {
        surface.rotateZ(360.0 / repeat);	// rotate to the next one
        zRotation += 360.0 / repeat;		// keep track of cumulative rotation
      }
      surface.offset(-getX(), 0.0, -getZ());			// move the surface over the offset point
      surface.rotateY(-getTangentAngle());

      // this is where the main work is done
      monitor.setProgress(num+1);
      monitor.setNote("CutPoint " + getNum() + ": " + i + "/" + repeat + "\n");
      for (CutPoint cPt : cpList) {
        if (cPt instanceof OffRosettePoint) {
          Vector2d offsetPt = offsetForCutPoint(cPt);
          ((OffRosettePoint) cPt).cutSurface(surface, offsetPt.x, offsetPt.y);			// and cut with it
        }
        if (monitor.isCanceled()) {
          break outerloop;
        }
      }

      surface.rotateY(getTangentAngle());
      surface.offset(getX(), 0.0, getZ());	// move the surface back so that the z rotation will be around the center
    }
    if (zRotation < 360.0) {
      surface.rotateZ(360.0 - zRotation);		// this should get you back to original rotation
    }
  }

  /**
   * Get the offset for the given CutPoint when this OffsetGroup is rotated to
   * 0,0.
   *
   * @param cPt given CutPoint
   * @return offset new location relative to 0,0
   */
  private Vector2d offsetForCutPoint(CutPoint cPt) {
    double dx = cPt.getX() - this.getX();		// delta distance between cutpoint and offset center
    double dz = cPt.getZ() - this.getZ();
    double R = Math.hypot(dx, dz);
    double beta = Math.toDegrees(Math.atan2(dz, dx));	// angle from horizontal to the CutPoint (usually negative)
    double gamma = beta + getTangentAngle();			// angle between tangent line and R (usually negative)
    double xp = R * Math.cos(Math.toRadians(gamma));	// new coordinates when rotated through tanAngle
    double zp = R * Math.sin(Math.toRadians(gamma));
    return new Vector2d(xp, zp);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<OffsetGroup"
        + " n='" + num + "'" // no depth or cutter
        + " repeat='" + repeat + "'"
        + " indexOffset='" + F3.format(indexOffset) + "'"
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    cpList.stream().forEach((cp) -> {
      cp.writeXML(out);
    });
    indentLess();
    out.println(indent + "</OffsetGroup>");
  }
  
  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot) {
    super.makeInstructions(controls, stepsPerRot);	// writes comments only
    for (CutPoint cPt : cpList) {
      if (cPt instanceof OffRosettePoint) {
        Vector2d offsetPt = offsetForCutPoint(cPt);
        ((OffRosettePoint) cPt).makeInstructions(controls, stepsPerRot, offsetPt.x, offsetPt.y);
      }
    }
  }

}
