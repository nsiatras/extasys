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

import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.NetworkPacket;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Nikos Siatras
 */
public final class MessageCollectorTCPClientPacket extends NetworkPacket
{

    private final TCPConnector fConnector;

    /**
     * Constructs a new (incoming) message collector packet.
     *
     * Use this class to receive data from the server. This is an incoming
     * message that will remain in the servers thread pool as a job for the
     * thread pool workers.
     *
     * @param connector is the packets TCP Connector.
     * @param data is the byte[] received.
     * @param previousPacket is the previous message collector packet of the TCP
     * Connector.
     */
    public MessageCollectorTCPClientPacket(TCPConnector connector, byte[] data, NetworkPacket previousPacket)
    {
        super(data, previousPacket, connector.getMyExtasysTCPClient().getMyThreadPool());
        fConnector = connector;

        try
        {
            SendToThreadPool();
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

            // Call append data
            if (!fCancel)
            {
                // Decrypt Data
                if (fConnector.isAutoApplyMessageSplitterOn())
                {
                    // Append data using AppendDataWithConversion.
                    // Message collector reverts data later
                    fConnector.fMessageCollector.AppendDataWithConversion(fPacketsData, fConnector.getConnectionDataConverter());
                }
                else
                {
                    // Revert data before append
                    byte[] revertedData = fConnector.getConnectionDataConverter().Revert(fPacketsData);
                    fConnector.fMessageCollector.AppendData(revertedData);
                }

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
