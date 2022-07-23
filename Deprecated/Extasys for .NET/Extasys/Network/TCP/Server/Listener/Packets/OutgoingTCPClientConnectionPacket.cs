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
using Extasys.Network.TCP.Server.Listener.Exceptions;
using System.Net.Sockets;

namespace Extasys.Network.TCP.Server.Listener.Packets
{
    public class OutgoingTCPClientConnectionPacket
    {
        public ManualResetEvent fDone = new ManualResetEvent(false);
        private ManualResetEvent fDataSendCompleted = new ManualResetEvent(false);
        private TCPClientConnection fClient;
        private byte[] fBytes;
        private int fOffset;
        private int fLength;
        private OutgoingTCPClientConnectionPacket fPreviousPacket;
        public bool fCancel = false;

        /// <summary>
        /// Constructs a new outgoing packet for an existing TCP client connection.
        /// 
        /// Use this class to send data from the server to a connected client.
        /// This is an outgoing message that will remain in the servers thread pool
        /// as a job for the thread pool workers.
        /// </summary>
        /// <param name="client">The client where this message belongs to.</param>
        /// <param name="bytes">The byte array to be sent.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        /// <param name="previousPacket">The previous outgoing packet of the client.</param>
        public OutgoingTCPClientConnectionPacket(TCPClientConnection client, byte[] bytes, int offset, int length, OutgoingTCPClientConnectionPacket previousPacket)
        {
            try
            {
                fClient = client;
                fBytes = bytes;
                fOffset = offset;
                fLength = length;
                fPreviousPacket = previousPacket;
                ThreadPool.QueueUserWorkItem(new WaitCallback(SendData));
            }
            catch (Exception ex)
            {
                throw new OutgoingPacketFailedException(this);
            }
        }

        /// <summary>
        /// Cancel this outgoing packet.
        /// By calling this method this and all the previous outgoing packets that 
        /// are stored in the thread pool will be canceled.
        /// Call this method for the last outgoing packet of the client
        /// when the client disconnects.
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
                    try
                    {
                        fClient.fConnection.BeginSend(fBytes, fOffset, fLength, 0, new AsyncCallback(SendCallback), fClient.fConnection);
                        fDataSendCompleted.WaitOne();
                    }
                    catch (Exception exception)
                    {
                        fCancel = true;
                        fClient.DisconnectMe();
                    }
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();

                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        try
                        {
                            fClient.fConnection.BeginSend(fBytes, fOffset, fLength, 0, new AsyncCallback(SendCallback), fClient.fConnection);
                            fDataSendCompleted.WaitOne();
                        }
                        catch (Exception exception)
                        {
                            fCancel = true;
                            fClient.DisconnectMe();
                        }
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

        private void SendCallback(IAsyncResult ar)
        {
            try
            {
                //Retrieve the socket from the state object.
                Socket handler = (Socket)ar.AsyncState;

                //Complete sending the data to the remote device.
                handler.EndSend(ar);

                fClient.fLastDataAction = DateTime.Now.Ticks;
            }
            catch (Exception ex)
            {
                fCancel = true;
                fClient.DisconnectMe();
            }
            fDataSendCompleted.Set();
        }

        /// <summary>
        /// Returns the byte array of this packet.
        /// </summary>
        public byte[] Bytes
        {
            get { return fBytes; }
        }

        /// <summary>
        /// Returns the offset of this packet.
        /// </summary>
        public int Offset
        {
            get { return fOffset; }
        }

        /// <summary>
        /// Returns the number of bytes to send from this packet.
        /// </summary>
        public int Length
        {
            get { return fLength; }
        }

    }
}
