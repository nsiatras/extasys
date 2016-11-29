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
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

namespace Extasys.Network.TCP.Client.Connectors.Packets
{
    public class IncomingTCPClientPacket
    {
        public ManualResetEvent fDone = new ManualResetEvent(false);
        private TCPConnector fConnector;
        private DataFrame fData;
        private IncomingTCPClientPacket fPreviousPacket;
        public bool fCancel = false;


        /// <summary>
        /// Constructs a new incoming packet received by a TCP connector.
        /// Use this class to receive data from a server.
        /// This is an incoming message that will remain in the client's thread pool
        /// as a job for the thread pool workers.
        /// </summary>
        /// <param name="connector">The TCP Connector where this message belongs to.</param>
        /// <param name="data">DataFrame class.</param>
        /// <param name="previousPacket">The previous incoming message of the TCP Connector.</param>
        public IncomingTCPClientPacket(TCPConnector connector, DataFrame data, IncomingTCPClientPacket previousPacket)
        {
            fConnector = connector;
            fData = data;
            fPreviousPacket = previousPacket;

            ThreadPool.QueueUserWorkItem(new WaitCallback(ReadData));
        }

        private void ReadData(object stateObject)
        {
            try
            {
                if (fPreviousPacket == null)
                {
                    fConnector.fMyTCPClient.OnDataReceive(fConnector, fData);
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        fConnector.fMyTCPClient.OnDataReceive(fConnector, fData);
                    }
                    else
                    {
                        fCancel = true;
                    }
                }
            }
            catch (Exception ex)
            {
            }

            if (fPreviousPacket != null)
            {
                fPreviousPacket = null;
            }

            fDone.Set();
        }

        /// <summary>
        /// Cancel this incoming packet.
        ///  
        /// By calling this method this and all the previous incoming packets that 
        /// are stored in the thread pool will be canceled.
        /// Call this method for the last incoming packet of the TCP Connector
        /// when the TCP Connector disconnects.
        /// </summary>
        public void Cancel()
        {
            fCancel = true;
            fDone.Set();
        }

        /// <summary>
        /// Returns the data of this packet.
        /// </summary>
        public DataFrame Data
        {
            get { return fData; }
        }

        /// <summary>
        /// Returns the previus incoming packet received by the client.
        /// If the packet is null means that the packet has been received and parsed from the client.
        /// </summary>
        public IncomingTCPClientPacket getPreviusPacket
        {
            get { return fPreviousPacket; }
        }
    }
}
