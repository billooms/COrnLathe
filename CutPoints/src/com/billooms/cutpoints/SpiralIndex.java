package com.billooms.cutpoints;

import com.billooms.controls.CoarseFine;
import static com.billooms.cutlist.Speed.*;
import static com.billooms.cutpoints.IndexPoint.Direction.*;
import static com.billooms.cutpoints.IndexPoint.INDEX_COLOR;
import static com.billooms.cutpoints.IndexPoint.INDEX_COLOR2;
import static com.billooms.cutpoints.IndexPoint.INDEX_SAFETY;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.RotMatrix;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.simple.Curve;
import com.billooms.drawables.simple.Line;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import javax.swing.ProgressMonitor;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Cutting a spiral at regular intervals around the shape. The beginning point
 * is an IndexPoint. Each cut of the repeat starts at the beginning with some
 * rotation, follows the spiral line, then follows any optional GoToPoints back
 * to the beginning. Note: the exception is that on the last cut, the optional
 * GoToPoints are ignored so the cutter remains above the last XZ point.
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
    itemid = "SpiralIndex",
    icon16 = "com/billooms/cutpoints/icons/SpiralIdx16.png",
    icon32 = "com/billooms/cutpoints/icons/SpiralIdx32.png",
    name = "SpiralIndex",
    body = "SpiralIndex body",
    tooltip = "Make spirals at regular intervals as with an index wheel.")
public class SpiralIndex extends SpiralCut {

