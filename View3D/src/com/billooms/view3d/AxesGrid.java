package com.billooms.view3d;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D axes and grid that can be added to the 3D view.
 *
 * Instances of this class are immutable.
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
public class AxesGrid extends Group {

  private final static PhongMaterial redMaterial = new PhongMaterial();	  // Red for x-axis
  private final static PhongMaterial greenMaterial = new PhongMaterial(); // Green for y-axis
  private final static PhongMaterial blueMaterial = new PhongMaterial();  // Blue for z-axis
  private final static PhongMaterial blackMaterial = new PhongMaterial(); // Black for the grid

  /** Thickness of the axes. */
  private final static double AXES_THICKNESS = 0.035;
  /** Thickness of the grid lines. */
  private final static double GRID_THICKNESS = 0.01;

  /**
   * Construct a new axes and grid in 3D.
   *
   * @param length length of the axis and grid
   */
  public AxesGrid(double length) {
    redMaterial.setDiffuseColor(Color.RED);
    redMaterial.setSpecularColor(Color.PINK);

    greenMaterial.setDiffuseColor(Color.GREEN);
    greenMaterial.setSpecularColor(Color.LIGHTGREEN);

    blueMaterial.setDiffuseColor(Color.BLUE);
    blueMaterial.setSpecularColor(Color.LIGHTBLUE);

    blackMaterial.setDiffuseColor(Color.BLACK);
    blackMaterial.setSpecularColor(Color.BLACK);

    final Box xAxis = new Box(length, AXES_THICKNESS, AXES_THICKNESS);
    xAxis.setTranslateX(length / 2.0);
    xAxis.setMaterial(redMaterial);

    final Box yAxis = new Box(AXES_THICKNESS, length, AXES_THICKNESS);
    yAxis.setTranslateY(length / 2.0);
    yAxis.setMaterial(greenMaterial);

    final Box zAxis = new Box(AXES_THICKNESS, AXES_THICKNESS, length);
    zAxis.setTranslateZ(length / 2.0);
    zAxis.setMaterial(blueMaterial);

    this.getChildren().addAll(xAxis, yAxis, zAxis);

    for (int i = (int) -length; i <= length; i++) {
      addXLine(length * 2, i);
    }
    for (int i = (int) -length; i <= length; i++) {
      addYLine(length * 2, i);
    }
  }

  /**
   * Add to the Group a narrow box that resembles a line in x-direction of the
   * given total length centered on the z-axis and offset by the given yOffset.
   *
   * @param length total length
   * @param yOffset position on y-axis
   */
  private void addXLine(double length, double yOffset) {
    Box xline = new Box(length, GRID_THICKNESS, GRID_THICKNESS);
    xline.setMaterial(blackMaterial);
    xline.setTranslateY(yOffset);
    this.getChildren().add(xline);
  }

  /**
   * Add to the Group a narrow box that resembles a line in y-direction of the
   * given total length centered on the z-axis and offset by the given xOffset.
   *
   * @param length total length
   * @param xOffset position on the x-axis
   */
  private void addYLine(double length, double xOffset) {
    Box yLine = new Box(GRID_THICKNESS, length, GRID_THICKNESS);
    yLine.setMaterial(blackMaterial);
    yLine.setTranslateX(xOffset);
    this.getChildren().add(yLine);
  }

}
