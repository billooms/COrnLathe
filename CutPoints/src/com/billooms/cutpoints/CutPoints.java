package com.billooms.cutpoints;

import com.billooms.clclass.CLclass;
import static com.billooms.clclass.CLclass.indent;
import static com.billooms.clclass.CLclass.indentLess;
import static com.billooms.clclass.CLclass.indentMore;
import com.billooms.controls.CoarseFine;
import com.billooms.cutpoints.surface.Line3D;
import com.billooms.cutters.Cutter;
import com.billooms.cutters.Cutters;
import com.billooms.drawables.Pt;
import com.billooms.drawables.simple.Curve;
import com.billooms.outline.Outline;
import com.billooms.patterns.Patterns;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Point3D;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CutPoints is a manager for all the CutPoints which cut the shape.
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
public class CutPoints extends CLclass {

  /** All CutPoints property change names start with this prefix. */
  public final static String PROP_PREFIX = "CutPoints" + "_";
  /** Property name used when adding a custom pattern. */
  public final static String PROP_ADD = PROP_PREFIX + "Add";
  /** Property name used when deleting a custom pattern. */
  public final static String PROP_DELETE = PROP_PREFIX + "Delete";
  /** Property name used when changing the order of CutPoints. */
  public final static String PROP_ORDER = PROP_PREFIX + "Order";
  /** Property name used when changing multiple points. */
  public final static String PROP_MULTI = PROP_PREFIX + "Multi";

  /** Default type of CutPoint to add. */
  public final static Class<? extends CutPoint> DEFAULT_CLASS = RosettePoint.class;
  /** Number of points in 3D line */
  protected final static int NUM_3D_PTS = 360;	// every 1 degrees

  /** A list of the CutPoints. */
  private ArrayList<CutPoint> list = null;

  /** Saved copy of Cutter manager. */
  private final Cutters cutterMgr;
  /** Saved copy of the Outline. */
  private final Outline outline;
  /** Saved copy of the Pattern Manager. */
  private final Patterns patMgr;

  /**
   * Construct an empty CutPoint manager.
   *
   * @param cutMgr cutter manager
   * @param outline outline
   * @param patMgr pattern manager
   */
  public CutPoints(Cutters cutMgr, Outline outline, Patterns patMgr) {
    this.cutterMgr = cutMgr;
    this.outline = outline;
    this.patMgr = patMgr;
    list = new ArrayList<>();
    outline.addPropertyChangeListener(this);	// listen for outline changes
    cutterMgr.addPropertyChangeListener(this);	// listen for cutter changes
  }

