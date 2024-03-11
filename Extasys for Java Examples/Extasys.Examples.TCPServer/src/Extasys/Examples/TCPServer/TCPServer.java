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
package Extasys.Examples.TCPServer;

import Extasys.DataFrame;
import Extasys.Encryption.Base64Encryptor;
import Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException;
import Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException;
import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import Extasys.Network.TCP.Server.Listener.TCPListener;
import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras
 */
public class TCPServer extends Extasys.Network.TCP.Server.ExtasysTCPServer
{

    public TCPServer(String name, String description, InetAddress listenerIP, int port, int maxConnections, int connectionsTimeOut, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);

        try
        {
            // Add a new TCPListener to the server
            TCPListener listener = super.AddListener("My listener", listenerIP, port, maxConnections, 8192, connectionsTimeOut, 100, (char) 3);
            listener.setAutoApplyMessageSplitterState(true);  // Auto apply message splitter to outgoing messages
            listener.setConnectionEncryptor(new Base64Encryptor()); // Base 64 Encryption
        }
        catch (Exception ex)
        {
        }
    }

    @Override
    public void OnDataReceive(TCPClientConnection sender, DataFrame data)
    {
        try
        {
            // I received data from a client
            final String incomingDataStr = new String(data.getBytes());

            // Send the incoming data back to the sender
            sender.SendData(incomingDataStr);
        }
        catch (ClientIsDisconnectedException | OutgoingPacketFailedException ex)
        {

        }
    }

    @Override
    public void OnClientConnect(TCPClientConnection client)
    {
        // New client connected.
        client.setName(client.getIPAddress()); // Set a name for this client if you want to.
        System.out.println(client.getIPAddress() + " connected.");
        System.out.println("Total clients connected: " + super.getCurrentConnectionsNumber());
    }

    @Override
    public void OnClientDisconnect(TCPClientConnection client)
    {
        // Client disconnected.
        System.out.println(client.getIPAddress() + " disconnected.");
    }
}
