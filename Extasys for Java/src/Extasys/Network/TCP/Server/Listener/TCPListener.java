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
import Extasys.Network.Abstract.AbstractListener;
import Extasys.Network.TCP.Server.ExtasysTCPServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 *
 * @author Nikos Siatras
 */
public class TCPListener extends AbstractListener
{

    // Extasys tcp server reference.
    private ExtasysTCPServer fMyExtasysTCPServer;

    // Socket.
    private ServerSocket fTcpListener;
    private Thread fTCPListenerThread;

    // Connections.
    private HashMap<String, TCPClientConnection> fConnectedClients;
    final Object fAddRemoveUsersLock = new Object();
    private int fMaxConnections;

    private int fConnectionTimeOut;
    private int fBackLog;

    // Message collector.
    private MessageETX fMessageETX = null;
    private boolean fAutoApplyMessageSplitter = false;

    /**
     * Constructs a new TCP listener.
     *
     * @param myServer is the Extasys TCP Server of this listener
     * @param name is the listener's name
     * @param ipAddress is the listener's IP Address
     * @param port is the listener's port
     * @param maxConnections is the listener's maximum connections limit
     * @param readBufferSize is the listener's each connection read buffer size
     * in bytes
     * @param connectionTimeOut is the listener's connections time-out time in
     * milliseconds
     * @param backLog is the number of outstanding connection requests this
     * listener can have
     *
     */
    public TCPListener(ExtasysTCPServer myServer, String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog)
    {
        Initialize(myServer, name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, false, null);
    }

    /**
     * Constructs a new TCP listener
     *
     * @param myServer is the Extasys TCP Server of this listener
     * @param name is the listener's name
     * @param ipAddress is the listener's IP Address
     * @param port is the listener's port
     * @param maxConnections is the listener's maximum allowed connections
     * @param readBufferSize is the listener's each connection read buffer size
     * in bytes
     * @param connectionTimeOut is the listener's connections time-out in
     * milliseconds. Set to 0 for no time-out
     * @param backLog is the number of outstanding connection requests this
     * listener can have
     * @param splitter is the message splitter
     */
    public TCPListener(ExtasysTCPServer myServer, String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, char splitter)
    {
        Initialize(myServer, name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, true, String.valueOf(splitter).getBytes());
    }

    /**
     * Constructs a new TCP listener
     *
     * @param myServer is the Extasys TCP Server of this listener
     * @param name is the listener's name
     * @param ipAddress is the listener's IP Address
     * @param port is the listener's port
     * @param maxConnections is the listener's maximum allowed connections
     * @param readBufferSize is the listener's each connection read buffer size
     * in bytes
     * @param connectionTimeOut is the listener's connections time-out in
     * milliseconds. Set to 0 for no time-out
     * @param backLog is the number of outstanding connection requests this
     * listener can have
     * @param splitter is the message splitter
     */
    public TCPListener(ExtasysTCPServer myServer, String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, String splitter)
    {
        Initialize(myServer, name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, true, splitter.getBytes());
    }

    private void Initialize(ExtasysTCPServer myServer, String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, boolean useMessageCollector, byte[] splitter)
    {
        fMyExtasysTCPServer = myServer;
        fConnectedClients = new HashMap<>();
        fName = name;
        fIPAddress = ipAddress;
        fPort = port;
        fMaxConnections = maxConnections;
        fReadBufferSize = readBufferSize;
        fConnectionTimeOut = connectionTimeOut;
        fBackLog = backLog;
        fMessageETX = (splitter != null) ? new MessageETX(splitter) : null;
        fBytesIn = 0;
        fBytesOut = 0;
    }

    /**
     * Start or restart the TCPListener
     *
     * @throws java.io.IOException
     */
    public void Start() throws IOException, Exception
    {
        Stop();
        try
        {
            fTcpListener = new ServerSocket(fPort, fBackLog, fIPAddress);
            fTcpListener.setSoTimeout(5000);
            fActive = true;
        }
        catch (IOException ex)
        {
            Stop();
            throw ex;
        }

        try
        {
            fTCPListenerThread = new Thread(new TCPListenerThread(fTcpListener, this));
            fTCPListenerThread.start();
        }
        catch (Exception ex)
        {
            Stop();
            throw ex;
        }
    }

    /**
     * Stop the TCP listener
     */
    public void Stop()
    {
        Stop(false);
    }

    /**
     * Force server stop
     */
    public void ForceStop()
    {
        Stop(true);
    }

