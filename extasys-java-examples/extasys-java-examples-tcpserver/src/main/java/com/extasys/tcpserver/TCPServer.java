/*Copyright (c) 2008 Nikos Siatras

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NonINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTIon OF ConTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN ConNECTIon WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.*/

package com.extasys.tcpserver;

import java.net.InetAddress;
import java.nio.charset.Charset;

import com.extasys.DataFrame;
import com.extasys.network.tcp.server.ExtasysTCPServer;
import com.extasys.network.tcp.server.listener.TCPClientConnection;
import com.extasys.network.tcp.server.listener.TCPListener;
import com.extasys.network.tcp.server.listener.exceptions.ClientIsDisconnectedException;
import com.extasys.network.tcp.server.listener.exceptions.OutgoingPacketFailedException;

/**
 *
 * @author Nikos Siatras
 */
public class TCPServer extends ExtasysTCPServer{

     private TCPListener fMyTCPListener;

     private final String fMessageSplitter = "#SPLITTER#";

     private Charset fCharset = Charset.forName("UTF-8");

     public TCPServer(String name, String description, InetAddress listenerIP, int port, int maxConnections, int connectionsTimeOut, int corePoolSize, int maximumPoolSize){
          super(name, description, corePoolSize, maximumPoolSize);

          try {
               // Add listener with message collector.
               fMyTCPListener = this.addListener("My listener", listenerIP, 5000, maxConnections, 8192, connectionsTimeOut, 100, fMessageSplitter);
          } catch (Exception ex) {
          }
     }

     @Override
     public void onDataReceive(TCPClientConnection sender, DataFrame data) {

          try {
               String incomingDataStr = new String(data.getBytes(), fCharset);
               // System.out.println("Data received: " + incomingDataStr);
               sender.SendData(incomingDataStr + fMessageSplitter);
          } catch (ClientIsDisconnectedException | OutgoingPacketFailedException ex) {

          }
     }

     @Override
     public void onClientConnect(TCPClientConnection client) {

          // New client connected.
          client.setName(client.getIPAddress()); // Set a name for this client if you want to.
          System.out.println(client.getIPAddress() + " connected.");
          System.out.println("Total clients connected: " + super.getCurrentConnectionsNumber());
     }

     @Override
     public void onClientDisconnect(TCPClientConnection client) {

          // Client disconnected.
          System.out.println(client.getIPAddress() + " disconnected.");
     }
}
