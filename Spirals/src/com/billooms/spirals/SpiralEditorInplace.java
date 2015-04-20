package com.billooms.spirals;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.Lookup;

/**
 * InplaceEditor for editing the spirals.
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
public class SpiralEditorInplace extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {

  /**
   * This method is called by the property sheet to pass the environment to the
   * property editor.
   *
   * @param env
   */
  @Override
  public void attachEnv(PropertyEnv env) {
    env.registerInplaceEditorFactory(this);
  }

  /**
   * Fetch or create an inplace editor instance.
   *
   * @return An inplace editor instance
   */
  @Override
  public InplaceEditor getInplaceEditor() {
    return new ProfileInplaceEditor();
  }

  private static class ProfileInplaceEditor implements InplaceEditor {

    private PropertyEditor editor;
    private PropertyModel model;
    private JComboBox<String> spiralCombo;
    private final SpiralMgr spiralMgr;

    public ProfileInplaceEditor() {
      this.spiralCombo = new JComboBox<>();

      spiralMgr = Lookup.getDefault().lookup(SpiralMgr.class);
      spiralCombo.removeAllItems();
      spiralMgr.getAllDisplayNames().stream().forEach((s) -> {
        spiralCombo.addItem(s);
      });
    }

    @Override
    public void connect(PropertyEditor pe, PropertyEnv env) {
      this.editor = pe;
      this.reset();
    }

    @Override
    public JComponent getComponent() {
      return this.spiralCombo;
    }

    @Override
    public void clear() {
      this.editor = null;
      this.model = null;
    }

    @Override
    public Object getValue() {
      return spiralMgr.get(spiralCombo.getSelectedIndex());
    }

    @Override
    public void setValue(Object o) {
      spiralCombo.setSelectedIndex(spiralMgr.indexOf(((SpiralStyle) o).getName()));
    }

    @Override
    public boolean supportsTextEntry() {
      return false;
    }

    @Override
    public void reset() {
      SpiralStyle s = (SpiralStyle) this.editor.getValue();
      this.setValue(s);
    }

    @Override
    public void addActionListener(ActionListener al) {
    }

    @Override
    public void removeActionListener(ActionListener al) {
    }

    @Override
    public KeyStroke[] getKeyStrokes() {
      return new KeyStroke[0];
    }

    @Override
    public PropertyEditor getPropertyEditor() {
      return this.editor;
    }

    @Override
    public PropertyModel getPropertyModel() {
      return this.model;
    }

    @Override
    public void setPropertyModel(PropertyModel pm) {
      this.model = pm;
    }

    @Override
    public boolean isKnownComponent(Component c) {
      return c == this.spiralCombo || this.spiralCombo.isAncestorOf(c);
    }

  }

}
