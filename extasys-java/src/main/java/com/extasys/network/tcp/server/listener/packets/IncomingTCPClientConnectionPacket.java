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

package com.extasys.network.tcp.server.listener.packets;

import java.util.concurrent.RejectedExecutionException;

import com.extasys.DataFrame;
import com.extasys.ManualResetEvent;
import com.extasys.network.tcp.server.listener.TCPClientConnection;

/**
 *
 * @author Nikos Siatras
 */
public final class IncomingTCPClientConnectionPacket implements Runnable{

     public final ManualResetEvent fDone = new ManualResetEvent(false);

     private final TCPClientConnection fClient;

     private final DataFrame fData;

     private IncomingTCPClientConnectionPacket fPreviousPacket;

     public boolean fCancel = false;

     /**
      * Constructs a new incoming packet for an existing TCP client connection. Use this class to receive data from a client. This is an incoming message that will remain in the servers thread pool as
      * a job for the thread pool workers.
      *
      * @param client
      * is the client where this message belongs to.
      * @param data
      * is a DataFrame class.
      * @param previousPacket
      * is the previous incoming message of the client.
      */
     public IncomingTCPClientConnectionPacket(TCPClientConnection client, DataFrame data, IncomingTCPClientConnectionPacket previousPacket){
          fClient = client;
          fData = data;
          fPreviousPacket = previousPacket;

          SendToThreadPool();
     }

     protected void SendToThreadPool() {

          try {
               fClient.fMyExtasysServer.fMyThreadPool.execute(this);
          } catch (RejectedExecutionException ex) {
               fClient.ForceDisconnect();
          }
     }

     /**
      * Cancel this incoming packet.
      *
      * By calling this method this and all the previous incoming packets that are stored in the thread pool will be canceled. Call this method for the last incoming packet of the client when the
      * client disconnects.
      */
     public void Cancel() {

          fCancel = true;
          fDone.Set();
     }

     @Override
     public void run() {

          try {
               if (fPreviousPacket == null) {
                    fClient.fMyExtasysServer.OnDataReceive(fClient, fData);
               } else {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel) {
                         fClient.fMyExtasysServer.OnDataReceive(fClient, fData);
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
      * Returns the data of this packet.
      *
      * @return the data of this packet.
      */
     public DataFrame getData() {

          return fData;
     }
}
