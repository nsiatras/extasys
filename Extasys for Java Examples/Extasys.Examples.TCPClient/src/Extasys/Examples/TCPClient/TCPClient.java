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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nikos Siatras
 */
public class TCPClient extends Extasys.Network.TCP.Client.ExtasysTCPClient
{

    private AutoSendMessages fAutoSendMessagesThread;
    private Charset fCharset = Charset.forName("UTF-8");

    public TCPClient(String name, String description, InetAddress remoteHostIP, int remoteHostPort, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);
        try
        {
            super.AddConnector(name, remoteHostIP, remoteHostPort, 10240, "#SPLITTER#");
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
            String dataStr = new String(data.getBytes(), fCharset);

            connector.SendData(dataStr + "#SPLITTER#");
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public void OnConnect(TCPConnector connector)
    {
        //System.out.println("Connected to server");
    }

    @Override
    public void OnDisconnect(TCPConnector connector)
    {
        //System.out.println("Disconnected from server");
        StopSendingMessages();
    }

    public void StartSendingMessages()
    {
        StopSendingMessages();
        try
        {
            SendData("1#SPLITTER#");

//fAutoSendMessagesThread = new AutoSendMessages(this);
            //fAutoSendMessagesThread.start();
        }
        catch (ConnectorDisconnectedException ex)
        {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ConnectorCannotSendPacketException ex)
        {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void StopSendingMessages()
    {
        if (fAutoSendMessagesThread != null)
        {
            fAutoSendMessagesThread.Dispose();
            fAutoSendMessagesThread.interrupt();
            fAutoSendMessagesThread = null;
        }
    }
}

class AutoSendMessages extends Thread
{

    private TCPClient fMyClient;
    private boolean fActive = true;

    public AutoSendMessages(TCPClient client)
    {
        fMyClient = client;
    }

    @Override
    public void run()
    {
        int messageCount = 0;
        while (fActive)
        {
            try
            {
                messageCount++;
                fMyClient.SendData(String.valueOf(messageCount) + "#SPLITTER#"); // Char 2 is the message splitter the server's message collector uses.
                Thread.sleep(10);
            }
            catch (ConnectorDisconnectedException ex)
            {
                System.err.println(ex.getConnectorInstance().getName() + " connector disconnected!");
                fActive = false;
                fMyClient.StopSendingMessages();
            }
            catch (ConnectorCannotSendPacketException ex)
            {
                System.err.println("Connector " + ex.getConnectorInstance().getName() + " cannot send packet" + ex.getOutgoingPacket().toString());
                fActive = false;
                fMyClient.StopSendingMessages();
            }
            catch (Exception ex)
            {
                Dispose();
                fMyClient.StopSendingMessages();
            }
        }
    }

    public void Dispose()
    {
        fActive = false;
    }
}
