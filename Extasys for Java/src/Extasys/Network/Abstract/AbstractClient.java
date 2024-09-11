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

import Extasys.ExtasysThreadPool;

/**
 *
 * @author Nikos Siatras - https://github.com/nsiatras
 *
 * This class holds common properties for Clients such as the TCPClient and the
 * UDPClient
 */
public abstract class AbstractClient
{

    private String fName, fDescription;
    protected final ExtasysThreadPool fMyThreadPool;
    public long fTotalBytesIn = 0, fTotalBytesOut = 0;

    public AbstractClient(String name, String description, int corePoolSize, int maximumPoolSize)
    {
        fName = name;
        fDescription = description;
        fMyThreadPool = new ExtasysThreadPool(corePoolSize, maximumPoolSize);
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
     * Sets the name of the TCPClient
     *
     * @param name
     */
    public void setName(String name)
    {
        fName = name;
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
     * Sets the description of the TCPClient
     *
     * @param description is the TCPClient's description
     */
    public void setDescription(String description)
    {
        fDescription = description;
    }

    /**
     * Return the client's Thread Pool.
     *
     * @return the client's Thread Pool.
     */
    public ExtasysThreadPool getMyThreadPool()
    {
        return fMyThreadPool;
    }

    /**
     * Returns the total number of bytes received by this client.
     *
     * @return the number of bytes received by this client.
     */
    public long getBytesIn()
    {
        return fTotalBytesIn;
    }

    /**
     * Returns the total number of bytes send from this client.
     *
     * @return the total number of bytes send from this client.
     */
    public long getBytesOut()
    {
        return fTotalBytesOut;
    }
}
