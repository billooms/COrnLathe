package com.billooms.gcodeoutput;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;
import javafx.geometry.Point3D;

/**
 * Class for controlling connections to LinuxCNC.
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
public class LinuxCNCConnection {

  /** Port for LinuxCNCrsh. */
  private final static int PORT = 5007;
  /** Time-out for connection is 5 seconds. */
  private final static int CONNECT_TIMEOUT = 5;

  private Socket socket;
  private InputStream instream;
  private OutputStream outstream;
  private Scanner in;
  private PrintWriter out;
  private boolean connected = false;

  /**
   * Connection to LinuxCNC.
   *
   * @param ipAddress IP address of the computer running LinuxCNC or "" for no
   * connection
   */
  public LinuxCNCConnection(String ipAddress) {
    if (ipAddress.equals("")) {
      return;
    }
    try {
      SocketAddress sa = new InetSocketAddress(ipAddress, PORT);
      socket = new Socket();
      socket.connect(sa, CONNECT_TIMEOUT * 1000);
      instream = socket.getInputStream();
      outstream = socket.getOutputStream();
      in = new Scanner(instream);
      out = new PrintWriter(outstream, true);

      String reply = sendCommand("hello EMC x 1");	// This should establish a connection
      if (reply.contains("EMCNETSVR")) {            // Check for the correct reply
        System.out.println("LinuxCNC connected to " + ipAddress);
        connected = true;
        sendCommand("set echo off");		// don't echo the command in the future
      } else {
        this.close();
      }
    } catch (Exception e) {
      this.close();
    }
  }

  /**
   * Send a LinuxCNCrsh command. Note: this assumes that a connection has been
   * established!
   *
   * @param s Command string
   * @return Response to the command
   */
  private String sendCommand(String s) {
    out.println(s);
    return in.nextLine();
  }

  /**
   * Check if LinuxCNC was connected OK.
   *
   * @return true=connected OK
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Quit the connection (if it was established) and close the connection.
   */
  public final void close() {
    if (connected) {
      out.println("quit");
    }
    try {
      in.close();
      instream.close();
      out.close();
      outstream.close();
      socket.close();
    } catch (Exception ex) {
    }
    connected = false;
    System.out.println("LinuxCNC connection closed");
  }

  /**
   * Get the relative actual position of LinuxCNC.
   *
   * @return XZC are in x,y,z respectively
   */
  public Point3D getPosition() {
    if (!connected) {
      return Point3D.ZERO;	// return 0,0,0 if no connection
    }
    String str = sendCommand("get rel_act_pos");
    String[] strs = str.split(" ");
    double x = Double.parseDouble(strs[1]);
    double z = Double.parseDouble(strs[3]);
    double c = Double.parseDouble(strs[6]);
    return new Point3D(x, z, c);
  }
}
