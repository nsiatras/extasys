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
using System.Collections;
using Extasys.Network.TCP.Server.Listener;
using System.Net;
using System.IO;

namespace Extasys.Network.TCP.Server
{
    public class ExtasysTCPServer
    {
        private String fName;
        private String fDescription;
        private ArrayList fListeners = new ArrayList();

        public ExtasysTCPServer(string name, string description)
        {
            fName = name;
            fDescription = description;
        }

        /// <summary>
        /// Add a new listener to this server.
        /// </summary>
        /// <param name="name">The listener's name.</param>
        /// <param name="ipAddress">The listener's IP address.</param>
        /// <param name="port">The listener's tcp port.</param>
        /// <param name="maxConnections">The listener's maximum allowed connections.</param>
        /// <param name="readBufferSize">The read buffer size for each connections in bytes.</param>
        /// <param name="connectionTimeOut">The connections time-out in milliseconds. Set to 0 for no time-out.</param>
        /// <param name="backLog">The number of outstanding connection requests the listener can have.</param>
        /// <returns>The listener.</returns>
        public TCPListener AddListener(string name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog)
        {
            TCPListener listener = new TCPListener(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog);
            listener.SetMyExtasysTCPServer(this);
            fListeners.Add(listener);
            return listener;
        }

        /// <summary>
        /// Add new listener with message collector (character splitter).
        /// </summary>
        /// <param name="name">The listener's name.</param>
        /// <param name="ipAddress">The listener's IP address.</param>
        /// <param name="port">The listener's tcp port.</param>
        /// <param name="maxConnections">The number of maximum allowed connections.</param>
        /// <param name="readBufferSize">The read buffer size for each connection in bytes.</param>
        /// <param name="connectionTimeOut">The connections time-out in milliseconds. Set to 0 for no time-out.</param>
        /// <param name="backLog">The number of outstanding connection requests the listener can have.</param>
        /// <param name="splitter">The message splitter.</param>
        /// <returns>The listener.</returns>
        public TCPListener AddListener(string name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, char splitter)
        {
            TCPListener listener = new TCPListener(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, splitter);
            listener.SetMyExtasysTCPServer(this);
            fListeners.Add(listener);
            return listener;
        }
        
        /// <summary>
        /// Add new listener with message collector (character splitter).
        /// </summary>
        /// <param name="name">The listener's name.</param>
        /// <param name="ipAddress">The listener's IP address.</param>
        /// <param name="port">The listener's tcp port.</param>
        /// <param name="maxConnections">The number of maximum allowed connections.</param>
        /// <param name="readBufferSize">The read buffer size for each connection in bytes.</param>
        /// <param name="connectionTimeOut">The connections time-out in milliseconds. Set to 0 for no time-out.</param>
        /// <param name="backLog">The number of outstanding connection requests the listener can have.</param>
        /// <param name="splitter">The message splitter.</param>
        /// <returns>The listener.</returns>
        public TCPListener AddListener(string name, IPAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, string splitter)
        {
            TCPListener listener = new TCPListener(name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, splitter);
            listener.SetMyExtasysTCPServer(this);
            fListeners.Add(listener);
            return listener;
        }

        /// <summary>
        /// Stop and remove a listener.
        /// </summary>
        /// <param name="name">The listener's name to remove.</param>
        public void RemoveListener(string name)
        {
            for (int i = 0; i < fListeners.Count; i++)
            {
                if (((TCPListener)fListeners[i]).Name==name)
                {
                    ((TCPListener)fListeners[i]).Stop();
                    fListeners.RemoveAt(i);
                    break;
                }
            }
        }

        /// <summary>
        /// Start or restart the server.
        /// </summary>
        public void Start()
        {
            Stop();
            try
            {
                //Start all listeners.
                for (int i = 0; i < fListeners.Count; i++)
                {
                    ((TCPListener)fListeners[i]).Start();
                }
            }
            catch (IOException ex)
            {
                Stop();
                throw ex;
            }
            catch (Exception ex)
            {
                Stop();
                throw ex;
            }
        }

        /// <summary>
        /// Stop the server.
        /// </summary>
        public void Stop()
        {
            //Stop all listeners.
            for (int i = 0; i < fListeners.Count; i++)
            {
                ((TCPListener)fListeners[i]).Stop();
            }
        }

