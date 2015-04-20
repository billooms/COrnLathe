package com.billooms.cornfile;

import com.billooms.clclass.CLUtilities;
import com.billooms.clclass.CLclass;
import com.billooms.comment.Comment;
import com.billooms.cutpoints.CutPoints;
import com.billooms.gcodeoutput.GCodeTopComponent;
import com.billooms.gcodeoutput.GControls;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.ERROR_MESSAGE;
import static org.openide.NotifyDescriptor.INFORMATION_MESSAGE;
import static org.openide.NotifyDescriptor.WARNING_MESSAGE;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * MultiDataObject for a COrnLathe file.
 *
 * This is kept very lightweight. I don't want to generate a big tree while
 * browsing files in the Favorites window. The main purpose here is to provide
 * for opening the xml file in a text editor.
 *
 * To load the file for anything except text editing, one must explicitly use
 * the LoadData action which calls buildNewDataNode() and causes the xml file to
 * be read and creates a COrnDataNode for viewing with the DataNavigator.
 *
 * NOTE: Project Properties must include ide Lexer to NetBeans Bridge or you
 * will get an error when inserting a blank line (and indent won't work).
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
@Messages({
  "LBL_COrn_LOADER=COrnLathe files"
})
@MIMEResolver.NamespaceRegistration(
    displayName = "#LBL_COrn_LOADER",
    mimeType = "text/cornlathe+xml",
    elementName = "COrnLathe"
)
@DataObject.Registration(
    mimeType = "text/cornlathe+xml",
    iconBase = "com/billooms/cornfile/COrnLathe16.png",
    displayName = "#LBL_COrn_LOADER",
    position = 300
)
@ActionReferences({
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
      position = 100,
      separatorAfter = 200
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
      position = 300
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
      position = 400,
      separatorAfter = 500
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
      position = 600
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
      position = 700,
      separatorAfter = 800
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
      position = 900,
      separatorAfter = 1000
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
      position = 1100,
      separatorAfter = 1200
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
      position = 1300
  ),
  @ActionReference(
      path = "Loaders/text/cornlathe+xml/Actions",
      id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
      position = 1400
  )
})
public class COrnFileDataObject extends MultiDataObject implements PropertyChangeListener {

  /** A separate DataNOde for exploring. */
  private COrnDataNode dataNode = null;
  /** The XML DOM Document. */
  private Document doc = null;
  /** The container for the top object */
  private COrnTopObject topObject = null;

  private static ExplorerManager em = null;   // all instances share one ExplorerManager
  private final InstanceContent ic = new InstanceContent();
  private final ProxyLookup proxy;
  private final ParseErrorHandler errHandler;
  private final FileChangeListener fcl = new MyFileListener();
  private GControls gControlPanel = null;

  /**
   * Construct a new COrnFileDataObject.
   *
   * @param pf
   * @param loader
   * @throws DataObjectExistsException
   * @throws IOException
   */
  public COrnFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    registerEditor("text/cornlathe+xml", true);

