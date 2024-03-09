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
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Nikos Siatras
 */
public final class OutgoingTCPClientPacket extends NetworkPacket implements Runnable
{

    private final TCPConnector fConnector;

    /**
     * Constructs a new outgoing packet for an existing TCP Connector.
     *
     * Use this class to send data from the TCP Connector to a server. This is
     * an outgoing message that will remain in the servers thread pool as a job
     * for the thread pool workers.
     *
     * @param connector is the TCPConnector where this message belongs to.
     * @param data is the byte array to be sent.
     *
     * @param previousPacket is the previous outgoing packet of the
     * TCPConnector.
     * @throws
     * Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException
     */
    public OutgoingTCPClientPacket(TCPConnector connector, byte[] data, NetworkPacket previousPacket) throws ConnectorCannotSendPacketException
    {
        // Always encrypt outgoing data !
        super(connector.getConnectionEncyptor().Encrypt(data), previousPacket);
        fConnector = connector;

        SendToThreadPool();
    }

    protected void SendToThreadPool()
    {
        try
        {
            fConnector.fMyTCPClient.fMyThreadPool.execute(this);
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
            this.WaitForPreviousPacketToBeProcessedAndCheckIfItWasCanceled();

            if (!fCancel)
            {
                try
                {
                    fConnector.fOutput.write(super.fPacketsData);
                    fConnector.fBytesOut += super.fPacketsData.length;
                    fConnector.fMyTCPClient.fTotalBytesOut += super.fPacketsData.length;
                }
                catch (IOException ioException)
                {
                    fCancel = true;
                    fDone.Set();
                    fConnector.Stop();
                    return;
                }
            }
        }
        catch (Exception ex)
        {
        }

        fDone.Set();
    }
}
