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
package Extasys.Network.UDP.Server;

import Extasys.Network.UDP.Server.Listener.UDPListener;
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
public class ExtasysUDPServer
{

    private String fName, fDescription;
    private final ArrayList<UDPListener> fListeners = new ArrayList<>();
    private final ArrayBlockingQueue fThreadPoolQueue = new ArrayBlockingQueue(50000);
    public final ThreadPoolExecutor fMyThreadPool;

    /**
     * Constructs a new Extasys UDP Server.
     *
     * @param name is the name of the server.
     * @param description is the description of the server.
     * @param corePoolSize is the number of threads to keep in the pool, even if
     * they are idle.
     * @param maximumPoolSize is the maximum number of threads to allow in the
     * pool.
     */
    public ExtasysUDPServer(String name, String description, int corePoolSize, int maximumPoolSize)
    {
        fName = name;
        fDescription = description;
        fMyThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 2, TimeUnit.SECONDS, fThreadPoolQueue);
    }

    /**
     * Add a new listener to this server.
     *
     * @param name is the listener's name.
     * @param ipAddress is the listener's IP address.
     * @param port is the listener's udp port.
     * @param readBufferSize is the maximum size of bytes the listener can use
     * to read incoming bytes at a time.
     * @param readDataTimeOut is the maximum time in milliseconds a client can
     * use to send data to the listener.
     * @return the listener.
     */
    public UDPListener AddListener(String name, InetAddress ipAddress, int port, int readBufferSize, int readDataTimeOut)
    {
        UDPListener listener = new UDPListener(this, name, ipAddress, port, readBufferSize, readDataTimeOut);
        fListeners.add(listener);
        return listener;
    }

    /**
     * Stops and removes a UDP listener from this server.
     *
     * @param name is the name of the UDP listener.
     */
    public void RemoveListener(String name)
    {
        for (int i = 0; i < fListeners.size(); i++)
        {
            if (((UDPListener) fListeners.get(i)).getName().equals(name))
            {
                ((UDPListener) fListeners.get(i)).Stop();
                fListeners.remove(i);
                break;
            }
        }
    }

    /**
     * Send data. This method sends the data from all listeners.
     *
     * @param packet is the DatagramPacket to send.
     */
    public void SendData(DatagramPacket packet)
    {
        for (UDPListener listener : fListeners)
        {
            listener.SendData(packet);
        }
    }

    /**
     * Start or restart the server.
     *
     * @throws java.net.SocketException
     */
    public void Start() throws SocketException
    {
        Stop();

        for (UDPListener listener : fListeners)
        {
            listener.Start();
        }
    }

    /**
     * Stop the server.
     */
    public void Stop()
    {
        for (UDPListener listener : fListeners)
        {
            listener.Stop();
        }
    }

    /**
     * Dispose the server. This method stops the server and disposes all the
     * active members of this class. After calling this method you cannot
     * re-start the server.
     */
    public void Dispose()
    {
        Stop();
        fMyThreadPool.shutdown();
    }

    public void OnDataReceive(UDPListener listener, DatagramPacket packet)
    {
        //System.out.println("Data received");
        //System.out.println("---" + packet.getAddress() + ":" + packet.getPort());
        //System.out.println("---" + new String(packet.getData(), 0, packet.getLength(), listener.getCharset()));

        //DatagramPacket reply = new DatagramPacket(packet.getData(), 0, packet.getLength(), packet.getAddress(), packet.getPort());
        //listener.SendData(reply);
    }

    /**
     * Returns the name of this UDP server.
     *
     * @return the name of this UDP server.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Sets the name of this UDP server
     *
     * @param name the name to set on this UDP server
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Returns the description of this UDP server.
     *
     * @return the description of this UDP server.
     */
    public String getDescription()
    {
        return fDescription;
    }

    /**
     * Sets the description of this UDP server
     *
     * @param description the description to set on this UDP server
     */
    public void setDescription(String description)
    {
        fDescription = description;
    }

    /**
     * Returns an ArrayList with this server's UDP listeners. The ArrayList
     * elements are UDPListener classes.
     *
     * @return ArrayList with this server's UDP listeners.
     */
    public ArrayList<UDPListener> getListeners()
    {
        return fListeners;
    }

    /**
     * Returns the total bytes received from this server.
     *
     * @return the total bytes received from this server.
     */
    public long getBytesIn()
    {
        long bytesIn = 0;
        try
        {
            for (UDPListener listener : fListeners)
            {
                bytesIn += listener.getBytesIn();
            }
        }
        catch (Exception ex)
        {
        }
        return bytesIn;
    }

    /**
     * Returns the total bytes send from this server.
     *
     * @return the total bytes send from this server.
     */
    public long getBytesOut()
    {
        long bytesOut = 0;
        try
        {
            for (UDPListener listener : fListeners)
            {
                bytesOut += listener.getBytesOut();
            }
        }
        catch (Exception ex)
        {
        }
        return bytesOut;
    }
}