  /**
   * Construct a CutPoint manager and read all CutPoints from the given DOM
   * Element.
   *
   * @param element DOM Element
   * @param cutMgr cutter manager
   * @param outline outline
   * @param patMgr pattern manager
   */
  public CutPoints(Element element, Cutters cutMgr, Outline outline, Patterns patMgr) {
    this(cutMgr, outline, patMgr);
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i) instanceof Element) {
        Element profElement = (Element) childNodes.item(i);
        if (profElement.getTagName().equals("GoToPoint")) {
          list.add(new GoToPoint(profElement, cutMgr, outline));
        }
        if (profElement.getTagName().equals("IndexPoint")) {
          list.add(new IndexPoint(profElement, cutMgr, outline));
        }
        if (profElement.getTagName().equals("PiercePoint")) {
          list.add(new PiercePoint(profElement, cutMgr, outline));
        }
        if (profElement.getTagName().equals("RosettePoint")) {
          list.add(new RosettePoint(profElement, cutMgr, outline, patMgr));
        }
        if (profElement.getTagName().equals("SpiralIndex")) {
          list.add(new SpiralIndex(profElement, cutMgr, outline));
        }
        if (profElement.getTagName().equals("SpiralRosette")) {
          list.add(new SpiralRosette(profElement, cutMgr, outline, patMgr));
        }
        if (profElement.getTagName().equals("SpiralLine")) {
          list.add(new SpiralLine(profElement, cutMgr, outline, patMgr));
        }
        if (profElement.getTagName().equals("PatternPoint")) {
          list.add(new PatternPoint(profElement, cutMgr, outline, patMgr));
        }
        if (profElement.getTagName().equals("OffsetGroup")) {
          list.add(new OffsetGroup(profElement, cutMgr, outline, patMgr));
        }
      }
    }
    renumber();	// just in case any were not numbered correctly
    list.stream().forEach((cp) -> {
      cp.addPropertyChangeListener(this);   // listen to each CutPoint
    });
  }

  @Override
  public String toString() {
    return "CutPoints: " + list.size() + " cuts";
  }

  /**
   * Draw all CutPoints for the given cutter, or all cutters if null.
   *
   * @param g2d Graphics2D
   * @param cutter Cutter (or null for all CutPoints)
   */
  public void paint(Graphics2D g2d, Cutter cutter) {
    if (!list.isEmpty()) {
      for (CutPoint cp : list) {
        if (cp.isVisible() && cutPtMatchesCutter(cp, cutter)) {
          cp.paint(g2d);
        }
      }
    }
  }

  /**
   * Add a CutPoint of the given class with default values. This fires a
   * PROP_ADD property change with the new CutPoint.
   *
   * @param <T> extends CutPoint
   * @param clazz The class of CutPoint to add
   * @param pt Location for the new CutPoint
   * @param cutter cutter
   */
  public <T extends CutPoint> void addCut(Class<T> clazz, Point2D.Double pt, Cutter cutter) {
    if (outline.getNumPts() < 2) {
      return;		// don't add cutPoints if there is no curve
    }
    try {
      T newCutPoint;
      if (clazz.getSimpleName().equals("RosettePoint")
          || clazz.getSimpleName().equals("SpiralRosette")
          || clazz.getSimpleName().equals("SpiralLine")
          || clazz.getSimpleName().equals("PatternPoint")) {
        // Look for a constructor of clazz for (Point2D.Double, Cutter, Outline, Patterns) and make a new instance with default values
        newCutPoint = clazz.getConstructor(Point2D.Double.class, Cutter.class, Outline.class, Patterns.class).newInstance(pt, cutter, outline, patMgr);
      } else {
        // Look for a constructor of clazz for (Point2D.Double, Cutter, Outline) and make a new instance with default values
        newCutPoint = clazz.getConstructor(Point2D.Double.class, Cutter.class, Outline.class).newInstance(pt, cutter, outline);
      }
      newCutPoint.setNum(list.size());
      list.add(newCutPoint);
      newCutPoint.addPropertyChangeListener(this);
      pcs.firePropertyChange(PROP_ADD, null, newCutPoint);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  /**
   * Add a new CutPoint to the end of the list. Make it the same as the last
   * CutPoint with the given cutter (exclude GoToPoints). If there are no prior
   * CutPoints, then make a DEFAULT_CUT. This fires a PROP_ADD property change
   * with the new CutPoint.
   *
   * @param pt Location for the new CutPoint
   * @param cutter cutter
   */
  public void addCut(Point2D.Double pt, Cutter cutter) {
    if (outline.getNumPts() < 2) {
      return;		// don't add cutPoints if there is no curve
    }
    CutPoint last = getLastCutPoint(cutter);
    if (last == null) {
      addCut(DEFAULT_CLASS, pt, cutter);  // use DEFAULT_CLASS with default values
    } else {
      Class<? extends CutPoint> clazz = last.getClass();
      try {
        // Look for a constructor of clazz for (Point2D.Double, clazz) and make a duplicate
        CutPoint newCutPoint = (CutPoint) clazz.getConstructor(Point2D.Double.class, clazz).newInstance(pt, last);
        newCutPoint.setNum(list.size());
        list.add(newCutPoint);
        newCutPoint.addPropertyChangeListener(this);
        pcs.firePropertyChange(PROP_ADD, null, newCutPoint);
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  /**
   * Duplicate the given CutPoint and add it to the list after the given
   * CutPoint and renumber all CutPoints.
   *
   * This fires a PROP_ADD property change with the new CutPoint.
   *
   * @param cp CutPoint
   * @return true=the list contained the point and it was duplicated;
   * false=point was not in the list
   */
  public boolean duplicateCut(CutPoint cp) {
    if (list.contains(cp)) {
      int idx = list.indexOf(cp) + 1;
      Class<? extends CutPoint> clazz = cp.getClass();
      try {
        // Look for a constructor of clazz for (Point2D.Double, clazz) and make a duplicate
        CutPoint newCutPoint = (CutPoint) clazz.getConstructor(Point2D.Double.class, clazz).newInstance(cp.getPos2D(), cp);
        list.add(idx, newCutPoint);
        newCutPoint.addPropertyChangeListener(this);
        renumber();
        pcs.firePropertyChange(PROP_ADD, null, newCutPoint);
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        Exceptions.printStackTrace(ex);
      }
      return true;
    }
    return false;
  }

  /**
   * Remove the given CutPoint from the list and renumber the CutPoints.
   *
   * This fires a PROP_REMOVE property change with the old CutPoint.
   *
   * @param cpt CutPoint
   * @return true=the list contained the point and it was removed; false=point
   * was not in the list
   */
  public boolean removeCut(CutPoint cpt) {
    boolean removed = false;
    if (list.contains(cpt)) {
      cpt.removePropertyChangeListener(this);
      cpt.clear();
      removed = list.remove(cpt);
    } else {
      for (CutPoint cc : list) {
        if (cc instanceof SpiralCut) {
          if (((SpiralCut) cc).getBeginPoint() == cpt) {
            cc.clear();
            removed = list.remove(cc);
            cc.removePropertyChangeListener(this);
            break;
          }
          for (int i = 0; i < ((SpiralCut) cc).getNumGoTos(); i++) {
            GoToPoint gPt = ((SpiralCut) cc).getGoToPoint(i);
            if (gPt == cpt) {
              ((SpiralCut) cc).removeGoToPoint((GoToPoint) cpt);
              removed = true;
              break;
            }
          }
        }
        if (cc instanceof OffsetGroup) {
          for (int i = 0; i < ((OffsetGroup) cc).getNumCutPoints(); i++) {
            CutPoint cp = ((OffsetGroup) cc).getCutPoint(i);
            if (cp == cpt) {
              ((OffsetGroup) cc).removeCutPoint(cpt);
              removed = true;
              break;
            }
          }
        }
      }
    }
    renumber();
    pcs.firePropertyChange(PROP_DELETE, cpt, null);
    return removed;
  }

  /**
   * Drop the given GoToPoint onto the given SpiralCut.
   *
   * @param gPt GoToPoint
   * @param cpt SpiralCut
   */
  public void dropGoTo(GoToPoint gPt, SpiralCut cpt) {
    if (list.contains(gPt)) {
      gPt.removePropertyChangeListener(this);
      list.remove(gPt);
    }
    cpt.addGoToPoint(gPt);
    renumber();
  }

  /**
   * Drop the given OffPoint onto the given OffsetGroup.
   *
   * @param offPt OffPoint
   * @param offGrp OffsetGroup
   */
  public void dropOffPoint(CutPoint offPt, OffsetGroup offGrp) {
    if (list.contains(offPt)) {
      offPt.removePropertyChangeListener(this);
      list.remove(offPt);
    }
    offGrp.addOffPoint(offPt);
    renumber();
  }

  /**
   * Clear all CutPoints from the list.
   *
   * This fires a PROP_DELETE propertyChange.
   */
  public synchronized void clear() {
    if (list.isEmpty()) {
      return;
    }
    for (CutPoint cpt : list) {
      cpt.removePropertyChangeListener(this);
      cpt.clear();
    }
    list.clear();
    pcs.firePropertyChange(PROP_DELETE, null, null);
  }

  /**
   * Determine if the CutPointList is empty.
   *
   * @return true=empty, false=not empty
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Get the size of the CutPointList.
   *
   * @return number of CutPoints
   */
  public int size() {
    return list.size();
  }

  /**
   * Get all the CutPoints in an unmodifiable list.
   *
   * @return list of all CutPoints
   */
  public List<CutPoint> getAll() {
    return Collections.unmodifiableList(list);
  }

  /**
   * Get all the CutPoints that have the given cutter (or all CutPoints if
   * cutter is null) in an unmodifiable list.
   *
   * @param cutter cutter (or null for all CutPoints)
   * @return list of all CutPoints
   */
  public List<CutPoint> getAll(Cutter cutter) {
    if (cutter == null) {
      return Collections.unmodifiableList(list);
    }
    ArrayList<CutPoint> newList = new ArrayList<>();
    for (CutPoint cp : list) {
      if (cutPtMatchesCutter(cp, cutter)) {
        newList.add(cp);
      }
    }
    return Collections.unmodifiableList(newList);
  }

  /**
   * Get all 3D lines that need to be drawn.
   *
   * @return list of all 3D lines
   */
  public List<Line3D> getAll3DLines() {
    ArrayList<Line3D> lines = new ArrayList<>();
    for (CutPoint cp : getAll()) {        // TODO: Filter this for the selected cutter?
      if (cp.isVisible()) {     // only get lines from visible CutPoints
        lines.addAll(cp.get3DLines());
      }
    }
    return lines;
  }

  /**
   * Determine if the given CutPoint uses the given cutter. This will always
   * return true if the cutter is null. GoToPoints are universal -- used by all
   * cutters.
   *
   * @param cp CutPoint
   * @param cutter Cutter (or null)
   * @return true: matches cutter (always true if cutter is null)
   */
  private boolean cutPtMatchesCutter(CutPoint cp, Cutter cutter) {
    if (cutter == null) {
      return true;
    }
    // GoToPoints now are associated with a particular cutter
//    if (cp instanceof GoToPoint) {
//      return true;    // GoToPoints are used for all cutters
//    }
    if (cp.getCutter().equals(cutter)) {
      return true;
    }
    return false;
  }

  /**
   * Get a list of any OffsetCutPoints.
   *
   * @return list of OffsetCutPoints (or empty list if there are none)
   */
  public ArrayList<OffsetCut> getOffsetCutPoints() {
    ArrayList<OffsetCut> oList = new ArrayList<>();
    list.stream()
        .filter((cp) -> (cp instanceof OffsetCut))
        .forEach((cp) -> {
          oList.add((OffsetCut) cp);
        });
    return oList;
  }

  /**
   * Get the CutPoint for the given index.
   *
   * @param idx index
   * @return CutPoint (or null if out of range)
   */
  public CutPoint get(int idx) {
    if ((idx >= list.size()) || (idx < 0)) {
      return null;
    }
    return list.get(idx);
  }

  /**
   * Find the last CutPoint with a matching cutter (or any CutPoint if cutter is
   * null) which is NOT a GoToPoint.
   *
   * @param cutter cutter to match or null for any
   * @return last CutPoint which is NOT a GoToPoint (or null if none)
   */
  public CutPoint getLastCutPoint(Cutter cutter) {
    if (list.isEmpty()) {
      return null;
    }
    for (int i = list.size() - 1; i >= 0; i--) {
      CutPoint cp = list.get(i);
      if (cutPtMatchesCutter(cp, cutter)) {
        return cp;
      }
    }
    return null;
  }

  /**
   * Move the specified CutPoint up in the order of the list (toward the
   * beginning). This fires a PROP_ORDER property change with the old and new
   * index.
   *
   * @param cPt CutPoint
   */
  public void moveUp(CutPoint cPt) {
    int idx = list.indexOf(cPt);
    if ((idx >= 1) && (idx < list.size())) {	// can't move point 0 up
      list.remove(cPt);
      list.add(idx - 1, cPt);
      renumber();
      pcs.firePropertyChange(PROP_ORDER, idx, idx - 1);
    }
  }

  /**
   * Move the specified CutPoint down in the order of the list (toward the end).
   * This fires a PROP_ORDER property change with the old and new index.
   *
   * @param cPt CutPoint
   */
  public void moveDown(CutPoint cPt) {
    int idx = list.indexOf(cPt);
    if ((idx >= 0) && (idx < (list.size() - 1))) {
      list.remove(cPt);
      list.add(idx + 1, cPt);
      renumber();
      pcs.firePropertyChange(PROP_ORDER, idx, idx + 1);
    }
  }

  /**
   * Renumber the CutPoints sequentially starting at zero.
   */
  public final void renumber() {
    for (int i = 0; i < list.size(); i++) {
      list.get(i).setNum(i);
    }
  }

  /**
   * Determine if the CutList contains a given CutPoint.
   *
   * @param cp CutPoint
   * @return true=yes, it's in the CutList
   */
  public boolean contains(CutPoint cp) {
    return list.contains(cp);
  }

  /**
   * Find the closest CutPoint (within minSep) to the given point that has the
   * given cutter (or any if cutter is null).
   *
   * @param pt given point
   * @param dist distance to check
   * @param cutter Cutter (or null for any)
   * @return Closest CutPoint (or null if there are none)
   */
  public CutPoint closestCutPt(Point2D.Double pt, double dist, Cutter cutter) {
    if (list.isEmpty()) {
      return null;
    }
    CutPoint closest = null;
    double sep;
    for (CutPoint cutPt : list) {
      if (!cutPtMatchesCutter(cutPt, cutter)) {
        continue;   // skip CutPoints with a different cutter
      }
      if (cutPt instanceof OffsetGroup) {		// if coincident, find the CutPoint first (not the OffsetGroup)
        for (CutPoint cp : ((OffsetGroup) cutPt).getAllCutPoints()) {
          sep = cp.separation(pt);
          if (sep < dist) {
            dist = sep;
            closest = cp;
          }
        }
      }

      sep = cutPt.separation(pt);
      if (sep < dist) {
        dist = sep;
        closest = cutPt;
      }

      if (cutPt instanceof SpiralCut) {		// for spirals look for begin point
        CutPoint ce = ((SpiralCut) cutPt).getBeginPoint();
        sep = ce.separation(pt);
        if (sep < dist) {
          dist = sep;
          closest = ce;
        }
        for (GoToPoint gPt : ((SpiralCut) cutPt).getAllGoTos()) {
          sep = gPt.separation(pt);
          if (sep < dist) {
            dist = sep;
            closest = gPt;
          }
        }
      }
    }
    return closest;
  }

  /**
   * Find the closest SpiralCut to the given location.
   *
   * @param pt location
   * @param dist distance to check
   * @return closest SpiralCut (or null if none are near)
   */
  public SpiralCut closestSpiralCut(Point2D.Double pt, double dist) {
    SpiralCut closest = null;
    double sep;
    for (CutPoint cc : list) {
      if (cc instanceof SpiralCut) {		// Look for just SpiralCutPoints
        sep = cc.separation(pt);
        if (sep < dist) {
          dist = sep;
          closest = (SpiralCut) cc;
        }
      }
    }
    return closest;
  }

  /**
   * Find the closest OffsetGroup to the given location.
   *
   * @param pt location
   * @param dist distance to check
   * @return closest OffsetGroup (or null if none are near)
   */
  public OffsetGroup closestOffsetGroup(Point2D.Double pt, double dist) {
    OffsetGroup closest = null;
    double sep;
    for (CutPoint cc : list) {
      if (cc instanceof OffsetGroup) {		// Look for just OffsetGroup
        sep = cc.separation(pt);
        if (sep < dist) {
          dist = sep;
          closest = (OffsetGroup) cc;
        }
      }
    }
    return closest;
  }

  /**
   * Snap all CutPoints to their respective cutter path curve.
   */
  public void snapAllCutPoints() {
    for (Cutter cutter : cutterMgr.getAllCutters()) {
      for (CutPoint c : getAll(cutter)) {
        c.snapToCurve();
        if (c instanceof SpiralCut) {
          ((SpiralCut) c).getBeginPoint().snapToCurve();
        }
      }
    }
  }

  /**
   * Invert all CutPoints (that is, flip vertically around the x-axis). This
   * fires a PROP_MULTI property change. Note that points are moved using drag()
   * which fires a PROP_DRAG property change. The reason for using drag() is so
   * that you can choose to ignore the multiple events and not re-render all the
   * time.
   */
  public void invert() {
    if (list.isEmpty()) {
      return;
    }
    for (CutPoint c : list) {			// move the cutPoints
      c.invert();
      if (c instanceof SpiralCut) {
        CutPoint beginPt = ((SpiralCut) c).getBeginPoint();
        beginPt.drag(new Point2D.Double(beginPt.getX(), -beginPt.getZ()));
        for (int i = 0; i < ((SpiralCut) c).getNumGoTos(); i++) {
          GoToPoint goToPt = ((SpiralCut) c).getGoToPoint(i);
          goToPt.drag(new Point2D.Double(goToPt.getX(), -goToPt.getZ()));
        }
      }
      if (c instanceof OffsetGroup) {
        for (int i = 0; i < ((OffsetGroup) c).getNumCutPoints(); i++) {
          CutPoint cPt = ((OffsetGroup) c).getCutPoint(i);
          cPt.drag(new Point2D.Double(cPt.getX(), -cPt.getZ()));
        }
      }
    }
    pcs.firePropertyChange(PROP_MULTI, null, null);
  }

  /**
   * Scales the coordinates of all CutPoints.
   *
   * This fires a PROP_MULTI property change. Note that points are moved using
   * drag() which fires a PROP_DRAG property change. The reason for using drag()
   * is so that you can choose to ignore the multiple events and not re-render
   * all the time.
   * 
   * @param factor scale factor (Must be in the range 0.1 to 10.0).
   */
  public void scale(double factor) {
    if (list.isEmpty()) {
      return;
    }
    if ((factor < 0.1) && (factor > 10.0) && (factor == 1.0)) {
      return;
    }
    for (CutPoint c : list) {			// move the cutPoints
      c.scale(factor);
      if (c instanceof SpiralCut) {
        CutPoint beginPt = ((SpiralCut) c).getBeginPoint();
        beginPt.drag(new Point2D.Double(beginPt.getX() * factor, beginPt.getZ() * factor));
        for (int i = 0; i < ((SpiralCut) c).getNumGoTos(); i++) {
          GoToPoint goToPt = ((SpiralCut) c).getGoToPoint(i);
          goToPt.drag(new Point2D.Double(goToPt.getX() * factor, goToPt.getZ() * factor));
        }
      }
      if (c instanceof OffsetGroup) {
        for (int i = 0; i < ((OffsetGroup) c).getNumCutPoints(); i++) {
          CutPoint cPt = ((OffsetGroup) c).getCutPoint(i);
          cPt.drag(new Point2D.Double(cPt.getX() * factor, cPt.getZ() * factor));
        }
      }
    }
    pcs.firePropertyChange(PROP_MULTI, null, null);
  }

  /**
   * Offset all CutPoints vertically by a given amount. This fires a PROP_MULTI
   * property change. Note that points are moved using drag() which fires a
   * PROP_DRAG property change. The reason for using drag() is so that you can
   * choose to ignore the multiple events and not re-render all the time.
   *
   * @param delta amount to move
   */
  public void offsetVertical(double delta) {
    if (list.isEmpty()) {
      return;
    }
    for (CutPoint c : list) {			// move the cutPoints
      c.offSetVertical(delta);
      if (c instanceof SpiralCut) {
        CutPoint beginPt = ((SpiralCut) c).getBeginPoint();
        beginPt.drag(new Point2D.Double(beginPt.getX(), beginPt.getZ() - delta));
        for (int i = 0; i < ((SpiralCut) c).getNumGoTos(); i++) {
          GoToPoint goToPt = ((SpiralCut) c).getGoToPoint(i);
          goToPt.drag(new Point2D.Double(goToPt.getX(), goToPt.getZ() - delta));
        }
      }
      if (c instanceof OffsetGroup) {
        for (int i = 0; i < ((OffsetGroup) c).getNumCutPoints(); i++) {
          CutPoint cPt = ((OffsetGroup) c).getCutPoint(i);
          cPt.drag(new Point2D.Double(cPt.getX(), cPt.getZ() - delta));
        }
      }
    }
    pcs.firePropertyChange(PROP_MULTI, null, null);
  }

  @Override
  public void writeXML(PrintWriter out) {
    if (!list.isEmpty()) {
      out.println(indent + "<CutPoints>");
      indentMore();
      list.stream().forEach((cp) -> {
        cp.writeXML(out);    // CutPoints can write out their own xml
      });
      indentLess();
      out.println(indent + "</CutPoints>");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CutPoints.propertyChange " + evt.getSource().getClass().getSimpleName() + " " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    String propName = evt.getPropertyName();

    // This CutPoint manager listens to all of the CutPoints
    if (propName.contains("Drag")) {
      return; // don't keep updating when dragging a point, and don't pass the propChange on
    }

    // Listen for manual changes to CutPoint XZ from DataNavigator, then re-snap
    // Listen for changing the snap of a CutPoint, then re-snap
    if (evt.getSource() instanceof CutPoint) {
      if (propName.equals(Pt.PROP_X)
          || propName.equals(Pt.PROP_Z)
          || propName.equals(CutPoint.PROP_SNAP)) {
        CutPoint cp = (CutPoint) evt.getSource();
        cp.snapToCurve();
      }
      if (propName.equals(CutPoint.PROP_CUTTER)) {
        CutPoint cp = (CutPoint) evt.getSource();
        cp.snapToCurve();     // have to snap it to a new curve when changing cutter
      }
    }

    // Listen for any outline changes and re-snap all CutPoints
    // MULTI are from Invert and Offset which are handled by the actions rather than simply snapped
    if (evt.getSource().equals(outline) && !propName.equals(Outline.PROP_MULTI)) {
      snapAllCutPoints();
      // but don't pass the info up the line
    } else if (evt.getSource().equals(cutterMgr)) {	  // listen for cutter changes
      // if a cutter is deleted, then delete all associated CutPoints
      if (propName.equals(Cutters.PROP_DELETE)) {
        deletePtsForCutter((String) evt.getOldValue());
      }
    } else {
      // pass the info through
      pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

  /**
   * Make instructions for all CutPoints in the list.
   *
   * @param cPt Output for a single OffsetCutPoint (do all if this is null)
   * @param cutter selected cutter
   * @param controls control panel data
   * @param stepsPerRot steps per rotation
   */
  public void makeInstructions(OffsetCut cPt, Cutter cutter, CoarseFine controls, int stepsPerRot) {
    if (list.isEmpty()) {
      return;
    }
    if (cPt == null) {
      for (CutPoint cp : getAll(cutter)) {
        if (!(cp instanceof OffsetCut)) {			// no reason to ever generate code for multiple OffsetCutPoint
          cp.makeInstructions(controls, stepsPerRot);
        }
      }
    } else {
      cPt.makeInstructions(controls, stepsPerRot);
    }
  }

  /** Delete all CutPoints that have the given cutter (the cutter is already
   * deleted). */
  private synchronized void deletePtsForCutter(String cutterName) {
    ArrayList<CutPoint> toDelete = new ArrayList<>();
    // do this in two steps to avoid ConcurrentModificationException
    for (CutPoint cp : getAll()) {
      if (cp.getCutter().getName().equals(cutterName)) {
        toDelete.add(cp);
      }
    }
    for (CutPoint cp : toDelete) {
      removeCut(cp);
    }
  }
  
  /**
   * Convert a SpiralCut to a series of individual CutPoints. This fires a
   * PROP_ADD property change.
   *
   * @param spiralCutPt SpiralCut to expand
   * @param nInserts number of cuts to insert
   */
  public void spiralToPoints(SpiralCut spiralCutPt, int nInserts) {
    Point3D[] rzcSurface = spiralCutPt.getSurfaceTwist();
    ArrayList<Point3D> xyzSurface = spiralCutPt.toXYZ(rzcSurface);
    // use a much finer resolution cutterPathCurve for smoothness
    Curve fineCut = outline.getCutterPathCurve(spiralCutPt.getCutter());
    fineCut.reSample(outline.getResolution() / 10.0);
    
    // cumLength[] is an array  of cummulative length at each point for interpolation
    double[] cumLength = new double[rzcSurface.length];
    cumLength[0] = 0.0;
    double totLength = 0.0;
    if (xyzSurface.size() >= 2) {
      for (int i = 1; i < xyzSurface.size(); i++) {
        totLength += xyzSurface.get(i).distance(xyzSurface.get(i - 1));
        cumLength[i] = totLength;
      }
    }
//    System.out.println("spiralToPoints totLength=" + F3.format(totLength));
    
    double ptSpacing = totLength / (double) (nInserts + 1);		// this is the step spacing between each new point
    double startDepth = spiralCutPt.getBeginPoint().getDepth();
    double endCutDepth = spiralCutPt.getEndDepth();
    double deltaDepth = endCutDepth - startDepth;
    int num = spiralCutPt.getNum();
    list.remove(spiralCutPt);		// take the old point out
    if (spiralCutPt instanceof SpiralRosette) {     // For SpiralRosettes
      RosettePoint beginRosPt = (RosettePoint) spiralCutPt.getBeginPoint();
      list.add(num, beginRosPt);		// and replace it with a new start
      num++;
      
      double rosStartAmp = beginRosPt.getRosette().getPToP();
      double rosStartPhase = beginRosPt.getRosette().getPhase();
      int repeat = beginRosPt.getRosette().getRepeat();
      
      double ros2StartAmp = 0, ros2StartPhase = 0;
      int repeat2 = 0;
      if (beginRosPt.getMotion().usesBoth()) {
        ros2StartAmp = beginRosPt.getRosette2().getPToP();
        ros2StartPhase = beginRosPt.getRosette2().getPhase();
        repeat2 = beginRosPt.getRosette2().getRepeat();
      }

      double target;
      double x = 0.0, z = 0.0, c = 0.0;
      double rStart = rzcSurface[0].getX();
      double rEnd = rzcSurface[rzcSurface.length-1].getX();
      for (int i = 1; i <= nInserts; i++) {
        RosettePoint newPt = new RosettePoint(spiralCutPt.getPos2D(), beginRosPt);   // position will be changed later
        switch (spiralCutPt.getCutter().getFrame()) {
          case HCF:
          case UCF:
            newPt.setSnap(false);  // don't snap because we want points to fall on a finer resolution
        }
        newPt.setNum(num);
        target = (double) i * ptSpacing;
        for (int j = 0; j < cumLength.length - 1; j++) {
          if (cumLength[j] >= target) {   // look for the first cumLength that is >= the target
            // Interpolate values for x, z, c
            x = rzcSurface[j-1].getX() + (rzcSurface[j].getX() - rzcSurface[j-1].getX()) * (target - cumLength[j-1]) / (cumLength[j] - cumLength[j-1]);
            z = rzcSurface[j-1].getY() + (rzcSurface[j].getY() - rzcSurface[j-1].getY()) * (target - cumLength[j-1]) / (cumLength[j] - cumLength[j-1]);
            c = rzcSurface[j-1].getZ() + (rzcSurface[j].getZ() - rzcSurface[j-1].getZ()) * (target - cumLength[j-1]) / (cumLength[j] - cumLength[j-1]);
            break;
          }
        }
        
        double radiusRatio = x / rStart;
        double cumRatio = target / totLength;
        double scaledDepth;
        if (Math.abs(rEnd) < (outline.getResolution() / 2.0)) {
          // if the end radius is essentially zero, then scale depth proportional to radius
          scaledDepth = startDepth * radiusRatio;
        } else if (endCutDepth == 0.0) {
          // if the end depth is zero, then scale depth proportional to change in radius
          scaledDepth = CutPoint.proportion(rStart, x, rEnd, startDepth, endCutDepth);
        } else {
          // This is the default when end is not zero depth or zero radius
          // This was first used on bowl2
          double scaledEndDepth = endCutDepth * rStart / rEnd;
          scaledDepth = (startDepth + cumRatio * (scaledEndDepth - startDepth)) * radiusRatio;
        }

        newPt.setDepth(scaledDepth);
        if (rosStartAmp == startDepth) {    // scale rosette amplitude if it's the same as the start depth
          newPt.getRosette().setPToP(scaledDepth);
        }
        newPt.getRosette().setPhase(rosStartPhase + c * (double) repeat);
        if (beginRosPt.getMotion().usesBoth()) {
          if (ros2StartAmp == startDepth) {   // scale rosette amplitude if it's the same as the start depth
            newPt.getRosette2().setPToP(scaledDepth);
          }
          newPt.getRosette2().setPhase(ros2StartPhase + c * (double) repeat2);
        }
        Point2D.Double near = fineCut.nearestPoint(new Point2D.Double(x, z));
        newPt.move(near);     // this is on the the finer cutter curve -- not snapped
        list.add(num, newPt);
//        System.out.println("x:" + F3.format(x) + " -> " + F3.format(newPt.getX())
//          + " z:" + F3.format(z) + " -> " + F3.format(newPt.getZ())
//          + " depth:" + F3.format(newPt.getDepth()) + " amp:" + F3.format(newPt.getRosette().getPToP()));
        num++;
      }
      
      RosettePoint endRosPt = new RosettePoint(spiralCutPt.getPos2D(), beginRosPt);	// x,z,snap are at the start
      endRosPt.setDepth(endCutDepth);
      endRosPt.getRosette().setPhase(rosStartPhase + rzcSurface[rzcSurface.length - 1].getZ() * (double)repeat);
      if (rosStartAmp == startDepth) {   // assume that we should scale amplitude if same as depth
        endRosPt.getRosette().setPToP(endCutDepth);
      }
      if (endRosPt.getMotion().usesBoth()) {
        if (ros2StartAmp == startDepth) {
          endRosPt.getRosette2().setPToP(endCutDepth);
        }
        endRosPt.getRosette2().setPhase(ros2StartPhase + rzcSurface[rzcSurface.length - 1].getZ() * (double)repeat2);
      }
      list.add(num, endRosPt);				// add the last Rosettepoint at the end
    } else if (spiralCutPt instanceof SpiralIndex) {    // for SpiralIndex
      IndexPoint beginIdxPt = (IndexPoint) spiralCutPt.getBeginPoint();
      list.add(num, beginIdxPt);	// and replace it with a new start
      num++;
      
      double target;
      double x = 0.0, z = 0.0, c = 0.0;
      for (int i = 1; i <= nInserts; i++) {
        IndexPoint newPt = new IndexPoint(spiralCutPt.getPos2D(), beginIdxPt);   // position will be changed later
        switch (spiralCutPt.getCutter().getFrame()) {
          case HCF:
          case UCF:
            newPt.setSnap(false);  // don't snap because we want points to fall on a finer resolution
        }
        newPt.setNum(num);
        target = (double) i * ptSpacing;
        for (int j = 0; j < cumLength.length - 1; j++) {
          if (cumLength[j] >= target) {   // look for the first cumLength that is >= the target
            // Interpolate values for x, z, c
            x = rzcSurface[j - 1].getX() + (rzcSurface[j].getX() - rzcSurface[j - 1].getX()) * (target - cumLength[j - 1]) / (cumLength[j] - cumLength[j - 1]);
            z = rzcSurface[j - 1].getY() + (rzcSurface[j].getY() - rzcSurface[j - 1].getY()) * (target - cumLength[j - 1]) / (cumLength[j] - cumLength[j - 1]);
            c = rzcSurface[j - 1].getZ() + (rzcSurface[j].getZ() - rzcSurface[j - 1].getZ()) * (target - cumLength[j - 1]) / (cumLength[j] - cumLength[j - 1]);
            break;
          }
        }
        newPt.setDepth(startDepth + deltaDepth * target / totLength);
        newPt.setPhase(beginIdxPt.getPhase() + c * beginIdxPt.getRepeat());
        Point2D.Double near = fineCut.nearestPoint(new Point2D.Double(x, z));
        // must snap inserted points so that the direction is calculated correctly
        newPt.move(near);     // this is on the the finer cutter curve -- will be snapped later
        list.add(num, newPt);
//        System.out.println("x:" + F3.format(x) + " -> " + F3.format(newPt.getX())
//          + " z:" + F3.format(z) + " -> " + F3.format(newPt.getZ())
//          + " depth:" + F3.format(newPt.getDepth()));
        num++;
      }

      IndexPoint endIdxPt = new IndexPoint(spiralCutPt.getPos2D(), beginIdxPt);	// x,z,snap are at the start
      endIdxPt.setDepth(spiralCutPt.getEndDepth());
      endIdxPt.setPhase(beginIdxPt.getPhase() + rzcSurface[rzcSurface.length - 1].getZ() * beginIdxPt.getRepeat());
      list.add(num, endIdxPt);				// add the last IndexPoint at the end
    }

    // snap back to the closest point on the cutter curve (only snappable points)
    snapAllCutPoints();   
    renumber();
    pcs.firePropertyChange(PROP_ADD, null, null);
  }

}
