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

package com.extasys.network.tcp.server.listener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Hashtable;

import com.extasys.network.tcp.server.ExtasysTCPServer;

/**
 *
 * @author Nikos Siatras
 */
public class TCPListener{
     // com.extasys tcp server reference.

     private ExtasysTCPServer fMyExtasysTCPServer;

     // Socket.
     private ServerSocket fTcpListener;

     private Thread fTCPListenerThread;

     private boolean fActive = false;

     // TCP listener identification.
     private String fName;

     private InetAddress fIPAddress;

     private int fPort;

     // Connections.
     private Hashtable fConnectedClients;

     final Object fAddRemoveUsersLock = new Object();

     private int fMaxConnections;

     private int fReadBufferSize;

     private int fConnectionTimeOut;

     private int fBackLog;

     // Bytes throughtput.
     public long fBytesIn, fBytesOut;

     // Message collector.
     private boolean fUseMessageCollector = false;

     private String fMessageCollectorSplitter;

     /**
      * Constructs a new TCP listener.
      *
      * @param name
      * is the listener's name.
      * @param ipAddress
      * is the listener's IP Address.
      * @param port
      * is the listener's port.
      * @param maxConnections
      * is the listener's maximum connections limit.
      * @param readBufferSize
      * is the listener's each connection read buffer size in bytes.
      * @param connectionTimeOut
      * is the listener's connections time-out time in milliseconds.
      * @param backLog
      * is the number of outstanding connection requests this listener can have.
      */
     public TCPListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog){
          Initialize(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, false, null);
     }

     /**
      * Constructs a new TCP listener.
      *
      * @param name
      * is the listener's name.
      * @param ipAddress
      * is the listener's IP Address.
      * @param port
      * is the listener's port.
      * @param maxConnections
      * is the listener's maximum allowed connections..
      * @param readBufferSize
      * is the listener's each connection read buffer size in bytes.
      * @param connectionTimeOut
      * is the listener's connections time-out in milliseconds. Set to 0 for no time-out
      * @param backLog
      * is the number of outstanding connection requests this listener can have.
      * @param splitter
      * is the message splitter.
      */
     public TCPListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, char splitter){
          Initialize(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, true, String.valueOf(splitter));
     }

     /**
      * Constructs a new TCP listener.
      *
      * @param name
      * is the listener's name.
      * @param ipAddress
      * is the listener's IP Address.
      * @param port
      * is the listener's port.
      * @param maxConnections
      * is the listener's maximum allowed connections..
      * @param readBufferSize
      * is the listener's each connection read buffer size in bytes.
      * @param connectionTimeOut
      * is the listener's connections time-out in milliseconds. Set to 0 for no time-out
      * @param backLog
      * is the number of outstanding connection requests this listener can have.
      * @param splitter
      * is the message splitter.
      */
     public TCPListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, String splitter){
          Initialize(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, true, splitter);
     }

     private void Initialize(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, boolean useMessageCollector, String splitter) {

          fConnectedClients = new Hashtable();
          fName = name;
          fIPAddress = ipAddress;
          fPort = port;
          fMaxConnections = maxConnections;
          fReadBufferSize = readBufferSize;
          fConnectionTimeOut = connectionTimeOut;
          fBackLog = backLog;
          fUseMessageCollector = useMessageCollector;
          fMessageCollectorSplitter = splitter;
          fBytesIn = 0;
          fBytesOut = 0;
     }

     /**
      * Start or restart the TCPListener.
      */
     public void Start() throws IOException, Exception {

          Stop();
          try {
               fTcpListener = new ServerSocket(fPort, fBackLog, fIPAddress);
               fTcpListener.setSoTimeout(5000);
               fActive = true;
          } catch (IOException ex) {
               Stop();
               throw ex;
          }

          try {
               fTCPListenerThread = new Thread(new TCPListenerThread(fTcpListener, this));
               fTCPListenerThread.start();
          } catch (Exception ex) {
               Stop();
               throw ex;
          }
     }

     /**
      * Stop the TCP listener.
      */
     public void Stop() {

          Stop(false);
     }

     /**
      * Force server stop
      */
     public void ForceStop() {

          Stop(true);
     }

     private void Stop(boolean force) {

          fActive = false;

          try {
               fTCPListenerThread.interrupt();
          } catch (Exception ex) {
          }

          // Disconnect all connected clients.
          try {
               for (Enumeration e = fConnectedClients.keys(); e.hasMoreElements();) {
                    try {
                         TCPClientConnection tmp = (TCPClientConnection) fConnectedClients.get(e.nextElement());
                         if (!force) {
                              tmp.DisconnectMe();
                         } else {
                              tmp.ForceDisconnect();
                         }
                    } catch (Exception ex) {
                         // System.out.println("Listener failed to disconnect a client");
                    }
               }
          } catch (Exception ex) {
          }

          try {
               fTcpListener.close();
          } catch (Exception ex) {
          }
     }

     /**
      * Add client to connected clients list.
      *
      * @param client
      * is the client object to add.
      */
     public void AddClient(TCPClientConnection client) {

          synchronized (fAddRemoveUsersLock) {
               try {
                    if (!fConnectedClients.contains(client.getIPAddress())) {
                         fConnectedClients.put(client.getIPAddress(), client);
                    }
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Remove client from connected clients list.
      *
      * @param ipAddress
      * is the client's IP address.
      */
     public void RemoveClient(String ipAddress) {

          synchronized (fAddRemoveUsersLock) {
               try {
                    if (fConnectedClients.containsKey(ipAddress)) {
                         fConnectedClients.remove(ipAddress);
                    }
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Send data to all connected clients.
      *
      * @param data
      * is the string to be send.
      */
     public void ReplyToAll(String data) {

          synchronized (fAddRemoveUsersLock) {
               for (Object client : fConnectedClients.values()) {
                    try {
                         ((TCPClientConnection) client).SendData(data);
                    } catch (Exception ex) {
                    }
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
     public void ReplyToAll(byte[] bytes, int offset, int length) {

          synchronized (fAddRemoveUsersLock) {
               for (Object client : fConnectedClients.values()) {
                    try {
                         ((TCPClientConnection) client).SendData(bytes, offset, length);
                    } catch (Exception ex) {
                    }
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
     public void ReplyToAllExceptSender(String data, TCPClientConnection sender) {

          if (sender != null) {
               synchronized (fAddRemoveUsersLock) {
                    TCPClientConnection tmp;
                    for (Object client : fConnectedClients.values()) {
                         tmp = (TCPClientConnection) client;
                         if (!tmp.getIPAddress().equals(sender.getIPAddress())) {
                              try {
                                   tmp.SendData(data);
                              } catch (Exception ex) {
                              }
                         }

                    }
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
     public void ReplyToAllExceptSender(byte[] bytes, int offset, int length, TCPClientConnection sender) {

          if (sender != null) {
               synchronized (fAddRemoveUsersLock) {
                    TCPClientConnection tmp;
                    for (Object client : fConnectedClients.values()) {
                         tmp = (TCPClientConnection) client;
                         if (!tmp.getIPAddress().equals(sender.getIPAddress())) {
                              try {
                                   tmp.SendData(bytes, offset, length);
                              } catch (Exception ex) {
                              }
                         }

                    }
               }
          }
     }

     /**
      * Returns TCP listener's ServerSocket.
      *
      * @return TCP listener's ServerSocket.
      */
     public ServerSocket getServerSocket() {

          return fTcpListener;
     }

     /**
      * Set my com.extasys TCP server.
      *
      * @param server
      * is the ExtasysTCPServer main reference at witch this TCPListener belongs.
      */
     public void setMyExtasysTCPServer(ExtasysTCPServer server) {

          fMyExtasysTCPServer = server;
     }

     /**
      * Returns a reference of this listener's main com.extasys TCP server.
      *
      * @return a reference of this listener's main com.extasys TCP server.
      */
     public ExtasysTCPServer getMyExtasysTCPServer() {

          return fMyExtasysTCPServer;
     }

     /**
      * Returns the active state of this TCPListener.
      *
      * @return True if this TCPListener is active.
      */
     public boolean isActive() {

          return fActive;
     }

     /**
      * Return's this listener's name.
      *
      * @return the name of this listener.
      */
     public String getName() {

          return fName;
     }

     /**
      * Returns listener's IP address.
      *
      * @return this listener's IP address.
      */
     public InetAddress getIPAddress() {

          return fIPAddress;
     }

     /**
      * Returns listener's port.
      *
      * @return this listener's port.
      */
     public int getPort() {

          return fPort;
     }

     /**
      * Returns a Hashtable with the connected clients of this listener. The Hashtable's key is a string contains client's IP address. The Hashtable's value is a TCPClientConnection.
      *
      * @return the connections hashtable of this listener.
      */
     @SuppressWarnings("UseOfObsoleteCollectionType")
     public Hashtable getConnectedClients() {

          return fConnectedClients;
     }

     /**
      * Returns allowed maximum connections.
      *
      * @return the allowed maximum connections of this listener.
      */
     public int getMaxConnections() {

          return fMaxConnections;
     }

     /**
      * Set the maximum allowed connections of this listener.
      *
      * @param value
      * is the maximum allowed connections of this listener.
      */
     public void setMaxConnections(int value) {

          fMaxConnections = value;
     }

     /**
      * Returns read buffer size in bytes for each client connection of this TCPListener.
      *
      * @return the read buffer size in bytes for each client connection.
      */
     public int getReadBufferSize() {

          return fReadBufferSize;
     }

     /**
      * Set read buffer size in bytes for each client connection of this TCPListener.
      *
      * @param value
      * is the read buffer size in bytes.
      */
     public void setReadBufferSize(int value) {

          fReadBufferSize = value;
     }

     /**
      * Returns the connections time-out in milliseconds of this listener.
      *
      * @return the connections time-out in milliseconds of this listener.
      */
     public int getConnectionTimeOut() {

          return fConnectionTimeOut;
     }

     /**
      * Set the connections time-out in milliseconds of this listener.
      *
      * @param value
      * is the connections time-out in milliseconds of this listener.
      */
     public void setConnectionTimeOut(int value) {

          fConnectionTimeOut = value;
     }

     /**
      * Returns the number of bytes received from this TCPListener.
      *
      * @return the number of bytes received.
      */
     public long getBytesIn() {

          return fBytesIn;
     }

     /**
      * Returns the number of bytes sent from this TCPListener.
      *
      * @return the number of bytes sent.
      */
     public long getBytesOut() {

          return fBytesOut;
     }

     /**
      * Returns the active state of this listener's message collector.
      *
      * @return True if the message collector of this listener is active.
      */
     public boolean isMessageCollectorInUse() {

          return fUseMessageCollector;
     }

     /**
      * Returns message collector's splitter in string format.
      *
      * @return the message collector's splitter in string format.
      */
     public String getMessageSplitter() {

          return fMessageCollectorSplitter;
     }
}
