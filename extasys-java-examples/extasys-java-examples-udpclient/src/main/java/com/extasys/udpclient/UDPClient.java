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

package com.extasys.udpclient;

import java.net.DatagramPacket;
import java.net.InetAddress;

import com.extasys.network.udp.client.ExtasysUDPClient;
import com.extasys.network.udp.client.connectors.UDPConnector;

/**
 *
 * @author Nikos Siatras
 */
public class UDPClient extends ExtasysUDPClient{

     private AutoSendMessages fAutoSendMessagesThread;

     public UDPClient(String name, String description, int readTimeOut, int corePoolSize, int maximumPoolSize, InetAddress remoteHostIP, int remoteHostPort){
          super(name, description, corePoolSize, maximumPoolSize);

          // Add a UDP connector to this UDP client.
          // You can add more than one connectors if you need to.
          super.addConnector("My connector", 10240, 10000, remoteHostIP, remoteHostPort);
     }

     @Override
     public void OnDataReceive(UDPConnector connector, DatagramPacket packet) {

          System.out.println("Data received: " + new String(packet.getData()));
     }

     public void StartSendingMessages() {

          StopSendingMessages();
          fAutoSendMessagesThread = new AutoSendMessages(this);
          fAutoSendMessagesThread.start();
     }

     public void StopSendingMessages() {

          if (fAutoSendMessagesThread != null) {
               fAutoSendMessagesThread.Dispose();
               fAutoSendMessagesThread.interrupt();
          }
     }
}

class AutoSendMessages extends Thread{

     private UDPClient fMyClient;

     private boolean fActive = true;

     public AutoSendMessages(UDPClient client){
          fMyClient = client;
     }

     @Override
     public void run() {

          int messageCount = 0;
          while (fActive) {
               try {
                    messageCount++;
                    String msgToSend = String.valueOf(messageCount);
                    fMyClient.sendData(msgToSend.getBytes(), 0, msgToSend.getBytes().length);
                    Thread.sleep(300);
               } catch (Exception ex) {
                    Dispose();
                    fMyClient.StopSendingMessages();
               }
          }
     }

     public void Dispose() {

          fActive = false;
     }
}
