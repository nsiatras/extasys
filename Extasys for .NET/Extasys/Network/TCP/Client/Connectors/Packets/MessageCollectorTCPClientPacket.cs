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
    public class MessageCollectorTCPClientPacket
    {
        public ManualResetEvent fDone = new ManualResetEvent(false);
        private TCPConnector fConnector;
        private String fData;
        private MessageCollectorTCPClientPacket fPreviousPacket;
        public bool fCancel = false;

        public MessageCollectorTCPClientPacket(TCPConnector connector, String data, MessageCollectorTCPClientPacket previousPacket)
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
                    fConnector.fMessageCollector.AppendData(fData);
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        fConnector.fMessageCollector.AppendData(fData);
                    }
                    else
                    {
                        fCancel = true;
                    }

                    fPreviousPacket = null;
                }
            }
            catch (Exception ex)
            {
            }

            fDone.Set();
        }

        /// <summary>
        /// Cancel this message collector packet.
        ///
        /// By calling this method this and all the previous message collector packets that 
        /// are stored in the thread pool will be canceled.
        /// Call this method for the last message collector packet of the connector
        /// when the connector disconnects.
        /// </summary>
        public void Cancel()
        {
            fCancel = true;
            fDone.Set();
        }

        /// <summary>
        ///  Returns the data of this packet.
        /// </summary>
        public string Data
        {
            get { return fData; }
        }

    }
}
