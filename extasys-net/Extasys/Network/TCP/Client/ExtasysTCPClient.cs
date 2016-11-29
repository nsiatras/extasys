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
using Extasys.Network.TCP.Client.Connectors;
using System.Net;

namespace Extasys.Network.TCP.Client
{
    public class ExtasysTCPClient
    {
        private string fName, fDescription;
        private ArrayList fConnectors = new ArrayList();

        public ExtasysTCPClient(string name, string description)
        {
            fName = name;
            fDescription = description;
        }

        /// <summary>
        /// Add a new connector to this client.
        /// </summary>
        /// <param name="name">The connector's name.</param>
        /// <param name="serverIP">The remote host's (server) IP address.</param>
        /// <param name="serverPort">The remote host's (server) port.</param>
        /// <param name="readBufferSize">The read buffer size for this connection in bytes.</param>
        /// <returns>The connector.</returns>
        public TCPConnector AddConnector(string name, IPAddress serverIP, int serverPort, int readBufferSize)
        {
            TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize);
            fConnectors.Add(connector);
            return connector;
        }

        /// <summary>
        /// Add a new connector with message collector.
        /// </summary>
        /// <param name="name">The connector's name.</param>
        /// <param name="serverIP">The remote host's (server) IP address.</param>
        /// <param name="serverPort">The remote host's (server) port.</param>
        /// <param name="readBufferSize">The read buffer size for this connection in bytes.</param>
        /// <param name="splitter">The the messages splitter character.</param>
        /// <returns>The connector.</returns>
        public TCPConnector AddConnector(string name, IPAddress serverIP, int serverPort, int readBufferSize, char splitter)
        {
            TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize, splitter);
            fConnectors.Add(connector);
            return connector;
        }

        /// <summary>
        /// Add a new connector with message collector.
        /// </summary>
        /// <param name="name">The connector's name.</param>
        /// <param name="serverIP">The remote host's (server) IP address.</param>
        /// <param name="serverPort">The remote host's (server) port.</param>
        /// <param name="readBufferSize">The read buffer size for this connection in bytes.</param>
        /// <param name="splitter">The the messages splitter string.</param>
        /// <returns>The connector.</returns>
        public TCPConnector AddConnector(string name, IPAddress serverIP, int serverPort, int readBufferSize, string splitter)
        {
            TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize, splitter);
            fConnectors.Add(connector);
            return connector;
        }

        /// <summary>
        /// Stop and remove a connector from this client.
        /// </summary>
        /// <param name="connectorName">Connector to remove.</param>
        public void RemoveConnector(string connectorName)
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                if (((TCPConnector)fConnectors[i]).Name.Equals(connectorName))
                {
                    ((TCPConnector)fConnectors[i]).Stop();
                    fConnectors.RemoveAt(i);
                    break;
                }
            }
        }

        /// <summary>
        /// Start or restart the client.
        /// </summary>
        public void Start()
        {
            Stop();

            try
            {
                for (int i = 0; i < fConnectors.Count; i++)
                {
                    ((TCPConnector)fConnectors[i]).Start();
                }
            }
            catch (Exception ex)
            {
                Stop();
                throw ex;
            }
        }

        /// <summary>
        /// Stop the client.
        /// </summary>
        public void Stop()
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                ((TCPConnector)fConnectors[i]).Stop();
            }
        }

        /// <summary>
        /// A connector of this client receives data.
        /// </summary>
        /// <param name="connector">The client's connector.</param>
        /// <param name="data">The received data.</param>
        public virtual void OnDataReceive(TCPConnector connector, DataFrame data)
        {
            Console.WriteLine(Encoding.Default.GetString(data.Bytes));
        }

        /// <summary>
        /// A connector of this client connected to a server.
        /// </summary>
        /// <param name="connector"></param>
        public virtual void OnConnect(TCPConnector connector)
        {
            // Console.WriteLine("Connector " + connector.Name + " connected!");
        }

        /// <summary>
        /// A connector of this client has been disconnected.
        /// </summary>
        /// <param name="connector"></param>
        public virtual void OnDisconnect(TCPConnector connector)
        {
            // Console.WriteLine("Connector " + connector.Name + " disconnected!");
        }

        /// <summary>
        /// Send data from all connector's to all hosts.
        /// </summary>
        /// <param name="data">String data to be send.</param>
        public virtual void SendData(string data)
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                ((TCPConnector)fConnectors[i]).SendData(data);
            }
        }

        /// <summary>
        /// Send data from all connector's to all hosts.
        /// </summary>
        /// <param name="bytes">The byte array to be send.</param>
        /// <param name="offset">The position in the data buffer at witch to begin sending.</param>
        /// <param name="length">The number of the bytes to be send.</param>
        public virtual void SendData(byte[] bytes, int offset, int length)
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                ((TCPConnector)fConnectors[i]).SendData(bytes, offset, length);
            }
        }

        /// <summary>
        /// Returns the name of this client.
        /// </summary>
        public string Name
        {
            get { return fName; }
            set { fName = value; }
        }

        /// <summary>
        /// Returns the description of this client.
        /// </summary>
        public string Description
        {
            get { return fDescription; }
            set { fDescription = value; }
        }

        /// <summary>
        /// Return ArrayList with the client's connectors.
        /// Each element of the ArrayList is a TCPConnector class.
        /// </summary>
        /// <returns>ArrayList with the client's connectors.</returns>
        public ArrayList Connectors
        {
            get { return fConnectors; }
        }

        /// <summary>
        /// Returns the total number of bytes received from all the connectors of the client.
        /// </summary>
        public int BytesIn
        {
            get
            {
                int bytesIn = 0;
                try
                {
                    for (int i = 0; i < fConnectors.Count; i++)
                    {
                        bytesIn += ((TCPConnector)fConnectors[i]).BytesIn;
                    }
                }
                catch (Exception ex)
                {
                }
                return bytesIn;
            }
        }

        /// <summary>
        /// Returns the total number of bytes send from all the connectors of the client.
        /// </summary>
        public int BytesOut
        {
            get
            {
                int bytesOut = 0;
                try
                {
                    for (int i = 0; i < fConnectors.Count; i++)
                    {
                        bytesOut += ((TCPConnector)fConnectors[i]).BytesOut;
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
