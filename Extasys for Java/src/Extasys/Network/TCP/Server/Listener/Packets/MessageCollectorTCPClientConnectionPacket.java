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
package Extasys.Network.TCP.Server.Listener.Packets;

import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import Extasys.Network.NetworkPacket;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Nikos Siatras
 */
public final class MessageCollectorTCPClientConnectionPacket extends NetworkPacket implements Runnable
{

    private final TCPClientConnection fClient;

    /**
     * Constructs a new (incoming) message collector packet.
     *
     * Use this class to receive data from a client. This is an incoming message
     * that will remain in the servers thread pool as a job for the thread pool
     * workers.
     *
     * @param client is the packets TCPClientConnection.
     * @param data
     * @param previousPacket is the previous message collector packet of the
     * TCPClientConnection.
     */
    public MessageCollectorTCPClientConnectionPacket(final TCPClientConnection client, final byte[] data, final NetworkPacket previousPacket)
    {
        // Always decrypt incoming data !
        super(data, previousPacket);
        fClient = client;

        SendToThreadPool();
    }

    protected void SendToThreadPool()
    {
        try
        {
            fClient.fMyExtasysServer.getMyThreadPool().execute(this);
        }
        catch (RejectedExecutionException ex)
        {
            fClient.ForceDisconnect();
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

            // Append Data to Message Collector
            if (!fCancel)
            {
                // Decrypt Data
                if (fClient.fMyListener.isAutoApplyMessageSplitterOn())
                {
                    // Append data as using AppendDataWithDecryption.
                    // Message collector decrypts data later
                    fClient.fMyMessageCollector.AppendDataWithDecryption(fPacketsData, fClient.fMyListener.getConnectionDataConverter());
                }
                else
                {
                    // Decrypt data before append
                    byte[] decyptedData = fClient.getMyTCPListener().getConnectionDataConverter().Revert(fPacketsData);
                    fClient.fMyMessageCollector.AppendData(decyptedData);
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
