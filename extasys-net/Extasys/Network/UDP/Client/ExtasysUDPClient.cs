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
using Extasys.Network.UDP.Client.Connectors;
using System.Net;

namespace Extasys.Network.UDP.Client
{
    public class ExtasysUDPClient
    {
        private string fName;
        private string fDescription;
        private ArrayList fConnectors = new ArrayList();

        /// <summary>
        /// Constructs a new Extasys UDP Client.
        /// </summary>
        /// <param name="name">The name of the client.</param>
        /// <param name="description">The description of the client.</param>
        public ExtasysUDPClient(string name, string description)
        {
            fName = name;
            fDescription = description;
        }

        /// <summary>
        /// Add a new connector to this client.
        /// </summary>
        /// <param name="name">The name of the connector.</param>
        /// <param name="readBufferSize">The Maximum number of bytes the connector can read at a time.</param>
        /// <param name="readTimeOut">The maximum time in milliseconds the connector can use to read incoming data.</param>
        /// <param name="serverIP">The server's ip address the connector will use to send data.</param>
        /// <param name="serverPort">The server's udp port.</param>
        /// <returns>The connector.</returns>
        public UDPConnector AddConnector(string name, int readBufferSize, int readTimeOut, IPAddress serverIP, int serverPort)
        {
            UDPConnector connector = new UDPConnector(this, name, readBufferSize, readTimeOut, serverIP, serverPort);
            fConnectors.Add(connector);
            return connector;
        }

        /// <summary>
        /// Stop and remove a connector.
        /// </summary>
        /// <param name="name">The name of the connector.</param>
        public void RemoveConnector(string name)
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                if (((UDPConnector)fConnectors[i]).Name ==name)
                {
                    ((UDPConnector)fConnectors[i]).Stop();
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
            for (int i = 0; i < fConnectors.Count; i++)
            {
                ((UDPConnector)fConnectors[i]).Start();
            }
        }

        /// <summary>
        /// Stop the client.
        /// </summary>
        public void Stop()
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                ((UDPConnector)fConnectors[i]).Stop();
            }
        }

        public virtual void OnDataReceive(UDPConnector connector, DatagramPacket packet)
        {
            //Console.WriteLine("Data received: " + Encoding.Default.GetString(packet.Bytes));
        }

        /// <summary>
        /// Send data from all connector's to all hosts.
        /// </summary>
        /// <param name="data">The string to be send.</param>
        public virtual void SendData(string data)
        {
            for (int i = 0; i < fConnectors.Count; i++)
            {
                ((UDPConnector)fConnectors[i]).SendData(data);
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
                ((UDPConnector)fConnectors[i]).SendData(bytes, offset, length);
            }
        }

        /// <summary>
        /// Return the name of the client.
        /// </summary>
        public string Name
        {
            get { return fName; }
        }

        /// <summary>
        /// Return the description of the client.
        /// </summary>
        public string Description
        {
            get { return fDescription; }
        }

        /// <summary>
        /// Return ArrayList with the client's connectors.
        /// Each element of the ArrayList is a UDPConnector class.
        /// </summary>
        public ArrayList Connectors
        {
            get { return fConnectors; }
        }

        /// <summary>
        /// Returns the total bytes received.
        /// </summary>
        public int BytesIn
        {
            get 
            {
                int result = 0;
                try
                {
                    for (int i = 0; i < fConnectors.Count; i++)
                    {
                        result += ((UDPConnector)fConnectors[i]).BytesIn;
                    }
                }
                catch (Exception ex)
                {
                }
                return result;
            }
        }

        /// <summary>
        /// Returns the total bytes send.
        /// </summary>
        public int BytesOut
        {
            get
            {
                int result = 0;
                try
                {
                    for (int i = 0; i < fConnectors.Count; i++)
                    {
                        result += ((UDPConnector)fConnectors[i]).BytesOut;
                    }
                }
                catch (Exception ex)
                {
                }
                return result;
            }
        }

    }
}

