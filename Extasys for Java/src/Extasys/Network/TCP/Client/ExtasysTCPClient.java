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
package Extasys.Network.TCP.Client;

import Extasys.DataFrame;
import Extasys.Network.AbstractClient;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.TCP.Client.Exceptions.*;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author Nikos Siatras
 */
public abstract class ExtasysTCPClient extends AbstractClient
{

    private final Object fConnectorsLock = new Object();
    private final ArrayList<TCPConnector> fConnectors = new ArrayList<>();

    /**
     * Constructs a new Extasys TCP Client.
     *
     * @param name is the name of the client.
     * @param description is the description of the client.
     * @param corePoolSize is the number of threads to keep in the pool, even if
     * they are idle.
     * @param maximumPoolSize is the maximum number of threads to allow in the
     * pool.
     */
    public ExtasysTCPClient(String name, String description, int corePoolSize, int maximumPoolSize)
    {
        super(name, description, corePoolSize, maximumPoolSize);
    }

    /**
     * Add a new connector to this client.
     *
     * @param name is the connector's name.
     * @param serverIP is the remote host's (server) IP address.
     * @param serverPort is the remote host's (server) port.
     * @param readBufferSize is the read buffer size for this connection in
     * bytes.
     * @return the connector.
     */
    public TCPConnector AddConnector(String name, InetAddress serverIP, int serverPort, int readBufferSize)
    {
        synchronized (fConnectorsLock)
        {
            TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize);
            fConnectors.add(connector);
            return connector;
        }
    }

    /**
     * Add a new connector with message collector.
     *
     * @param name is the connector's name.
     * @param serverIP is the remote host's (server) IP address.
     * @param serverPort is the remote host's (server) port.
     * @param readBufferSize is the read buffer size for this connection in
     * bytes.
     * @param ETX is the End of Text character.
     *
     * @return the connector.
     */
    public TCPConnector AddConnector(String name, InetAddress serverIP, int serverPort, int readBufferSize, char ETX)
    {
        synchronized (fConnectorsLock)
        {
            TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize, ETX);
            fConnectors.add(connector);
            return connector;
        }
    }

    /**
     * Add a new connector with message collector.
     *
     * @param name is the connector's name.
     * @param serverIP is the remote host's (server) IP address.
     * @param serverPort is the remote host's (server) port.
     * @param readBufferSize is the read buffer size for this connection in
     * bytes.
     * @param splitter is the message splitter.
     * @return the connector.
     */
    public TCPConnector AddConnector(String name, InetAddress serverIP, int serverPort, int readBufferSize, String splitter)
    {
        synchronized (fConnectorsLock)
        {
            TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize, splitter);
            fConnectors.add(connector);
            return connector;
        }
    }

    /**
     * Stop and remove a connector from this client.
     *
     * @param name is the connector's name.
     */
    public void RemoveConnector(String name)
    {
        synchronized (fConnectorsLock)
        {
            for (int i = 0; i < fConnectors.size(); i++)
            {
                if (fConnectors.get(i).getName().equals(name))
                {
                    fConnectors.get(i).Stop();
                    fConnectors.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * Start or restart the client.
     *
     * @throws java.lang.Exception
     */
    public void Start() throws Exception
    {
        synchronized (fConnectorsLock)
        {
            Stop();

            try
            {
                for (TCPConnector conn : fConnectors)
                {
                    conn.Start();
                }
            }
            catch (Exception ex)
            {
                Stop();
                throw ex;
            }
        }
    }

    /**
     * Stop the client.
     */
    public void Stop()
    {
        Stop(false);
    }

    /**
     * Force client to stop.
     */
    public void ForceStop()
    {
        Stop(true);
    }

    private void Stop(boolean force)
    {
        synchronized (fConnectorsLock)
        {
            for (TCPConnector conn : fConnectors)
            {
                if (!force)
                {
                    conn.Stop();
                }
                else
                {
                    conn.ForceStop();
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
     * Dispose the client. This method stops the client and disposes all the
     * active members of this class. After calling this method you cannot
     * re-start the client.
     */
    public void Dispose()
    {
        Stop();
        fMyThreadPool.shutdown();
    }

    /**
     * Send data from all connector's to all hosts.
     *
     * @param data is the string to be send.
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException
     */
    public void SendData(String data) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fConnectorsLock)
        {
            for (TCPConnector conn : fConnectors)
            {
                conn.SendData(data);
            }
        }
    }

    /**
     * Send data from all connector's to all hosts.
     *
     * @param bytes is the byte array to be send.
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException
     */
    public void SendData(byte[] bytes) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fConnectorsLock)
        {
            for (TCPConnector conn : fConnectors)
            {
                conn.SendData(bytes);
            }
        }
    }

    /**
     * A connector of this client receives data.
     *
     * @param connector is the client's connector.
     * @param data is the received data.
     */
    public abstract void OnDataReceive(TCPConnector connector, DataFrame data);

    /**
     * A connector of this client connected to a server.
     *
     * @param connector
     */
    public abstract void OnConnect(TCPConnector connector);

    /**
     * A connector of this client has been disconnected.
     *
     * @param connector
     */
    public abstract void OnDisconnect(TCPConnector connector);

    /**
     * Returns ArrayList with the client's TCP connectors.
     *
     * @return ArrayList with the client's TCP connectors.
     */
    public ArrayList<TCPConnector> getConnectors()
    {
        return fConnectors;
    }

}
