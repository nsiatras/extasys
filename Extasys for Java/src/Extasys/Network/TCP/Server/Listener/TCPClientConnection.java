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

import Extasys.DataFrame;
import Extasys.ExtasysCalendar;
import Extasys.Network.TCP.Server.ExtasysTCPServer;
import Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException;
import Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException;
import Extasys.Network.TCP.Server.Listener.Packets.IncomingTCPClientConnectionPacket;
import Extasys.Network.TCP.Server.Listener.Packets.MessageCollectorTCPClientConnectionPacket;
import Extasys.Network.TCP.Server.Listener.Packets.OutgoingTCPClientConnectionPacket;
import Extasys.Network.TCP.Server.Listener.Tools.TCPClientConnectionMessageCollector;
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

    //Connection properties
    protected Socket fConnection;
    protected boolean fActive = false;
    protected boolean fIsConnected = false;
    public TCPListener fMyListener;
    public ExtasysTCPServer fMyExtasysServer;
    private String fIPAddress;
    private String fName = "";
    private Object fTag = null;
    private Thread fClientDataReaderThread;
    private final Date fConnectionStartUpDateTime;
    //Data input-output streams.
    public InputStream fInput;
    public OutputStream fOutput;
    //Data throughput.
    public long fBytesIn = 0, fBytesOut = 0;
    //Message collector.
    public TCPClientConnectionMessageCollector fMyMessageCollector;
    protected final boolean fUseMessageCollector;
    //Messages IO.
    public IncomingTCPClientConnectionPacket fLastIncomingPacket = null;
    public OutgoingTCPClientConnectionPacket fLastOugoingPacket = null;
    public MessageCollectorTCPClientConnectionPacket fLastMessageCollectorPacket = null;

    public TCPClientConnection(Socket socket, TCPListener myTCPListener, boolean useMessageCollector, String ETX)
    {
        fConnectionStartUpDateTime = ExtasysCalendar.fCalendar.getTime();
        fUseMessageCollector = useMessageCollector;
        fConnection = socket;

        try
        {
            fIPAddress = socket.getInetAddress().toString() + ":" + String.valueOf(socket.getPort());
            fIsConnected = true;

            if (myTCPListener.getConnectionTimeOut() > 0) //Connection time-out.
            {
                fConnection.setSoTimeout(myTCPListener.getConnectionTimeOut());
            }

            fConnection.setReceiveBufferSize(myTCPListener.getReadBufferSize());
            fConnection.setSendBufferSize(myTCPListener.getReadBufferSize());
            fMyListener = myTCPListener;
            fMyExtasysServer = myTCPListener.getMyExtasysTCPServer();
        }
        catch (SocketException ex)
        {
            DisconnectMe();
            return;
        }
        catch (Exception ex)
        {
            DisconnectMe();
            return;
        }

        if (fUseMessageCollector)
        {
            fMyMessageCollector = new TCPClientConnectionMessageCollector(this, ETX);
        }

        fMyListener.AddClient(this);
        StartReceivingData();
    }

    private void StartReceivingData()
    {
        fActive = true;
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
    public void SendData(String data) throws ClientIsDisconnectedException, OutgoingPacketFailedException
    {
        byte[] bytes = data.getBytes();
        SendData(bytes, 0, bytes.length);
    }

    /**
     * Send data to client.
     *
     * @param bytes is the byte array to be send.
     * @param offset is the position in the data buffer at witch to begin
     * sending.
     * @param length is the number of the bytes to be send.
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException
     */
    public void SendData(byte[] bytes, int offset, int length) throws ClientIsDisconnectedException, OutgoingPacketFailedException
    {
        if (!this.fIsConnected)
        {
            throw new ClientIsDisconnectedException(this);
        }
        fLastOugoingPacket = new OutgoingTCPClientConnectionPacket(this, bytes, offset, length, fLastOugoingPacket);
    }

    /**
     * Send data to client and wait until data transfer complete.
     *
     * @param data is the string to send.
     * @throws ClientIsDisconnectedException
     */
    public void SendDataSynchronous(String data) throws ClientIsDisconnectedException
    {
        byte[] bytes = data.getBytes();
        SendDataSynchronous(bytes, 0, bytes.length);
    }

    /**
     * Send data to client and wait until data transfer complete.
     *
     * @param bytes is the byte array to be send.
     * @param offset is the position in the data buffer at witch to begin
     * @param length is the number of the bytes to be send.
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException
     */
    public void SendDataSynchronous(byte[] bytes, int offset, int length) throws ClientIsDisconnectedException
    {
        if (!this.fIsConnected)
        {
            throw new ClientIsDisconnectedException(this);
        }

        try
        {
            fOutput.write(bytes, offset, length);
            fBytesOut += length;
            fMyListener.fBytesOut += length;
        }
        catch (IOException ex)
        {
            if (fLastOugoingPacket != null)
            {
                fLastOugoingPacket.Cancel();
            }
            this.DisconnectMe();
            throw new ClientIsDisconnectedException(this);
        }
    }

    /**
     * Disconnect this client.
     */
    public void DisconnectMe()
    {
        if (fActive)
        {
            fActive = false;
            fIsConnected = false;
            try
            {
                fInput.close();
            }
            catch (Exception ex)
            {
            }

            try
            {
                fOutput.close();
            }
            catch (Exception ex)
            {
            }

            try
            {
                fConnection.close();
            }
            catch (Exception ex)
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

            if (fLastOugoingPacket != null)
            {
                fLastOugoingPacket.Cancel();
            }

            if (fLastMessageCollectorPacket != null)
            {
                fLastMessageCollectorPacket.Cancel();
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
            fLastOugoingPacket = null;
            fLastMessageCollectorPacket = null;
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
        return fConnectionStartUpDateTime;
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

    public boolean isActive()
    {
        return fActive;
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
        int bytesRead;

        while (fClientConnection.fActive)
        {
            try
            {
                bytesRead = fClientConnection.fInput.read(fReadBuffer);

                if (bytesRead > 0)
                {
                    fClientConnection.fBytesIn += bytesRead;
                    fClientConnection.getMyTCPListener().fBytesIn += bytesRead;

                    if (!fClientConnection.fUseMessageCollector)
                    {
                        fClientConnection.fLastIncomingPacket = new IncomingTCPClientConnectionPacket(fClientConnection, new DataFrame(fReadBuffer, 0, bytesRead), fClientConnection.fLastIncomingPacket);
                    }
                    else
                    {
                        fClientConnection.fLastMessageCollectorPacket = new MessageCollectorTCPClientConnectionPacket(fClientConnection, Arrays.copyOfRange(fReadBuffer, 0, bytesRead), fClientConnection.fLastMessageCollectorPacket);
                    }
                }
                else
                {
                    fClientConnection.DisconnectMe();
                }
            }
            catch (SocketTimeoutException socketTimeOutException) //Socket timeout.
            {
                fClientConnection.DisconnectMe();
            }
            catch (IOException ioException)
            {
                fClientConnection.DisconnectMe();
            }
            catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException)
            {
                fClientConnection.DisconnectMe();
            }
            catch (Exception exception)
            {
                fClientConnection.DisconnectMe();
            }
        }
    }
}
