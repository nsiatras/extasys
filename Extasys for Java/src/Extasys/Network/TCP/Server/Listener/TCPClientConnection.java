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
package Extasys.Network.TCP.Server.Listener;

import Extasys.MessageCollector.MessageETX;
import Extasys.Network.NetworkPacket;
import Extasys.Network.TCP.Server.ExtasysTCPServer;
import Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException;
import Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException;
import Extasys.Network.TCP.Server.Listener.Packets.IncomingTCPClientConnectionPacket;
import Extasys.Network.TCP.Server.Listener.Packets.MessageCollectorTCPClientConnectionPacket;
import Extasys.Network.TCP.Server.Listener.Packets.OutgoingTCPClientConnectionPacket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author Nikos Siatras
 */
public final class TCPClientConnection
{

    // Connection properties
    protected Socket fConnection;
    protected boolean fIsConnected = false;
    public final TCPListener fMyListener;
    public final ExtasysTCPServer fMyExtasysServer;
    private final String fIPAddress;
    private String fName = "";
    private Object fTag = null;
    private Thread fClientDataReaderThread;
    private final long fConnectionStartUpDateTime;
    // Data input-output streams.
    public InputStream fInput;
    public OutputStream fOutput;
    protected final Object fSendDataLock = new Object();
    protected final Object fReceiveDataLock = new Object();
    // Data I/O counters.
    public long fBytesIn = 0, fBytesOut = 0;
    // Message collector.
    public TCPClientConnectionMessageCollector fMyMessageCollector;
    protected final boolean fUseMessageCollector;
    // Messages IO.
    protected NetworkPacket fLastIncomingPacket = null;
    protected NetworkPacket fLastOutgoingPacket = null;

    public TCPClientConnection(Socket socket, TCPListener myTCPListener, boolean useMessageCollector, MessageETX messageETX)
    {
        fConnection = socket;

        fMyListener = myTCPListener;
        fMyExtasysServer = myTCPListener.getMyExtasysTCPServer();

        fIPAddress = (socket.getInetAddress() != null) ? socket.getInetAddress().toString() + ":" + String.valueOf(socket.getPort()) : "";

        // Initialize a new message collector or not
        fUseMessageCollector = useMessageCollector;
        fMyMessageCollector = (fUseMessageCollector) ? new TCPClientConnectionMessageCollector(this, messageETX) : null;

        // Connection startup time
        fConnectionStartUpDateTime = System.currentTimeMillis();

        try
        {
            fIsConnected = true;

            if (myTCPListener.getConnectionTimeOut() > 0) //Connection time-out.
            {
                fConnection.setSoTimeout(myTCPListener.getConnectionTimeOut());
            }

            fConnection.setReceiveBufferSize(myTCPListener.getReadBufferSize());
            fConnection.setSendBufferSize(myTCPListener.getReadBufferSize());
        }
        catch (SocketException ex)
        {
            DisconnectMe();
            return;
        }

        fMyListener.AddClient(this);
        StartReceivingAndSendingData();
    }

    private void StartReceivingAndSendingData()
    {
        try
        {
            fInput = fConnection.getInputStream();
            fOutput = fConnection.getOutputStream();
        }
        catch (IOException ex)
        {
            DisconnectMe();
            return;
        }

        fMyListener.getMyExtasysTCPServer().OnClientConnect(this);

        fClientDataReaderThread = new Thread(new ClientDataReader(this));
        fClientDataReaderThread.start();
    }

    /**
     * Send data to client.
     *
     * @param data is the string to send.
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException
     */
    public void SendData(final String data) throws ClientIsDisconnectedException, OutgoingPacketFailedException
    {
        final byte[] bytes = data.getBytes();
        SendData(bytes);
    }

    /**
     * Send data to client.
     *
     * @param bytes is the byte array to be send.
     *
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException
     */
    public void SendData(byte[] bytes) throws ClientIsDisconnectedException, OutgoingPacketFailedException
    {
        synchronized (fSendDataLock)
        {
            if (!this.fIsConnected)
            {
                throw new ClientIsDisconnectedException(this);
            }
            fLastOutgoingPacket = new OutgoingTCPClientConnectionPacket(this, bytes, fLastOutgoingPacket);
        }
    }

    /**
     * Disconnect this client.
     */
    public void DisconnectMe()
    {
        Disconnect(false);
    }

    /**
     * Client force disconnect
     */
    public void ForceDisconnect()
    {
        Disconnect(true);
    }

