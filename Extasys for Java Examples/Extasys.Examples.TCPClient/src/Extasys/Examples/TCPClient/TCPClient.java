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
import Extasys.DataConvertion.Base64Converter;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras
 */
public class TCPClient extends Extasys.Network.TCP.Client.ExtasysTCPClient
{

    private boolean fKeepSendingMessages = false;
    private int fPreviousNumber = 0;

    public TCPClient(String name, String description, InetAddress remoteHostIP, int remoteHostPort, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);

        try
        {
            // Add a new connector to this TCP Client
            TCPConnector connector = super.AddConnector(name, remoteHostIP, remoteHostPort, 8192, ((char) 3));
            connector.setAutoApplyMessageSplitterState(true); // Auto apply message splitter to outgoing messages
            connector.setConnectionDataConverter(new Base64Converter()); // Base 64 Encoding
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
            final String dataReceivedFromServer = new String(data.getBytes());

            int number = Integer.parseInt(dataReceivedFromServer);
            if (fPreviousNumber != number)
            {
                System.err.println("Message Lost");
            }
            else
            {
                fPreviousNumber += 1;
            }

            // Every time I receive data from the server I send the
            // fMessageToExchange string back
            if (fKeepSendingMessages)
            {
                connector.SendData(String.valueOf(fPreviousNumber));
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
            fPreviousNumber = 1000000000;
            SendData(String.valueOf(fPreviousNumber));
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
