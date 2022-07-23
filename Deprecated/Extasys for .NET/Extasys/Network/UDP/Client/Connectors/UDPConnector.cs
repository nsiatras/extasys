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
using Extasys.Network.UDP.Client.Connectors.Packets;
using System.Threading;
using System.Net;
using System.Net.Sockets;

namespace Extasys.Network.UDP.Client.Connectors
{
    public class UDPConnector
    {
        private bool fActive = false;
        private ExtasysUDPClient fMyUDPClient; //Extasys UDP Client reference.
        public UdpClient fSocket;
        private byte[] fReadBuffer;
        private IPAddress fServerIP;
        private IPEndPoint fServerEndPoint;
        private int fServerPort;
        private string fName;
        private int fReadBufferSize;
        private int fReadTimeOut;
        public int fBytesIn = 0;
        public int fBytesOut = 0;
        public IncomingUDPClientPacket fLastIncomingPacket = null;
        public OutgoingUDPClientPacket fLastOutgoingPacket = null;

        /// <summary>
        /// Constructs a new UDP Connector.
        /// </summary>
        /// <param name="myClient">The connectors main Extasys UDP Client.</param>
        /// <param name="name">The name of the connector.</param>
        /// <param name="readBufferSize">The maximum number of bytes the socket can read at a time.</param>
        /// <param name="readTimeOut">The maximum time in milliseconds in wich a datagram packet can be received. Set to 0 for no time-out.</param>
        /// <param name="serverIP">The server's ip address the connector will use to send data.</param>
        /// <param name="serverPort">The server's udp port.</param>
        public UDPConnector(ExtasysUDPClient myClient, string name, int readBufferSize, int readTimeOut, IPAddress serverIP, int serverPort)
        {
            fMyUDPClient = myClient;
            fName = name;
            fReadBufferSize = readBufferSize;
            fReadTimeOut = readTimeOut;
            fServerIP = serverIP;
            fServerPort = serverPort;

            fReadBuffer = new byte[readBufferSize];

            fServerEndPoint = new IPEndPoint(fServerIP, fServerPort);
        }

        /// <summary>
        /// Start the udp connector.
        /// </summary>
        public void Start()
        {
            if (!fActive)
            {
                try
                {
                    fActive = true;
                    try
                    {
                        fSocket = new UdpClient(0);
                    }
                    catch (SocketException ex)
                    {
                        fActive = false;
                        throw ex;
                    }

                    fLastIncomingPacket = null;
                    fLastOutgoingPacket = null;

                    if (fReadTimeOut > 0)
                    {
                        /*fSocket.SendTimeout = fReadTimeOut;
                        fSocket.ReceiveTimeout = fReadTimeOut;*/
                    }

                    try
                    {
                        fSocket.BeginReceive(new AsyncCallback(OnReceive), null);
                    }
                    catch (Exception ex)
                    {
                        throw ex;
                    }
                }
                catch (SocketException ex)
                {
                    Stop();
                    throw ex;
                }
            }
        }

        private void OnReceive(IAsyncResult ar)
        {
            try
            {
                IPEndPoint remote = new IPEndPoint(IPAddress.Any, 0);
                byte[] data = fSocket.EndReceive(ar, ref remote);

                DatagramPacket packet = new DatagramPacket(data, remote);
                fLastIncomingPacket = new IncomingUDPClientPacket(this, packet, fLastIncomingPacket);
                fSocket.BeginReceive(OnReceive, null);
            }
            catch (Exception ex)
            {

            }
        }
        
        /// <summary>
        /// Stop the udp connector.
        /// </summary>
        public void Stop()
        {
            if (fActive)
            {
                fActive = false;
                try
                {
                    fSocket.Close();
                }
                catch (Exception ex)
                {
                }

                if (fLastIncomingPacket != null)
                {
                    fLastIncomingPacket.Cancel();
                }

                if (fLastOutgoingPacket != null)
                {
                    fLastOutgoingPacket.Cancel();
                }
            }
        }

        /// <summary>
        /// Send data to all host.
        /// </summary>
        /// <param name="data">The string to be send.</param>
        public void SendData(string data)
        {
            byte[] bytes = Encoding.Default.GetBytes(data);
            SendData(bytes, 0, bytes.Length);
        }

        /// <summary>
        /// Send data to host.
        /// </summary>
        /// <param name="bytes">The byte array to be send.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        public void SendData(byte[] bytes, int offset, int length)
        {
            DatagramPacket outPacket = new DatagramPacket(bytes, offset, length, fServerEndPoint);
            fLastOutgoingPacket = new OutgoingUDPClientPacket(this, outPacket, fLastOutgoingPacket);
        }

        public bool isActive
        {
            get { return fActive; }
        }

        /// <summary>
        /// Returns the main Extasys UDP Client of the connector.
        /// </summary>
        public ExtasysUDPClient MyExtasysUDPClient
        {
            get { return fMyUDPClient; }
        }

        /// <summary>
        /// Returns the name of this connector.
        /// </summary>
        public string Name
        {
            get { return fName; }
        }

        /// <summary>
        /// Returns the read buffer size of the connection.
        /// </summary>
        public int ReadBufferSize
        {
            get { return fReadBufferSize; }
        }

        /// <summary>
        /// Returns the maximum time in milliseconds in wich a datagram packet can be received. 
        /// </summary>
        public int ReadTimeOut
        {
            get { return fReadTimeOut; }
        }

        /// <summary>
        /// Return the number of bytes received from this connector.
        /// </summary>
        public int BytesIn
        {
            get { return fBytesIn; }
        }

        /// <summary>
        /// Return the number of bytes send from this connector.
        /// </summary>
        public int BytesOut
        {
            get { return fBytesOut; }
        }

    }
}
