package com.billooms.cutpoints.surface;

import com.billooms.cutpoints.CutPoints;
import com.billooms.cutters.Cutter;
import com.billooms.outline.Outline;
import com.billooms.profiles.Profile;
import com.billooms.cutpoints.surface.RotMatrix.Axis;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javafx.geometry.Point3D;

/**
 * A surface is a two-dimensional array of 3D points (in lathe coordinates) and
 * are indexed by angle and position on the defining curve.
 *
 * First index is 0 to the (number of points on outline curve - 1), and the
 * second index is 0 to (DEFAULT_SECTORS - 1).
 *
 * The Surface will listen for non-drag outline changes and then rebuild from
 * the new outline points. You can listen to the Surface for a
 * propertyChangeEvent when the Surface has been rebuilt.
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
public class Surface implements PropertyChangeListener {

  /** All Surface property change names start with this prefix. */
  public final static String PROP_PREFIX = "Surface" + "_";
  /** Property name used when rebuilding after an outline change. */
  public final static String PROP_REBUILD = PROP_PREFIX + "Rebuild";

  /** For convenience. */
  private final static double TWOPI = 2.0 * Math.PI;
  /** The number of sectors around the shape. */
  public final static int DEFAULT_SECTORS = 360;

  /**
   * First index is 0 to the (number of points on outline curve - 1), and the
   * second index is 0 to (DEFAULT_SECTORS - 1).
   */
  public Point3D[][] pts = null;
  /** Local copy of the outline. */
  protected final Outline outline;
  /** Show inside (true) or outside (false). */
  private boolean inOut;
  /** Local copy of the CutPoint manager. */
  private final CutPoints cutPtMgr;
  /** Render CutPoint (true) or not (false). */
  private boolean render;

  /** The Surface can fire propertyChanges. */
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /**
   * Construct a new Surface from the given outline points.
   *
   * @param outline Outline
   * @param inOut true: inside curve; false: outside curve
   * @param cutPtMgr CutPoint manager
   */
  public Surface(Outline outline, boolean inOut, CutPoints cutPtMgr) {
    this.outline = outline;
    this.inOut = inOut;
    this.cutPtMgr = cutPtMgr;
    rebuild();
    outline.addPropertyChangeListener(this);
    cutPtMgr.addPropertyChangeListener(this);
  }

  /**
   * Clear -- mainly remove any PropertyChangeListeners.
   */
  public void clear() {
    outline.removePropertyChangeListener(this);
    cutPtMgr.removePropertyChangeListener(this);
  }

  /**
   * Get the number of angle sectors.
   *
   * @return number of angle sectors
   */
  public int numSectors() {
    return pts[0].length;
  }

  /**
   * Get the number of points per sector.
   *
   * @return number of points per sector
   */
  public int getLength() {
    return pts.length;
  }

  /**
   * Get the outline for this surface.
   *
   * @return outline
   */
  public Outline getOutline() {
    return outline;
  }

  /**
   * Set the surface to inside or outside curve.
   *
   * @param inOut true: inside curve; false: outside curve
   */
  public synchronized void setInOut(boolean inOut) {
    boolean old = this.inOut;
    this.inOut = inOut;
    if (inOut != old) {
      rebuild();    // this will also fire a propertyChangeEvent
    }
  }

  /**
   * Is the render flag set?
   *
   * @return true: render flag is set
   */
  public boolean isRender() {
    return render;
  }

  /**
   * Set the render flag: true means cut the surface with the CutPoints, false
   * means don't cut the surface.
   *
   * @param render true: cut surface, false: plain surface
   */
  public void setRender(boolean render) {
    this.render = render;
    rebuild();
  }

  /**
   * Is this surface inside or outside curve?
   *
   * @return true: inside curve; false: outside curve
   */
  public boolean isInside() {
    return inOut;
  }

  /**
   * Rebuild the Surface from the given outline points.
   */
  public final void rebuild() {
    pts = makeCleanSurface();
    if (render) {
      cutPtMgr.getAll().stream().forEach((cp) -> {
        if (cp.isVisible()) {
          cp.cutSurface(this);
        }
      });
    }
    pcs.firePropertyChange(PROP_REBUILD, null, outline);
  }

  /**
   * Make a clean new surface from the outline.
   *
   * @return array of Point3D[][]
   */
  public Point3D[][] makeCleanSurface() {
    Point2D.Double[] curvePts;
    if (inOut) {
      curvePts = outline.getInsideCurve().getPoints();
    } else {
      curvePts = outline.getOutsideCurve().getPoints();
    }
    Point3D[][] newPts = new Point3D[curvePts.length][DEFAULT_SECTORS];
    for (int i = 0; i < curvePts.length; i++) {
      Point2D.Double pt = curvePts[i];
      for (int j = 0; j < DEFAULT_SECTORS; j++) {
        double angleRad = -TWOPI * (double) j / DEFAULT_SECTORS;    // minus to match rotation of lathe
        // convert 2D outline point to a point in 3D lathe space
        // use abs(x) in case of -x so that surface always starts the right place
        newPts[i][j] = new Point3D(
            Math.abs(pt.x) * Math.cos(angleRad),
            Math.abs(pt.x) * Math.sin(angleRad),
            pt.y);
      }
    }
    return newPts;
  }

  /**
   * Add the given listener to this object.
   *
   * @param listener
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Remove the given listener to this object.
   *
   * @param listener
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("Surface.propertyChange: " + evt.getSource().getClass().getSimpleName()+ " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    // this listens to the Outline and to the CutPoint manager
    if (!evt.getPropertyName().contains("Drag")) {  // don't keep updating when dragging a point
      rebuild();      // rebuild will fire a propertyChangeEvent
    }
  }

  /**
   * Rotate all points around the z-axis by the given incremental angle.
   *
   * @param deg incremental angle in degrees
   */
  public void rotateZ(double deg) {
    if (deg == 0.0) {
      return;
    }
    RotMatrix rotZ = new RotMatrix(Axis.Z, deg);
    for (int a = 0; a < pts[0].length; a++) {
      for (Point3D[] pt : pts) {
        pt[a] = rotZ.apply(pt[a]);
      }
    }
  }

  /**
   * Rotate all points around the y-axis by the given incremental angle.
   *
   * @param deg incremental angle in degrees
   */
  public void rotateY(double deg) {
    if (deg == 0.0) {
      return;
    }
    RotMatrix rotY = new RotMatrix(Axis.Y, deg);
    for (int a = 0; a < pts[0].length; a++) {
      for (Point3D[] pt : pts) {
        pt[a] = rotY.apply(pt[a]);
      }
    }
  }

  /**
   * Offset all points by the given incremental amount.
   *
   * @param x incremental x-axis offset
   * @param y incremental y-axis offset
   * @param z incremental z-axis offset
   */
  public void offset(double x, double y, double z) {
    for (int a = 0; a < pts[0].length; a++) {
      for (Point3D[] pt : pts) {
        pt[a] = new Point3D(x + pt[a].getX(), y + pt[a].getY(), z + pt[a].getZ());
      }
    }
  }

  /* DON'T REFORMAT OR ALL THIS WILL GO AWAY!                                     */
  /*                                                                              */
  /* Lathe:              View3D:             Cutter:             Curve:           */
  /*       z                   z                   y                   y          */
  /*       |  y                |  y                |                   |          */
  /*       | /                 | /                 | /                 |          */
  /*       |/                  |/                  |/                  |          */
  /* ------+------ x     ------+------ x     ------+------ x     ------+------ x  */
  /*      /|                  /|                  /|                   |          */
  /*     / |                 / |                 / |                   |          */
  /*       |                   |                z  |                   |          */
  /**
   * Make a cut on the given surface with the cutter at the given x,z coordinate
   * and the surface rotated on the spindle.
   *
   * @param cutter Cutter
   * @param cutX cutter x-coordinate
   * @param cutZ cutter z-coordinate
   */
  public synchronized void cutSurface(Cutter cutter, double cutX, double cutZ) {
    int sectors = this.numSectors();			// number of sectors around shape
    double rodR = cutter.getTipWidth() / 2.0;
    double radius = cutter.getRadius();
    Profile profile = cutter.getProfile();

    RotMatrix angleMat = new RotMatrix(Axis.Z, -cutter.getUCFAngle());
    RotMatrix angleMatI = new RotMatrix(Axis.Z, cutter.getUCFAngle());
    RotMatrix rotateMat = new RotMatrix(Axis.Y, cutter.getUCFRotate());
    RotMatrix rotateMatI = new RotMatrix(Axis.Y, -cutter.getUCFRotate());

    switch (cutter.getFrame()) {
      case HCF:
        for (Point3D[] pt1 : pts) {
          for (int a = 0; a < sectors; a++) {
            final double prof = profile.profileAt(pt1[a].getY(), rodR);
            if (prof >= 0.0) {
              final double radiusAtYOffset = radius - prof;
              if (radiusAtYOffset >= 0.0) {
                final double dx = pt1[a].getX() - cutX;		// distance from surface point to center of cutter
                final double dz = pt1[a].getZ() - cutZ;
                final double h = Math.hypot(dx, dz);
                if (h < radiusAtYOffset) {
                  pt1[a] = new Point3D(cutX + dx * radiusAtYOffset / h, pt1[a].getY(), cutZ + dz * radiusAtYOffset / h);   // cutter y --> lathe z
                }
              }
            }
          }
        }
        break;
      case UCF:
        for (Point3D[] pt1 : pts) {
          for (int a = 0; a < sectors; a++) {
            final Point3D p = new Point3D(pt1[a].getX() - cutX, pt1[a].getZ() - cutZ, -pt1[a].getY());	// actual xyz relative to cutter
            final Point3D p1 = rotateMatI.apply(angleMatI.apply(p));			// convert actual xyz to cutter xyz
            final double prof = profile.profileAt(p1.getZ(), rodR);
            if (prof >= 0.0) {
              final double radiusAtYOffset = radius - prof;
              if (radiusAtYOffset >= 0.0) {
                final double h = Math.hypot(p1.getX(), p1.getY());
                if (h < radiusAtYOffset) {
                  final Point3D p2 = new Point3D(p1.getX() * radiusAtYOffset / h, p1.getY() * radiusAtYOffset / h, p1.getZ());	// push out the points
                  final Point3D p3 = angleMat.apply(rotateMat.apply(p2));	// convert cutter xyz to actual xyz
                  pt1[a] = new Point3D(p3.getX() + cutX, pt1[a].getY(), p3.getY() + cutZ);   // cutter y --> lathe z
                }
              }
            }
          }
        }
        break;
      case Drill:
        for (Point3D[] pt1 : pts) {
          for (int a = 0; a < sectors; a++) {
            final Point3D p = new Point3D(pt1[a].getX() - cutX, pt1[a].getZ() - cutZ, -pt1[a].getY());	// actual xyz relative to cutter
            final Point3D p1 = angleMatI.apply(p);	// convert actual xyz to cutter xyz
            if (p1.getY() > 0.0) {
              final double h = Math.hypot(p1.getX(), p1.getZ());
              final double prof = profile.profileAt(h, rodR);
              if (prof >= 0.0) {
                if (prof <= p1.getY()) {
                  final Point3D p2 = new Point3D(p1.getX(), prof, p1.getZ());		// push out the points
                  final Point3D p3 = angleMat.apply(p2);	// convert cutter xyz to actual xyz
                  pt1[a] = new Point3D(p3.getX() + cutX, pt1[a].getY(), p3.getY() + cutZ);   // cutter y --> lathe z
                }
              }
            }
          }
        }
        break;
      case ECF:
        for (Point3D[] pt1 : pts) {
          for (int a = 0; a < sectors; a++) {
            final Point3D p = new Point3D(pt1[a].getX() - cutX, pt1[a].getZ() - cutZ, -pt1[a].getY());	// actual xyz relative to cutter
            final Point3D p1 = angleMatI.apply(p);	// convert actual xyz to cutter xyz
            if (p1.getY() > 0.0) {
              final double h = Math.hypot(p1.getX(), p1.getZ());
              if ((h <= (radius + rodR)) && (h >= (radius - rodR))) {
                final double prof = profile.profileAt(h - radius, rodR);
                if ((prof >= 0.0) && (prof <= p1.getY())) {
                  final Point3D p2 = new Point3D(p1.getX(), prof, p1.getZ());		// push out the points
                  final Point3D p3 = angleMat.apply(p2);	// convert cutter xyz to actual xyz
                  pt1[a] = new Point3D(p3.getX() + cutX, pt1[a].getY(), p3.getY() + cutZ);   // cutter y --> lathe z
                }
              }
            }
          }
        }
        break;
    }
  }

  /**
   * Make a fast cut on the given surface with the cutter at the given x,z
   * coordinate and the surface rotated on the spindle. The surface cut is only
   * calculated at the given angle, so the full profile of the cutter will not
   * be rendered.
   *
   * @param cutter cutter
   * @param cutX cutter x-coordinate
   * @param cutZ cutter z-coordinate
   * @param cDeg surface rotation on the spindle in degrees
   */
  public synchronized void cutSurface(Cutter cutter, double cutX, double cutZ, double cDeg) {
    int sectors = this.numSectors();			// number of sectors around shape
    double rodR = cutter.getTipWidth() / 2.0;
    double cutRadius = cutter.getRadius();
    Profile profile = cutter.getProfile();
    if (cutter.getLocation().isBack()) {
      cDeg += 180.0;    // cutter is on back of shape
    }

    while (cDeg < 0) {
      cDeg += 360.0;		// no negative angle
    }
    int a = ((int) Math.round(cDeg / (360.0 / (double) sectors))) % sectors;	// index for surface[][] -- must be in range 0 to sectors
    switch (cutter.getFrame()) {
      case HCF:
        for (Point3D[] pt1 : pts) {
          final double dx = pt1[a].getX() - cutX;		// distance from surface point to center of cutter
          final double dz = pt1[a].getZ() - cutZ;
          final double h = Math.hypot(dx, dz);
          if (h < cutRadius) {
            // contour the surface to the cutter
            pt1[a] = new Point3D(cutX + dx * cutRadius / h, pt1[a].getY(), cutZ + dz * cutRadius / h);   // cutter y --> lathe z
          }
        }
        break;
      case Drill:
        RotMatrix angleMat = new RotMatrix(Axis.Z, -cutter.getUCFAngle());
        RotMatrix angleMatI = new RotMatrix(Axis.Z, cutter.getUCFAngle());
        for (Point3D[] pt1 : pts) {
          // actual xyz relative to cutter and convert actual xyz to cutter xyz
          final Point3D p1 = angleMatI.apply(new Point3D(pt1[a].getX() - cutX, pt1[a].getZ() - cutZ, -pt1[a].getY()));
          if (p1.getY() > 0.0) {
            final double prof = profile.profileAt(Math.hypot(p1.getX(), p1.getZ()), rodR);
            if ((prof >= 0.0) && (prof <= p1.getY())) {
              // push out the points and convert cutter xyz to actual xyz
              final Point3D p3 = angleMat.apply(new Point3D(p1.getX(), prof, p1.getZ()));
              pt1[a] = new Point3D(p3.getX() + cutX, pt1[a].getY(), p3.getY() + cutZ);   // cutter y --> lathe z
            }
          }
        }
        break;
    }
  }
}
