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
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.*/

package com.extasys.network.tcp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.extasys.DataFrame;
import com.extasys.ExtasysThreadPool;
import com.extasys.network.tcp.server.listener.TCPClientConnection;
import com.extasys.network.tcp.server.listener.TCPListener;
import com.extasys.network.tcp.server.listener.exceptions.ClientIsDisconnectedException;
import com.extasys.network.tcp.server.listener.exceptions.OutgoingPacketFailedException;

/**
 *
 * @author Nikos Siatras
 */
public class ExtasysTCPServer{

     private final String fName;

     private final String fDescription;

     private final ArrayList fListeners = new ArrayList();

     public final ExtasysThreadPool fMyThreadPool;

     /**
      * Constructs an new com.extasys TCP Server.
      *
      * @param name
      * is the name of the server.
      * @param description
      * is the description of the server.
      * @param corePoolSize
      * is the number of threads to keep in the pool, even if they are idle.
      * @param maximumPoolSize
      * is the maximum number of threads to allow in the pool.
      */
     public ExtasysTCPServer(String name, String description, int corePoolSize, int maximumPoolSize){
          fName = name;
          fDescription = description;
          fMyThreadPool = new ExtasysThreadPool(corePoolSize, maximumPoolSize, 10, TimeUnit.SECONDS);
     }

