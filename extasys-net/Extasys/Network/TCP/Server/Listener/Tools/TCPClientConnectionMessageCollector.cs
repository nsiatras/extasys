﻿/*Copyright (c) 2008 Nikos Siatras

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

namespace Extasys.Network.TCP.Server.Listener.Tools
{
    public class TCPClientConnectionMessageCollector
    {
        private TCPClientConnection fMyClient;
        private String fETXStr;
        private StringBuilder fIncomingDataBuffer;
        private int fIndexOfETX;
        private int fETXLength = 0;

        public TCPClientConnectionMessageCollector(TCPClientConnection myClient, char splitter)
        {
            Initialize(myClient, splitter.ToString());
            fETXLength = 1;
        }

        public TCPClientConnectionMessageCollector(TCPClientConnection myClient, string splitter)
        {
            Initialize(myClient, splitter);
            fETXLength = splitter.Length;
        }

        private void Initialize(TCPClientConnection myClient, string splitter)
        {
            fMyClient = myClient;
            fETXStr = splitter;
            fIncomingDataBuffer = new StringBuilder();
        }

        public void AppendData(string data)
        {
            try
            {
                fIncomingDataBuffer.Append(data);
                fIndexOfETX = fIncomingDataBuffer.ToString().IndexOf(fETXStr);
                while (fIndexOfETX > -1)
                {
                    fMyClient.MyTCPListener.MyExtasysTCPServer.OnDataReceive(fMyClient, new DataFrame(Encoding.Default.GetBytes(fIncomingDataBuffer.ToString().Substring(0, fIndexOfETX))));
                    fIncomingDataBuffer = fIncomingDataBuffer.Remove(0, fIndexOfETX + fETXLength);
                    fIndexOfETX = fIncomingDataBuffer.ToString().IndexOf(fETXStr);
                }
            }
            catch (Exception ex)
            {
            }
        }

        public void Dispose()
        {
            try
            {
                fIncomingDataBuffer = null;
                fETXStr = null;
                fMyClient = null;
            }
            catch (Exception ex)
            {
            }
        }
    }
}
