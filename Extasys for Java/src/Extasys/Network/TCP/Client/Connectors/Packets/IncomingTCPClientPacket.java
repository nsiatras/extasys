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
import Extasys.Network.NetworkPacket;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Nikos Siatras
 */
public final class IncomingTCPClientPacket extends NetworkPacket implements Runnable
{

    private final TCPConnector fConnector;

    /**
     * Constructs a new incoming packet received by a TCP connector.
     *
     * Use this class to receive data from a server. This is an incoming message
     * that will remain in the client's thread pool as a job for the thread pool
     * workers.
     *
     * @param connector is the TCP Connector where this message belongs to.
     * @param data
     * @param previousPacket is the previous incoming message of the TCP
     * Connector.
     */
    public IncomingTCPClientPacket(TCPConnector connector, byte[] data, NetworkPacket previousPacket)
    {
        super(data, previousPacket);
        fConnector = connector;

        SendToThreadPool();
    }

    protected void SendToThreadPool()
    {
        try
        {
            fConnector.getMyExtasysTCPClient().getMyThreadPool().execute(this);
        }
        catch (RejectedExecutionException ex)
        {
            fConnector.ForceStop();
        }
    }

    @Override
    public void run()
    {
        try
        {
            // Wait for previous Packet to be processed
            // by the thread pool.
            super.WaitForPreviousPacketToBeProcessedAndCheckIfItWasCanceled();

            // Call OnDataReceive
            if (!fCancel)
            {
                // Decrypt Data
                final byte[] decryptedData = fConnector.getConnectionEncyptor().Decrypt(fPacketsData);

                // Call OnDataReceive
                fConnector.getMyExtasysTCPClient().OnDataReceive(fConnector, new DataFrame(decryptedData));
            }
        }
        catch (Exception ex)
        {
        }

        // Mark previous Packet as null.
        // GC will take it out later...
        fPreviousPacket = null;

        fDone.Set();
    }

}
