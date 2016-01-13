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

package com.extasys.network.tcp.client.connectors.packets;

import java.util.concurrent.RejectedExecutionException;

import com.extasys.ManualResetEvent;
import com.extasys.network.tcp.client.connectors.TCPConnector;

/**
 *
 * @author Nikos Siatras
 */
public final class MessageCollectorTCPClientPacket implements Runnable{

     public ManualResetEvent fDone = new ManualResetEvent(false);

     private final TCPConnector fConnector;

     private final byte[] fData;

     private MessageCollectorTCPClientPacket fPreviousPacket;

     public boolean fCancel = false;

     /**
      * Constructs a new (incoming) message collector packet.
      *
      * Use this class to receive data from the server. This is an incoming message that will remain in the servers thread pool as a job for the thread pool workers.
      *
      * @param connector
      * is the packets TCP Connector.
      * @param data
      * is the byte[] received.
      * @param previousPacket
      * is the previous message collector packet of the TCP Connector.
      */
     public MessageCollectorTCPClientPacket(TCPConnector connector, byte[] data, MessageCollectorTCPClientPacket previousPacket){
          fConnector = connector;
          fData = data;
          fPreviousPacket = previousPacket;

          SendToThreadPool();
     }

     protected void SendToThreadPool() {

          try {
               fConnector.fMyTCPClient.fMyThreadPool.execute(this);
          } catch (RejectedExecutionException ex) {
               fConnector.ForceStop();
          }
     }

     /**
      * Cancel this message collector packet.
      *
      * By calling this method this and all the previous message collector packets that are stored in the thread pool will be canceled. Call this method for the last message collector packet of the
      * connector when the connector disconnects.
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
                    fConnector.fMessageCollector.AppendData(fData);
               } else {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel) {
                         fConnector.fMessageCollector.AppendData(fData);
                    } else {
                         fCancel = true;
                    }

                    fPreviousPacket = null;
               }
          } catch (Exception ex) {
          }

          fDone.Set();
     }

     /**
      * Returns the data of this packet.
      *
      * @return the data of this packet.
      */
     public byte[] getData() {

          return fData;
     }
}
