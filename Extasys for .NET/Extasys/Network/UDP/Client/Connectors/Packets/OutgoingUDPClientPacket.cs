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
using System.Net;
using System.Net.Sockets;

namespace Extasys.Network.UDP.Client.Connectors.Packets
{
    public class OutgoingUDPClientPacket
    {
        public ManualResetEvent fDone = new ManualResetEvent(false);
        private UDPConnector fConnector;
        private DatagramPacket fData;
        private OutgoingUDPClientPacket fPreviousPacket;
        public bool fCancel = false;

        public OutgoingUDPClientPacket(UDPConnector connector, DatagramPacket data, OutgoingUDPClientPacket previousPacket)
        {
            fConnector = connector;
            fData = data;
            fPreviousPacket = previousPacket;

            ThreadPool.QueueUserWorkItem(new WaitCallback(SendData));
        }

        /// <summary>
        /// Cancel this outgoing packet.
        /// By calling this method this and all the previous outgoing packets that 
        /// are stored in the thread pool will be canceled.
        /// Call this method for the last outgoing packet of the UDPConnector
        /// when the UDPConnector disconnects.
        /// </summary>
        public void Cancel()
        {
            fCancel = true;
            fDone.Set();
        }

        private void SendData(object stateObject)
        {
            try
            {
                if (fPreviousPacket == null)
                {
                    fConnector.fSocket.Send(fData.Bytes, fData.Length, fData.ServerEndPoint);
                    fConnector.fBytesOut += fData.Length;
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        fConnector.fSocket.Send(fData.Bytes, fData.Length, fData.ServerEndPoint);
                        fConnector.fBytesOut += fData.Length;
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
        /// Returns the DatagramPacket of this outgoing UDP packet.
        /// </summary>
        public DatagramPacket Data
        {
            get { return fData; }
        }

        /// <summary>
        /// Returns the previus outgoing packet of the client.
        /// If the packet is null means that the packet has been send to server.
        /// </summary>
        public OutgoingUDPClientPacket PreviusPacket
        {
            get { return fPreviousPacket; }
        }
    }
}
