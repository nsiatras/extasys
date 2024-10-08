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
package Extasys.Network.TCP.Client.Connectors;

import Extasys.MessageCollector.MessageETX;
import Extasys.Network.Abstract.AbstractConnector;
import Extasys.Network.TCP.Client.Connectors.Packets.*;
import Extasys.Network.TCP.Client.ExtasysTCPClient;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * @author Nikos Siatras
 */
public class TCPConnector extends AbstractConnector
{

    protected ExtasysTCPClient fMyTCPClient;

    // Socket properties.
    public Socket fConnection;
    public InputStream fInput;
    public OutputStream fOutput;
    private ReadIncomingDataThread fReadIncomingDataThread;

    protected boolean fIsConnected = false;

    // Message collector properties.
    public TCPConnectorMessageCollector fMessageCollector;
    private MessageETX fMessageETX = null;
    private boolean fAutoApplyMessageSplitter = false;

    /**
     * Constructs a new TCP Connector
     *
     * @param myExtasysTCPClient is the ExtasysTCPClient of this connector
     * @param name is the connector's name
     * @param serverIP is the server's IP address the connector will use to
     * connect
     * @param serverPort is the server's TCP port the connector will use to
     * connect
     * @param readBufferSize is the read buffer size in bytes for this
     * connection
     *
     */
    public TCPConnector(ExtasysTCPClient myExtasysTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize)
    {
        Initialize(myExtasysTCPClient, name, serverIP, serverPort, readBufferSize, null);
    }

    /**
     * Constructs a new TCP Connector
     *
     * @param myExtasysTCPClient is the ExtasysTCPClient of this connector
     * @param name is the connector's name
     * @param serverIP is the server's IP address the connector will use to
     * connect
     * @param serverPort is the server's TCP port the connector will use to
     * connect
     * @param readBufferSize is the read buffer size in bytes for this
     * connection
     * @param splitter is the message splitter character
     */
    public TCPConnector(ExtasysTCPClient myExtasysTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize, char splitter)
    {
        Initialize(myExtasysTCPClient, name, serverIP, serverPort, readBufferSize, String.valueOf(splitter).getBytes());
    }

    /**
     * Constructs a new TCP Connector
     *
     * @param myExtasysTCPClient is the ExtasysTCPClient of this connector
     * @param name is the connector's name
     * @param serverIP is the server's IP address the connector will use to
     * connect
     * @param serverPort is the server's TCP port the connector will use to
     * connect
     * @param readBufferSize is the read buffer size in bytes for this
     * connection
     * @param splitter is the message splitter string
     */
    public TCPConnector(ExtasysTCPClient myExtasysTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize, String splitter)
    {
        Initialize(myExtasysTCPClient, name, serverIP, serverPort, readBufferSize, splitter.getBytes());
    }

    private void Initialize(ExtasysTCPClient myExtasysTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize, byte[] splitter)
    {
        fMyTCPClient = myExtasysTCPClient;
        fName = name;
        fServerIP = serverIP;
        fServerPort = serverPort;
        fReadBufferSize = readBufferSize;
        fMessageETX = (splitter != null) ? new MessageETX(splitter) : null;
        fMessageCollector = (splitter != null) ? new TCPConnectorMessageCollector(this, fMessageETX) : null;
        fBytesIn = 0;
        fBytesOut = 0;
    }

    /**
     * Start the connector (connect to the server).
     *
     * @throws java.lang.Exception
     */
    public void Start() throws Exception
    {
        if (!fActive)
        {
            fActive = true;
            try
            {
                fConnection = new Socket(fServerIP, fServerPort);
                fInput = fConnection.getInputStream();
                fOutput = fConnection.getOutputStream();
            }
            catch (Exception ex)
            {
                Stop();
                throw new Exception(ex.getMessage());
            }

            fLastIncomingPacket = null;
            fLastOutgoingPacket = null;

            try
            {
                fReadIncomingDataThread = new ReadIncomingDataThread(this);
                fReadIncomingDataThread.start();
            }
            catch (Exception ex)
            {
                Stop();
                throw new Exception(ex.getMessage());
            }
        }
    }

    /**
     * Stop the connector (disconnect from the server).
     */
    public void Stop()
    {
        Stop(false);
    }

    /**
     * Force connector stop (disconnect from the server).
     */
    public void ForceStop()
    {
        Stop(true);
    }

    private void Stop(boolean force)
    {
        if (fActive)
        {
            if (!force)
            {
                // Wait to process incoming and outgoing TCP Packets
                if (fLastIncomingPacket != null)
                {
                    fLastIncomingPacket.fDone.WaitOne();
                }

                if (fLastOutgoingPacket != null)
                {
                    fLastOutgoingPacket.fDone.WaitOne();
                }
            }

            try
            {
                if (fConnection != null)
                {
                    fConnection.close();
                }
            }
            catch (Exception ex)
            {
            }

            try
            {
                if (fInput != null)
                {
                    fInput.close();
                }
            }
            catch (Exception ex)
            {
            }
            fInput = null;

            try
            {
                if (fOutput != null)
                {
                    fOutput.close();
                }
            }
            catch (Exception ex)
            {
            }
            fOutput = null;

            if (fLastIncomingPacket != null)
            {
                fLastIncomingPacket.Cancel();
            }

            if (fLastOutgoingPacket != null)
            {
                fLastOutgoingPacket.Cancel();
            }

            fActive = false;
            fIsConnected = false;

            if (fReadIncomingDataThread != null)
            {
                fReadIncomingDataThread.Dispose();
            }

            if (fMessageCollector != null)
            {
                fMessageCollector.Dispose();
                fMessageCollector = null;
            }

            try
            {
                fMyTCPClient.OnDisconnect(this);
            }
            catch (Exception ex)
            {
            }
            fConnection = null;
        }
    }

