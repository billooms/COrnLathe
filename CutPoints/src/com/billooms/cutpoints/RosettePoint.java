package com.billooms.cutpoints;

import com.billooms.clclass.CLUtilities;
import com.billooms.controls.CoarseFine;
import static com.billooms.controls.CoarseFine.Rotation.*;
import static com.billooms.cutlist.Speed.*;
import static com.billooms.cutpoints.CutPoints.NUM_3D_PTS;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutpoints.surface.Surface;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import static com.billooms.drawables.Drawable.SOLID_LINE;
import com.billooms.drawables.Pt;
import com.billooms.drawables.simple.Arc;
import com.billooms.drawables.simple.Line;
import com.billooms.drawables.vecmath.Vector2d;
import com.billooms.outline.Outline;
import com.billooms.patterns.CustomPattern;
import com.billooms.patterns.CustomPattern.CustomStyle;
import com.billooms.patterns.Patterns;
import com.billooms.rosette.BasicRosette;
import com.billooms.rosette.CompoundRosette;
import com.billooms.rosette.Rosette;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javafx.geometry.Point3D;
import javax.swing.ProgressMonitor;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.openide.text.ActiveEditorDrop;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Cut with a rosette.
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
    itemid = "RosettePoint",
    icon16 = "com/billooms/cutpoints/icons/Rosette16.png",
    icon32 = "com/billooms/cutpoints/icons/Rosette32.png",
    name = "RosettePoint",
    body = "RosettePoint body",
    tooltip = "Regular cuts with a rosette.")
public class RosettePoint extends CutPoint implements ActiveEditorDrop {

  /** Property name used when changing an RosettePoint's motion */
  String PROP_MOTION = PROP_PREFIX + "Motion";
  /** Property name used when changing a rosette */
  String PROP_ROSETTE = PROP_PREFIX + "Rosette";

  /** Default motion for index cuts. */
  public final static Motion DEFAULT_MOTION = Motion.ROCK;
  /** Color used for the maximum movement of the cut. */
  protected final static Color ROSETTE_COLOR = Color.YELLOW;
  /** Color used for the maximum movement of the cut with rosette. */
  protected final static Color ROSETTE_COLOR2 = Color.GREEN;
  /** Color used for the maximum movement of the cut with rosette2. */
  protected final static Color ROSETTE_COLOR3 = Color.BLUE;
  /** Margin to turn a solid circle if not yet at a depth where pToP makes a
   * difference. */
  private final static double CUT_MARGIN = 0.0049;

  /** Directions for rosette motion. */
  public enum Motion {

    /** Rocking motion. */
    ROCK,
    /** Pumping motion. */
    PUMP,
    /** Motion perpendicular to the surface. */
    PERP,
    /** Motion tangental to the surface. */
    TANGENT,
    /** Both rocking and pumping motion. */
    BOTH,
    /** Both perpendicular and tangental motion */
    PERPTAN;
    
    public boolean usesBoth() {
      switch (Motion.this) {
        default:
        case ROCK:
        case PUMP:
        case PERP:
        case TANGENT:
          return false;
        case BOTH:
        case PERPTAN:
          return true;
      }
    }
  }

  /** Motion of the cuts. */
  protected Motion motion = DEFAULT_MOTION;
  /** Primary rosette. */
  protected BasicRosette rosette = null;
  /** Second rosette used only for BOTH and PERPTAN. */
  protected BasicRosette rosette2 = null;

  /**
   * Construct a new RosettePoint from the given DOM Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   * @param patMgr pattern manager
   */
  public RosettePoint(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    super(element, cutMgr, outline);
    this.motion = CLUtilities.getEnum(element, "motion", Motion.class, DEFAULT_MOTION);
    NodeList rosNodes = element.getChildNodes();
    for (int i = 0; i < rosNodes.getLength(); i++) {
      if (rosNodes.item(i) instanceof Element) {
        Element rosElement = (Element) rosNodes.item(i);
        if (rosElement.getTagName().equals("Rosette")) {
          if (rosette == null) {            // first one is rosette
            rosette = new Rosette(rosElement, patMgr);
          } else if (rosette2 == null) {    // if there's a second one it's rosette2
            rosette2 = new Rosette(rosElement, patMgr);
          }
        }
        if (rosElement.getTagName().equals("CompoundRosette")) {
          if (rosette == null) {            // first one is rosette
            rosette = new CompoundRosette(rosElement, patMgr);
          } else if (rosette2 == null) {   // if there's a second one it's rosette2
            rosette2 = new CompoundRosette(rosElement, patMgr);
          }
        }
      }
    }
    makeDrawables();
    rosette.addPropertyChangeListener(this);
    if (motion.usesBoth()) {
      rosette2.addPropertyChangeListener(this);
    }
  }

