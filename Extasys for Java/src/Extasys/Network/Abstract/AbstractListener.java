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

import Extasys.DataConvertion.DataConverter;
import Extasys.DataConvertion.NullDataConverter;
import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras - https://github.com/nsiatras
 *
 * This class holds common properties for Listeners such as the TCPListener and
 * the UDPListener
 */
public abstract class AbstractListener
{

    // Description
    protected String fName;

    // Host properties
    protected InetAddress fIPAddress;
    protected int fPort;
    protected int fReadBufferSize;

    // If fActive = true then the listener is Started
    protected boolean fActive = false;

    // Data I/O Counters
    public long fBytesIn = 0, fBytesOut = 0;

    // Connection DataConverter (Encryption, Encoding, Compression)
    private DataConverter fConnectionDataConverter = new NullDataConverter();

    /**
     * Returns the name of this Listener.
     *
     * @return the name of this Listener.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Sets the name to this Listener
     *
     * @param name the name to set to this Listener
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Returns the IP address of this Listener.
     *
     * @return the IP address of this Listener.
     */
    public InetAddress getIPAddress()
    {
        return fIPAddress;
    }

    /**
     * Returns the port of this listener.
     *
     * @return the port of this listener.
     */
    public int getPort()
    {
        return fPort;
    }

    /**
     * Returns the read buffer size of this listener.
     *
     * @return the read buffer size of this listener.
     */
    public int getReadBufferSize()
    {
        return fReadBufferSize;
    }

    /**
     * Sets the read buffer size in bytes for each client connection of this
     * Listener.
     *
     * @param value is the read buffer size in bytes.
     */
    public void setReadBufferSize(int value)
    {
        fReadBufferSize = value;
    }

    /**
     * Returns the total number of bytes received from this listener.
     *
     * @return the total number of bytes received from this listener.
     */
    public long getBytesIn()
    {
        return fBytesIn;
    }

    /**
     * Returns the total number of bytes sent from this listener.
     *
     * @return the total number of bytes sent from this listener.
     */
    public long getBytesOut()
    {
        return fBytesOut;
    }

    /**
     * Returns true if this listener is Active (Started).
     *
     * @return the active state of this listener.
     */
    public boolean isActive()
    {
        return fActive;
    }

    /**
     * Returns the listener's DataConverter
     *
     * @return
     */
    public DataConverter getConnectionDataConverter()
    {
        return fConnectionDataConverter;
    }

    /**
     * Sets the listener's DataConverter
     *
     * @param dataConverter
     */
    public void setConnectionDataConverter(DataConverter dataConverter)
    {
        fConnectionDataConverter = (dataConverter == null) ? new NullDataConverter() : dataConverter;
    }
}
