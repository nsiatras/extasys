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
 * This class holds common properties for Listeners such as the TCPServer and
 * the UDPServer
 */
public abstract class AbstractServer
{

    private String fName, fDescription;
    protected final ExtasysThreadPool fMyThreadPool;
    public long fTotalBytesIn = 0, fTotalBytesOut = 0;

    public AbstractServer(String name, String description, int corePoolSize, int maximumPoolSize)
    {
        fName = name;
        fDescription = description;
        fMyThreadPool = new ExtasysThreadPool(corePoolSize, maximumPoolSize);
    }
    
    public abstract void Dispose();

    /**
     * Server's name.
     *
     * @return server's name.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Set's the server's name
     *
     * @param name
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Server's description.
     *
     * @return server's description.
     */
    public String getDescription()
    {
        return fDescription;
    }

    /**
     * Set's the server's description
     *
     * @param description is the server's description
     */
    public void setDescription(String description)
    {
        fDescription = description;
    }

    /**
     * Returns the server's ThreadPool.
     *
     * @return My Thread Pool.
     */
    public ExtasysThreadPool getMyThreadPool()
    {
        return fMyThreadPool;
    }

    /**
     * Returns the total bytes received from this server.
     *
     * @return the total bytes received from this server.
     */
    public long getBytesIn()
    {
        return fTotalBytesIn;
    }

    /**
     * Returns the total bytes sent from this server.
     *
     * @return the total bytes sent from this server.
     */
    public long getBytesOut()
    {
        return fTotalBytesOut;
    }
}
