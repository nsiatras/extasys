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

import Extasys.DataFrame;
import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import Extasys.Network.NetworkPacket;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Nikos Siatras
 */
public final class IncomingTCPClientConnectionPacket extends NetworkPacket
{

    private final TCPClientConnection fClient;

    /**
     * Constructs a new incoming packet for an existing TCP client connection.
     * Use this class to receive data from a client. This is an incoming message
     * that will remain in the servers thread pool as a job for the thread pool
     * workers.
     *
     * @param client is the client where this message belongs to.
     * @param data
     * @param previousPacket is the previous incoming message of the client.
     */
    public IncomingTCPClientConnectionPacket(TCPClientConnection client, byte[] data, NetworkPacket previousPacket)
    {
        super(data, previousPacket, client.getMyExtasysTCPServer().getMyThreadPool());
        fClient = client;

        try
        {
            SendToThreadPool();
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

            // Call on data receive
            if (!fCancel)
            {
                // Decrypt Data
                final byte[] decryptedData = fClient.getMyTCPListener().getConnectionDataConverter().Revert(fPacketsData);

                // Call OnDataReceive
                fClient.fMyExtasysServer.OnDataReceive(fClient, new DataFrame(decryptedData));
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