     /**
      * Add a new listener to this server.
      *
      * @param name
      * is the listener's name.
      * @param ipAddress
      * is the listener's IP address.
      * @param port
      * is the listener's tcp port.
      * @param maxConnections
      * is the listener's maximum allowed connections.
      * @param readBufferSize
      * is the read buffer size for each connections in bytes.
      * @param connectionTimeOut
      * is the connections time-out in milliseconds. Set to 0 for no time-out.
      * @param backLog
      * is the number of outstanding connection requests the listener can have.
      * @return the listener.
      */
     public TCPListener addListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog) {

          TCPListener listener = new TCPListener(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog);
          listener.setMyExtasysTCPServer(this);
          fListeners.add(listener);
          return listener;
     }

     /**
      * Add new listener with message collector (character splitter).
      *
      * @param name
      * is the listener's name.
      * @param ipAddress
      * is the listener's IP address.
      * @param port
      * is the listener's tcp port.
      * @param maxConnections
      * is the number of maximum allowed connections.
      * @param readBufferSize
      * is the read buffer size for each connection in bytes.
      * @param connectionTimeOut
      * is the connections time-out in milliseconds. Set to 0 for no time-out.
      * @param backLog
      * is the number of outstanding connection requests the listener can have.
      * @param splitter
      * is the message splitter.
      * @return the listener.
      */
     public TCPListener addListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, char splitter) {

          TCPListener listener = new TCPListener(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, splitter);
          listener.setMyExtasysTCPServer(this);
          fListeners.add(listener);
          return listener;
     }

     /**
      * Add new listener with message collector (string splitter).
      *
      * @param name
      * is the listener's name.
      * @param ipAddress
      * is the listener's IP address.
      * @param port
      * is the listener's tcp port.
      * @param maxConnections
      * is the number of maximum allowed connections.
      * @param readBufferSize
      * is the read buffer size for each connection in bytes.
      * @param connectionTimeOut
      * is the connections time-out in milliseconds. Set to 0 for no time-out.
      * @param backLog
      * is the number of outstanding connection requests the listener can have.
      * @param splitter
      * is the message splitter.
      * @return the listener.
      */
     public TCPListener addListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, String splitter) {

          TCPListener listener = new TCPListener(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, splitter);
          listener.setMyExtasysTCPServer(this);
          fListeners.add(listener);
          return listener;
     }

     /**
      * Stop and remove a listener.
      *
      * @param name
      * is the listener's name to remove.
      */
     public void removeListener(String name) {

          for (int i = 0; i < fListeners.size(); i++) {
               if (((TCPListener) fListeners.get(i)).getName().equals(name)) {
                    ((TCPListener) fListeners.get(i)).Stop();
                    fListeners.remove(i);
                    break;
               }
          }
     }

     /**
      * Start or restart the server.
      *
      * @throws java.io.IOException
      */
     public void start() throws IOException, Exception {

          stop();
          try {
               // Start all listeners.
               for (int i = 0; i < fListeners.size(); i++) {
                    ((TCPListener) fListeners.get(i)).Start();
               }
          } catch (IOException ex) {
               stop();
               throw ex;
          } catch (Exception ex) {
               stop();
               throw ex;
          }
     }

     /**
      * Stop the server.
      */
     public void stop() {

          stop(false);
     }

     /**
      * Force server stop.
      */
     public void forceStop() {

          stop(true);
     }

     private void stop(boolean force) {

          // Stop all listeners.
          for (int i = 0; i < fListeners.size(); i++) {
               if (!force) {
                    ((TCPListener) fListeners.get(i)).Stop();
               } else {
                    ((TCPListener) fListeners.get(i)).ForceStop();
               }
          }

          try {
               fMyThreadPool.getQueue().clear();
          } catch (Exception ex) {
          }
     }

     /**
      * Dispose the server.
      *
      * This method stops the server and disposes all the active members of this class. After calling this method you cannot re-start the server.
      */
     public void dispose() {

          stop();
          fMyThreadPool.shutdown();
     }

     /**
      * Server is receiving data from a client connection.
      *
      * @param sender
      * is the client sends the data to this server.
      * @param data
      * is the incoming DataFrame.
      */
     public void onDataReceive(TCPClientConnection sender, DataFrame data) {

          // System.out.println(sender.getIPAddress() + " " + new String(data.getBytes()).substring(0, data.getBytes().length));
     }

     /**
      * A client connects to this server.
      *
      * @param client
      * Client.
      */
     public void onClientConnect(TCPClientConnection client) {

          // System.out.println(client.getIPAddress() + " connected.");
     }

     /**
      * A client disconnects from this server.
      *
      * @param client
      * Client.
      */
     public void onClientDisconnect(TCPClientConnection client) {

          // System.out.println(client.getIPAddress() + " disconnected.");
     }

     /**
      * Send data to a client.
      *
      * @param data
      * is the string to send.
      * @param sender
      * is the client to send the data.
      * @throws com.extasys.network.tcp.server.listener.exceptions.ClientIsDisconnectedException
      * @throws com.extasys.network.tcp.server.listener.exceptions.OutgoingPacketFailedException
      */
     public void replyToSender(String data, TCPClientConnection sender) throws ClientIsDisconnectedException, OutgoingPacketFailedException {

          sender.SendData(data);
     }

     /**
      * Send data to a client.
      *
      * @param bytes
      * is the byte array to be send.
      * @param offset
      * is the position in the data buffer at witch to begin sending.
      * @param length
      * is the number of the bytes to be send.
      * @param sender
      * is the client connection to receive the data.
      * @throws com.extasys.network.tcp.server.listener.exceptions.ClientIsDisconnectedException
      * @throws com.extasys.network.tcp.server.listener.exceptions.OutgoingPacketFailedException
      */
     public void replyToSender(byte[] bytes, int offset, int length, TCPClientConnection sender) throws ClientIsDisconnectedException, OutgoingPacketFailedException {

          sender.SendData(bytes, offset, length);
     }

     /**
      * Send data to all connected clients.
      *
      * @param data
      * is the string to be send.
      */
     public void replyToAll(String data) {

          for (int i = 0; i < fListeners.size(); i++) {
               try {
                    ((TCPListener) fListeners.get(i)).ReplyToAll(data);
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Send data to all connected clients.
      *
      * @param bytes
      * is the byte array to be send.
      * @param offset
      * is the position in the data buffer at witch to begin sending.
      * @param length
      * is the number of the bytes to be send.
      */
     public void replyToAll(byte[] bytes, int offset, int length) {

          for (int i = 0; i < fListeners.size(); i++) {
               try {
                    ((TCPListener) fListeners.get(i)).ReplyToAll(bytes, offset, length);
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Send data to all connected clients excepts sender.
      *
      * @param data
      * is the string to be send.
      * @param sender
      * is the TCP client exception.
      */
     public void replyToAllExceptSender(String data, TCPClientConnection sender) {

          for (int i = 0; i < fListeners.size(); i++) {
               try {
                    ((TCPListener) fListeners.get(i)).ReplyToAllExceptSender(data, sender);
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Send data to all connected clients excepts sender.
      *
      * @param bytes
      * is the byte array to be send.
      * @param offset
      * is the position in the data buffer at witch to begin sending.
      * @param length
      * is the number of the bytes to be send.
      * @param sender
      * is the TCP client exception.
      */
     public void replyToAllExceptSender(byte[] bytes, int offset, int length, TCPClientConnection sender) {

          for (int i = 0; i < fListeners.size(); i++) {
               try {
                    ((TCPListener) fListeners.get(i)).ReplyToAllExceptSender(bytes, offset, length, sender);
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Server's name.
      *
      * @return server's name.
      */
     public String getName() {

          return fName;
     }

     /**
      * Server's description.
      *
      * @return server's description.
      */
     public String getDescription() {

          return fDescription;
     }

     /**
      * Returns an ArrayList with this server's tcp listeners. The ArrayList elements are TCPListener classes.
      *
      * @return ArrayList with this server's tcp listeners.
      */
     public ArrayList getListeners() {

          return fListeners;
     }

     /**
      * Returns the current connections number of this server.
      *
      * @return the current connections number of this server.
      */
     public int getCurrentConnectionsNumber() {

          int currentConnections = 0;

          for (int i = 0; i < fListeners.size(); i++) {
               try {
                    currentConnections += ((TCPListener) fListeners.get(i)).getConnectedClients().size();
               } catch (Exception ex) {
               }
          }
          return currentConnections;
     }

     /**
      * Returns my Thread Pool.
      *
      * @return My Thread Pool.
      */
     public ThreadPoolExecutor getMyThreadPool() {

          return fMyThreadPool;
     }

     /**
      * Returns the total bytes received from this server.
      *
      * @return the total bytes received from this server.
      */
     public long getBytesIn() {

          long bytesIn = 0;
          try {
               for (int i = 0; i < fListeners.size(); i++) {
                    try {
                         bytesIn += ((TCPListener) fListeners.get(i)).getBytesIn();
                    } catch (Exception ex) {
                    }
               }
          } catch (Exception ex) {
          }
          return bytesIn;
     }

     /**
      * Returns the total bytes send from this server.
      *
      * @return the total bytes send from this server.
      */
     public long getBytesOut() {

          long bytesOut = 0;
          try {
               for (int i = 0; i < fListeners.size(); i++) {
                    try {
                         bytesOut += ((TCPListener) fListeners.get(i)).getBytesOut();
                    } catch (Exception ex) {
                    }
               }
          } catch (Exception ex) {
          }
          return bytesOut;
     }
}
