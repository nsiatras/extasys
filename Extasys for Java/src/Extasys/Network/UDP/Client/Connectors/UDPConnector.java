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
package Extasys.Network.UDP.Client.Connectors;

import Extasys.DataConvertion.DataConverter;
import Extasys.DataConvertion.NullDataConverter;
import Extasys.Network.UDP.Client.Connectors.Packets.*;
import Extasys.Network.UDP.Client.ExtasysUDPClient;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author Nikos Siatras
 */
public class UDPConnector
{

    private boolean fActive = false;
    private final ExtasysUDPClient fMyUDPClient; //Extasys UDP Client reference.
    public DatagramSocket fSocket;
    private final InetAddress fServerIP;
    private final int fServerPort;
    private Thread fReadDataThread;
    private final String fName;
    private final int fReadBufferSize;
    private final int fReadTimeOut;
    public long fBytesIn = 0, fBytesOut = 0;

    public IncomingUDPClientPacket fLastIncomingPacket = null;
    public OutgoingUDPClientPacket fLastOutgoingPacket = null;

    // Connection DataConverter (Encryption, Encoding, Compression)
    private DataConverter fConnectionDataConverter = new NullDataConverter();

    /**
     * Constructs a new UDP Connector.
     *
     * @param myClient is the connectors main Extasys UDP Client.
     * @param name is the name of the connector.
     * @param readBufferSize is the maximum number of bytes the socket can read
     * at a time.
     * @param readTimeOut is the maximum time in milliseconds in which a
     * datagram packet can be received. Set to 0 for no time-out.
     * @param serverIP is the server's IP address the connector will use to send
     * data.
     * @param serverPort is the server's UDP port.
     */
    public UDPConnector(ExtasysUDPClient myClient, String name, int readBufferSize, int readTimeOut, InetAddress serverIP, int serverPort)
    {
        fMyUDPClient = myClient;
        fName = name;
        fReadBufferSize = readBufferSize;
        fReadTimeOut = readTimeOut;
        fServerIP = serverIP;
        fServerPort = serverPort;
    }

    /**
     * Start the UDP connector.
     *
     * @throws java.net.SocketException
     */
    public void Start() throws SocketException, Exception
    {
        if (!fActive)
        {
            try
            {
                fActive = true;
                try
                {
                    fSocket = new DatagramSocket();
                }
                catch (SocketException ex)
                {
                    fActive = false;
                    throw ex;
                }

                fSocket.setReceiveBufferSize(fReadBufferSize);
                fSocket.setSendBufferSize(fReadBufferSize);

                fLastIncomingPacket = null;
                fLastOutgoingPacket = null;

                if (fReadTimeOut > 0)
                {
                    fSocket.setSoTimeout(fReadTimeOut);
                }

                try
                {
                    fReadDataThread = new ReadIncomingData(this);
                    fReadDataThread.start();
                }
                catch (Exception ex)
                {
                    throw ex;
                }
            }
            catch (SocketException ex)
            {
                Stop();
                throw ex;
            }
        }
    }

    /**
     * Stop the UDP connector.
     */
    public void Stop()
    {
        if (fActive)
        {
            fActive = false;
            try
            {
                fSocket.close();
            }
            catch (Exception ex)
            {
            }

            if (fLastIncomingPacket != null)
            {
                fLastIncomingPacket.Cancel();
            }

            if (fLastOutgoingPacket != null)
            {
                fLastOutgoingPacket.Cancel();
            }

            try
            {
                fReadDataThread.interrupt();
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Send data to all host.
     *
     * @param data is the string to be send.
     * @throws java.io.IOException
     */
    public void SendData(String data) throws IOException
    {
        SendData(data.getBytes());
    }

    /**
     * Send data to host.
     *
     * @param bytes is the byte array to be send.
     * @throws java.io.IOException
     */
    public void SendData(byte[] bytes) throws IOException
    {
        DatagramPacket outPacket = new DatagramPacket(bytes, 0, bytes.length, fServerIP, fServerPort);
        fLastOutgoingPacket = new OutgoingUDPClientPacket(this, outPacket, fLastOutgoingPacket);
    }

    public boolean isActive()
    {
        return fActive;
    }

    /**
     * Returns the main Extasys UDP Client of the connector.
     *
     * @return the main Extasys UDP Client of the connector.
     */
    public ExtasysUDPClient getMyExtasysUDPClient()
    {
        return fMyUDPClient;
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
     * Returns the read buffer size of the connection.
     *
     * @return the read buffer size of the connection
     */
    public int getReadBufferSize()
    {
        return fReadBufferSize;
    }

    /**
     * Returns the maximum time in milliseconds in which a datagram packet can
     * be received.
     *
     * @return the maximum time in milliseconds in which a datagram packet can
     * be received.
     */
    public int getReadTimeOut()
    {
        return fReadTimeOut;
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
     * Return the number of bytes send from this connector.
     *
     * @return the number of bytes send from this connector.
     */
    public long getBytesOut()
    {
        return fBytesOut;
    }

    /**
     * Returns the connection's Data Converter
     *
     * @return
     */
    public DataConverter getConnectionDataConverter()
    {
        return fConnectionDataConverter;
    }

    /**
     * Sets the connection's Data Converter of this UDPConnector
     *
     * @param dataConverter
     */
    public void setConnectionDataConverter(DataConverter dataConverter)
    {
        fConnectionDataConverter = (dataConverter == null) ? new NullDataConverter() : dataConverter;
    }

}

class ReadIncomingData extends Thread
{

    private final UDPConnector fMyConnector;

    public ReadIncomingData(UDPConnector myUDPConnector)
    {
        fMyConnector = myUDPConnector;
    }

    @Override
    public void run()
    {
        DatagramPacket receivedPacket;

        while (fMyConnector.isActive())
        {
            try
            {
                final byte[] data = new byte[fMyConnector.getReadBufferSize()];
                receivedPacket = new DatagramPacket(data, data.length);
                fMyConnector.fSocket.receive(receivedPacket);
                fMyConnector.fBytesIn += receivedPacket.getLength();
                fMyConnector.getMyExtasysUDPClient().fTotalBytesIn += receivedPacket.getLength();

                fMyConnector.fLastIncomingPacket = new IncomingUDPClientPacket(fMyConnector, receivedPacket, fMyConnector.fLastIncomingPacket);
            }
            catch (IOException ex)
            {
                fMyConnector.Stop();
            }
        }
    }
}
