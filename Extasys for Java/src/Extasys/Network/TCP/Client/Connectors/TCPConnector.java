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

import Extasys.DataFrame;
import Extasys.Network.TCP.Client.Connectors.Packets.IncomingTCPClientPacket;
import Extasys.Network.TCP.Client.Connectors.Packets.MessageCollectorTCPClientPacket;
import Extasys.Network.TCP.Client.Connectors.Packets.OutgoingTCPClientPacket;
import Extasys.Network.TCP.Client.ExtasysTCPClient;
import Extasys.Network.TCP.Client.Connectors.Tools.TCPClientMessageCollector;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 *
 * @author Nikos Siatras
 */
public class TCPConnector
{

    public ExtasysTCPClient fMyTCPClient;
    private final String fName;
    private final InetAddress fServerIP;
    private final int fServerPort;
    private boolean fActive;
    protected boolean fIsConnected = false;
    //Socket properties.
    public Socket fConnection;
    private final int fReadBufferSize;
    public InputStream fInput;
    public OutputStream fOutput;
    protected final Object fSendDataLock = new Object();
    protected final Object fReceiveDataLock = new Object();
    private ReadIncomingDataThread fReadIncomingDataThread;
    //Data throughput.
    public long fBytesIn = 0, fBytesOut = 0;
    //Message collector properties.
    private boolean fUseMessageCollector;
    private String fETX;
    public TCPClientMessageCollector fMessageCollector;
    //Messages IO.
    public IncomingTCPClientPacket fLastIncomingPacket = null;
    public MessageCollectorTCPClientPacket fLastMessageCollectorPacket = null;
    private OutgoingTCPClientPacket fLastOutgoingPacket = null;

    private final Charset fCharset;

    /**
     * Constructs a new TCP Connector.
     *
     * @param myTCPClient is the TCP connector's main Extasys TCP Client.
     * @param name is the connector's name.
     * @param serverIP is the server's IP address the connector will use to
     * connect.
     * @param serverPort is the server's TCP port the connector will use to
     * connect.
     * @param readBufferSize is the read buffer size in bytes for this
     * connection.
     * @param charset is the charset to use for this TCPConnector
     */
    public TCPConnector(ExtasysTCPClient myTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize, Charset charset)
    {
        fMyTCPClient = myTCPClient;
        fName = name;
        fServerIP = serverIP;
        fServerPort = serverPort;
        fReadBufferSize = readBufferSize;
        fCharset = charset;
    }

    /**
     * Constructs a new TCP Connector with message collector use (ETX).
     *
     * @param myTCPClient is the TCP connector's main Extasys TCP Client.
     * @param name is the connector's name.
     * @param serverIP is the server's IP address the connector will connect to.
     * @param serverPort is the server's TCP port the connector will connect to.
     * @param readBufferSize is the read buffer size in bytes for this
     * connection.
     * @param charset is the charset to use for this TCPConnector
     * @param ETX is the End of Text character.
     */
    public TCPConnector(ExtasysTCPClient myTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize, Charset charset, char ETX)
    {
        fMyTCPClient = myTCPClient;
        fName = name;
        fServerIP = serverIP;
        fServerPort = serverPort;
        fReadBufferSize = readBufferSize;
        fCharset = charset;

        fUseMessageCollector = true;
        fETX = String.valueOf(ETX);
        fMessageCollector = new TCPClientMessageCollector(this, ETX);
    }

    /**
     * Constructs a new TCP Connector with message collector use (Splitter).
     *
     * @param myTCPClient is the TCP connector's main Extasys TCP Client.
     * @param name is the connector's name.
     * @param serverIP is the server's IP address the connector will connect to.
     * @param serverPort is the server's TCP port the connector will connect to.
     * @param readBufferSize is the read buffer size in bytes for this
     * connection.
     * @param splitter is the message splitter.
     */
    public TCPConnector(ExtasysTCPClient myTCPClient, String name, InetAddress serverIP, int serverPort, int readBufferSize, Charset charset, String splitter)
    {
        fMyTCPClient = myTCPClient;
        fName = name;
        fServerIP = serverIP;
        fServerPort = serverPort;
        fReadBufferSize = readBufferSize;
        fCharset = charset;

        fUseMessageCollector = true;
        fETX = splitter;
        fMessageCollector = new TCPClientMessageCollector(this, splitter);
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
            fLastMessageCollectorPacket = null;
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
                if (fLastMessageCollectorPacket != null)
                {
                    fLastMessageCollectorPacket.fDone.WaitOne();
                }
                else if (fLastIncomingPacket != null)
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

            if (fLastMessageCollectorPacket != null)
            {
                fLastMessageCollectorPacket.Cancel();
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

            if (fUseMessageCollector)
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
            byte[] bytes = data.getBytes(fCharset);
            SendData(bytes, 0, bytes.length);
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
     * @param offset is the position in the data buffer at witch to begin
     * sending.
     * @param length is the number of the bytes to be send.
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException
     */
    public void SendData(byte[] bytes, int offset, int length) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fSendDataLock)
        {
            if (fIsConnected)
            {
                fLastOutgoingPacket = new OutgoingTCPClientPacket(this, bytes, offset, length, fLastOutgoingPacket);
            }
            else
            {
                throw new ConnectorDisconnectedException(this);
            }
        }
    }