    /**
     * Send string data to server.
     *
     * @param data is the string to send.
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException
     */
    public void SendData(String data) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        try
        {
            byte[] bytes = data.getBytes();
            SendData(bytes);
        }
        catch (Exception ex)
        {
            throw new ConnectorCannotSendPacketException(this, null);
        }
    }

    /**
     * Send data to server.
     *
     * @param bytes is the byte array to be send.
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException
     */
    public void SendData(byte[] bytes) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fSendDataLock)
        {
            if (fIsConnected)
            {
                fLastOutgoingPacket = new OutgoingTCPClientPacket(this, bytes, fLastOutgoingPacket);
            }
            else
            {
                throw new ConnectorDisconnectedException(this);
            }
        }
    }

    /**
     * Returns the main Extasys TCP Client of the connector.
     *
     * @return the main Extasys TCP Client of the connector.
     */
    public ExtasysTCPClient getMyExtasysTCPClient()
    {
        return fMyTCPClient;
    }

    /**
     * Returns the active state of the message collector.
     *
     * @return True if this connector uses message collector.
     */
    public boolean isUsingMessageCollector()
    {
        return fMessageETX != null;
    }

    /**
     * Returns the message collector of this connector.
     *
     * @return the message collector of this connector.
     */
    public TCPConnectorMessageCollector getMyMessageCollector()
    {
        return fMessageCollector;
    }

    /**
     * Returns message collector's splitter
     *
     * @return the message collector's splitter.
     */
    public MessageETX getMessageETX()
    {
        return fMessageETX;
    }

    /**
     * Returns True if this connector is connected to the host.
     *
     * @return True if this connector is connected to the host.
     */
    public boolean isConnected()
    {
        return fIsConnected;
    }

    /**
     * Returns true if this TCP Connector automatically applies Message Splitter
     * to outgoing messages
     *
     * @return
     */
    public boolean isAutoApplyMessageSplitterOn()
    {
        return fAutoApplyMessageSplitter;
    }

    /**
     * Sets the Auto-Apply Message Splitter state. If this is set to true then
     * Extasys will automatically apply the Message Collector splitter to all
     * outgoing messages of this TCP Connector.
     *
     * @param value
     */
    public void setAutoApplyMessageSplitterState(boolean value)
    {
        fAutoApplyMessageSplitter = value;
    }

}

/**
 * This thread reads the incoming data.
 *
 * @author Nikos Siatras
 */
class ReadIncomingDataThread extends Thread
{

    private final TCPConnector fMyTCPConnector;
    private final byte[] fReadBuffer;
    private boolean fActive = false;

    public ReadIncomingDataThread(TCPConnector myTCPConnector)
    {
        fMyTCPConnector = myTCPConnector;
        fReadBuffer = new byte[myTCPConnector.getReadBufferSize()];

        if (!fMyTCPConnector.fIsConnected)
        {
            fMyTCPConnector.fIsConnected = true;
            fMyTCPConnector.getMyExtasysTCPClient().OnConnect(fMyTCPConnector);
        }
    }

    @Override
    public void start()
    {
        fActive = true;
        super.start();
    }

    public void Dispose()
    {
        fActive = false;
        try
        {
            this.interrupt();
        }
        catch (Exception ex)
        {
        }
    }

    @Override
    public void run()
    {
        if (fMyTCPConnector.isUsingMessageCollector())
        {
            ConnectionWithMessageCollector();
        }
        else
        {
            ConnectionWithoutMessageCollector();
        }

    }

    private void ConnectionWithMessageCollector()
    {
        int bytesRead;

        while (fActive)
        {
            try
            {
                bytesRead = fMyTCPConnector.fInput.read(fReadBuffer);

                if (bytesRead > 0)
                {
                    fMyTCPConnector.fBytesIn += bytesRead;
                    fMyTCPConnector.fMyTCPClient.fTotalBytesIn += bytesRead;

                    try
                    {
                        synchronized (fMyTCPConnector.fReceiveDataLock)
                        {
                            fMyTCPConnector.fLastIncomingPacket = new MessageCollectorTCPClientPacket(fMyTCPConnector, Arrays.copyOfRange(fReadBuffer, 0, bytesRead), fMyTCPConnector.fLastIncomingPacket);
                        }
                    }
                    catch (Exception ex)
                    {
                        fMyTCPConnector.Stop();
                    }
                }
                else
                {
                    fMyTCPConnector.Stop();
                }
            }
            catch (IOException ex)
            {
                fMyTCPConnector.Stop();
            }
            catch (Exception ex)
            {
            }
        }
    }

    private void ConnectionWithoutMessageCollector()
    {
        int bytesRead;

        while (fActive)
        {
            try
            {
                bytesRead = fMyTCPConnector.fInput.read(fReadBuffer);

                if (bytesRead > 0)
                {
                    fMyTCPConnector.fBytesIn += bytesRead;
                    fMyTCPConnector.fMyTCPClient.fTotalBytesIn += bytesRead;

                    try
                    {
                        synchronized (fMyTCPConnector.fReceiveDataLock)
                        {
                            fMyTCPConnector.fLastIncomingPacket = new IncomingTCPClientPacket(fMyTCPConnector, Arrays.copyOfRange(fReadBuffer, 0, bytesRead), fMyTCPConnector.fLastIncomingPacket);
                        }
                    }
                    catch (Exception ex)
                    {
                        fMyTCPConnector.Stop();
                    }
                }
                else
                {
                    fMyTCPConnector.Stop();
                }
            }
            catch (IOException ex)
            {
                fMyTCPConnector.Stop();
            }
            catch (Exception ex)
            {
            }
        }
    }

}
