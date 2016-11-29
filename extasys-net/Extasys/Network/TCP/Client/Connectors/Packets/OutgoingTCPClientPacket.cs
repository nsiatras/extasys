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
using System.IO;
using System.Net.Sockets;

namespace Extasys.Network.TCP.Client.Connectors.Packets
{
    public class OutgoingTCPClientPacket
    {
        internal ManualResetEvent fDone = new ManualResetEvent(false);
        private TCPConnector fConnector;
        private byte[] fBytes;
        private int fOffset;
        private int fLength;
        private OutgoingTCPClientPacket fPreviousPacket;
        public bool fCancel = false;

        private ManualResetEvent fDataSendCompleted = new ManualResetEvent(false);


        /// <summary>
        /// Constructs a new outgoing packet for an existing TCP Connector.
        /// Use this class to send data from the TCP Connector to a server.
        /// This is an outgoing message that will remain in the servers thread pool 
        /// as a job for the thread pool workers.
        /// </summary>
        /// <param name="connector">TCPConnector where this message belongs to.</param>
        /// <param name="bytes">The byte array to be sent.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        /// <param name="previousPacket">The previous outgoing packet of the TCPConnector.</param>
        public OutgoingTCPClientPacket(TCPConnector connector, byte[] bytes, int offset, int length, OutgoingTCPClientPacket previousPacket)
        {
            fConnector = connector;
            fBytes = bytes;
            fOffset = offset;
            fLength = length;
            fPreviousPacket = previousPacket;

            ThreadPool.QueueUserWorkItem(new WaitCallback(SendData));
        }

        /// <summary>
        /// Cancel this outgoing packet.
        /// 
        /// By calling this method this and all the previous outgoing packets that 
        /// are stored in the thread pool will be canceled.
        /// Call this method for the last outgoing packet of the TCP Connector
        /// when the TCP Connector disconnects.
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
                        fConnector.fConnection.GetStream().BeginWrite(fBytes, fOffset, fLength, new AsyncCallback(OnEndSend), fConnector.fConnection.GetStream());
                        fConnector.fBytesOut += fLength;
                        fDataSendCompleted.WaitOne();
                    }
                    catch (IOException ioException)
                    {
                        fCancel = true;
                        fConnector.Stop();
                    }
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();

                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        try
                        {
                            fConnector.fConnection.GetStream().BeginWrite(fBytes, fOffset, fLength, new AsyncCallback(OnEndSend), fConnector.fConnection.GetStream());
                            fConnector.fBytesOut += fLength;
                            fDataSendCompleted.WaitOne();
                        }
                        catch (IOException ioException)
                        {
                            fCancel = true;
                            fConnector.Stop();
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

        //TcpClient asynchronous data send.
        private void OnEndSend(IAsyncResult result)
        {
            try
            {
                NetworkStream stream = (NetworkStream)result.AsyncState;

                try
                {
                    stream.EndWrite(result);
                }
                catch (IOException iOException) //The socket is closed.
                {
                    fConnector.Stop();
                }
                catch (ObjectDisposedException objectDisposedException) //The NetworkStream is closed. 
                {
                    fConnector.Stop();
                }
                catch (Exception ex)
                {
                }

                stream.Flush();
            }
            catch (Exception ex)
            {
                fConnector.Stop();
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
        public int OffSet
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
