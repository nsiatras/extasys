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
package Extasys.Network.TCP.Server;

import Extasys.DataFrame;
import Extasys.Network.Abstract.AbstractServer;
import Extasys.Network.TCP.Server.Listener.Exceptions.*;
import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import Extasys.Network.TCP.Server.Listener.TCPListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author Nikos Siatras
 */
public abstract class ExtasysTCPServer extends AbstractServer
{

    private final ArrayList<TCPListener> fListeners = new ArrayList<>();
    private final Object fListenersLock = new Object();

    /**
     * Constructs an new Extasys TCP Server.
     *
     * @param name is the name of the server.
     * @param description is the description of the server.
     * @param corePoolSize is the number of threads to keep in the pool, even if
     * they are idle.
     * @param maximumPoolSize is the maximum number of threads to allow in the
     * pool.
     */
    public ExtasysTCPServer(String name, String description, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);
    }

    /**
     * Add a new listener to this server.
     *
     * @param name is the listener's name.
     * @param ipAddress is the listener's IP address.
     * @param port is the listener's TCP port.
     * @param maxConnections is the listener's maximum allowed connections.
     * @param readBufferSize is the read buffer size for each connections in
     * bytes.
     * @param connectionTimeOut is the connections time-out in milliseconds. Set
     * to 0 for no time-out.
     * @param backLog is the number of outstanding connection requests the
     * listener can have.
     *
     * @return the TCPListener that was added to this server
     *
     */
    public TCPListener AddListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog)
    {
        synchronized (fListenersLock)
        {
            TCPListener listener = new TCPListener(this, name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog);
            fListeners.add(listener);
            return listener;
        }
    }

    /**
     * Add new listener with message collector (character splitter).
     *
     * @param name is the listener's name.
     * @param ipAddress is the listener's IP address.
     * @param port is the listener's TCP port.
     * @param maxConnections is the number of maximum allowed connections.
     * @param readBufferSize is the read buffer size for each connection in
     * bytes.
     * @param connectionTimeOut is the connections time-out in milliseconds. Set
     * to 0 for no time-out.
     * @param backLog is the number of outstanding connection requests the
     * listener can have.
     * @param splitter is the message splitter.
     *
     * @return the TCPListener that was added to this server
     */
    public TCPListener AddListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, char splitter)
    {
        synchronized (fListenersLock)
        {
            TCPListener listener = new TCPListener(this, name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, splitter);
            fListeners.add(listener);
            return listener;
        }
    }

    /**
     * Add new listener with message collector (string splitter).
     *
     * @param name is the listener's name.
     * @param ipAddress is the listener's IP address.
     * @param port is the listener's TCP port.
     * @param maxConnections is the number of maximum allowed connections.
     * @param readBufferSize is the read buffer size for each connection in
     * bytes.
     * @param connectionTimeOut is the connections time-out in milliseconds. Set
     * to 0 for no time-out.
     * @param backLog is the number of outstanding connection requests the
     * listener can have.
     * @param splitter is the message splitter.
     *
     * @return the TCPListener that was added to this server
     */
    public TCPListener AddListener(String name, InetAddress ipAddress, int port, int maxConnections, int readBufferSize, int connectionTimeOut, int backLog, String splitter)
    {
        synchronized (fListenersLock)
        {
            TCPListener listener = new TCPListener(this, name, ipAddress, port, maxConnections, readBufferSize, connectionTimeOut, backLog, splitter);
            fListeners.add(listener);
            return listener;
        }
    }

    /**
     * Stop and remove a listener.
     *
     * @param name is the listener's name to remove.
     */
    public void RemoveListener(String name)
    {
        synchronized (fListenersLock)
        {
            fListeners.stream()
                    .filter(l -> l.getName().equals(name))
                    .findFirst()
                    .ifPresent(this::RemoveListener);
        }
    }

    /**
     * Stop and remove a listener.
     *
     * @param listener the listener to stop and remove
     */
    public void RemoveListener(TCPListener listener)
    {
        synchronized (fListenersLock)
        {
            if (listener != null)
            {
                listener.Stop();
                fListeners.remove(listener);
            }
        }
    }

    /**
     * Start or restart the server.
     *
     * @throws java.io.IOException
     */
    public void Start() throws IOException, Exception
    {
        Stop();
        try
        {
            //Start all listeners.
            for (TCPListener listener : fListeners)
            {
                listener.Start();
            }
        }
        catch (IOException ex)
        {
            Stop();
            throw ex;
        }
        catch (Exception ex)
        {
            Stop();
            throw ex;
        }
    }

    /**
     * Stop the server.
     */
    public void Stop()
    {
        Stop(false);
    }

    /**
     * Force server stop.
     */
    public void ForceStop()
    {
        Stop(true);
    }

    private void Stop(boolean force)
    {
        synchronized (fListenersLock)
        {
            // Stop all listeners.
            for (TCPListener listener : fListeners)
            {
                if (!force)
                {
                    listener.Stop();
                }
                else
                {
                    listener.ForceStop();
                }
            }

            try
            {
                fMyThreadPool.getQueue().clear();
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Dispose the server.
     *
     * This method stops the server and disposes all the active members of this
     * class. After calling this method you cannot re-start the server.
     */
    @Override
    public void Dispose()
    {
        Stop();
        fMyThreadPool.shutdown();
    }

    /**
     * Server is receiving data from a client connection.
     *
     * @param sender is the client sends the data to this server.
     * @param data is the incoming DataFrame.
     */
    public abstract void OnDataReceive(TCPClientConnection sender, DataFrame data);

    /**
     * A client connects to this server.
     *
     * @param client Client.
     */
    public abstract void OnClientConnect(TCPClientConnection client);

    /**
     * A client disconnects from this server.
     *
     * @param client Client.
     */
    public abstract void OnClientDisconnect(TCPClientConnection client);

    /**
     * Send data to a client.
     *
     * @param data is the string to send.
     * @param sender is the client to send the data.
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException
     */
    public void ReplyToSender(String data, TCPClientConnection sender) throws ClientIsDisconnectedException, OutgoingPacketFailedException
    {
        sender.SendData(data);
    }

    /**
     * Send data to a client.
     *
     * @param bytes is the byte array to be send.
     * @param sender is the client connection to receive the data.
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException
     */
    public void ReplyToSender(byte[] bytes, TCPClientConnection sender) throws ClientIsDisconnectedException, OutgoingPacketFailedException
    {
        sender.SendData(bytes);
    }

    /**
     * Send data to all connected clients.
     *
     * @param data is the string to be send.
     */
    public void ReplyToAll(String data)
    {
        synchronized (fListenersLock)
        {
            for (TCPListener listener : fListeners)
            {
                listener.ReplyToAll(data);
            }
        }
    }

    /**
     * Send data to all connected clients.
     *
     * @param bytes is the byte array to be send.
     */
    public void ReplyToAll(byte[] bytes)
    {
        synchronized (fListenersLock)
        {
            for (TCPListener listener : fListeners)
            {
                listener.ReplyToAll(bytes);
            }
        }
    }

    /**
     * Send data to all connected clients excepts sender.
     *
     * @param data is the string to be send.
     * @param sender is the TCP client exception.
     */
    public void ReplyToAllExceptSender(String data, TCPClientConnection sender)
    {
        synchronized (fListenersLock)
        {
            for (TCPListener listener : fListeners)
            {
                listener.ReplyToAllExceptSender(data, sender);
            }
        }
    }

    /**
     * Send data to all connected clients excepts sender.
     *
     * @param bytes is the byte array to be send.
     * @param sender is the TCP client exception.
     */
    public void ReplyToAllExceptSender(byte[] bytes, TCPClientConnection sender)
    {
        synchronized (fListenersLock)
        {
            for (TCPListener listener : fListeners)
            {
                listener.ReplyToAllExceptSender(bytes, sender);
            }
        }
    }

    /**
     * Returns an ArrayList with this server's TCP listeners.
     *
     * @return ArrayList with this server's TCP listeners.
     */
    public ArrayList<TCPListener> getListeners()
    {
        synchronized (fListenersLock)
        {
            return fListeners;
        }
    }

    /**
     * Returns the current connections number of this server.
     *
     * @return the current connections number of this server.
     */
    public int getCurrentConnectionsNumber()
    {
        synchronized (fListenersLock)
        {
            int currentConnections = 0;

            for (TCPListener listener : fListeners)
            {
                try
                {
                    currentConnections += listener.getConnectedClients().size();
                }
                catch (Exception ex)
                {
                }
            }

            return currentConnections;
        }
    }

}
