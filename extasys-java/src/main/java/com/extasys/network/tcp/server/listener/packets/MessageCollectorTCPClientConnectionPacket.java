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

import com.extasys.ManualResetEvent;
import com.extasys.network.tcp.server.listener.TCPClientConnection;

/**
 *
 * @author Nikos Siatras
 */
public final class MessageCollectorTCPClientConnectionPacket implements Runnable{

     public final ManualResetEvent fDone = new ManualResetEvent(false);

     private final TCPClientConnection fClient;

     private final byte[] fData;

     private MessageCollectorTCPClientConnectionPacket fPreviousPacket;

     public boolean fCancel = false;

     /**
      * Constructs a new (incoming) message collector packet.
      *
      * Use this class to receive data from a client. This is an incoming message that will remain in the servers thread pool as a job for the thread pool workers.
      *
      * @param client
      * is the packets TCPClientConnection.
      * @param data
      * is the string received.
      * @param previousPacket
      * is the previous message collector packet of the TCPClientConnection.
      */
     public MessageCollectorTCPClientConnectionPacket(TCPClientConnection client, byte[] data, MessageCollectorTCPClientConnectionPacket previousPacket){
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
      * Cancel this message collector packet.
      *
      * By calling this method this and all the previous message collector packets that are stored in the thread pool will be canceled. Call this method for the last message collector of the
      * TCPClientConnection when the TCPClientConnection disconnects.
      *
      */
     public void Cancel() {

          fCancel = true;
          fDone.Set();
     }

     @Override
     public void run() {

          try {
               if (fPreviousPacket == null) {
                    fClient.fMyMessageCollector.AppendData(fData);
               } else {
                    fPreviousPacket.fDone.WaitOne();

                    if (!fCancel && !fPreviousPacket.fCancel) {
                         fClient.fMyMessageCollector.AppendData(fData);
                    } else {
                         fCancel = true;
                    }

                    fPreviousPacket = null;
               }
          } catch (Exception ex) {
          }

          fDone.Set();
     }

}
