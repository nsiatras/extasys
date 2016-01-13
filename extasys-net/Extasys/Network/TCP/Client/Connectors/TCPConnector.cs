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
using System.Net;
using System.Net.Sockets;
using Extasys.Network.TCP.Client.Connectors.Tools;
using Extasys.Network.TCP.Client.Connectors.Packets;
using System.IO;
using Extasys.Network.TCP.Client.Exceptions;

namespace Extasys.Network.TCP.Client.Connectors
{
    public class TCPConnector
    {
        public ExtasysTCPClient fMyTCPClient;
        private string fName;
        private IPAddress fServerIP;
        private int fServerPort;
        private bool fActive;
        protected bool fIsConnected = false;
        // Socket properties.
        public TcpClient fConnection;
        private int fReadBufferSize;
        private byte[] fReadBuffer;
        private int fIncomingBytes;
        private byte[] fIncomingBytesArray;
        // Data throughput.
        public int fBytesIn = 0;
        public int fBytesOut = 0;
        // Message collector properties.
        private bool fUseMessageCollector;
        private string fETX;
        public TCPClientMessageCollector fMessageCollector;
        // Messages IO.
        public IncomingTCPClientPacket fLastIncomingPacket = null;
        public MessageCollectorTCPClientPacket fLastMessageCollectorPacket = null;
        private OutgoingTCPClientPacket fLastOutgoingPacket = null;

        /// <summary>
        /// Constructs a new TCP Connector.
        /// </summary>
        /// <param name="myTCPClient">TCP connector's main Extasys TCP Client.</param>
        /// <param name="name">Connector's name.</param>
        /// <param name="serverIP">Server's ip address the connector will use to connect.</param>
        /// <param name="serverPort">Server's tcp port the connector will use to connect.</param>
        /// <param name="readBufferSize">Read buffer size in bytes for this connection.</param>
        public TCPConnector(ExtasysTCPClient myTCPClient, string name, IPAddress serverIP, int serverPort, int readBufferSize)
        {
            fMyTCPClient = myTCPClient;
            fName = name;
            fServerIP = serverIP;
            fServerPort = serverPort;
            fReadBufferSize = readBufferSize;
        }

        /// <summary>
        /// Constructs a new TCP Connector.
        /// </summary>
        /// <param name="myTCPClient">TCP connector's main Extasys TCP Client.</param>
        /// <param name="name">Connector's name.</param>
        /// <param name="serverIP">Server's ip address the connector will use to connect.</param>
        /// <param name="serverPort">Server's tcp port the connector will use to connect.</param>
        /// <param name="readBufferSize">Read buffer size in bytes for this connection.</param>
        /// <param name="ETX">Message splitter character.</param>
        public TCPConnector(ExtasysTCPClient myTCPClient, string name, IPAddress serverIP, int serverPort, int readBufferSize, char splitter)
        {
            fMyTCPClient = myTCPClient;
            fName = name;
            fServerIP = serverIP;
            fServerPort = serverPort;
            fReadBufferSize = readBufferSize;

            fUseMessageCollector = true;
            fETX = splitter.ToString();
            fMessageCollector = new TCPClientMessageCollector(this, splitter);
        }

        /// <summary>
        /// Constructs a new TCP Connector.
        /// </summary>
        /// <param name="myTCPClient">TCP connector's main Extasys TCP Client.</param>
        /// <param name="name">Connector's name.</param>
        /// <param name="serverIP">Server's ip address the connector will use to connect.</param>
        /// <param name="serverPort">Server's tcp port the connector will use to connect.</param>
        /// <param name="readBufferSize">Read buffer size in bytes for this connection.</param>
        /// <param name="ETXsplitter">Message splitter string.</param>
        public TCPConnector(ExtasysTCPClient myTCPClient, string name, IPAddress serverIP, int serverPort, int readBufferSize, string splitter)
        {
            fMyTCPClient = myTCPClient;
            fName = name;
            fServerIP = serverIP;
            fServerPort = serverPort;
            fReadBufferSize = readBufferSize;

            fUseMessageCollector = true;
            fETX = splitter;
            fMessageCollector = new TCPClientMessageCollector(this, splitter);
        }

        /// <summary>
        /// Start the connector (connect to the server).
        /// </summary>
        public void Start()
        {
            try
            {
                StartReadingData();
            }
            catch (IOException ioException) // The socket is closed.
            {
                Stop();
                return;
            }
            catch (ObjectDisposedException objectDisposedException) // The NetworkStream is closed. 
            {
                Stop();
                return;
            }
            catch (Exception ex)
            {
            }
        }

