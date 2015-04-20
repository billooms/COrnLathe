package com.billooms.view3d;

import com.billooms.cutpoints.surface.Line3D;
import java.util.List;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * Multiple disconnected lines.
 *
 * modified from http://birdasaur.github.io/FXyz/
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
public class MultiLine3D extends Group {

  /** Color of the lines. */
  private final static Color color = Color.BLACK;
  /** Vertical width of the lines. */
  private final static float width = 0.01f;

  /** This is the mesh that defines the lines. */
  private final TriangleMesh mesh = new TriangleMesh();
  /** To add a TriangleMesh to a 3D scene you need a MeshView container object. */
  private final MeshView meshView = new MeshView(mesh);
  /** Material used for the lines. */
  private final static PhongMaterial lineMaterial = new PhongMaterial();

  /**
   * Construct a new MultiLine3D object.
   *
   * @param lines list of lines
   */
  public MultiLine3D(List<Line3D> lines) {
    lineMaterial.setDiffuseColor(color);
    lineMaterial.setSpecularColor(color);

    //The MeshView allows you to control how the TriangleMesh is rendered
    meshView.setDrawMode(DrawMode.FILL);    //Fill so that the line shows width
//    meshView.setDrawMode(DrawMode.LINE);    // only show lines
    meshView.setCullFace(CullFace.NONE);    // no culling (backs are black)
    meshView.setMaterial(lineMaterial);
    this.getChildren().add(meshView);

    // add dummy Texture Coordinate
    mesh.getTexCoords().addAll(0, 0);

    setDepthTest(DepthTest.INHERIT);

    if (!lines.isEmpty()) {
      refresh(lines);
    }
  }
  
  /**
   * Clear the lines.
   */
  public void clear() {
    mesh.getPoints().clear();
    mesh.getFaces().clear();
  }

  /**
   * Refresh with the given set of lines.
   *
   * @param lines new lines
   */
  public synchronized final void refresh(List<Line3D> lines) {
    mesh.getPoints().clear();
    mesh.getFaces().clear();
    // dummy TexCoords stay the same
    if (lines.isEmpty()) {
      return;
    }

    for (Line3D line : lines) {
      int n = mesh.getPoints().size() / 18;  // 3 * 6 = 18 floats per line point
      // Add each point. Shift each direction by +width/2 and -width/2
      // These extra points allow us to build triangles later
      for (Point3D point : line.pts) {
        mesh.getPoints().addAll((float) point.getX() - width / 2.0f, (float) point.getY(), (float) point.getZ());
        mesh.getPoints().addAll((float) point.getX() + width / 2.0f, (float) point.getY(), (float) point.getZ());
        mesh.getPoints().addAll((float) point.getX(), (float) point.getY() - width / 2.0f, (float) point.getZ());
        mesh.getPoints().addAll((float) point.getX(), (float) point.getY() + width / 2.0f, (float) point.getZ());
        mesh.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ() - width / 2.0f);
        mesh.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ() + width / 2.0f);
      }
      // Now generate trianglestrips for each line segment
//      for (int i = 2 + n * 2; i < (n + line.size()) * 2; i += 2) {  //add each segment
//        // Vertices wound counter-clockwise which is the default front face of any Triange
//        // These triangles live on the frontside of the line facing the camera
//        mesh.getFaces().addAll(i, 0, i - 2, 0, i + 1, 0);     // 2, 0, 3
//        mesh.getFaces().addAll(i + 1, 0, i - 2, 0, i - 1, 0); // 3, 0, 1
//      }
      for (int i = 6 + n*6; i < (n + line.size()) * 6; i += 6) {
        mesh.getFaces().addAll(i, 0, i - 6, 0, i + 1, 0);       // 6, 0, 7
        mesh.getFaces().addAll(i + 1, 0, i - 6, 0, i - 5, 0);   // 7, 0, 1
        mesh.getFaces().addAll(i + 2, 0, i - 4, 0, i + 3, 0);   // 8, 2, 9
        mesh.getFaces().addAll(i + 3, 0, i - 4, 0, i - 3, 0);   // 9, 2, 3
        mesh.getFaces().addAll(i + 4, 0, i - 2, 0, i + 5, 0);   // 10, 4, 11
        mesh.getFaces().addAll(i + 5, 0, i - 2, 0, i - 1, 0);   // 11, 4, 5
      }
    }
  }
}