        /// <summary>
        /// Server is receiving data from a client connection.
        /// </summary>
        /// <param name="sender">The client sends the data to this server.</param>
        /// <param name="data">The incoming DataFrame.</param>
        public virtual void OnDataReceive(TCPClientConnection sender, DataFrame data)
        {
        }

        /// <summary>
        /// A client connects to this server.
        /// </summary>
        /// <param name="client"></param>
        public virtual void OnClientConnect(TCPClientConnection client)
        {
        }

        /// <summary>
        /// A client disconnects from this server.
        /// </summary>
        /// <param name="client"></param>
        public virtual void OnClientDisconnect(TCPClientConnection client)
        {
        }

        /// <summary>
        /// Send data to a client.
        /// </summary>
        /// <param name="data">The string to send.</param>
        /// <param name="sender">The client to send the data.</param>
        public virtual void ReplyToSender(string data, TCPClientConnection sender)
        {
            sender.SendData(data);
        }

        /// <summary>
        /// Send data to a client.
        /// </summary>
        /// <param name="bytes">The byte array to be send.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        /// <param name="sender">The client to send the data.</param>
        public virtual void ReplyToSender(byte[] bytes, int offset, int length, TCPClientConnection sender)
        {
            sender.SendData(bytes, offset, length);
        }

        /// <summary>
        /// Send data to all connected clients.
        /// </summary>
        /// <param name="data">The string to be send.</param>
        public virtual void ReplyToAll(string data)
        {
            for (int i = 0; i < fListeners.Count; i++)
            {
                try
                {
                    ((TCPListener)fListeners[i]).ReplyToAll(data);
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
        public virtual void ReplyToAll(byte[] bytes, int offset, int length)
        {
            for (int i = 0; i < fListeners.Count; i++)
            {
                try
                {
                    ((TCPListener)fListeners[i]).ReplyToAll(bytes,offset,length);
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
        public virtual void ReplyToAllExceptSender(string data, TCPClientConnection sender)
        {
            for (int i = 0; i < fListeners.Count; i++)
            {
                try
                {
                    ((TCPListener)fListeners[i]).ReplyToAllExceptSender(data,sender);
                }
                catch (Exception ex)
                {
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
        public virtual void ReplyToAllExceptSender(byte[] bytes, int offset, int length, TCPClientConnection sender)
        {
            for (int i = 0; i < fListeners.Count; i++)
            {
                try
                {
                    ((TCPListener)fListeners[i]).ReplyToAllExceptSender(bytes,offset,length, sender);
                }
                catch (Exception ex)
                {
                }
            }
        }

        /// <summary>
        /// Server's name.
        /// </summary>
        public string Name
        {
            get { return fName; }
        }

        /// <summary>
        /// Server's description.
        /// </summary>
        public string Description
        {
            get { return fDescription; }
        }

        /// <summary>
        /// Returns an ArrayList with this server's tcp listeners.
        /// The ArrayList elements are TCPListener classes.
        /// </summary>
        public ArrayList Listeners
        {
            get { return fListeners; }
        }

        /// <summary>
        /// Returns the current connections number of this server.
        /// </summary>
        public int CurrentConnectionsNumber
        {
            get
            {
                int currentConnections = 0;

                for (int i = 0; i < fListeners.Count; i++)
                {
                    try
                    {
                        currentConnections += ((TCPListener)fListeners[i]).ConnectedClients.Count;
                    }
                    catch (Exception ex)
                    {
                    }
                }
                return currentConnections;
            }
        }

        /// <summary>
        /// Returns the total bytes received from this server.
        /// </summary>
        public int BytesIn
        {
            get 
            {
                int bytesIn = 0;
                try
                {
                    for (int i = 0; i < fListeners.Count; i++)
                    {
                        try
                        {
                            bytesIn += ((TCPListener)fListeners[i]).BytesIn;
                        }
                        catch (Exception ex)
                        {
                        }
                    }
                }
                catch (Exception ex)
                {
                }
                return bytesIn;
            }
        }

        /// <summary>
        /// Returns the total bytes send from this server.
        /// </summary>
        public int BytesOut
        {
            get
            {
                int bytesOut = 0;
                try
                {
                    for (int i = 0; i < fListeners.Count; i++)
                    {
                        try
                        {
                            bytesOut += ((TCPListener)fListeners[i]).BytesOut;
                        }
                        catch (Exception ex)
                        {
                        }
                    }
                }
                catch (Exception ex)
                {
                }
                return bytesOut;
            }
        }

    }
}
