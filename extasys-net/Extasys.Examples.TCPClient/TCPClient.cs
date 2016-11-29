using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Extasys.Network.TCP.Client.Exceptions;

namespace Extasys.Examples.TCPClient
{
    public class TCPClient:Extasys.Network.TCP.Client.ExtasysTCPClient
    {
        private Thread fAutoSendMessages;
        public TCPClient(string name,string description)
            :base(name,description)
        {
        }

        public override void OnDataReceive(Extasys.Network.TCP.Client.Connectors.TCPConnector connector, Extasys.Network.DataFrame data)
        {
            string dataReceived = Encoding.ASCII.GetString(data.Bytes);

        }

        public override void OnConnect(Extasys.Network.TCP.Client.Connectors.TCPConnector connector)
        {
            Console.WriteLine("Connected to server");
        }

        public override void OnDisconnect(Extasys.Network.TCP.Client.Connectors.TCPConnector connector)
        {
            Console.WriteLine("Disconnected from server");
        }

        public void StartSendingMessages()
        {
            if (fAutoSendMessages == null)
            {
                fAutoSendMessages = new Thread(new ThreadStart(AutoSendMessages));
                fAutoSendMessages.Start();
            }
            else
            {
                fAutoSendMessages.Abort();
                fAutoSendMessages = new Thread(new ThreadStart(AutoSendMessages));
                fAutoSendMessages.Start();
            }
        }

        public void StopSendingMessages()
        {
            try
            {
                fAutoSendMessages.Abort();
            }
            catch (Exception ex)
            {
            }
        }

        private void AutoSendMessages()
        {
            int messageCount = 0;
            while (true)
            {
                try
                {
                    messageCount++;
                    SendData(messageCount.ToString() + ((char)2)); // Char 2 is the message splitter the server's message collector uses.
                    Thread.Sleep(2);
                }
                catch (ConnectorDisconnectedException ex)
                {
                    Console.WriteLine(ex.ConnectorInstance.Name + " connector disconnected!");
                    StopSendingMessages();
                }
                catch (ConnectorCannotSendPacketException ex)
                {
                    Console.WriteLine("Connector " + ex.ConnectorInstance.Name + " cannot send packet");
                    StopSendingMessages();
                }
                catch (Exception ex)
                {
                   StopSendingMessages();
                }
            }
        }


    }
}