    /**
     * Disconnect this client.
     */
    private void Disconnect(boolean force)
    {
        if (fIsConnected)
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

            fIsConnected = false;

            try
            {
                fInput.close();
            }
            catch (IOException ex)
            {
            }

            try
            {
                fOutput.close();
            }
            catch (IOException ex)
            {
            }

            try
            {
                fConnection.close();
            }
            catch (IOException ex)
            {
            }

            if (fUseMessageCollector && fMyMessageCollector != null)
            {
                fMyMessageCollector.Dispose();
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
                fClientDataReaderThread.interrupt();
            }
            catch (Exception ex)
            {
            }
            finally
            {
                fClientDataReaderThread = null;
            }

            fInput = null;
            fOutput = null;
            fConnection = null;
            fLastIncomingPacket = null;
            fLastOutgoingPacket = null;
            fMyMessageCollector = null;

            fMyListener.RemoveClient(fIPAddress);
            fMyListener.getMyExtasysTCPServer().OnClientDisconnect(this);
        }
    }

    /**
     * Returns the reference of the TCP Listener of this client.
     *
     * @return the reference of the TCP Listener of this client.
     */
    public TCPListener getMyTCPListener()
    {
        return fMyListener;
    }

    /**
     * Returns the reference of the Extasys TCP Server of this client.
     *
     * @return the reference of the Extasys TCP Server of this client.
     */
    public ExtasysTCPServer getMyExtasysTCPServer()
    {
        return fMyExtasysServer;
    }

    /**
     * Return client's IP Address.
     *
     * @return client's IP Address.
     */
    public String getIPAddress()
    {
        return fIPAddress;
    }

    /**
     * Return the client's name.
     *
     * @return the client's name.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Set the client's name.
     *
     * @param name is the client's name.
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Return connection start up Date-Time.
     *
     * @return connection start up Date-Time.
     */
    public Date getConnectionStartUpDateTime()
    {
        return new Date(fConnectionStartUpDateTime);
    }

    /**
     * Set client's tag. Tag is a simple object that can be used as a reference
     * for something else.
     *
     * @param tag.
     */
    public void setTag(Object tag)
    {
        fTag = tag;
    }

    /**
     * Return client's tag.
     *
     * @return client's tag.
     */
    public Object getTag()
    {
        return fTag;
    }

    /**
     * Return number of bytes received from this client.
     *
     * @return number of bytes received from this client.
     */
    public long getBytesIn()
    {
        return fBytesIn;
    }

    /**
     * Return number of bytes sent from this client.
     *
     * @return number of bytes sent from this client.
     */
    public long getBytesOut()
    {
        return fBytesOut;
    }

    /**
     * Returns the message collector of this client.
     *
     * @return the message collector of this client.
     */
    public TCPClientConnectionMessageCollector getMyMessageCollector()
    {
        return fMyMessageCollector;
    }
}

//Client data read.
class ClientDataReader implements Runnable
{

    private final TCPClientConnection fClientConnection;
    private final byte[] fReadBuffer;

    public ClientDataReader(TCPClientConnection clientConnection)
    {
        fClientConnection = clientConnection;
        fReadBuffer = new byte[fClientConnection.getMyTCPListener().getReadBufferSize()];
    }

    @Override
    public void run()
    {
        if (fClientConnection.fUseMessageCollector)
        {
            ClientWithMessageCollector();
        }
        else
        {
            ClientWithoutMessageCollector();
        }
    }

    private void ClientWithMessageCollector()
    {
        int bytesRead;

        while (fClientConnection.fIsConnected)
        {
            try
            {
                bytesRead = fClientConnection.fInput.read(fReadBuffer);

                if (bytesRead > 0)
                {
                    fClientConnection.fBytesIn += bytesRead;
                    fClientConnection.fMyListener.fBytesIn += bytesRead;
                    fClientConnection.fMyExtasysServer.fTotalBytesIn += bytesRead;

                    // PACKET WITH MESSAGE COLLECTOR
                    synchronized (fClientConnection.fReceiveDataLock)
                    {
                        fClientConnection.fLastIncomingPacket = new MessageCollectorTCPClientConnectionPacket(fClientConnection, Arrays.copyOfRange(fReadBuffer, 0, bytesRead), fClientConnection.fLastIncomingPacket);
                    }
                }
                else
                {
                    fClientConnection.ForceDisconnect();
                }
            }
            catch (SocketTimeoutException ex) //Socket timeout.
            {
                fClientConnection.ForceDisconnect();
            }
            catch (IOException | StringIndexOutOfBoundsException ex)
            {
                fClientConnection.ForceDisconnect();
            }
        }
    }

    private void ClientWithoutMessageCollector()
    {
        int bytesRead;

        while (fClientConnection.fIsConnected)
        {
            try
            {
                bytesRead = fClientConnection.fInput.read(fReadBuffer);

                if (bytesRead > 0)
                {
                    fClientConnection.fBytesIn += bytesRead;
                    fClientConnection.fMyListener.fBytesIn += bytesRead;
                    fClientConnection.fMyExtasysServer.fTotalBytesIn += bytesRead;

                    // PACKET WITHOUT MESSAGE COLLECTOR
                    synchronized (fClientConnection.fReceiveDataLock)
                    {
                        fClientConnection.fLastIncomingPacket = new IncomingTCPClientConnectionPacket(fClientConnection, Arrays.copyOfRange(fReadBuffer, 0, bytesRead), fClientConnection.fLastIncomingPacket);
                    }
                }
                else
                {
                    fClientConnection.ForceDisconnect();
                }
            }
            catch (SocketTimeoutException ex) //Socket timeout.
            {
                fClientConnection.ForceDisconnect();
            }
            catch (IOException | StringIndexOutOfBoundsException ex)
            {
                fClientConnection.ForceDisconnect();
            }
        }
    }
}
