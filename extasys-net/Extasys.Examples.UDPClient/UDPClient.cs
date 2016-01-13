using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Extasys.Examples.UDPClient
{
    public class UDPClient : Extasys.Network.UDP.Client.ExtasysUDPClient
    {
        public UDPClient(string name, string description)
            : base(name, description)
        {

        }

        public override void OnDataReceive(Extasys.Network.UDP.Client.Connectors.UDPConnector connector, Extasys.Network.DatagramPacket packet)
        {
            Console.WriteLine(packet.ServerEndPoint.Address.ToString() + " : " + Encoding.Default.GetString(packet.Bytes));
        }
    }
}