  /**
   * Construct a new RosettePoint at the given position with information from
   * the given RosettePoint. This is primarily used when duplicating an
   * RosettePoint.
   *
   * @param pos new position
   * @param cpt RosettePoint to copy from
   */
  public RosettePoint(Point2D.Double pos, RosettePoint cpt) {
    super(pos, cpt);
    this.motion = cpt.getMotion();
    this.rosette = (cpt.getRosette() instanceof Rosette) ? new Rosette((Rosette) cpt.getRosette()) : new CompoundRosette((CompoundRosette) cpt.getRosette()); 
    if (motion.usesBoth()) {
      this.rosette2 = (cpt.getRosette2() instanceof Rosette) ? new Rosette((Rosette) cpt.getRosette2()) : new CompoundRosette((CompoundRosette) cpt.getRosette2()); 
    }
    makeDrawables();
    rosette.addPropertyChangeListener(this);
    if (motion.usesBoth()) {
      rosette2.addPropertyChangeListener(this);
    }
  }

  /**
   * Construct a new RosettePoint at the given position with default values.
   * This is primarily used when adding a first RosettePoint from the
   * OutlineEditor.
   *
   * @param pos new position
   * @param cut cutter
   * @param outline outline
   * @param patMgr pattern manager
   */
  public RosettePoint(Point2D.Double pos, Cutter cut, Outline outline, Patterns patMgr) {
    super(pos, cut, outline);
    motion = DEFAULT_MOTION;
    rosette = new Rosette(patMgr);
    makeDrawables();
    rosette.addPropertyChangeListener(this);
  }

  @Override
  public String toString() {
    return super.toString() + " " + motion.toString();
  }

  @Override
  public synchronized void clear() {
    super.clear();
    rosette.removePropertyChangeListener(this);
    rosette.clear();
    if (motion.usesBoth()) {
      rosette2.removePropertyChangeListener(this);
      rosette2.clear();
    }
  }

  /**
   * Get the motion of the cut.
   *
   * @return motion of the cut
   */
  public Motion getMotion() {
    return motion;
  }

  /**
   * Set the motion of the cut. This fires a PROP_MOTION property change with
   * the old and new values.
   *
   * @param newDir motion of the cut
   */
  public void setMotion(Motion newDir) {
    if (motion.equals(newDir)) {
      return;
    }
    if (motion.usesBoth()) {
      rosette2.removePropertyChangeListener(this);
      rosette2.clear();
    }
    Motion old = this.motion;
    this.motion = newDir;
    if (!old.usesBoth() && motion.usesBoth()) {
      rosette2 = (rosette instanceof Rosette) ? new Rosette((Rosette) rosette) : new CompoundRosette((CompoundRosette) rosette); 
      rosette2.addPropertyChangeListener(this);
    }
    makeDrawables();
    pcs.firePropertyChange(PROP_MOTION, old, motion);
  }

  /**
   * Get the primary rosette.
   *
   * @return primary rosette
   */
  public BasicRosette getRosette() {
    return rosette;
  }

  /**
   * Get the secondary rosette.
   *
   * @return secondary rosette
   */
  public BasicRosette getRosette2() {
    return rosette2;
  }
  
  /**
   * Set the rosette to a new rosette.
   * This fires a PROP_ROSETE with old and new values
   * 
   * @param newRosette new rosette
   */
  public void setRosette(BasicRosette newRosette) {
    if (rosette != null) {
      rosette.removePropertyChangeListener(this);
    }
    BasicRosette old = this.rosette;
    this.rosette = newRosette;
    if (rosette != null) {
      rosette.addPropertyChangeListener(this);
    }
    pcs.firePropertyChange(PROP_ROSETTE, old, newRosette);
  }
  
  /**
   * Set the rosette2 to a new rosette.
   * This fires a PROP_ROSETE with old and new values
   * 
   * @param newRosette new rosette
   */
  public void setRosette2(BasicRosette newRosette) {
    if (rosette2 != null) {
      rosette.removePropertyChangeListener(this);
    }
    BasicRosette old = this.rosette2;
    this.rosette2 = newRosette;
    if (rosette2 != null) {
      rosette.addPropertyChangeListener(this);
    }
    pcs.firePropertyChange(PROP_ROSETTE, old, newRosette);
  }
  
