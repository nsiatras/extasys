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
package Extasys.Examples.TCPClient;

import Extasys.DataFrame;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 *
 * @author Nikos Siatras
 */
public class TCPClient extends Extasys.Network.TCP.Client.ExtasysTCPClient
{

    private Charset fCharset = Charset.forName("UTF-8");
    private boolean fKeepSendingMessages = false;
    private String fMessageToExchange = "TESTING EXTASYS TCP SOCKET";
    private final String fMessageSplitter = "#SPLITTER#";

    public TCPClient(String name, String description, InetAddress remoteHostIP, int remoteHostPort, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);

        while (fMessageToExchange.length() < 10240)
        {
            fMessageToExchange += "!";
        }

        try
        {
            super.AddConnector(name, remoteHostIP, remoteHostPort, 8192, fMessageSplitter);
        }
        catch (Exception ex)
        {
        }
    }

    @Override
    public void OnDataReceive(TCPConnector connector, DataFrame data)
    {
        try
        {
            // String incomingData = new String(data.getBytes(), fCharset);
            if (fKeepSendingMessages)
            {
                SendData(fMessageToExchange + fMessageSplitter);
            }
        }
        catch (ConnectorCannotSendPacketException | ConnectorDisconnectedException ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void OnConnect(TCPConnector connector)
    {
        System.out.println("Connected to server");
    }

    @Override
    public void OnDisconnect(TCPConnector connector)
    {
        System.out.println("Disconnected from server");
        StopSendingMessages();
    }

    public void StartSendingMessages()
    {
        fKeepSendingMessages = true;
        try
        {
            SendData(fMessageToExchange + "#SPLITTER#");
        }
        catch (ConnectorDisconnectedException | ConnectorCannotSendPacketException ex)
        {
            fKeepSendingMessages = false;
        }
    }

    public void StopSendingMessages()
    {
        fKeepSendingMessages = false;
    }
}
