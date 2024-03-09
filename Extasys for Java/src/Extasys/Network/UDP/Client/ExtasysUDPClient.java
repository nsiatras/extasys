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
package Extasys.Network.UDP.Client;

import Extasys.Network.UDP.Client.Connectors.UDPConnector;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Nikos Siatras
 */
public class ExtasysUDPClient
{
    
    private String fName, fDescription;
    private final ArrayList<UDPConnector> fConnectors = new ArrayList<>();
    private final ArrayBlockingQueue fThreadPoolQueue = new ArrayBlockingQueue(50000);
    private final ThreadPoolExecutor fMyThreadPool;

    /**
     * Constructs a new Extasys UDP Client.
     *
     * @param name is the name of the client.
     * @param description is the description of the client.
     * @param corePoolSize is the number of threads to keep in the pool, even if
     * they are idle.
     * @param maximumPoolSize is the maximum number of threads to allow in the
     * pool.
     */
    public ExtasysUDPClient(String name, String description, int corePoolSize, int maximumPoolSize)
    {
        fName = name;
        fDescription = description;
        fMyThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 2, TimeUnit.SECONDS, fThreadPoolQueue);
    }

    /**
     * Add a new connector to this client.
     *
     * @param name is the name of the connector.
     * @param readBufferSize is the maximum number of bytes the connector can
     * read at a time.
     * @param readTimeOut is the maximum time in milliseconds the connector can
     * use to read incoming data.
     * @param serverIP is the server's ip address the connector will use to send
     * data.
     * @param serverPort is the server's udp port.
     */
    public UDPConnector AddConnector(String name, int readBufferSize, int readTimeOut, InetAddress serverIP, int serverPort)
    {
        UDPConnector connector = new UDPConnector(this, name, readBufferSize, readTimeOut, serverIP, serverPort);
        fConnectors.add(connector);
        return connector;
    }

    /**
     * Stop and remove a connector.
     *
     * @param name is the name of the connector.
     */
    public void RemoveConnector(String name)
    {
        for (int i = 0; i < fConnectors.size(); i++)
        {
            if (((UDPConnector) fConnectors.get(i)).getName().equals(name))
            {
                ((UDPConnector) fConnectors.get(i)).Stop();
                fConnectors.remove(i);
                break;
            }
        }
    }

    /**
     * Send data from all connector's to all hosts.
     *
     * @param data is the string to be send.
     * @throws java.io.IOException
     */
    public void SendData(String data) throws IOException
    {
        for (UDPConnector conn : fConnectors)
        {
            conn.SendData(data);
        }
    }

    /**
     * Send data from all connector's to all hosts.
     *
     * @param bytes is the byte array to be send.
     * @throws java.io.IOException
     */
    public void SendData(byte[] bytes) throws IOException
    {
        for (UDPConnector conn : fConnectors)
        {
            conn.SendData(bytes);
        }
    }

    /**
     * Start or restart the client.
     *
     * @throws java.net.SocketException
     */
    public void Start() throws SocketException, Exception
    {
        Stop();
        try
        {
            for (UDPConnector conn : fConnectors)
            {
                conn.Start();
            }
        }
        catch (SocketException ex)
        {
            throw ex;
        }
    }

    /**
     * Stop the client.
     */
    public void Stop()
    {
        try
        {
            for (UDPConnector conn : fConnectors)
            {
                conn.Stop();
            }
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Dispose the client. This method stops the client and disposes all active
     * members of this class. After calling this method you cannot re-start the
     * client.
     */
    public void Dispose()
    {
        Stop();
        fMyThreadPool.shutdown();
    }
    
    public void OnDataReceive(UDPConnector connector, DatagramPacket packet)
    {
        //System.out.println("Data received");
        //System.out.println("---" + packet.getAddress() + ":" + packet.getPort());
        //System.out.println("---" + new String(packet.getData(), 0, packet.getLength()));
    }

    /**
     * Return the name of the UDP client.
     *
     * @return the name of the client.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Sets the name of the UDP Client.
     *
     * @param name
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Return the description of the UDP client.
     *
     * @return the description of the client.
     */
    public String getDescription()
    {
        return fDescription;
    }

    /**
     * Sets the description of the UDP Client.
     *
     * @param description
     */
    public void setDescription(String description)
    {
        fDescription = description;
    }

    /**
     * Return ArrayList with the client's connectors. Each element of the
     * ArrayList is a UDPConnector class.
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
     * Returns the total bytes received.
     *
     * @return the total bytes received.
     */
    public long getBytesIn()
    {
        long result = 0;
        try
        {
            for (UDPConnector conn : fConnectors)
            {
                result += conn.getBytesIn();
            }
        }
        catch (Exception ex)
        {
        }
        return result;
    }

    /**
     * Returns the total bytes send.
     *
     * @return the total bytes send.
     */
    public long getBytesOut()
    {
        long result = 0;
        try
        {
            for (UDPConnector conn : fConnectors)
            {
                result += conn.getBytesOut();
            }
        }
        catch (Exception ex)
        {
        }
        return result;
    }
    
}
