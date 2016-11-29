using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Extasys.Network.TCP.Server.Listener.Exceptions;

namespace Extasys.Examples.TCPServer
{
    public class TCPServer : Extasys.Network.TCP.Server.ExtasysTCPServer
    {
        public TCPServer(string name,string description)
            :base(name,description)
        {
        }

        public override void OnClientConnect(Extasys.Network.TCP.Server.Listener.TCPClientConnection client)
        {
            client.Name = client.IPAddress.ToString(); // Set a name for this client if you want to.
            Console.WriteLine(client.IPAddress.ToString() + " connected.");
            Console.WriteLine("Total clients connected: " + this.CurrentConnectionsNumber);
        }

        public override void OnClientDisconnect(Extasys.Network.TCP.Server.Listener.TCPClientConnection client)
        {
            
        }

        public override void OnDataReceive(Extasys.Network.TCP.Server.Listener.TCPClientConnection sender, Extasys.Network.DataFrame data)
        {
            // Client sends data to server.
            // Reply the received data back to sender.
            byte [] bytes = new byte[data.Length+1];
            Buffer.BlockCopy(data.Bytes,0,bytes,0,data.Length);
            bytes[data.Length] = (byte)((char)2);
            try
            {
                sender.SendData(bytes, 0, bytes.Length);
            }
            catch (ClientIsDisconnectedException ex)
            {
            }
            catch (OutgoingPacketFailedException ex)
            {
            }
            catch (Exception ex)
            {

            }
        }


    }
}
