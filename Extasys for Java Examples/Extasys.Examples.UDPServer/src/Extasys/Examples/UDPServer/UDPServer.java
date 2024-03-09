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
package Extasys.Examples.UDPServer;

import Extasys.Encryption.Base64Encryptor;
import Extasys.Network.UDP.Server.Listener.UDPListener;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras
 */
public class UDPServer extends Extasys.Network.UDP.Server.ExtasysUDPServer
{

    public UDPServer(String name, String description, InetAddress listenerIP, int port, int connectionsTimeOut, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);
        UDPListener listener = this.AddListener("My UDP Listener", listenerIP, port, 10240, connectionsTimeOut);
        //listener.setConnectionEncryptor(new Base64Encryptor());
    }

    @Override
    public void OnDataReceive(UDPListener listener, DatagramPacket packet)
    {
        System.out.println("Data received from " + packet.getAddress().toString() + ": " + new String(packet.getData()));

        try
        {
            // Send data back to the sender.
            listener.SendData(new DatagramPacket(packet.getData(), 0, packet.getLength(), packet.getAddress(), packet.getPort()));
        }
        catch (Exception ex)
        {
        }
    }
}
