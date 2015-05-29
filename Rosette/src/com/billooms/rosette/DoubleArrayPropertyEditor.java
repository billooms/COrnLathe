package com.billooms.rosette;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.*;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 * InplaceEditor for editing a string of numbers representing an array of double.
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
public class DoubleArrayPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {
  
  @Override
  public void attachEnv(PropertyEnv env) {
    env.registerInplaceEditorFactory(this);
  }

  @Override
  public InplaceEditor getInplaceEditor() {
    return new DoubleArrayInplaceEditor();
  }
  
  private static class DoubleArrayInplaceEditor implements InplaceEditor {

    private PropertyEditor editor;
    private PropertyModel model;
    private final JTextField textArea;
        
    public DoubleArrayInplaceEditor() {
      textArea = new JTextField("initial value");
    }
    
    @Override
    public void connect(PropertyEditor pe, PropertyEnv env) {
      this.editor = pe;
      this.reset();
    }

    @Override
    public JComponent getComponent() {
      return this.textArea;
    }

    @Override
    public void clear() {
      this.editor = null;
      this.model = null;
    }

    @Override
    public Object getValue() {
      return new DoubleArray(textArea.getText());
    }

    @Override
    public void setValue(Object o) {
      textArea.setText(o.toString());
    }

    @Override
    public boolean supportsTextEntry() {
      return false;
    }

    @Override
    public void reset() {
      this.setValue("");
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
      return c == textArea;
    }
    
  }
  
}
