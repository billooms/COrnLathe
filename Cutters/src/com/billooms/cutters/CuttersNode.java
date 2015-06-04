package com.billooms.cutters;

import com.billooms.cornlatheprefs.COrnLathePrefs;
import com.billooms.profiles.Profiles;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.actions.MoveUpAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Node for Cutters.
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
public class CuttersNode extends AbstractNode implements PropertyChangeListener {

  private static final COrnLathePrefs prefs = Lookup.getDefault().lookup(COrnLathePrefs.class);
  
  /** Local copy of the cutter manager. */
  private final Cutters cutterMgr;

  /**
   * Create a new CuttersNode for the given cutterMgr.
   *
   * @param cutterMgr profile manager
   */
  public CuttersNode(Cutters cutterMgr) {
    super(Children.create(new CuttersChildFactory(cutterMgr), true), Lookups.singleton(cutterMgr));
    this.cutterMgr = cutterMgr;
    this.setName("Cutters");
    this.setDisplayName(cutterMgr.toString());
    this.setIconBaseWithExtension("com/billooms/cutters/Cutter16.png");

    cutterMgr.addPropertyChangeListener((PropertyChangeListener) this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CuttersNode.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When profiles change, simply update the DisplayName
    this.setDisplayName(cutterMgr.toString());
  }

  @Override
  public Action[] getActions(boolean context) {
    Action[] defaults = super.getActions(context);
    int numAdd = prefs.useLibrary() ? 2 : 1;
    Action[] newActions = new Action[defaults.length + numAdd];
    newActions[0] = new AddAction();
    if (prefs.useLibrary()) {
      newActions[1] = new AddLibAction();
       SystemAction.get(MoveUpAction.class);
    }
    System.arraycopy(defaults, 0, newActions, numAdd, defaults.length);
    return newActions;
  }

  /** Nested inner class for action adding a cutter. */
  private class AddAction extends AbstractAction {

    /** Create the AddAction */
    public AddAction() {
      putValue(NAME, "Add Cutter");
    }

    /**
     * Copy the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      Cutters mgr = getLookup().lookup(Cutters.class);
      String str = (String) JOptionPane.showInputDialog(
          null,
          "Enter display name for new cutter:",
          "New Cutter Dialog",
          JOptionPane.PLAIN_MESSAGE,
          null,
          null,
          "New Name");
      if ((str != null) && (str.length() > 0)) {
        mgr.add(new Cutter(str, cutterMgr.getProfileMgr()));
      }
    }
  }

  /** Nested inner class for action adding a cutter from the library. */
  private class AddLibAction extends AbstractAction {
  
    /** Create the AddAction */
    public AddLibAction() {
      putValue(NAME, "Add Cutter from Library");
    }

    /**
     * Copy the selected point.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      System.out.println("path: " + prefs.getLibPath());

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      try {
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(EntityCatalog.getDefault());
        Document doc = db.parse(new File(prefs.getLibPath()));  // parse the xml file
        Element topElement = doc.getDocumentElement();
        NodeList cutterList = topElement.getElementsByTagName("Cutters");
        Profiles profMgr = new Profiles();    // get the default (built-in) profiles
        Cutters cutLibMgr = new Cutters((Element) cutterList.item(0), profMgr); // this is the cutter library

        String[] selections = new String[cutLibMgr.size()];
        for (int i = 0; i < cutLibMgr.size(); i++) {
          selections[i] = cutLibMgr.get(i).toString();
        }
        String str = (String) JOptionPane.showInputDialog(    // prompt the user to make a selection
            null, 
            "Select the desired cutter", 
            "Cutter Library", 
            JOptionPane.PLAIN_MESSAGE,
            null, 
            selections, 
            selections[0]);
        if ((str != null) && (str.length() > 0)) {
          Cutters cutMgr = getLookup().lookup(Cutters.class);   // The active cutter manager
          cutMgr.add(new Cutter(cutLibMgr.getCutter(str.split(" ")[0]))); // make a copy of the library cutter and add it
        }
      } catch (ParserConfigurationException | SAXException | IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }
}
