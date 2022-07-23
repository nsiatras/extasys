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
using System.Net;

namespace Extasys.Network
{
    public class DatagramPacket
    {
        private byte[] fBytes;
        private int fLength;
        private IPEndPoint fServerIPEndPoint;

        public DatagramPacket(byte[] bytes, IPEndPoint serverIPEndPoint)
        {
            fBytes = bytes;
            fLength = bytes.Length;
            fServerIPEndPoint = serverIPEndPoint;
        }

        public DatagramPacket(byte[] bytes, int offset, int length, IPEndPoint serverIPEndPoint)
        {
            fBytes = new byte[length];
            Buffer.BlockCopy(bytes, offset, fBytes, 0, length);
            fLength = length;
            fServerIPEndPoint = serverIPEndPoint;
        }

        public byte[] Bytes
        {
            get { return fBytes; }
        }

        public int Length
        {
            get { return fLength; }
        }

        public IPEndPoint ServerEndPoint
        {
            get { return fServerIPEndPoint; }
        }

    }
}