  /** 
   * Get the move vector for PERP. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut.
   * 
   * @return normalized perpendicular vector
   */
  private Vector2d perpVectorN() {
    Vector2d perpVectN = getPerpVector(1.0);
    return new Vector2d(-perpVectN.x, -perpVectN.y);	// Always opposite direction from perpVector
  }
  
  /** 
   * Get the move vector for TANGENT. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut.
   * 
   * @return normalized tangent vector
   */
  private Vector2d tanVectorN() {
    Vector2d perpVectN = getPerpVector(1.0);
    switch (cutter.getLocation()) {
      case FRONT_INSIDE:
      default:
        return new Vector2d(perpVectN.y, -perpVectN.x);	// back toward center and downward
      case BACK_INSIDE:
        return new Vector2d(-perpVectN.y, perpVectN.x);	// forward toward center and downward
      case FRONT_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          return new Vector2d(-perpVectN.y, perpVectN.x);	// move out to front and downward
        } else {						// bottom of a shape
          return new Vector2d(perpVectN.y, -perpVectN.x);	// move out to front and upward
        }
      case BACK_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          return new Vector2d(perpVectN.y, -perpVectN.x);	// move out to back and downward
        } else {						// bottom of a shape
          return new Vector2d(-perpVectN.y, perpVectN.x);	// move out to back and upward
        }
    }
  }
  
  /**
   * Correct the sign of the motion for the location of the cutter. 
   * 
   * @param xMove x-movement
   * @param zMove z-movement
   * 
   * @return corrected vector
   */
  protected Vector2d correctForCutter(double xMove, double zMove) {
    switch (cutter.getLocation()) {		// correct the sign of the motion for location of cutter
      case FRONT_INSIDE:
      default:
        return new Vector2d(-xMove, zMove);	// move back to center and/or upward
      case BACK_INSIDE:
        return new Vector2d(xMove, zMove);		// move forward to center and/or upward
      case FRONT_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          return new Vector2d(xMove, zMove);		// move out to front and/or up
        } else {						// bottom of a shape
          return new Vector2d(xMove, -zMove);	// move out to front and/or down
        }
      case BACK_OUTSIDE:
        if (isTopOutside()) {			// top of a shape
          return new Vector2d(-xMove, zMove);	// move out to back and/or up
        } else {						// bottom of a shape
          return new Vector2d(-xMove, -zMove);	// move out to back and/or down
        }
    }
  }

  /**
   * Get the scaled movement vector for the rosette. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut. 
   * In the case of BOTH or PERPTAN, it is the movement of only the first rosette.
   *
   * @return scaled movement vector
   */
  protected Vector2d getMoveVectorS() {
    double xMove = 0.0, zMove = 0.0;
    switch (motion) {
      default:
      case PERP:
      case PERPTAN:
        Vector2d perpS = perpVectorN();
        perpS.scale(rosette.getPToP());
        return perpS;
      case TANGENT:
        Vector2d tanS = tanVectorN();
        tanS.scale(rosette.getPToP());
        return tanS;

      case BOTH:
        xMove = rosette.getPToP();      // only get the motion from the primary rosette
        break;
      case PUMP:
        zMove = rosette.getPToP();
        break;
      case ROCK:
        xMove = rosette.getPToP();
        break;
    }
    return correctForCutter(xMove, zMove);
  }

  /**
   * Get the scaled movement vector for rosette2. 
   * It is the direction of movement that will produce the rosette pattern. 
   * It will be in a direction AWAY from the point of deepest cut. 
   * In the case of BOTH or PERPTAN, it is the movement of only the second rosette.
   *
   * @return scaled movement vector
   */
  protected Vector2d getMoveVector2S() {
    double xMove = 0.0, zMove = 0.0;
    switch (motion) {
      default:      // should not call this except for BOTH and PERPTAN
      case PUMP:
      case ROCK:
      case PERP:
      case TANGENT:
        return new Vector2d();
      case PERPTAN:
        Vector2d tan = tanVectorN();
        tan.scale(rosette2.getPToP());
        return tan;

      case BOTH:
        zMove = rosette2.getPToP();     // get the z movement from rosette2 only when there is both
        break;
    }
    return correctForCutter(xMove, zMove);
  }

  /**
   * Build the specific drawable shapes for an RosettePoint
   */
  @Override
