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

namespace Extasys.Network.TCP.Server.Listener.Packets
{
    public class MessageCollectorTCPClientConnectionPacket
    {
        public ManualResetEvent fDone = new ManualResetEvent(false);
        private TCPClientConnection fClient;
        private string fData;
        private MessageCollectorTCPClientConnectionPacket fPreviousPacket;
        public bool fCancel = false;

        /// <summary>
        /// Constructs a new (incoming) message collector packet.
        /// 
        /// Use this class to receive data from a client.
        /// This is an incoming message that will remain in the servers thread pool
        /// as a job for the thread pool workers.
        /// </summary>
        /// <param name="client">The packet's TCPClientConnection.</param>
        /// <param name="data">The string received.</param>
        /// <param name="previousPacket">The previous message collector packet of the TCPClientConnection.</param>
        public MessageCollectorTCPClientConnectionPacket(TCPClientConnection client, string data, MessageCollectorTCPClientConnectionPacket previousPacket)
        {
            fClient = client;
            fData = data;
            fPreviousPacket = previousPacket;

            ThreadPool.QueueUserWorkItem(new WaitCallback(ReadData));
        }

        /// <summary>
        /// Cancel this message collector packet.
        /// 
        /// By calling this method this and all the previous message collector packets that 
        /// are stored in the thread pool will be canceled.
        /// Call this method for the last message collector of the TCPClientConnection
        /// when the TCPClientConnection disconnects.
        /// </summary>
        public void Cancel()
        {
            fCancel = true;
            fDone.Set();
        }

        private void ReadData(object stateObject)
        {
            try
            {
                if (fPreviousPacket == null)
                {
                    fClient.fMyMessageCollector.AppendData(fData);
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        fClient.fMyMessageCollector.AppendData(fData);
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
        /// Returns the data of this packet.
        /// </summary>
        public string Data
        {
            get { return fData; }
        }
    }
}
