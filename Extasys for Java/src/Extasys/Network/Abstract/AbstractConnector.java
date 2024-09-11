/*Copyright (c) 2024 Nikos Siatras

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
package Extasys.Network.Abstract;

import Extasys.DataConvertion.*;
import Extasys.Network.NetworkPacket;
import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras - https://github.com/nsiatras
 *
 * This class holds common properties for Connectors such as the TCPConnector
 * and the UDPConnector
 */
public abstract class AbstractConnector
{

    // Description
    protected String fName;

    // Remote host properties
    protected InetAddress fServerIP;
    protected int fServerPort;
    protected int fReadBufferSize;

    protected boolean fActive = false;

    // Data I/O Counters
    public long fBytesIn = 0, fBytesOut = 0;

    // Data I/O Synchronization
    public final Object fSendDataLock = new Object();
    public final Object fReceiveDataLock = new Object();

    // Last I/O Packets
    public NetworkPacket fLastIncomingPacket = null;
    public NetworkPacket fLastOutgoingPacket = null;

    // Connection DataConverter (Encryption, Encoding, Compression)
    private DataConverter fConnectionDataConverter = new NullDataConverter();

    public AbstractConnector()
    {

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
     * Sets the name of this connector.
     *
     * @param name the name to set to this connector
     */
    public void setName(String name)
    {
        fName = name;
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
     * Returns true if this Connector is active (Started).
     *
     * @return the active state of this connector.
     */
    public boolean isActive()
    {
        return fActive;
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
     * Returns the connection Data Converter of this Connector.
     *
     * @return
     */
    public DataConverter getConnectionDataConverter()
    {
        return fConnectionDataConverter;
    }

    /**
     * Sets the connection DataConverter of this Connector
     *
     * @param dataConverter
     */
    public void setConnectionDataConverter(DataConverter dataConverter)
    {
        fConnectionDataConverter = (dataConverter == null) ? new NullDataConverter() : dataConverter;
    }
}
