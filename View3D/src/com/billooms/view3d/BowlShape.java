package com.billooms.view3d;

import com.billooms.cutpoints.surface.Surface;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * 3D Bowl shape made from the given surface.
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
public class BowlShape extends Group {

  /** This is the mesh that defines the bowl. */
  private final TriangleMesh mesh = new TriangleMesh();
  /** This is the mesh that defines layer1. */
  private final TriangleMesh mesh1 = new TriangleMesh();
  /** This is the mesh that defines layer2. */
  private final TriangleMesh mesh2 = new TriangleMesh();
  /** To add a TriangleMesh to a 3D scene you need a MeshView container object. */
  private final MeshView meshView = new MeshView(mesh);
  /** To add a TriangleMesh to a 3D scene you need a MeshView container object. */
  private final MeshView meshView1 = new MeshView(mesh1);
  /** To add a TriangleMesh to a 3D scene you need a MeshView container object. */
  private final MeshView meshView2 = new MeshView(mesh2);
  /** Material used for the bowl. */
  private final static PhongMaterial bowlMaterial = new PhongMaterial();
  /** Material used for the bowl layer1. */
  private final static PhongMaterial bowlMaterial1 = new PhongMaterial();
  /** Material used for the bowl layer2. */
  private final static PhongMaterial bowlMaterial2 = new PhongMaterial();

  /**
   * Construct the Bowl from the given Surface.
   *
   * @param surface surface
   */
  public BowlShape(Surface surface) {
    bowlMaterial.setDiffuseColor(Color.PERU);	  // defaults are overwritten in refresh()
    bowlMaterial.setSpecularColor(Color.WHITE);   // seems to be shinier with white
    bowlMaterial1.setSpecularColor(Color.WHITE);   // seems to be shinier with white
    bowlMaterial2.setSpecularColor(Color.WHITE);   // seems to be shinier with white

    //The MeshView allows you to control how the TriangleMesh is rendered
//    meshView.setDrawMode(DrawMode.LINE); // show lines only 
    meshView.setDrawMode(DrawMode.FILL);
    meshView.setCullFace(CullFace.NONE); // no culling
//    meshView.setCullFace(CullFace.BACK);
    meshView.setMaterial(bowlMaterial);
    this.getChildren().add(meshView);

    meshView1.setDrawMode(DrawMode.FILL);
    meshView1.setCullFace(CullFace.NONE); // no culling
    meshView1.setMaterial(bowlMaterial1);
    this.getChildren().add(meshView1);

    meshView2.setDrawMode(DrawMode.FILL);
    meshView2.setCullFace(CullFace.NONE); // no culling
    meshView2.setMaterial(bowlMaterial2);
    this.getChildren().add(meshView2);

    //for now we'll just make an empty texCoordinate group
    mesh.getTexCoords().setAll(0, 0);
    mesh1.getTexCoords().setAll(0, 0);
    mesh2.getTexCoords().setAll(0, 0);

    if (surface != null) {
      refresh(surface);
    }
  }

