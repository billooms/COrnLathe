package com.billooms.cornfile;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.ERROR_MESSAGE;
import static org.openide.NotifyDescriptor.WARNING_MESSAGE;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ErrorHandler for use when parsing the XML file.
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
public class ParseErrorHandler implements ErrorHandler {

  /** Local copy of the COrnFileDataObject. */
  private final COrnFileDataObject fileDObj;
  /** Cumulative message. */
  private String message = "";
  /** True: error or fatal error occurred. */
  private boolean error = false;

  /**
   * Construct a new ErrorHandler for the given COrnFileDataObject.
   *
   * @param obj given COrnFileDataObject
   */
  public ParseErrorHandler(COrnFileDataObject obj) {
    super();
    this.fileDObj = obj;
  }

  /**
   * Clear the message string.
   */
  protected void clear() {
    message = "";
    error = false;
  }

  /**
   * Show any messages that occurred.
   *
   * @return true: errors or fatal errors occurred
   */
  protected boolean showMessages() {
    if (message.isEmpty()) {
      return false;
    } else {
      NotifyDescriptor d = new NotifyDescriptor.Message(
          message,
          (error ? ERROR_MESSAGE : WARNING_MESSAGE));
      DialogDisplayer.getDefault().notify(d);
    }
    return error;
  }

  /**
   * Generate a string of information from the given ParseException.
   *
   * @param spe given ParseException
   * @return string of information
   */
  private String getInfo(SAXParseException spe) {
    if (message.isEmpty()) {
      message += "File: " + fileDObj.getPrimaryFile().getNameExt() + "\n";
    }
    String info = "Line=" + spe.getLineNumber() + ": " + spe.getMessage();
    return info;
  }

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    message += "Warning: " + getInfo(exception) + "\n";
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    message += "ERROR: " + getInfo(exception) + "\n";
    error = true;
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    message += "FATAL ERROR: " + getInfo(exception) + "\n";
    error = true;
  }

}
