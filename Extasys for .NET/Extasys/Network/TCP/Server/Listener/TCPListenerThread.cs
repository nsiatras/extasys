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
using System.Net.Sockets;

namespace Extasys.Network.TCP.Server.Listener
{
    public class TCPListenerThread  
    {
        private Thread fListenThread;
        private TcpListener fSocket;
        private TCPListener fMyListener;
        private bool fThreadIsRunning = false;

        private ManualResetEvent fClientConnectionCompleted = new ManualResetEvent(false);

        public TCPListenerThread(TcpListener socket, TCPListener myListener)
        {
            fSocket = socket;
            fMyListener = myListener;
        }

        public void Start()
        {
            fThreadIsRunning = true;
            fListenThread = new Thread(new ThreadStart(DoListen));
            fListenThread.Start();
        }

        private void DoListen()
        {
            fClientConnectionCompleted.Reset();
            try
            {
                fSocket.BeginAcceptSocket(new AsyncCallback(DoAcceptSocketCallback), fSocket);
                fClientConnectionCompleted.WaitOne();
            }
            catch (Exception ex)
            {
                fClientConnectionCompleted.Set();
            }
        }

        
        private void DoAcceptSocketCallback(IAsyncResult ar)
        {
            if (fThreadIsRunning)
            {
                try
                {
                    TcpListener listener = (TcpListener)ar.AsyncState;

                    TCPClientConnection client = new TCPClientConnection(listener.EndAcceptSocket(ar), fMyListener, fMyListener.IsMessageCollectorInUse, fMyListener.MessageSplitter);
                    if (fMyListener.ConnectedClients.Count >= fMyListener.MaxConnections)
                    {
                        client.DisconnectMe();
                    }
                }
                catch (Exception ex)
                {
                }

                DoListen();
            }
        }

        public void Stop()
        {
            fThreadIsRunning = false;
            fClientConnectionCompleted.Set();
        }

    }
}