        private void StartReadingData()
        {
            try
            {
                fConnection = new TcpClient(fServerIP.ToString(), fServerPort);
                fConnection.ReceiveBufferSize = fReadBufferSize;
                fConnection.SendBufferSize = fReadBufferSize;

                fReadBuffer = new byte[fReadBufferSize];
                fConnection.GetStream().BeginRead(fReadBuffer, 0, fReadBufferSize, new AsyncCallback(ReadIncomingData), null);

                fIsConnected = true;
                fActive = true;
                fMyTCPClient.OnConnect(this);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }

        private void ReadIncomingData(IAsyncResult ar)
        {
            try
            {
                fIncomingBytes = fConnection.GetStream().EndRead(ar);
            }
            catch (IOException ioException) // The socket is closed.
            {
                Stop();
                return;
            }
            catch (ObjectDisposedException objectDisposedException) // The NetworkStream is closed. 
            {
                Stop();
                return;
            }
            catch (Exception ex)
            {
            }

            try
            {
                if (fIncomingBytes > 0)
                {
                    fIncomingBytesArray = new byte[fIncomingBytes];

                    Buffer.BlockCopy(fReadBuffer, 0, fIncomingBytesArray, 0, fIncomingBytes);
                    fBytesIn += fIncomingBytes;

                    // Take the incoming data.
                    switch (fUseMessageCollector)
                    {
                        case true:
                            fLastMessageCollectorPacket = new MessageCollectorTCPClientPacket(this, Encoding.Default.GetString(fReadBuffer, 0, fIncomingBytes), fLastMessageCollectorPacket);
                            break;

                        case false:
                            fLastIncomingPacket = new IncomingTCPClientPacket(this, new DataFrame(fIncomingBytesArray, 0, fIncomingBytes), fLastIncomingPacket);
                            break;
                    }
                }
                else
                {
                    Stop();
                    return;
                }
            }
            catch (Exception ex)
            {
            }

            if (fActive)
            {
                try
                {
                    fConnection.GetStream().BeginRead(fReadBuffer, 0, fReadBufferSize, ReadIncomingData, null);
                }
                catch (IOException ioException) //The socket is closed.
                {
                    Stop();
                    return;
                }
                catch (ObjectDisposedException objectDisposedException) //The NetworkStream is closed. 
                {
                    Stop();
                    return;
                }
                catch (Exception ex)
                {
                }
            }

            fIncomingBytes = 0;
            //fIncomingBytesArray.Initialize();
        }

        public void Stop()
        {
            if (fActive)
            {
                fMyTCPClient.OnDisconnect(this);

                try
                {
                    fConnection.Close();
                }
                catch (Exception ex)
                {
                }

                if (fLastIncomingPacket != null)
                {
                    fLastIncomingPacket.Cancel();
                }

                if (fLastMessageCollectorPacket != null)
                {
                    fLastMessageCollectorPacket.Cancel();
                }

                if (fLastOutgoingPacket != null)
                {
                    fLastOutgoingPacket.Cancel();
                }

                fActive = false;
                fIsConnected = false;
            }
        }

        /// <summary>
        /// Send string data to server.
        /// </summary>
        /// <param name="data">String data to be send.</param>
        public void SendData(string data)
        {
            try
            {
                byte[] bytes = Encoding.Default.GetBytes(data);
                SendData(bytes, 0, bytes.Length);
            }
            catch (Exception ex)
            {
                throw new ConnectorCannotSendPacketException(this, null);
            }
        }

        /// <summary>
        /// Send data to server. 
        /// </summary>
        /// <param name="bytes">The byte array to be send</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        public void SendData(byte[] bytes, int offset, int length) 
        {
            if (fIsConnected)
            {
                fLastOutgoingPacket = new OutgoingTCPClientPacket(this, bytes, offset, length, fLastOutgoingPacket);
            }
            else
            {
                throw new ConnectorDisconnectedException(this);
            }
        }

        /// <summary>
        /// Returns the main Extasys TCP Client of the connector.
        /// </summary>
        public ExtasysTCPClient MyExtasysTCPClient
        {
            get { return fMyTCPClient; }
        }

        /// <summary>
        /// Returns the active state of this connector.
        /// </summary>
        public bool IsActive
        {
            get { return fActive; }
        }

        /// <summary>
        /// Returns the name of this connector.
        /// </summary>
        public string Name
        {
            get { return fName; }
        }

        /// <summary>
        /// Returns the remote server's ip address.
        /// </summary>
        public IPAddress ServerIPAddress
        {
            get { return fServerIP; }
        }

        /// <summary>
        /// Returns the remote server TCP port.
        /// </summary>
        public int ServerPort
        {
            get { return fServerPort; }
        }

        /// <summary>
        /// Returns the read buffer size of the connection.
        /// </summary>
        public int ReadBufferSize
        {
            get { return fReadBufferSize; }
        }

        /// <summary>
        /// Return the number of bytes received from this connector.
        /// </summary>
        public int BytesIn
        {
            get { return fBytesIn; }
        }

        /// <summary>
        /// Returns the number of bytes send from this connector.
        /// </summary>
        public int BytesOut
        {
            get { return fBytesOut; }
        }

        /// <summary>
        /// Returns the active state of the message collector.
        /// </summary>
        public bool IsMessageCollectorInUse
        {
            get { return fUseMessageCollector; }
        }

        /// <summary>
        /// Returns the message collector of this connector.
        /// </summary>
        public TCPClientMessageCollector MyMessageCollector
        {
            get { return fMessageCollector; }
        }

        /// <summary>
        /// Returns message collector's splitter in string format.
        /// </summary>
        public string MessageSplitter
        {
            get { return fETX; }
        }

        /// <summary>
        /// Returns True if this conenctor is connected to the host.
        /// </summary>
        public bool IsConnected
        {
            get { return fIsConnected; }
        }

    }
}