    /**
     * Send data to server and wait until data transfer complete.
     *
     * @param data is the string data to send
     * @throws ConnectorDisconnectedException
     */
    public void SendDataSynchronous(String data) throws ConnectorDisconnectedException
    {
        byte[] bytes = data.getBytes(fCharset);
        SendDataSynchronous(bytes, 0, bytes.length);
    }

    /**
     * Send data to server and wait until data transfer complete.
     *
     * @param bytes is the byte array to be send.
     * @param offset is the position in the data buffer at witch to begin
     * sending.
     * @param length is the number of the bytes to be send.
     * @throws ConnectorDisconnectedException
     */
    public void SendDataSynchronous(byte[] bytes, int offset, int length) throws ConnectorDisconnectedException
    {
        try
        {
            fOutput.write(bytes, offset, length);
            fBytesOut += length;
        }
        catch (IOException ex)
        {
            Stop();
            throw new ConnectorDisconnectedException(this);
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
     * Returns the active state of this connector.
     *
     * @return True if connector is active and connected.
     */
    public boolean isActive()
    {
        return fActive;
    }

    /**
     * Returns the name of this connector.
     *
     * @return the name of this connector
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Returns the remote server's IP address.
     *
     * @return the remote server's IP address
     */
    public InetAddress getServerIPAddress()
    {
        return fServerIP;
    }

    /**
     * Returns the remote server TCP port.
     *
     * @return the remote server TCP port.
     */
    public int getServerPort()
    {
        return fServerPort;
    }

    /**
     * Returns the read buffer size of the connection.
     *
     * @return the read buffer size of the connection
     */
    public int getReadBufferSize()
    {
        return fReadBufferSize;
    }

    /**
     * Return the number of bytes received from this connector.
     *
     * @return the number of bytes received from this connector.
     */
    public long getBytesIn()
    {
        return fBytesIn;
    }

    /**
     * Returns the number of bytes send from this connector.
     *
     * @return the number of bytes send from this connector.
     */
    public long getBytesOut()
    {
        return fBytesOut;
    }

    /**
     * Returns the active state of the message collector.
     *
     * @return True if this connector uses message collector.
     */
    public boolean isMessageCollectorInUse()
    {
        return fUseMessageCollector;
    }

    /**
     * Returns the message collector of this connector.
     *
     * @return the message collector of this connector.
     */
    public TCPClientMessageCollector getMyMessageCollector()
    {
        return fMessageCollector;
    }

    /**
     * Returns message collector's splitter in string format.
     *
     * @return the message collector's splitter in string format.
     */
    public String getMessageSplitter()
    {
        return fETX;
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
     * Return's the charset of this TCPConnector
     *
     * @return
     */
    public Charset getCharset()
    {
        return fCharset;
    }
}

/**
 * This thread reads the incoming data.
 *
 * @author Nikos Siatras
 */
class ReadIncomingDataThread extends Thread
{

    private TCPConnector fMyTCPConnector;
    private byte[] fReadBuffer;
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
        int bytesRead = 0;

        while (fActive)
        {
            try
            {
                bytesRead = fMyTCPConnector.fInput.read(fReadBuffer);
                fMyTCPConnector.fBytesIn += bytesRead;
                if (bytesRead > 0)
                {
                    if (!fMyTCPConnector.isMessageCollectorInUse()) //No message collector.
                    {
                        try
                        {
                            synchronized (fMyTCPConnector.fReceiveDataLock)
                            {
                                fMyTCPConnector.fLastIncomingPacket = new IncomingTCPClientPacket(fMyTCPConnector, new DataFrame(fReadBuffer, 0, bytesRead), fMyTCPConnector.fLastIncomingPacket);
                            }
                        }
                        catch (Exception ex)
                        {
                            fMyTCPConnector.Stop();
                        }
                    }
                    else //Message collector is enabled.
                    {
                        try
                        {
                            synchronized (fMyTCPConnector.fReceiveDataLock)
                            {
                                fMyTCPConnector.fLastMessageCollectorPacket = new MessageCollectorTCPClientPacket(fMyTCPConnector, Arrays.copyOfRange(fReadBuffer, 0, bytesRead), fMyTCPConnector.fLastMessageCollectorPacket);
                            }
                        }
                        catch (Exception ex)
                        {
                            fMyTCPConnector.Stop();
                        }
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
