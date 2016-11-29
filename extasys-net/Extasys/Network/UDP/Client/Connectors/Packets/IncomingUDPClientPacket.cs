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

namespace Extasys.Network.UDP.Client.Connectors.Packets
{
    public class IncomingUDPClientPacket
    {
        public ManualResetEvent fDone = new ManualResetEvent(false);
        private UDPConnector fConnector;
        private DatagramPacket fData;
        private IncomingUDPClientPacket fPreviousPacket;
        public bool fCancel = false;

        public IncomingUDPClientPacket(UDPConnector connector, DatagramPacket data, IncomingUDPClientPacket previousPacket)
        {
            fConnector = connector;
            fData = data;
            fPreviousPacket = previousPacket;
            connector.fBytesIn += data.Length;
            ThreadPool.QueueUserWorkItem(new WaitCallback(ReadData));
        }

        public void Cancel()
        {
            fCancel = true;
            fDone.Set();
        }

        private void ReadData(object stateObject)
        {
            try
            {
                if (fPreviousPacket == null)
                {
                    fConnector.MyExtasysUDPClient.OnDataReceive(fConnector, fData);
                }
                else
                {
                    fPreviousPacket.fDone.WaitOne();
                    if (!fCancel && !fPreviousPacket.fCancel)
                    {
                        fConnector.MyExtasysUDPClient.OnDataReceive(fConnector, fData);
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

    }
}