    errHandler = new ParseErrorHandler(this);
    proxy = new ProxyLookup(new AbstractLookup(ic), super.getLookup());
  }

  @Override
  protected int associateLookup() {
    return 1;		// the highest supported fileVersion
  }

  @Override
  public Lookup getLookup() {
    return proxy;	// the original lookup + InstanceContent
  }

  @MultiViewElement.Registration(
      displayName = "#LBL_COrn_EDITOR",
      iconBase = "com/billooms/cornfile/COrnLathe16.png",
      mimeType = "text/cornlathe+xml",
      persistenceType = TopComponent.PERSISTENCE_NEVER,
      preferredID = "COrn",
      position = 1000
  )
  @Messages("LBL_COrn_EDITOR=Source")
  public static MultiViewEditorElement createEditor(Lookup lkp) {
    return new MultiViewEditorElement(lkp);
  }

  /**
   * Get the COrnTopObject container which holds the top objects.
   *
   * @return COrnTopObject which holds the top objects
   */
  public COrnTopObject getTopObject() {
    return topObject;
  }

  /**
   * Get the list of top objects.
   *
   * @return list of top objects
   */
  public ArrayList<CLclass> getTopObjects() {
    return topObject.getTopObjects();
  }

  /**
   * Build a new COrnDataNode for the primary file.
   *
   * This is usually called by the LoadData action or by the FileChangeListener.
   *
   * @param reload forces a re-load (discard changes and read the file in again)
   */
  public synchronized void buildNewDataNode(boolean reload) {
    if (em == null) {	  // only do this once
      em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
    }
    if (gControlPanel == null) {
      gControlPanel = ((GCodeTopComponent) WindowManager.getDefault().findTopComponent("GCodeTopComponent")).getLookup().lookup(GControls.class);
    }

    if (reload || (doc == null)) {	  // doc will be null if the file hasn't yet been read
      if (reload) {
        forgetChanges();    // forget any changes
      }
      // Parse the file and generate the top CLclass objects
      parseFile();
      if (doc == null) {
        em.setRootContext(Node.EMPTY);	  // there was some problem with the file
        return;
      }

      // Create the COrnDataNode
      COrnDataNode newDataNode = new COrnDataNode(this);
      if (dataNode == null) {   // only do this the first time
        dataNode = newDataNode;
        this.getPrimaryFile().addFileChangeListener(fcl);
      } else {
        dataNode = newDataNode;
      }

      // set the root context of the DataNavigator to this new COrnDataNode
      CutPoints lookup = dataNode.getLookup().lookup(CutPoints.class);
      em.setRootContext(dataNode);

      // add propertyChangeListeners to all the top elements
      topObject.getTopObjects().stream().forEach((topObj) -> {
        topObj.addPropertyChangeListener(this);
      });
    } else {	  // doc is not null, so it's already been read
      // set the root context of the DataNavigator to the original COrnDataNode 
      CutPoints lookup = dataNode.getLookup().lookup(CutPoints.class);
      em.setRootContext(dataNode);
    }

    // If there is a comment, display it for the user
    Comment c = (Comment) topObject.getClass(Comment.class);
    if (!c.isEmpty()) {
      NotifyDescriptor d = new NotifyDescriptor.Message(
          c.getText(),
          INFORMATION_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
    }
    StatusDisplayer.getDefault().setStatusText("Loaded file: " + this.getPrimaryFile().getNameExt());
  }

  /**
   * Parse the file into a DOM Document and extract the first layer of elements.
   *
   * This builds objects for all the nodes in the document.
   */
  private void parseFile() {
    InputStream inputStream = null;
    try {
      inputStream = getPrimaryFile().getInputStream();
      errHandler.clear();   // clear any old messages
      doc = XMLUtil.parse(new InputSource(inputStream), true, true, errHandler, EntityCatalog.getDefault());
      if (errHandler.showMessages()) {
        doc = null;
        return;	  // the finally below will close inputStream
      }

      // Get the topElement and look for first layer of elements.
      Element topElement = doc.getDocumentElement();
      double fileVersion = CLUtilities.getDouble(topElement, "version", 99.0);

      double thisVersion = Double.parseDouble(NbBundle.getMessage(COrnFileDataObject.class, "COrnLathe_Version"));
      if (fileVersion > thisVersion) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "XML file was written by a newer version of software.\nFound version " + fileVersion,
            ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        doc = null;
        return;	  // the finally below will close inputStream
      }
      if (fileVersion < thisVersion) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "XML file was written by an older version of software.\nFound version " + fileVersion + "\n",
            ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        doc = null;
        return;	  // the finally below will close inputStream
      }
      topObject = new COrnTopObject(topElement);
    } catch (FileNotFoundException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IOException | SAXException ex) {
      Exceptions.printStackTrace(ex);
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);

      }
    }
  }

  /**
   * Call this to indicate that the data has been modified and it needs to be
   * saved.
   */
  private void modified() {
    if (getLookup().lookup(MySavable.class) == null) {
      ic.add(new MySavable());
    }
    if (gControlPanel != null) {
      gControlPanel.notifyChange(true);
    }
  }

  /**
   * Call this to indicate that you want to forget any changes.
   */
  private void forgetChanges() {
    MySavable sav = getLookup().lookup(MySavable.class);
    if (sav != null) {
      sav.forget();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("COrnFileDataObject.propertyChange: " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());

    String propName = evt.getPropertyName();
    if (propName.contains("Ignore") || propName.contains("Visible")) {
      return;	  // these are not "real" changes
    }
    if (isModified()) {	  // check if there are Editor changes
      NotifyDescriptor d = new NotifyDescriptor.Message(
          "Multiple edits on file: " + COrnFileDataObject.this.getPrimaryFile().getNameExt() + "\n"
          + "There are unsaved changes made by the editor.\n"
          + "Go to the Editor window and save changes\n"
          + "or close the Editor and discard changes",
          WARNING_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
    }
    modified();
  }

  /**
   * Saver for the data.
   */
  protected class MySavable extends AbstractSavable {

    /** Construct a new saver and register it. */
    public MySavable() {
      register();
    }

    @Override
    protected String findDisplayName() {
      return getPrimaryFile().getNameExt();
    }

    @Override
    protected synchronized void handleSave() throws IOException {
      if (COrnFileDataObject.this.isModified()) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Multiple edits on file: " + COrnFileDataObject.this.getPrimaryFile().getNameExt() + "\n"
            + "There are unsaved changes made by the editor.\n"
            + "Go to the Editor window and save changes\n"
            + "or close the Editor and discard changes",
            WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        return;
      }

      COrnFileDataObject.this.getPrimaryFile().removeFileChangeListener(fcl);

      try (OutputStream outputStream = getPrimaryFile().getOutputStream();
          PrintWriter out = new PrintWriter(outputStream)) {
        topObject.writeXML(out);
      }

      COrnFileDataObject.this.getPrimaryFile().addFileChangeListener(fcl);
      getFileDObj().ic.remove(this);
      unregister();
    }

    /** Remove this from the ic and unregister. */
    protected void forget() {
      getFileDObj().ic.remove(this);
      unregister();
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof MySavable) {
        MySavable m = (MySavable) other;
        return getFileDObj() == m.getFileDObj();
      }
      return false;
    }

    @Override
    public int hashCode() {
      return getFileDObj().hashCode();
    }

    /** Get the COrnFileDataObject associated with this saver. */
    private COrnFileDataObject getFileDObj() {
      return COrnFileDataObject.this;
    }

  }

  /**
   * Listen for file changes that might be caused by the MultiViewEditor.
   *
   * If there are changes, you know that the COrnDataNode is not current.
   */
  private class MyFileListener implements FileChangeListener {

    @Override
    public void fileFolderCreated(FileEvent fe) {
      // Do nothing
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
      // Do nothing
    }

    @Override
    public void fileChanged(FileEvent fe) {
      System.out.println("MyFileListener.fileChanged");
      if (getLookup().lookup(MySavable.class) != null) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Multiple edits on file: " + COrnFileDataObject.this.getPrimaryFile().getNameExt() + "\n"
            + "The file was changed by editor and saved.\n"
            + "All other edits were lost.",
            WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
      }
      synchronized (this) {
        COrnFileDataObject.this.buildNewDataNode(true);   // build it all over new when it changes
      }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
      System.out.println("MyFileListener.fileDeleted");
      if (getLookup().lookup(MySavable.class) != null) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Deleted file: " + COrnFileDataObject.this.getPrimaryFile().getNameExt() + " with un-saved changes.\n"
            + "The file was deleted.\n"
            + "All other edits were lost.",
            WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
      }
      synchronized (this) {
        doc = null;
        em.setRootContext(Node.EMPTY);
      }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
      System.out.println("MyFileListener.fileRenamed");
      if (getLookup().lookup(MySavable.class) != null) {
        NotifyDescriptor d = new NotifyDescriptor.Message(
            "Renaming file: " + COrnFileDataObject.this.getPrimaryFile().getNameExt() + " with un-saved changes.\n"
            + "The file was renamed.\n"
            + "All other edits were lost.",
            WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
      }
      synchronized (this) {
        COrnFileDataObject.this.buildNewDataNode(true);   // build it all over new when it changes
      }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
      // Do nothing
    }

  }
}
