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

package com.extasys.network.udp.server.listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.extasys.network.udp.server.ExtasysUDPServer;
import com.extasys.network.udp.server.listener.packets.IncomingUDPServerPacket;
import com.extasys.network.udp.server.listener.packets.OutgoingUDPServerPacket;

/**
 *
 * @author Nikos Siatras
 */
public class UDPListener{

     private ExtasysUDPServer fMyUDPServer; // com.extasys UDP server reference.

     private String fName;

     private InetAddress fIPAddress;

     private int fPort;

     private int fReadBufferSize;

     private int fReadDataTimeOut;

     public DatagramSocket fSocket;

     private boolean fActive = false;

     private Thread fReadIncomingDataThread;

     public long fBytesIn = 0, fBytesOut = 0;

     // Messages IO.
     public IncomingUDPServerPacket fLastIncomingPacket = null;

     public OutgoingUDPServerPacket fLastOutgoingPacket = null;

     /**
      * Constructs a new UDP Listener.
      *
      * @param myUDPServer
      * is the udp listener's main UDP server.
      * @param name
      * is the name of the listener.
      * @param ipAddress
      * is the listener's IP address.
      * @param port
      * is the listener's UDP port.
      * @param readBufferSize
      * is the read buffer size of the listener.
      * @param readDataTimeOut
      * is the maximum time in milliseconds in wich a datagram packet can be received. Set to 0 for no time-out.
      */
     public UDPListener(ExtasysUDPServer myUDPServer, String name, InetAddress ipAddress, int port, int readBufferSize, int readDataTimeOut){
          fMyUDPServer = myUDPServer;
          fName = name;
          fIPAddress = ipAddress;
          fPort = port;
          fReadBufferSize = readBufferSize;
          fReadDataTimeOut = readDataTimeOut;
     }

     /**
      * Start or restart the UDP listener.
      */
     public void Start() throws SocketException {

          Stop();
          try {
               fSocket = new DatagramSocket(fPort, fIPAddress);
               fSocket.setReceiveBufferSize(fReadBufferSize);
               fSocket.setSendBufferSize(fReadBufferSize);

               if (fReadDataTimeOut > 0) {
                    fSocket.setSoTimeout(fReadDataTimeOut);
               }

               fLastIncomingPacket = null;
               fLastOutgoingPacket = null;

               fActive = true;
               fReadIncomingDataThread = new ReadIncomingDataThread(this);
               fReadIncomingDataThread.start();
          } catch (SocketException socketException) {
               fActive = false;
               Stop();
               throw socketException;
          }
     }

     /**
      * Stop the UDP listener.
      */
     public void Stop() {

          if (fActive) {
               fActive = false;
               try {
                    fSocket.close();
               } catch (Exception ex) {

               }

               if (fLastIncomingPacket != null) {
                    fLastIncomingPacket.Cancel();
               }

               if (fLastOutgoingPacket != null) {
                    fLastOutgoingPacket.Cancel();
               }

               try {
                    fReadIncomingDataThread.interrupt();
               } catch (Exception ex) {
               }
          }
     }

     /**
      * Send data to udp client.
      *
      * @param packet
      * is the datagram packet to be send.
      */
     public void SendData(DatagramPacket packet) {

          fLastOutgoingPacket = new OutgoingUDPServerPacket(this, packet, fLastOutgoingPacket);
     }

     /**
      * Returns the main com.extasys UDP Server of this UDP listener.
      *
      * @return the main com.extasys UDP Server of this UDP listener.
      */
     public ExtasysUDPServer getMyExtasysUDPServer() {

          return fMyUDPServer;
     }

     /**
      * Returns the name of this UDP Listener.
      *
      * @return the name of this UDP Listener.
      */
     public String getName() {

          return fName;
     }

     /**
      * Returns the IP address of this UDP Listener.
      *
      * @return the IP address of this UDP Listener.
      */
     public InetAddress getIPAddress() {

          return fIPAddress;
     }

     /**
      * Returns the UDP port of this listener.
      *
      * @return the UDP port of this listener.
      */
     public int getPort() {

          return fPort;
     }

     /**
      * Returns the read buffer size of this listener.
      *
      * @return the read buffer size of this listener.
      */
     public int getReadBufferSize() {

          return fReadBufferSize;
     }

     /**
      * Return the number of bytes received from this listener.
      *
      * @return the number of bytes received from this listener.
      */
     public long getBytesIn() {

          return fBytesIn;
     }

     /**
      * Return the number of bytes send from this listener.
      *
      * @return the number of bytes send from this listener.
      */
     public long getBytesOut() {

          return fBytesOut;
     }

     /**
      * Returns the active state of this listener.
      *
      * @return the active state of this listener.
      */
     public boolean isActive() {

          return fActive;
     }

}

class ReadIncomingDataThread extends Thread{

     private UDPListener fMyUDPListener;

     public ReadIncomingDataThread(UDPListener myUDPListener){
          fMyUDPListener = myUDPListener;
     }

     @Override
     public void run() {

          byte[] data;
          DatagramPacket receivedPacket;

          while (fMyUDPListener.isActive()) {
               try {
                    data = new byte[fMyUDPListener.getReadBufferSize()];
                    receivedPacket = new DatagramPacket(data, data.length);
                    fMyUDPListener.fSocket.receive(receivedPacket);
                    fMyUDPListener.fBytesIn += receivedPacket.getLength();
                    fMyUDPListener.fLastIncomingPacket = new IncomingUDPServerPacket(fMyUDPListener, receivedPacket, fMyUDPListener.fLastIncomingPacket);
               } catch (IOException ex) {

               } catch (Exception ex) {

               }
          }

          System.out.println("UDP Listener read incoming data thread interrupted.");
     }
}
