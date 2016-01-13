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
using System.Collections;
using System.Threading;
using System.Net.Sockets;

namespace Extasys.Network.TCP.Server.Listener
{
    public class TCPListener
    {
        private ExtasysTCPServer fMyExtasysTCPServer;
        // Socket.
        private TcpListener fTcpListener;
        private TCPListenerThread fTCPListenerThread;
        private bool fActive = false;
        // TCP listener identification.
        private string fName;
        private IPAddress fIPAddress;
        private int fPort;
        // Connections.
        private Hashtable fConnectedClients;
        private object fAddRemoveUsersLock = new Object();
        private int fMaxConnections;
        private int fReadBufferSize;
        private int fConnectionTimeOut;
        private int fBackLog;
        // Bytes throughtput.
        public int fBytesIn, fBytesOut;
        // Message collector.
        private bool fUseMessageCollector = false;
        private string fMessageCollectorSplitter;

        /// <summary>
        /// Constructs a new TCP listener.
        /// </summary>
        /// <param name="name">The listener's name.</param>
        /// <param name="ipAddress">The listener's IP Address.</param>
        /// <param name="port">The listener's port.</param>
        /// <param name="maxConnections">The listener's maximum connections limit.</param>
        /// <param name="readBufferSize">The listener's each connection read buffer size in bytes.</param>
        /// <param name="connectionTimeOut">The listener's connections time-out time in milliseconds.</param>
        /// <param name="backLog">The number of outstanding connection requests this listener can have.</param>
        public TCPListener(string name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog)
        {
            Initialize(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, false, null);
        }

        /// <summary>
        /// Constructs a new TCP listener.
        /// </summary>
        /// <param name="name">The listener's name.</param>
        /// <param name="ipAddress">The listener's IP Address.</param>
        /// <param name="port">The listener's port.</param>
        /// <param name="maxConnections">The listener's maximum connections limit.</param>
        /// <param name="readBufferSize">The listener's each connection read buffer size in bytes.</param>
        /// <param name="connectionTimeOut">The listener's connections time-out time in milliseconds.</param>
        /// <param name="backLog">The number of outstanding connection requests this listener can have.</param>
        /// <param name="splitter">The message splitter.</param>
        public TCPListener(String name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, char splitter)
        {
            Initialize(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, true, splitter.ToString());
        }

        /// <summary>
        /// Constructs a new TCP listener.
        /// </summary>
        /// <param name="name">The listener's name.</param>
        /// <param name="ipAddress">The listener's IP Address.</param>
        /// <param name="port">The listener's port.</param>
        /// <param name="maxConnections">The listener's maximum connections limit.</param>
        /// <param name="readBufferSize">The listener's each connection read buffer size in bytes.</param>
        /// <param name="connectionTimeOut">The listener's connections time-out time in milliseconds.</param>
        /// <param name="backLog">The number of outstanding connection requests this listener can have.</param>
        /// <param name="splitter">The message splitter.</param>
        public TCPListener(string name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, string splitter)
        {
            Initialize(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, true, splitter);
        }

        private void Initialize(string name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, bool useMessageCollector, string splitter)
        {
            fConnectedClients = new Hashtable();
            fName = name;
            fIPAddress = ipAddress;
            fPort = port;
            fMaxConnections = maxConnections;
            fReadBufferSize = readBufferSize;
            fConnectionTimeOut = connectionTimeOut;
            fBackLog = backLog;
            fUseMessageCollector = useMessageCollector;
            fMessageCollectorSplitter = splitter;
            fBytesIn = 0;
            fBytesOut = 0;
        }

        /// <summary>
        /// Start or restart the TCPListener.
        /// </summary>
        public void Start()
        {
            Stop();
            try
            {
                fTcpListener = new TcpListener(fIPAddress, fPort);
                fTcpListener.Start();
                fActive = true;
            }
            catch (Exception ex)
            {
                Stop();
                throw ex;
            }

            try
            {
                fTCPListenerThread = new TCPListenerThread(fTcpListener,this);
                fTCPListenerThread.Start();
            }
            catch (Exception ex)
            {
                Stop();
                throw ex;
            }
        }

