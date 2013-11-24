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
package Extasys.Network.TCP.Client.Connectors.Packets;

import Extasys.DataFrame;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.ManualResetEvent;

/**
 *
 * @author Nikos Siatras
 */
public class IncomingTCPClientPacket implements Runnable
{

    public ManualResetEvent fDone = new ManualResetEvent(false);
    private TCPConnector fConnector;
    private DataFrame fData;
    private IncomingTCPClientPacket fPreviousPacket;
    public boolean fCancel = false;

    /**
     * Constructs a new incoming packet received by a TCP connector.
     * 
     * Use this class to receive data from a server.
     * This is an incoming message that will remain in the client's thread pool
     * as a job for the thread pool workers.
     * 
     * @param TCPConnector is the TCP Connector where this message belongs to.
     * @param data is a DataFrame class.
     * @param previousPacket is the previous incoming message of the TCP Connector.
     */
    public IncomingTCPClientPacket(TCPConnector connector, DataFrame data, IncomingTCPClientPacket previousPacket)
    {
        fConnector = connector;
        fData = data;
        fPreviousPacket = previousPacket;

        connector.fMyTCPClient.fMyThreadPool.execute(this);
    }

    /**
     * Cancel this incoming packet.
     * 
     * By calling this method this and all the previous incoming packets that 
     * are stored in the thread pool will be canceled.
     * Call this method for the last incoming packet of the TCP Connector
     * when the TCP Connector disconnects.
     * 
     */
    public void Cancel()
    {
        fCancel = true;
        fDone.Set();
    }

    @Override
    public void run()
    {
        try
        {
            if (fPreviousPacket == null)
            {
                fConnector.fMyTCPClient.OnDataReceive(fConnector, fData);
            }
            else
            {
                fPreviousPacket.fDone.WaitOne();
                if (!fCancel && !fPreviousPacket.fCancel)
                {
                    fConnector.fMyTCPClient.OnDataReceive(fConnector, fData);
                }
                else
                {
                    fCancel = true;
                }
            }
        }
        catch (Exception ex)
        {
        }

        if (fPreviousPacket != null)
        {
            fPreviousPacket = null;
        }

        fDone.Set();
    }

    /**
     * Returns the data of this packet.
     * 
     * @return  the data of this packet.
     */
    public DataFrame getData()
    {
        return fData;
    }

    /**
     * Returns the previus incoming packet received by the client.
     * If the packet is null means that the packet has been received and parsed from the client.
     * 
     * @return the previus incoming packet received by the client.
     */
    public IncomingTCPClientPacket getPreviusPacket()
    {
        return fPreviousPacket;
    }
}
