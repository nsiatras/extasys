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
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.TCP.Client.Exceptions.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Nikos Siatras
 */
public class ExtasysTCPClient
{

    private final String fName, fDescription;
    private final ArrayList fConnectors = new ArrayList();
    private final ArrayBlockingQueue fThreadPoolQueue = new ArrayBlockingQueue(100000);
    public final ThreadPoolExecutor fMyThreadPool;

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
        fName = name;
        fDescription = description;
        fMyThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 10, TimeUnit.SECONDS, fThreadPoolQueue);
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
        TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize);
        fConnectors.add(connector);
        return connector;
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
     * @return the connector.
     */
    public TCPConnector AddConnector(String name, InetAddress serverIP, int serverPort, int readBufferSize, char ETX)
    {
        TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize, ETX);
        fConnectors.add(connector);
        return connector;
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
        TCPConnector connector = new TCPConnector(this, name, serverIP, serverPort, readBufferSize, splitter);
        fConnectors.add(connector);
        return connector;
    }

    /**
     * Stop and remove a connector from this client.
     *
     * @param name is the connector's name.
     */
    public void RemoveConnector(String name)
    {
        for (int i = 0; i < fConnectors.size(); i++)
        {
            if (((TCPConnector) fConnectors.get(i)).getName().equals(name))
            {
                ((TCPConnector) fConnectors.get(i)).Stop();
                fConnectors.remove(i);
                break;
            }
        }
    }

    /**
     * Start or restart the client.
     */
    public void Start() throws Exception
    {
        Stop();

        try
        {
            for (int i = 0; i < fConnectors.size(); i++)
            {
                ((TCPConnector) fConnectors.get(i)).Start();
            }
        }
        catch (Exception ex)
        {
            Stop();
            throw ex;
        }
    }

    /**
     * Stop the client.
     */
    public void Stop()
    {
        for (int i = 0; i < fConnectors.size(); i++)
        {
            ((TCPConnector) fConnectors.get(i)).Stop();
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
     */
    public void SendData(String data) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        for (int i = 0; i < fConnectors.size(); i++)
        {
            ((TCPConnector) fConnectors.get(i)).SendData(data);
        }
    }

    /**
     * Send data from all connector's to all hosts.
     *
     * @param bytes is the byte array to be send.
     * @param offset is the position in the data buffer at witch to begin
     * sending.
     * @param length is the number of the bytes to be send.
     */
    public void SendData(byte[] bytes, int offset, int length) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        for (int i = 0; i < fConnectors.size(); i++)
        {
            ((TCPConnector) fConnectors.get(i)).SendData(bytes, offset, length);
        }
    }

    /**
     * A connector of this client receives data.
     *
     * @param connector is the client's connector.
     * @param data is the received data.
     */
    public void OnDataReceive(TCPConnector connector, DataFrame data)
    {
        //System.out.println(new String(data.getBytes()));
    }

    /**
     * A connector of this client connected to a server.
     *
     * @param connector
     */
    public void OnConnect(TCPConnector connector)
    {
        //System.out.println("Connector " + connector.getName() + " connected!");
    }

    /**
     * A connector of this client has been disconnected.
     *
     * @param connector
     */
    public void OnDisconnect(TCPConnector connector)
    {
        //System.out.println("Connector " + connector.getName() + " disconnected!");
    }

    /**
     * Return the name of the client.
     *
     * @return the name of the client.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Return the description of the client.
     *
     * @return the description of the client.
     */
    public String getDescription()
    {
        return fDescription;
    }

    /**
     * Return ArrayList with the client's connectors. Each element of the
     * ArrayList is a TCPConnector class.
     *
     * @return ArrayList with the client's connectors.
     */
    public ArrayList getConnectors()
    {
        return fConnectors;
    }

    /**
     * Return the client's Thread Pool.
     *
     * @return the client's Thread Pool.
     */
    public ThreadPoolExecutor getMyThreadPool()
    {
        return fMyThreadPool;
    }

    /**
     * Returns the total number of bytes received from all the connectors of the
     * client.
     *
     * @return the number of bytes received from all the connectors of the
     * client.
     */
    public long getBytesIn()
    {
        long bytesIn = 0;
        try
        {
            for (int i = 0; i < fConnectors.size(); i++)
            {
                bytesIn += ((TCPConnector) fConnectors.get(i)).getBytesIn();
            }
        }
        catch (Exception ex)
        {
        }
        return bytesIn;
    }

    /**
     * Returns the total number of bytes send from all the connectors of the
     * client.
     *
     * @return the total number of bytes send from all the connectors of the
     * client.
     */
    public long getBytesOut()
    {
        long bytesOut = 0;
        try
        {
            for (int i = 0; i < fConnectors.size(); i++)
            {
                bytesOut += ((TCPConnector) fConnectors.get(i)).getBytesOut();
            }
        }
        catch (Exception ex)
        {
        }
        return bytesOut;
    }
}