        /// <summary>
        /// Stop the TCP listener.
        /// </summary>
        public void Stop()
        {
            fActive = false;

            try
            {
                if (fTCPListenerThread != null)
                {
                    fTCPListenerThread.Stop();
                }
            }
            catch (Exception ex)
            {
            }

            // Disconnect all connected clients.
            try
            {
                foreach (DictionaryEntry entry in fConnectedClients)
                {
                    try
                    {
                        ((TCPClientConnection)entry.Value).DisconnectMe();
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
            catch (Exception ex)
            {
            }

            try
            {
                if (fTcpListener != null)
                {
                    fTcpListener.Stop();
                }
            }
            catch (Exception ex)
            {
            }
        }

        /// <summary>
        /// Send data to all connected clients.
        /// </summary>
        /// <param name="data">The string to be send.</param>
        public void ReplyToAll(string data)
        {
            foreach (DictionaryEntry entry in fConnectedClients)
            {
                try
                {
                    ((TCPClientConnection)entry.Value).SendData(data);
                }
                catch (Exception ex)
                {

                }
            }
        }

        /// <summary>
        /// Send data to all connected clients.
        /// </summary>
        /// <param name="bytes">The byte array to be send.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        public void ReplyToAll(byte[] bytes, int offset, int length)
        {
            foreach (DictionaryEntry entry in fConnectedClients)
            {
                try
                {
                    ((TCPClientConnection)entry.Value).SendData(bytes,offset,length);
                }
                catch (Exception ex)
                {

                }
            }
        }

        /// <summary>
        /// Send data to all connected clients excepts sender.
        /// </summary>
        /// <param name="data">The string to be send.</param>
        /// <param name="sender">The TCP client exception.</param>
        public void ReplyToAllExceptSender(string data, TCPClientConnection sender)
        {
            if (sender != null)
            {
                foreach (DictionaryEntry entry in fConnectedClients)
                {
                    try
                    {
                        TCPClientConnection tmp = (TCPClientConnection)entry.Value;
                        if (tmp.IPAddress != sender.IPAddress)
                        {
                            tmp.SendData(data);
                        }
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
        }

        /// <summary>
        /// Send data to all connected clients excepts sender.
        /// </summary>
        /// <param name="bytes">The byte array to be send.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        /// <param name="sender">The TCP client exception.</param>
        public void ReplyToAllExceptSender(byte[] bytes, int offset, int length, TCPClientConnection sender)
        {
            if (sender != null)
            {
                foreach (DictionaryEntry entry in fConnectedClients)
                {
                    try
                    {
                        TCPClientConnection tmp = (TCPClientConnection)entry.Value;
                        if (tmp.IPAddress != sender.IPAddress)
                        {
                            tmp.SendData(bytes,offset,length);
                        }
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
        }

        public void AddClient(TCPClientConnection client)
        {
            lock (fAddRemoveUsersLock)
            {
                try
                {
                    if (!fConnectedClients.ContainsKey(client.IPAddress))
                    {
                        fConnectedClients.Add(client.IPAddress, client);
                    }
                }
                catch (Exception ex)
                {
                }
            }
        }


        public void RemoveClient(string ipAddress)
        {
            lock (fAddRemoveUsersLock)
            {
                try
                {
                    if (fConnectedClients.ContainsKey(ipAddress))
                    {
                        fConnectedClients.Remove(ipAddress);
                    }
                }
                catch (Exception ex)
                {
                }
            }
        }

        /// <summary>
        /// Returns TCP listener's ServerSocket
        /// </summary>
        public TcpListener ServerSocket
        {
            get { return fTcpListener; }
        }

        /// <summary>
        /// Set my Extasys TCP server.
        /// </summary>
        /// <param name="server">the ExtasysTCPServer main reference at witch this TCPListener belongs.</param>
        public void SetMyExtasysTCPServer(ExtasysTCPServer server)
        {
            fMyExtasysTCPServer = server;
        }

        /// <summary>
        /// Returns a reference of this listener's main Extasys TCP server.
        /// </summary>
        public ExtasysTCPServer MyExtasysTCPServer
        {
            get { return fMyExtasysTCPServer; }
        }

        /// <summary>
        /// Returns the active state of this TCPListener.
        /// </summary>
        public bool IsActive
        {
            get { return fActive; }
        }

        /// <summary>
        /// Return listener's name.
        /// </summary>
        public string Name
        {
            get { return fName; }
        }

        /// <summary>
        /// Returns listener's IP address.
        /// </summary>
        public IPAddress IPAddress
        {
            get { return fIPAddress; }
        }

        /// <summary>
        /// Returns listener's port.
        /// </summary>
        public int Port
        {
            get { return fPort; }
        }

        /// <summary>
        /// Returns a Hashtable with the connected clients of this listener.
        /// The Hashtable's key is a string contains client's IP address.
        /// The Hashtable's value is a TCPClientConnection.
        /// </summary>
        public Hashtable ConnectedClients
        {
            get { return fConnectedClients; }
        }

        /// <summary>
        /// Gets/Sets allowed maximum connections.
        /// </summary>
        public int MaxConnections
        {
            get { return fMaxConnections; }
            set { fMaxConnections = value; }
        }

        /// <summary>
        /// Gets/Sets read buffer size in bytes for each client connection of this TCPListener.
        /// </summary>
        public int ReadBufferSize
        {
            get { return fReadBufferSize; }
            set { fReadBufferSize = value; }
        }

        /// <summary>
        /// Gets/Sets the connections time-out in milliseconds of this listener.
        /// </summary>
        public int ConnectionTimeOut
        {
            get { return fConnectionTimeOut; }
            set { fConnectionTimeOut = value; }
        }

        /// <summary>
        /// Returns the number of bytes received from this TCPListener.
        /// </summary>
        public int BytesIn
        {
            get { return fBytesIn; }
        }

        /// <summary>
        /// Returns the number of bytes sent from this TCPListener.
        /// </summary>
        public int BytesOut
        {
            get { return fBytesOut; }
        }

        /// <summary>
        /// Returns the active state of this listener's message collector.
        /// </summary>
        public bool IsMessageCollectorInUse
        {
            get { return fUseMessageCollector;}
        }

        /// <summary>
        /// Returns message collector's splitter in string format.
        /// </summary>
        public string MessageSplitter
        {
            get { return fMessageCollectorSplitter; }
        }


    }
}
