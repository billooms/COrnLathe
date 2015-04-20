package com.billooms.cutpoints;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
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
public class CutPointPaletteFactory {

  public static PaletteController getCutPointPalette() {
    PaletteController controller = null;
    try {
      controller = PaletteFactory.createPalette(
          "MyPalette",
          new PaletteActions() {
            @Override
            public Action[] getImportActions() {
              return null;
            }

            @Override
            public Action[] getCustomPaletteActions() {
              return null;
            }

            @Override
            public Action[] getCustomCategoryActions(Lookup category) {
              return null;
            }

            @Override
            public Action[] getCustomItemActions(Lookup item) {
              return null;
            }

            @Override
            public Action getPreferredAction(Lookup item) {
              return null;
            }
          });
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return controller;
  }
}