    private void Stop(boolean force)
    {
        fActive = false;

        try
        {
            fTCPListenerThread.interrupt();
        }
        catch (Exception ex)
        {
        }

        // Disconnect all connected clients.
        try
        {
            for (TCPClientConnection client : fConnectedClients.values())
            {
                if (!force)
                {
                    client.DisconnectMe();
                }
                else
                {
                    client.ForceDisconnect();
                }
            }
        }
        catch (Exception ex)
        {
        }

        try
        {
            fTcpListener.close();
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Add client to connected clients list.
     *
     * @param client is the client object to add.
     */
    public void AddClient(TCPClientConnection client)
    {
        synchronized (fAddRemoveUsersLock)
        {
            try
            {
                if (!fConnectedClients.containsKey(client.getIPAddress()))
                {
                    fConnectedClients.put(client.getIPAddress(), client);
                }
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Remove client from connected clients list.
     *
     * @param ipAddress is the client's IP address.
     */
    public void RemoveClient(String ipAddress)
    {
        synchronized (fAddRemoveUsersLock)
        {
            try
            {
                if (fConnectedClients.containsKey(ipAddress))
                {
                    fConnectedClients.remove(ipAddress);
                }
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Send data to all connected clients.
     *
     * @param data is the string to be send.
     */
    public void ReplyToAll(String data)
    {
        synchronized (fAddRemoveUsersLock)
        {
            for (TCPClientConnection client : fConnectedClients.values())
            {
                try
                {
                    client.SendData(data);
                }
                catch (Exception ex)
                {
                }
            }
        }
    }

    /**
     * Send data to all connected clients.
     *
     * @param bytes is the byte array to be send.
     */
    public void ReplyToAll(final byte[] bytes)
    {
        synchronized (fAddRemoveUsersLock)
        {
            for (TCPClientConnection client : fConnectedClients.values())
            {
                try
                {
                    client.SendData(bytes);
                }
                catch (Exception ex)
                {
                }
            }
        }
    }

    /**
     * Send data to all connected clients excepts sender.
     *
     * @param data is the string to be send.
     * @param sender is the TCP client exception.
     */
    public void ReplyToAllExceptSender(final String data, final TCPClientConnection sender)
    {
        synchronized (fAddRemoveUsersLock)
        {
            if (sender != null)
            {
                for (TCPClientConnection client : fConnectedClients.values())
                {
                    if (client != sender)
                    {
                        try
                        {
                            client.SendData(data);
                        }
                        catch (Exception ex)
                        {
                        }
                    }
                }
            }
        }
    }

    /**
     * Send data to all connected clients excepts sender.
     *
     * @param bytes is the byte array to be send.
     * @param sender is the TCP client exception.
     */
    public void ReplyToAllExceptSender(final byte[] bytes, final TCPClientConnection sender)
    {
        synchronized (fAddRemoveUsersLock)
        {
            if (sender != null)
            {
                for (TCPClientConnection client : fConnectedClients.values())
                {
                    if (client != sender)
                    {
                        try
                        {
                            client.SendData(bytes);
                        }
                        catch (Exception ex)
                        {
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns TCP listener's ServerSocket.
     *
     * @return TCP listener's ServerSocket.
     */
    public ServerSocket getServerSocket()
    {
        return fTcpListener;
    }

    /**
     * Returns a reference of this listener's main Extasys TCP server.
     *
     * @return a reference of this listener's main Extasys TCP server.
     */
    public ExtasysTCPServer getMyExtasysTCPServer()
    {
        return fMyExtasysTCPServer;
    }

    /**
     * Returns a HashMap with the connected clients of this listener. The
     * HashMap's key is a string contains client's IP address. The HashMap's
     * value is a TCPClientConnection.
     *
     * @return the connections HashMap of this listener.
     */
    public HashMap<String, TCPClientConnection> getConnectedClients()
    {
        return fConnectedClients;
    }

    /**
     * Returns allowed maximum connections.
     *
     * @return the allowed maximum connections of this listener.
     */
    public int getMaxConnections()
    {
        return fMaxConnections;
    }

    /**
     * Set the maximum allowed connections of this listener.
     *
     * @param value is the maximum allowed connections of this listener.
     */
    public void setMaxConnections(int value)
    {
        fMaxConnections = value;
    }

    /**
     * Returns the connections time-out in milliseconds of this listener.
     *
     * @return the connections time-out in milliseconds of this listener.
     */
    public int getConnectionTimeOut()
    {
        return fConnectionTimeOut;
    }

    /**
     * Set the connections time-out in milliseconds of this listener.
     *
     * @param value is the connections time-out in milliseconds of this
     * listener.
     */
    public void setConnectionTimeOut(int value)
    {
        fConnectionTimeOut = value;
    }

    /**
     * Returns the active state of this listener's message collector.
     *
     * @return True if the message collector of this listener is active.
     */
    public boolean isUsingMessageCollector()
    {
        return fMessageETX != null;
    }

    /**
     * Returns message collector's splitter in string format.
     *
     * @return the message collector's splitter in string format.
     */
    public MessageETX getMessageETX()
    {
        return fMessageETX;
    }

    /**
     * Returns true if this TCP Listener automatically applies Message Splitter
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
     * outgoing messages of this TCP Listener.
     *
     * @param value
     */
    public void setAutoApplyMessageSplitterState(boolean value)
    {
        fAutoApplyMessageSplitter = value;
    }

}
