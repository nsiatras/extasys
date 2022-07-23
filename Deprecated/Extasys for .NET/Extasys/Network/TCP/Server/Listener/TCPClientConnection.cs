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
using System.Net.Sockets;
using Extasys.Network.TCP.Server.Listener.Tools;
using Extasys.Network.TCP.Server.Listener.Packets;
using System.Threading;
using Extasys.Network.TCP.Server.Listener.Exceptions;

namespace Extasys.Network.TCP.Server.Listener
{
    public class TCPClientConnection
    {
        // Connection properties.
        internal Socket fConnection;
        internal long fLastDataAction = 0;
        protected bool fActive = false;
        protected bool fIsConnected = false;
        internal TCPListener fMyListener;
        internal ExtasysTCPServer fMyExtasysServer;
        private string fIPAddress;
        private string fName = "";
        private Object fTag = null;
        private DateTime fConnectionStartUpDateTime;
        private int fReadBufferSize;
        private byte[] fReadBuffer;
        // Data throughput.
        internal int fBytesIn = 0;
        internal int fBytesOut = 0;
        // Message collector.
        internal TCPClientConnectionMessageCollector fMyMessageCollector;
        protected bool fUseMessageCollector;
        // Messages IO.
        internal IncomingTCPClientConnectionPacket fLastIncomingPacket = null;
        internal OutgoingTCPClientConnectionPacket fLastOugoingPacket = null;
        internal MessageCollectorTCPClientConnectionPacket fLastMessageCollectorPacket = null;

        public TCPClientConnection(Socket socket, TCPListener myTCPListener, bool useMessageCollector, string ETX)
        {
            try
            {
                fUseMessageCollector = useMessageCollector;
                fIPAddress = socket.RemoteEndPoint.ToString();
                fConnection = socket;
                fIsConnected = true;

                fReadBufferSize = myTCPListener.ReadBufferSize;
                fReadBuffer = new byte[fReadBufferSize];

                if (myTCPListener.ConnectionTimeOut > 0) //Connection time-out.
                {
                    fConnection.ReceiveTimeout = myTCPListener.ConnectionTimeOut;
                    fConnection.SendTimeout = myTCPListener.ConnectionTimeOut;
                }
                fConnection.ReceiveBufferSize = myTCPListener.ReadBufferSize;
                fConnection.SendBufferSize = myTCPListener.ReadBufferSize;

                fMyListener = myTCPListener;
                fMyExtasysServer = myTCPListener.MyExtasysTCPServer;
            }
            catch (SocketException ex)
            {
                DisconnectMe();
                return;
            }
            catch (Exception ex)
            {
                DisconnectMe();
                return;
            }

            if (fUseMessageCollector)
            {
                fMyMessageCollector = new TCPClientConnectionMessageCollector(this, ETX);
            }

            fMyListener.AddClient(this);
            StartReceivingData();
            
            fConnectionStartUpDateTime = DateTime.Now;
        }

        private void StartReceivingData()
        {
            fLastDataAction = DateTime.Now.Ticks;

            fActive = true;
            fMyListener.MyExtasysTCPServer.OnClientConnect(this);
            try
            {
                fConnection.BeginReceive(fReadBuffer, 0, fReadBufferSize, SocketFlags.None, new AsyncCallback(StreamReceiver), null);
                if (fConnection.SendTimeout > 0)
                {
                    AsyncCallback call = new AsyncCallback(CheckConnectionTimeOut);
                    call.BeginInvoke(null, call, null);
                }
            }
            catch (ArgumentNullException argumentNullException) // Buffer is a null reference. 
            {
                DisconnectMe();
            }
            catch (SocketException socketException) // An error occurred when attempting to access the socket.
            {
                DisconnectMe();
            }
            catch (ObjectDisposedException objectDisposedException) // Socket has been closed.
            {
                DisconnectMe();
            }
            catch (ArgumentOutOfRangeException argumentOutOfRangeException) // Offset is less than 0.
            {
                DisconnectMe();
            }
            catch (Exception ex)
            {
            }
        }

        private void CheckConnectionTimeOut(IAsyncResult ar)
        {
            try
            {
                if (fIsConnected)
                {
                    ManualResetEvent timeOutEvent = new ManualResetEvent(false);
                    timeOutEvent.WaitOne(fConnection.ReceiveTimeout);
                    if ((DateTime.Now.Ticks - fLastDataAction) / 10000 > fConnection.ReceiveTimeout)
                    {
                        Console.WriteLine((DateTime.Now.Ticks - fLastDataAction) / 10000);
                        DisconnectMe();
                    }
                    else
                    {
                        CheckConnectionTimeOut(null);
                    }
                }
            }
            catch (Exception ex)
            {
                DisconnectMe();
            }
        }