//  protected final synchronized void makeDrawables() {
  protected synchronized void makeDrawables() {
    super.makeDrawables();    // text and cutter at rest
    pt.setColor(ROSETTE_COLOR);
    if (drawList.size() >= 2) {
      drawList.get(0).setColor(ROSETTE_COLOR);	// the text
      drawList.get(1).setColor(ROSETTE_COLOR);	// the cutter
    }

    if (cutDepth == 0.0) {
      return;   // don't draw anything more with no amplitude
    }

    // Line indicating direction of the cut into the shape (which will always be perpendicular) 
    Vector2d perpVectorS = getPerpVector(cutDepth);	// maximum cut displacement
    drawList.add(new Line(getPos2D(), perpVectorS.x, perpVectorS.y, ROSETTE_COLOR));

    if (this instanceof OffRosettePoint) {
      return;     // don't draw anymore for OffRosettePoint
    }
    
    // cut extent
    Vector2d moveVectorS = getMoveVectorS();		// scaled movement vector for rosette
    Vector2d moveVector2S = getMoveVector2S();		// scaled movement vector for rosette2
    switch (cutter.getFrame()) {
      // Arc showing cut depth (for HCF & UCF)
      case HCF:
      case UCF:
        double angle = Math.atan2(perpVectorS.y, perpVectorS.x) * 180.0 / Math.PI;
        drawList.add(new Arc(new Point2D.Double(getX() + perpVectorS.x + moveVectorS.x, getZ() + perpVectorS.y + moveVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR2));
        if (motion.usesBoth()) {
          drawList.add(new Arc(new Point2D.Double(getX() + perpVectorS.x + moveVector2S.x, getZ() + perpVectorS.y + moveVector2S.y),
              cutter.getRadius(),
              cutter.getUCFRotate(), cutter.getUCFAngle(),
              angle, ARC_ANGLE, ROSETTE_COLOR3));
        }
        drawList.add(new Arc(new Point2D.Double(getX() + perpVectorS.x, getZ() + perpVectorS.y),
            cutter.getRadius(),
            cutter.getUCFRotate(), cutter.getUCFAngle(),
            angle, ARC_ANGLE, ROSETTE_COLOR));
        break;
      // Profile of drill & Fixed at cut depth
      case Fixed:
      case Drill:
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + moveVectorS.x, getZ() + perpVectorS.y + moveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + moveVector2S.x, getZ() + perpVectorS.y + moveVector2S.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x, getZ() + perpVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
      // For ECF add two profiles at +/- radius
      case ECF:
        Vector2d v1 = new Vector2d(cutter.getRadius(), 0.0);
        v1 = v1.rotate(-cutter.getUCFAngle());   // minus because + is toward front
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVectorS.x, getZ() + perpVectorS.y + v1.y + moveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVector2S.x, getZ() + perpVectorS.y + v1.y + moveVector2S.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + v1.x, getZ() + perpVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        v1 = v1.rotate(180.0);
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVectorS.x, getZ() + perpVectorS.y + v1.y + moveVectorS.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR2, SOLID_LINE));
        if (motion.usesBoth()) {
          drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + v1.x + moveVector2S.x, getZ() + perpVectorS.y + v1.y + moveVector2S.y),
              cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR3, SOLID_LINE));
        }
        drawList.add(cutter.getProfile().getDrawable(new Point2D.Double(getX() + perpVectorS.x + v1.x, getZ() + perpVectorS.y + v1.y),
            cutter.getTipWidth(), -cutter.getUCFAngle(), ROSETTE_COLOR, SOLID_LINE));
        break;
    }
  }

  @Override
  protected void make3DLines() {
    list3D.clear();
    Line3D line = new Line3D();

    double x0 = getX();		// point where cutter contacts the surface
    double y0 = getY();     // (y should always be zero)		
    double z0 = getZ();
    switch (cutter.getFrame()) {
      case HCF:
      case UCF:
        Vector2d cutPerpR = getPerpVector(cutter.getRadius());
        x0 = x0 + cutPerpR.x;	// offset for cutter radius
        z0 = z0 + cutPerpR.y;	// these are now lathe/view3d coordinates
        break;
      default:
        break;
    }

    for (int i = 0; i < (NUM_3D_PTS + 1); i++) {
      double angDeg = (double) i * 360.0 / (double) NUM_3D_PTS;	// in degrees
      double angRad = Math.toRadians(angDeg);
      Vector2d xz = rosetteMove(angDeg, x0, z0);   // xz location due to rosette motion
      double r = Math.hypot(xz.x, y0);		// cylindrical radius  NOTE: y should always be 0.0
      if (cutter.getLocation().isBack()) {	// if in back, add 180 degrees rotation
        angRad += Math.PI;
      }
      line.add(new Point3D(r * Math.cos(-angRad), r * Math.sin(-angRad), xz.y));
    }
    list3D.add(line);
  }

  @Override
  public void writeXML(PrintWriter out) {
    out.println(indent + "<RosettePoint"
        + xmlCutPointInfo()
        + " motion='" + motion.toString() + "'"
        + ">");
    indentMore();
    super.writeXML(out);    // for point
    rosette.writeXML(out);
    if (motion.usesBoth()) {
      rosette2.writeXML(out);
    }
    indentLess();
    out.println(indent + "</RosettePoint>");
  }

  /**
   * Cut the given surface with this CutPoint.
   *
   * @param surface Surface
   * @param monitor progress monitor which can be canceled
   */
  @Override
  public synchronized void cutSurface(Surface surface, ProgressMonitor monitor) {
    Vector2d cutVectorS = getPerpVector(cutDepth);
    double x0 = getX() + cutVectorS.x;			// cutter location with depth
    double z0 = getZ() + cutVectorS.y;

    int nSectors = surface.numSectors();		// number of sectors around shape
    double dAngle = 360.0 / (double) nSectors;	// angle increment degrees

    Vector2d cutXZ;						// Location of center of cutter
    double spindleC, lastC = 0.0;		// rotation in degrees
    int count;
    for (count = 0, spindleC = 0.0; count < nSectors; count++, spindleC += dAngle) {
      cutXZ = rosetteMove(spindleC, x0, z0);
      surface.rotateZ(spindleC - lastC);		// incremental rotate the surface
      if (cutter.canFastRender()) {
        surface.cutSurface(cutter, cutXZ.x, cutXZ.y, spindleC);
      } else {
        surface.cutSurface(cutter, cutXZ.x, cutXZ.y);
      }
      lastC = spindleC;
    }
    surface.rotateZ(360.0 - lastC);		// bring it back to the starting point
  }

  /**
   * Determine the offset from the given point caused by a rosette at a given
   * angle. Note that the x,y value would be interpreted as x,z in lathe
   * coordinate space.
   *
   * @param angDeg angle in degrees
   * @param x x-coordinate
   * @param z z-coordinate (lathe space)
   * @return offset from the given point
   */
  protected Vector2d rosetteMove(double angDeg, double x, double z) {
    Vector2d move = rosetteMove(angDeg);
    return new Vector2d(x + move.x, z + move.y);
  }

  /**
   * Determine the offset caused by a rosette at a given angle. Note that the
   * x,y value would be interpreted as x,z in lathe coordinate space.
   *
   * @param angDeg angle in degrees
   * @return x,z offset from zero.
   */
  protected Vector2d rosetteMove(double angDeg) {
    double xMove = 0.0, zMove = 0.0;
    switch (motion) {
      default:
      case PERP:
        Vector2d perpS = perpVectorN();
        perpS.scale(rosette.getAmplitudeAt(angDeg, cutter.getLocation().isOutside()));
        return perpS;
      case TANGENT:
        Vector2d tanS = tanVectorN();
        tanS.scale(rosette.getAmplitudeAt(angDeg, cutter.getLocation().isOutside()));
        return tanS;
      case PERPTAN:
        Vector2d perp = perpVectorN();
        perp.scale(rosette.getAmplitudeAt(angDeg, cutter.getLocation().isOutside()));
        Vector2d tan = tanVectorN();
        tan.scale(rosette2.getAmplitudeAt(angDeg, cutter.getLocation().isOutside()));
        return new Vector2d(perp.x + tan.x, perp.y + tan.y);
        
      case BOTH:
        // get the z movement from rosette2 only when there is both
        zMove = rosette2.getAmplitudeAt(angDeg, cutter.getLocation().isOutside());
        // otherwise, always get the motion from the primary rosette
        xMove = rosette.getAmplitudeAt(angDeg, cutter.getLocation().isOutside());
        break;
      case PUMP:
        zMove = rosette.getAmplitudeAt(angDeg, cutter.getLocation().isOutside());
        break;
      case ROCK:
        xMove = rosette.getAmplitudeAt(angDeg, cutter.getLocation().isOutside());
        break;
    }
    return correctForCutter(xMove, zMove);
  }

  /**
   * Make instructions for this CutPoint
   *
   * @param controls control panel data
   * @param stepsPerRot steps per rotation
   */
  @Override
  public void makeInstructions(CoarseFine controls, int stepsPerRot) {
    cutList.comment("RosettePoint " + num);
    cutList.comment("Cutter: " + cutter);
    cutList.spindleWrapCheck();

    // move to position before applying depth
    Vector2d start = rosetteMove(0.0, getX(), getZ());
    cutList.goToXZC(FAST, start.x, start.y, 0.0);

    // Increasing cut depth with the coarse depth per cut
    double depth = 0.0;
    while (depth < (cutDepth - controls.getLastDepth())) {
      depth = Math.min(cutDepth - controls.getLastDepth(), depth + controls.getPassDepth());
      boolean dir = (controls.getRotation() == NEG_LAST) ? NOT_LAST : ((controls.getRotation() == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followRosette(depth, controls.getPassStep(), dir, stepsPerRot);
    }
    if (controls.getLastDepth() > 0.0) {
      boolean dir = (controls.getRotation() == NEG_LAST) ? LAST : ((controls.getRotation() == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followRosette(cutDepth, controls.getLastStep(), dir, stepsPerRot);
    }
    if (controls.isSoftLift()) {
      boolean dir = (controls.getRotation() == NEG_LAST) ? LAST : ((controls.getRotation() == PLUS_ALWAYS) ? ROTATE_POS : ROTATE_NEG);
      followRosetteLift(cutDepth, controls, dir, stepsPerRot);
    }

    // back to the starting position
    cutList.spindleWrapCheck();
    cutList.goToXZC(FAST, start.x, start.y, 0.0);
  }

  /**
   * Add instructions to the cutList for following rosettes.
   *
   * @param depth the depth of this cut
   * @param controls control panel data
   * @param negRotate true=rotate negative; false=rotate positive
   * @param stepsPerRot steps per rotation
   */
  private void followRosetteLift(double depth, CoarseFine controls, boolean negRotate, int stepsPerRot) {
    int step = controls.getLastStep();
    double liftHeight = controls.getSoftLiftHeight();
    double liftDeg = controls.getSoftLiftDeg();
    int stepLift = (int)((double)stepsPerRot * (liftDeg / 360.0));
    
    Vector2d perpVectorN = getPerpVector(1.0);
    
    cutList.spindleWrapCheck();
    
    double c = 0.0, x0, z0;
    for (int i = 0; i <= stepLift; i = i + step) {
      c = 360.0 * (double) i / (double) stepsPerRot;
      double lift = liftHeight * c / liftDeg;
      x0 = getX() + (depth - lift) * perpVectorN.x;
      z0 = getZ() + (depth - lift) * perpVectorN.y;
      if (negRotate) {
        c = -c;     // negative rotation
      }
      if (i == 0) {
        cutList.goToXZC(VELOCITY, rosetteMove(c, x0, z0), c);	// first point at velocity
      } else {
        cutList.goToXZC(RPM, rosetteMove(c, x0, z0), c);	// and other points at RPM
      }
    }
    cutList.goToXZC(VELOCITY, rosetteMove(c, getX(), getZ()), c);	// first point at velocity
  }

  /**
   * Add instructions to the cutList for following rosettes.
   *
   * @param depth the depth of this cut
   * @param step the number of micro-steps per increment (for the spindle motor)
   * @param negRotate true=rotate negative; false=rotate positive
   * @param stepsPerRot steps per rotation
   */
  private void followRosette(double depth, int step, boolean negRotate, int stepsPerRot) {
    Vector2d perpVectorN = getPerpVector(1.0);
    double x0 = getX() + depth * perpVectorN.x;
    double z0 = getZ() + depth * perpVectorN.y;
    followRosette(perpVectorN, x0, z0, depth, step, negRotate, stepsPerRot);
  }

  /**
   * Add instructions to the cutList for following rosettes.
   *
   * @param perpVectorN normalized perpendicular vector
   * @param x0 location of cut x-coordinate
   * @param z0 location of cut z-coordinate
   * @param depth the depth of this cut
   * @param step the number of micro-steps per increment (for the spindle motor)
   * @param negRotate true=rotate negative; false=rotate positive
   * @param stepsPerRot steps per rotation
   */
  protected void followRosette(Vector2d perpVectorN, double x0, double z0, double depth, int step, boolean negRotate, int stepsPerRot) {
    cutList.spindleWrapCheck();

    // If custom pattern with STRAIGHT line segments, just use the breakpoints.
    // It runs faster!
    // Can't currently use this if BOTH rosettes are used.
    if (!motion.usesBoth() && (rosette instanceof Rosette) && (((Rosette)rosette).getPattern() instanceof CustomPattern)) {
      CustomPattern pat = (CustomPattern) ((Rosette)rosette).getPattern();
      if (pat.getCustomStyle() == CustomStyle.STRAIGHT) {
        int repeat = rosette.getRepeat();
        double phase = rosette.getPhase();
        ArrayList<Double> angles = new ArrayList<>();   // a list of the angles
        for (int i = 0; i < repeat; i++) {
          double cc = (double) i / (double) repeat * 360.0;
          for (Pt pt : pat.getAllPoints()) {
            double c = pt.getX() * 360.0 / (double) repeat + cc - phase / repeat;	// angle including phase
            if (c < 0.0) {				// make sure all angles are 0.0 <= c <= 360.0
              c = c + 360.0;
            } else if (c > 360.0) {
              c = c - 360.0;
            }
            angles.add(c);
          }
        }
        angles.add(0.0);			// make sure we include 0.0 and 360.0
        angles.add(360.0);
        Collections.sort(angles);	// sorted order 0.0 to 360.0
        for (int i = angles.size() - 1; i > 0; i--) {	// delete any duplicates
          if (aboutEqual(angles.get(i), angles.get(i - 1))) {
            angles.remove(i);
          }
        }
        for (int i = angles.size() - 2; i > 0; i--) {	// Don't have more than 2 in a row with same pattern value
          if (aboutEqual(rosette.getAmplitudeAt(angles.get(i)),
              rosette.getAmplitudeAt(angles.get(i - 1)),
              rosette.getAmplitudeAt(angles.get(i + 1)))) {
            angles.remove(i);		// remove the middle point
          }
        }
        boolean firstPt = true;
        if (negRotate) {			// negative rotation
          for (int i = angles.size() - 1; i >= 0; i--) {
            double a = angles.get(i) - 360.0;
            if (firstPt) {
              cutList.goToXZC(VELOCITY, rosetteMove(a, x0, z0), a);	// first point at velocity
              firstPt = false;
            } else {
              cutList.goToXZC(RPM, rosetteMove(a, x0, z0), a);	// go to this point at rpm
            }
          }
        } else {			// positive rotation
          for (Double a : angles) {
            if (firstPt) {
              cutList.goToXZC(VELOCITY, rosetteMove(a, x0, z0), a);	// first point at velocity
              firstPt = false;
            } else {
              cutList.goToXZC(RPM, rosetteMove(a, x0, z0), a);	// go to this point at rpm
            }
          }
        }
        return;
      }
    }

    // Cut a simple circle in certain cases
    if (cutACircle(depth)) {
      cutList.goToXZC(VELOCITY, x0, z0, 0.0);	// go to x,z at spindleC=0.0
      if (negRotate) {
        cutList.turn(-360.0);
      } else {
        cutList.turn(360.0);	// just turn a circle
      }
      cutList.spindleWrapCheck();
      return;
    }

    boolean inAir = false;					// flag for indicating the cutter is in the air
    Vector2d saveXZ = new Vector2d();		// first point going into the air
    double saveC = 0.0;						// (don't really need values)
    for (int i = 0; i <= stepsPerRot; i = i + step) {
      double c = 360.0 * (double) i / (double) stepsPerRot;
      if (negRotate) {
        c = -c;     // negative rotation
      }
      // For PUMP and strictly vertical cut
      if ((perpVectorN.x == 0.0) && (motion == Motion.PUMP)) {
        if (depth < (-perpVectorN.y * rosetteMove(c).y)) {		// this point is in the air
          saveXZ = rosetteMove(c, x0, z0);
          saveC = c;
//		  System.out.println("Air: " + saveX + " " + saveZ + " " + c0);
          if (!inAir) {		// this is the first point in the air
            cutList.goToXZC(FAST, rosetteMove(c, x0, z0), c);	// so must go there
          }
          inAir = true;
        } else {				// this point is NOT in the air
          if (inAir) {		// and the last point was in the air
            cutList.goToXZC(FAST, saveXZ, saveC);	// go to last point for proper re-entry
          }
          inAir = false;
          if (i == 0) {
            cutList.goToXZC(VELOCITY, rosetteMove(c, x0, z0), c);	// first point at velocity
          } else {
            cutList.goToXZC(RPM, rosetteMove(c, x0, z0), c);	// go to this point at rpm
          }
        }
        continue;
      }
      // for ROCK and strictly horizontal cut
      if ((perpVectorN.y == 0.0) && (motion == Motion.ROCK)) {
        if (depth < (-perpVectorN.x * rosetteMove(c).x)) {		// this point is in the air
          saveXZ = rosetteMove(c, x0, z0);
          saveC = c;
//		  System.out.println("Air: " + saveX + " " + saveZ + " " + c0);
          if (!inAir) {		// this is the first point in the air
            cutList.goToXZC(FAST, rosetteMove(c, x0, z0), c);	// so must go there
          }
          inAir = true;
        } else {				// this point is NOT in the air
          if (inAir) {		// and the last point was in the air
            cutList.goToXZC(FAST, saveXZ, saveC);	// go to last point for proper re-entry
          }
          inAir = false;
          if (i == 0) {
            cutList.goToXZC(VELOCITY, rosetteMove(c, x0, z0), c);	// first point at velocity
          } else {
            cutList.goToXZC(RPM, rosetteMove(c, x0, z0), c);	// go to this point at rpm
          }
        }
        continue;
      }
      // for PERP motion
      if ((motion == Motion.PERP)) {		// perpendicular cut, rocking rosette
        if (depth < Math.hypot(rosetteMove(c).x, rosetteMove(c).y)) {	// this point is in the air
          saveXZ = rosetteMove(c, x0, z0);
          saveC = c;
//		  System.out.println("Air: " + saveX + " " + saveZ + " " + c0);
          if (!inAir) {		// this is the first point in the air
            cutList.goToXZC(FAST, rosetteMove(c, x0, z0), c);	// so must go there
          }
          inAir = true;
        } else {				// this point is NOT in the air
          if (inAir) {		// and the last point was in the air
            cutList.goToXZC(FAST, saveXZ, saveC);	// go to last point for proper re-entry
          }
          inAir = false;
          if (i == 0) {
            cutList.goToXZC(VELOCITY, rosetteMove(c, x0, z0), c);	// first point at velocity
          } else {
            cutList.goToXZC(RPM, rosetteMove(c, x0, z0), c);	// go to this point at rpm
          }
        }
        continue;
      }
      // for all other situations
      if (i == 0) {
        cutList.goToXZC(VELOCITY, rosetteMove(c, x0, z0), c);	// first point at velocity
      } else {
        cutList.goToXZC(RPM, rosetteMove(c, x0, z0), c);	// and other points at RPM
      }
    }
    // After we've cut all the way around,
    // if we're still in the air, go to the last point anyway.
    if (inAir) {
      cutList.goToXZC(FAST, saveXZ, saveC);
    }

    // When step is not a submultiple of stepsPerRot, 
    // then we might not be back at +/- 360.0
    if ((stepsPerRot % step) != 0) {
      if (negRotate) {
        cutList.goToXZC(RPM, rosetteMove(-360, x0, z0), -360.0);
      } else {
        cutList.goToXZC(RPM, rosetteMove(360, x0, z0), 360.0);
      }
    }
  }

  /**
   * Determine the conditions to cut a simple circle instead of a pattern.
   *
   * @return true=cut a simple circle
   */
  private boolean cutACircle(double depth) {
    switch (motion) {
      case ROCK:
      case PERP:
      case TANGENT:
      case PUMP:
        if ((rosette instanceof Rosette) && (((Rosette)rosette).getPattern().getName().equals("NONE"))) {
          return true;
        }
        if (rosette.getPToP() == 0.0) {
          return true;
        }
        break;
      case BOTH:
      case PERPTAN:
        if (((Rosette)rosette).getPattern().getName().equals("NONE")
            && ((Rosette)rosette2).getPattern().getName().equals("NONE")) {
          return true;
        }
        if ((rosette.getPToP() == 0.0)
            && (rosette2.getPToP() == 0.0)) {
          return true;
        }
        break;
    }
    Vector2d perpVectorN = getPerpVector(1.0);
    if ((perpVectorN.x == 0.0) && (motion == Motion.PUMP) && // vertical cut, pumping only
        (depth <= (cutDepth - rosette.getPToP() - CUT_MARGIN))) { // and cut is above final pattern
      return true;
    }
    if ((perpVectorN.y == 0.0) && (motion == Motion.ROCK) && // horizontal cut, rocking only
        (depth <= (cutDepth - rosette.getPToP() - CUT_MARGIN))) { // and cut is above final pattern
      return true;
    }
    if ((motion == Motion.PERP) && // Perpendicular cut
        (depth <= (cutDepth - rosette.getPToP() - CUT_MARGIN))) { // and cut is above final pattern
      return true;
    }

    return false;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("RosettePoint.propertyChange " + evt.getSource().getClass().getSimpleName() + " "  + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // Listening to rosettes
    super.propertyChange(evt);    // will pass the info on up the line
    makeDrawables();    // but we still need to make drawables here
  }

  @Override
  public boolean handleTransfer(JTextComponent targetComponent) {
    throw new UnsupportedOperationException("RosettePoint.handleTransfer Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