  /**
   * Construct a new SpiralIndex from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline Outline
   */
  public SpiralIndex(Element element, Cutters cutMgr, Outline outline) {
    super(element, cutMgr, outline);
    NodeList ipNodes = element.getElementsByTagName("IndexPoint");
    beginPt = new IndexPoint((Element) ipNodes.item(0), cutMgr, outline);
    beginPt.setCutter(this.cutter);   // make sure beginPt uses same cutter
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  /**
   * Construct a new SpiralIndex copying information from the given SpiralIndex.
   *
   * @param pos new position
   * @param cpt SpiralIndex to copy from
   */
  public SpiralIndex(Point2D.Double pos, SpiralIndex cpt) {
    super(pos, cpt);
    beginPt = new IndexPoint(cpt.getBeginPoint().getPos2D(), (IndexPoint) cpt.getBeginPoint());
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }

  /**
   * Construct a new SpiralIndex at the given position with default values. This
   * is primarily used when adding a first SpiralIndex from the OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   */
  public SpiralIndex(Point2D.Double pos, Cutter cut, Outline outline) {
    super(pos, cut, outline);
    beginPt = new IndexPoint(pos, cut, outline);
    beginPt.addPropertyChangeListener(this);
    makeDrawables();
  }
  
  @Override
  public void clear() {
    super.clear();
    beginPt.removePropertyChangeListener(this);
  }

  /**
   * Calculate the end moveVector. It is the direction of movement that will
   * produce the pattern at the endpoint.
   *
   * @param scale scale factor (use 1.0 for normalized)
   * @return
   */
  private Vector2d getEndMoveVector(double scale) {
    Vector2d perpVectN = getPerpVector(1.0);
    Vector2d moveN;
    switch (((IndexPoint) beginPt).getDirection()) {
      case INDEX_X:
        if (cutter.getLocation().isFrontInOrBackOut()) {
          moveN = new Vector2d(1.0, 0.0);
        } else {
          moveN = new Vector2d(-1.0, 0.0);
        }
        break;
      case INDEX_Z:
        if (cutter.getLocation().isInside()) {
          moveN = new Vector2d(0.0, -1.0);
        } else {			// direction depends on cutting top versus cutting bottom
          if (perpVectN != null) {	// not sure why we're checking for null
            moveN = new Vector2d(0.0, Math.copySign(1.0, perpVectN.y));
          } else {
            moveN = new Vector2d(0.0, 1.0);
          }
        }
        break;
      case INDEX_CURVE:
      default:
        moveN = perpVectN;
        break;
    }
    moveN.scale(scale);
    return moveN;
  }

  /**
   * Build the specific drawable shapes for an SpiralIndex
   */
  @Override
  protected final void makeDrawables() {
    super.makeDrawables();
    pt.setColor(INDEX_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(INDEX_COLOR);	// the text
      drawList.get(1).setColor(INDEX_COLOR);	// the cutter
    }
    
    if (endCutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // Line indicating direction of moveVectorS
    Vector2d endMoveVectorS = getEndMoveVector(endCutDepth);
    drawList.add(new Line(getPos2D(), endMoveVectorS.x, endMoveVectorS.y, INDEX_COLOR2));

    // cut extent
    Vector2d perpVectorN = getPerpVector(1.0);
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(perpVectorN.y, perpVectorN.x) * 180.0 / Math.PI;
        drawList.add(new Arc(new Point2D.Double(getX() + endMoveVectorS.x, getZ() + endMoveVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, INDEX_COLOR2));
        break;
      // Profile of drill at cut depth
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endMoveVectorS.x, getZ() + endMoveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), INDEX_COLOR2, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endMoveVectorS.x + v1.x, getZ() + endMoveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), INDEX_COLOR2, SOLID_LINE));
        v1 = v1.rotate(180.0);
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + endMoveVectorS.x + v1.x, getZ() + endMoveVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), INDEX_COLOR2, SOLID_LINE));
        break;
    }
  }
  
  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<SpiralIndex"
        + xmlCutPointInfo2()     // don't write depth
        + " endDepth='" + F4.format(endCutDepth) + "'"
        + ">");
    indentMore();
    super.writeXML(out);    // for beginPt, Pt, Spiral
    indentLess();
    out.println(indent + "</SpiralIndex>");
  }
  
  @Override
  protected void make3DLines() {
    list3D.clear();
    String mask = ((IndexPoint) beginPt).getMask();
    int repeat = ((IndexPoint) beginPt).getRepeat();
    double phase = ((IndexPoint) beginPt).getPhase();
    
    String fullMask = mask;
    if (!fullMask.isEmpty()) {
      while (fullMask.length() < repeat) {
        fullMask += mask;	// fill out to full length
      }
    }
    double ang = phase / repeat;
    for (int i = 0; i < repeat; i++) {
      if (mask.isEmpty() || (fullMask.charAt(i) != '0')) {
        Line3D line = new Line3D(getPointsForLine());
        line.rotate(RotMatrix.Axis.Z, ang);
        list3D.add(line);
      }
      ang -= 360.0 / (double) repeat;
    }
  }
  
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("SpiralIndex.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }
  
  @Override
  public synchronized void cutSurface(Surface surface, ProgressMonitor monitor) {
    Point3D[] rzc = getCutterTwist();
    ArrayList<Point3D> xyz = toXYZ(rzc);
    
    double totLength = getTotalDistance(xyz);	// actual length on the spiral
    if (totLength <= 0.0) {         // so we don't divide by zero further down
      beginPt.cutSurface(surface, monitor);	// no movement, so just cut this one place
      return;
    }
    
    IndexPoint modPt = new IndexPoint(beginPt.getPos2D(), (IndexPoint) beginPt);	// this is modified along the length of the spiral
    double startDepth = ((IndexPoint) beginPt).getDepth();
    double deltaDepth = endCutDepth - startDepth;
    double beginPhase = ((IndexPoint) beginPt).getPhase();
    double repeat = ((IndexPoint) beginPt).getRepeat();
    double cumLength = 0.0;
    for (int i = 0; i < rzc.length; i++) {  // xyz is the same length as tw
      if (i > 0) {
        cumLength += xyz.get(i).distance(xyz.get(i - 1));
      }
      modPt.setDepth(startDepth + cumLength / totLength * deltaDepth);
      modPt.setPhase(beginPhase + rzc[i].getZ() * repeat);
      modPt.move(rzc[i].getX(), rzc[i].getY());
      monitor.setProgress(num+1);
      monitor.setNote("CutPoint " + getNum() + ": " + i + "/" + rzc.length + "\n");
      modPt.cutSurface(surface, monitor);
      if (monitor.isCanceled()) {
        break;
      }
    }
  }

  /**
   * Make instructions for this CutPoint
   *
   * @param controls control panel data
   * @param stepsPerRot steps per rotation
   */
  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot) {
    cutList.comment("SpiralIndex " + num);
    cutList.comment("Cutter: " + cutter);
    
    Point3D[] rzc = getCutterTwist();			// twist indegrees for each point
    ArrayList<Point3D> xyz = toXYZ(rzc);
    
    double totLength = getTotalDistance(xyz);	// actual length on the spiral
    if (totLength <= 0.0) {		// so we don't divide by zero further down
      beginPt.makeInstructions(controls, stepsPerRot);	// no movement, so just cut this one place
      return;
    }

    // Make an array of cut depth/direction at each point 
    Vector2d[] cuts = new Vector2d[rzc.length];    
    double startDepth = ((IndexPoint) beginPt).getDepth();
    double deltaDepth = endCutDepth - startDepth;
    // use finer resolution for smoothness
    Curve fineCut = outline.getCutterPathCurve(cutter);
    fineCut.reSample(outline.getResolution() / 10.0);
    double cumLength = 0.0;
    for (int i = 0; i < rzc.length; i++) {
      if (i > 0) {
        cumLength += xyz.get(i).distance(xyz.get(i - 1));
      }
      double depth = startDepth + cumLength / totLength * deltaDepth;
      Vector2d perp = fineCut.perpendicular(new Point2D.Double(rzc[i].getX(), rzc[i].getY()), cutter.getLocation().isFrontInOrBackOut());
      Vector2d v;
      switch (((IndexPoint) beginPt).getDirection()) {
        case INDEX_X:		// all in the X direction
          if (cutter.getLocation().isFrontInOrBackOut()) {
            v = new Vector2d(depth, 0.0);
          } else {
            v = new Vector2d(-depth, 0.0);
          }
          break;
        case INDEX_Z:		// all in z-direction
          if (perp.y < 0.0) {
            v = new Vector2d(0.0, -depth);	// get sign from perp
          } else {
            v = new Vector2d(0.0, depth);
          }
          break;
        case INDEX_CURVE:	// each is perpendicular
        default:
          v = perp;
          v.scale(depth);
          break;
      }
      cuts[i] = v;
    }
    
    Vector2d safety = ((IndexPoint) beginPt).getMoveVector(INDEX_SAFETY);
    Vector2d endSafety = getEndMoveVector(INDEX_SAFETY);
    double beginPhase = ((IndexPoint) beginPt).getPhase();
    double repeat = ((IndexPoint) beginPt).getRepeat();
    
    cutList.spindleWrapCheck();
    double lastC = 0.0;
    for (int i = 0; i < repeat; i++) {
      double cAngle = 360.0 * (double) i / repeat - beginPhase / repeat;	// minus to match rosette phase
      cutList.goToXZC(FAST, beginPt.getX() - safety.x, beginPt.getZ() - safety.y, cAngle - rzc[0].getZ());	// go there first without cutting
      lastC = cAngle - rzc[0].getZ();
      for (int j = 0; j < rzc.length; j++) {
        cutList.goToXZC(VELOCITY, rzc[j].getX() + cuts[j].x, rzc[j].getY() + cuts[j].y, cAngle - rzc[j].getZ());
        lastC = cAngle - rzc[j].getZ();
      }
      cutList.goToXZ(VELOCITY, getX() - endSafety.x, getZ() - endSafety.y);			// pull out before moving
      if (i < repeat - 1) {
        for (GoToPoint gPt : goList) {
          cutList.goToXZ(FAST, gPt.getX(), gPt.getZ());
        }
      }
    }
    // always go to 360.0 at the end of the SpiralIndexPoint
    if (lastC != 360.0) {
      cutList.turn(360.0);
    }
    cutList.spindleWrapCheck();
    // Note: we don't go back to the beginning when all cuts are done
  }
  
}