  /**
   * Refresh the meshes from the surface.
   *
   * When layers are present, there are 3 different meshes -- one for each
   * layer. The mesh points are the same for all 3 meshes. The triangular faces
   * are put into one of the three meshes. The distance of all 3 points of the
   * face is measured from an uncut surface. If all 3 points are greater than
   * the layer thickness, then they are put into the corresponding mesh.
   *
   * @param surface surface
   */
  public synchronized final void refresh(Surface surface) {
    try {
      mesh.getPoints().clear();
      mesh.getFaces().clear();
      mesh1.getPoints().clear();
      mesh1.getFaces().clear();
      mesh2.getPoints().clear();
      mesh2.getFaces().clear();
      // dummy TexCoords stay the same
      if (surface == null) {
        return;
      }

      java.awt.Color c = surface.getOutline().getColor();
      bowlMaterial.setDiffuseColor(Color.rgb(c.getRed(), c.getGreen(), c.getBlue()));
//    bowlMaterial.setSpecularColor(Color.rgb(c.getRed(), c.getGreen(), c.getBlue()));  // leave it white
      java.awt.Color c1 = surface.getOutline().getColor1();
      bowlMaterial1.setDiffuseColor(Color.rgb(c1.getRed(), c1.getGreen(), c1.getBlue()));
      java.awt.Color c2 = surface.getOutline().getColor2();
      bowlMaterial2.setDiffuseColor(Color.rgb(c2.getRed(), c2.getGreen(), c2.getBlue()));

      Point3D[][] uncutSurface = null;      // uncut surface used for determining colors
      if (surface.isRender() && surface.getOutline().usesLayers()) {
        uncutSurface = surface.makeCleanSurface();
      }

      int nCurvePts = surface.getLength();
      int nSects = surface.numSectors();
      // fill the array with x,y,z coordinates of every point
      // first go up the curve (the j value)
      // then go to the next sector (the i value)
      for (int i = 0; i < nSects; i++) {
        for (int j = 0; j < nCurvePts; j++) {
          mesh.getPoints().addAll(
              (float) surface.pts[j][i].getX(),
              (float) surface.pts[j][i].getY(),
              (float) surface.pts[j][i].getZ());
        }
      }
      if (surface.isRender() && surface.getOutline().usesLayers()) {    // make copies into mesh1 and mesh2 for colors
        mesh1.getPoints().addAll(mesh.getPoints());
        mesh2.getPoints().addAll(mesh.getPoints());
      }

      // fill the array with triangle definitions 
      // from bottom to the top on each sector
      double dist1 = surface.getOutline().getLayer1();        // depth of first color boundary
      double dist2 = surface.getOutline().getLayer1plus2();   // depth of second color boundary
      for (int i = 0; i < nSects; i++) {
        int k = (i + 1) % nSects;	    // wrap around back to zero for the last sector
        for (int j = 0; j < nCurvePts - 1; j++) {
          if (surface.isInside()) {     // inside wraps Counterclockwise on the inside
            if (surface.isRender() && surface.getOutline().usesLayers()) {    // use layers?
              if (surface.pts[j][i].distance(uncutSurface[j][i]) > dist2
                  && surface.pts[j][k].distance(uncutSurface[j][k]) > dist2
                  && surface.pts[j + 1][i].distance(uncutSurface[j + 1][i]) > dist2) {
                mesh2.getFaces().addAll( // 1st triangle on mesh2 for layer2
                    i * nCurvePts + j, 0,
                    k * nCurvePts + j, 0,
                    i * nCurvePts + j + 1, 0);
              } else if (surface.pts[j][i].distance(uncutSurface[j][i]) > dist1
                  && surface.pts[j][k].distance(uncutSurface[j][k]) > dist1
                  && surface.pts[j + 1][i].distance(uncutSurface[j + 1][i]) > dist1) {
                mesh1.getFaces().addAll( // 1st triangle on mesh1 for layer1
                    i * nCurvePts + j, 0,
                    k * nCurvePts + j, 0,
                    i * nCurvePts + j + 1, 0);
              } else {
                mesh.getFaces().addAll( // 1st triangle on mesh for outer surface
                    i * nCurvePts + j, 0,
                    k * nCurvePts + j, 0,
                    i * nCurvePts + j + 1, 0);
              }
              if (surface.pts[j + 1][i].distance(uncutSurface[j + 1][i]) > dist2
                  && surface.pts[j][k].distance(uncutSurface[j][k]) > dist2
                  && surface.pts[j + 1][k].distance(uncutSurface[j + 1][k]) > dist2) {
                mesh2.getFaces().addAll( // 2nd triangle on mesh2 for layer2
                    i * nCurvePts + j + 1, 0,
                    k * nCurvePts + j, 0,
                    k * nCurvePts + j + 1, 0);
              } else if (surface.pts[j + 1][i].distance(uncutSurface[j + 1][i]) > dist1
                  && surface.pts[j][k].distance(uncutSurface[j][k]) > dist1
                  && surface.pts[j + 1][k].distance(uncutSurface[j + 1][k]) > dist1) {
                mesh1.getFaces().addAll( // 2nd triangle on mesh1 for layer1
                    i * nCurvePts + j + 1, 0,
                    k * nCurvePts + j, 0,
                    k * nCurvePts + j + 1, 0);
              } else {
                mesh.getFaces().addAll( // 2nd triangle on mesh for outer surface
                    i * nCurvePts + j + 1, 0,
                    k * nCurvePts + j, 0,
                    k * nCurvePts + j + 1, 0);
              }
            } else {        // no layers are used
              mesh.getFaces().addAll(
                  i * nCurvePts + j, 0,
                  k * nCurvePts + j, 0,
                  i * nCurvePts + j + 1, 0);
              mesh.getFaces().addAll(
                  i * nCurvePts + j + 1, 0,
                  k * nCurvePts + j, 0,
                  k * nCurvePts + j + 1, 0);
            }
          } else {        // outside wraps Counterclockwise on the outside
            if (surface.isRender() && surface.getOutline().usesLayers()) {
              if (surface.pts[j][k].distance(uncutSurface[j][k]) > dist2
                  && surface.pts[j][i].distance(uncutSurface[j][i]) > dist2
                  && surface.pts[j + 1][k].distance(uncutSurface[j + 1][k]) > dist2) {
                mesh2.getFaces().addAll( // 1st triangle on mesh2 for layer2
                    k * nCurvePts + j, 0,
                    i * nCurvePts + j, 0,
                    k * nCurvePts + j + 1, 0);
              } else if (surface.pts[j][k].distance(uncutSurface[j][k]) > dist1
                  && surface.pts[j][i].distance(uncutSurface[j][i]) > dist1
                  && surface.pts[j + 1][k].distance(uncutSurface[j + 1][k]) > dist1) {
                mesh1.getFaces().addAll( // 1st triangle on mesh1 for layer1
                    k * nCurvePts + j, 0,
                    i * nCurvePts + j, 0,
                    k * nCurvePts + j + 1, 0);
              } else {
                mesh.getFaces().addAll( // 1st triangle on mesh for outer surface
                    k * nCurvePts + j, 0,
                    i * nCurvePts + j, 0,
                    k * nCurvePts + j + 1, 0);
              }
              if (surface.pts[j + 1][k].distance(uncutSurface[j + 1][k]) > dist2
                  && surface.pts[j][i].distance(uncutSurface[j][i]) > dist2
                  && surface.pts[j + 1][i].distance(uncutSurface[j + 1][i]) > dist2) {
                mesh2.getFaces().addAll( // 2nd triangle on mesh2 for layer2
                    k * nCurvePts + j + 1, 0,
                    i * nCurvePts + j, 0,
                    i * nCurvePts + j + 1, 0);
              } else if (surface.pts[j + 1][k].distance(uncutSurface[j + 1][k]) > dist1
                  && surface.pts[j][i].distance(uncutSurface[j][i]) > dist1
                  && surface.pts[j + 1][i].distance(uncutSurface[j + 1][i]) > dist1) {
                mesh1.getFaces().addAll( // 2nd triangle on mesh1 for layer1
                    k * nCurvePts + j + 1, 0,
                    i * nCurvePts + j, 0,
                    i * nCurvePts + j + 1, 0);
              } else {
                mesh.getFaces().addAll( // 2nd triangle on mesh for outer surface
                    k * nCurvePts + j + 1, 0,
                    i * nCurvePts + j, 0,
                    i * nCurvePts + j + 1, 0);
              }
            } else {
              mesh.getFaces().addAll( // Counterclockwise on the outside
                  k * nCurvePts + j, 0,
                  i * nCurvePts + j, 0,
                  k * nCurvePts + j + 1, 0);
              mesh.getFaces().addAll(
                  k * nCurvePts + j + 1, 0,
                  i * nCurvePts + j, 0,
                  i * nCurvePts + j + 1, 0);
            }
          }
        }
      }
    } catch (Exception e) {
      // ignore ArrayIndexOutOfBoundsException and NullPointerException
    }
  }

}
