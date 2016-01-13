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

package com.extasys.network.udp.server.listener.packets;

import java.net.DatagramPacket;

import com.extasys.ManualResetEvent;
import com.extasys.network.udp.server.listener.UDPListener;

/**
 *
 * @author Nikos Siatras
 */
public class OutgoingUDPServerPacket implements Runnable{

     private ManualResetEvent fDone = new ManualResetEvent(false);

     private boolean fCancel = false;

     private UDPListener fMyListener;

     private DatagramPacket fData;

     private OutgoingUDPServerPacket fPreviousPacket;

     public OutgoingUDPServerPacket(UDPListener listener, DatagramPacket packet, OutgoingUDPServerPacket previousPacket){
          fMyListener = listener;
          fData = packet;
          listener.getMyExtasysUDPServer().fMyThreadPool.execute(this);
     }

     public void Cancel() {

          fCancel = true;
          fDone.Set();
     }

     @Override
     public void run() {

          try {
               if (fPreviousPacket == null) {
                    fMyListener.fSocket.send(fData);
                    fMyListener.fBytesOut += fData.getLength();
               } else {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel) {
                         fMyListener.fSocket.send(fData);
                         fMyListener.fBytesOut += fData.getLength();
                    } else {
                         fCancel = true;
                    }
               }
          } catch (Exception ex) {
          }

          if (fPreviousPacket != null) {
               fPreviousPacket = null;
          }

          fDone.Set();
     }

     /**
      * Returns the DatagramPacket of this outgoing UDP packet.
      *
      * @return the DatagramPacket of this outgoing UDP packet.
      */
     public DatagramPacket getData() {

          return fData;
     }

     /**
      * Returns the previus outgoing packet of the server. If the packet is null means that the packet has been send.
      *
      * @return the previus outgoing packet of the server.
      */
     public OutgoingUDPServerPacket getPreviusPacket() {

          return fPreviousPacket;
     }
}
