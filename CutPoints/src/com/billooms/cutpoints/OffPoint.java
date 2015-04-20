package com.billooms.cutpoints;

import com.billooms.controls.CoarseFine;
import com.billooms.cutpoints.surface.Surface;

/**
 * Interface defining a CutPoint that can be added to an OffsetGroup.
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
public interface OffPoint {

  /**
   * Cut the given surface with this CutPoint.
   *
   * @param surface Surface
   * @param x new x-coordinate
   * @param y new y-coordinate
   */
  void cutSurface(Surface surface, double x, double y);

  /**
   * Make instructions for this CutPoint
   *
   * @param passDepth depth per pass (course cut)
   * @param passStep spindle steps per instruction (course cut)
   * @param lastDepth depth of final cut
   * @param lastStep spindle steps per instruction (final cut)
   * @param stepsPerRot steps per rotation
   * @param rotation Rotation of spindle
   * @param x new x-coordinate
   * @param z new z-coordinate
   */
  void makeInstructions(double passDepth, int passStep, double lastDepth, int lastStep, int stepsPerRot, CoarseFine.Rotation rotation, double x, double z);
}