        private void StreamReceiver(IAsyncResult ar)
        {
            int bytesRead;
            try
            {
                if (fIsConnected)
                {
                    bytesRead = fConnection.EndReceive(ar);

                    if (bytesRead > 0)
                    {
                        fBytesIn += bytesRead;
                        fMyListener.fBytesIn += bytesRead;
                        fLastDataAction = DateTime.Now.Ticks;

                        switch (fUseMessageCollector)
                        {
                            case true:
                                fLastMessageCollectorPacket = new MessageCollectorTCPClientConnectionPacket(this, Encoding.Default.GetString(fReadBuffer, 0, bytesRead), fLastMessageCollectorPacket);
                                break;

                            case false:
                                fLastIncomingPacket = new IncomingTCPClientConnectionPacket(this, new DataFrame(fReadBuffer, 0, bytesRead), fLastIncomingPacket);
                                break;
                        }
                    }
                    else
                    {
                        DisconnectMe();
                        return;
                    }

                    //Start a new asynchronous read into readBuffer.
                    fConnection.BeginReceive(fReadBuffer, 0, fReadBufferSize, SocketFlags.None, new AsyncCallback(StreamReceiver), null);
                }
            }
            catch (ArgumentNullException argumentNullException) //asyncResult is a null reference (Nothing in Visual Basic). 
            {
                DisconnectMe();
            }
            catch (ArgumentException argumentException) //asyncResult was not returned by a call to the BeginReceive method. 
            {
                DisconnectMe();
            }
            catch (SocketException socketException) //An error occurred when attempting to access the socket. See the Remarks section for more information
            {
                DisconnectMe();
            }
            catch (ObjectDisposedException objectDisposedException) //Socket has been closed.
            {
                DisconnectMe();
            }
            catch (Exception ex)
            {
                fConnection.BeginReceive(fReadBuffer, 0, fReadBufferSize, SocketFlags.None, new AsyncCallback(StreamReceiver), null);
            }
        }

        public void DisconnectMe()
        {
            if (fActive)
            {
                fActive = false;
                fIsConnected = false;
                
                try
                {
                    fConnection.Close();
                }
                catch (Exception ex)
                {
                }

                if (fUseMessageCollector && fMyMessageCollector != null)
                {
                    fMyMessageCollector.Dispose();
                }

                if (fLastIncomingPacket != null)
                {
                    fLastIncomingPacket.Cancel();
                }

                if (fLastOugoingPacket != null)
                {
                    fLastOugoingPacket.Cancel();
                }

                if (fLastMessageCollectorPacket != null)
                {
                    fLastMessageCollectorPacket.Cancel();
                }

                fConnection = null;
                fLastIncomingPacket = null;
                fLastOugoingPacket = null;
                fLastMessageCollectorPacket = null;
                fMyMessageCollector = null;

                fMyListener.RemoveClient(fIPAddress);
                fMyListener.MyExtasysTCPServer.OnClientDisconnect(this);
            }
        }

        /// <summary>
        /// Send data to client.
        /// </summary>
        /// <param name="data">The string to send.</param>
        public void SendData(string data)
        {
            byte[] bytes = Encoding.Default.GetBytes(data);
            SendData(bytes, 0, bytes.Length);
        }

        /// <summary>
        /// Send data to client. 
        /// </summary>
        /// <param name="bytes">The byte array to be send.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        public void SendData(byte[] bytes, int offset, int length)
        {
            if (!this.fIsConnected)
            {
                throw new ClientIsDisconnectedException(this);
            }
            fBytesOut += length;
            fMyListener.fBytesOut += length;
            fLastOugoingPacket = new OutgoingTCPClientConnectionPacket(this, bytes, offset, length, fLastOugoingPacket);
        }

        /// <summary>
        /// Returns the reference of the TCP Listener of this client.
        /// </summary>
        public TCPListener MyTCPListener
        {
            get { return fMyListener; }
        }

        /// <summary>
        /// Returns the reference of the Extasys TCP Server of this client.
        /// </summary>
        public ExtasysTCPServer MyExtasysTCPServer
        {
            get { return fMyExtasysServer; }
        }

        /// <summary>
        /// Return client's IP Address.
        /// </summary>
        public string IPAddress
        {
            get { return fIPAddress; }
        }

        /// <summary>
        /// Gets/Sets the client's name.
        /// </summary>
        public string Name
        {
            get { return fName; }
            set { fName = value; }
        }

        /// <summary>
        /// Return connection start up Date-Time.
        /// </summary>
        public DateTime ConnectionStartUpDateTime
        {
            get { return fConnectionStartUpDateTime; }
        }

        /// <summary>
        /// Gets/Sets client's tag.
        /// Tag is a simple object that can be used as a reference for something else.
        /// </summary>
        public Object Tag
        {
            get { return fTag; }
            set { fTag = value; }
        }

        /// <summary>
        /// Return number of bytes received from this client.
        /// </summary>
        public int BytesIn
        {
            get { return fBytesIn; }
        }

        /// <summary>
        /// Return number of bytes sent from this client.
        /// </summary>
        public int BytesOut
        {
            get { return fBytesOut; }
        }

        public bool IsActive
        {
            get { return fActive; }
        }

        /// <summary>
        /// Returns the message collector of this client.
        /// </summary>
        public TCPClientConnectionMessageCollector MyMessageCollector
        {
            get { return fMyMessageCollector; }
        }


    }
}
