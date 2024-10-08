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
import Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Nikos Siatras
 */
public final class OutgoingTCPClientConnectionPacket extends NetworkPacket
{
    
    private final TCPClientConnection fClient;

    /**
     * Constructs a new outgoing packet for an existing TCP client connection.
     *
     * Use this class to send data from the server to a connected client. This
     * is an outgoing message that will remain in the servers thread pool as a
     * job for the thread pool workers.
     *
     * @param client is the client where this message belongs to.
     * @param data
     * @param previousPacket is the previous outgoing packet of the client.
     * @throws
     * Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException
     */
    public OutgoingTCPClientConnectionPacket(TCPClientConnection client, byte[] data, NetworkPacket previousPacket) throws OutgoingPacketFailedException
    {
        super(data, previousPacket);
        fClient = client;
        
        try
        {
            client.getMyExtasysTCPServer().getMyThreadPool().EnqueNetworkPacket(this);
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
            
            if (!fCancel)
            {
                final byte[] bytesToSent;

                // Check if the MessageSplitter must be applied to the end of 
                // the Converted Data byte array to sent.
                if (fClient.fMyListener.isUsingMessageCollector() && fClient.fMyListener.isAutoApplyMessageSplitterOn())
                {
                    // ConvertData data
                    final byte[] convertedData = fClient.getMyTCPListener().getConnectionDataConverter().Convert(super.fPacketsData);

                    // Add ETX to the end of convertedData
                    bytesToSent = super.Combine2ByteArrays(convertedData, fClient.fMyListener.getMessageETX().getBytes());
                }
                else
                {
                    // Convert Data using the client's DataConverter data before Sent
                    bytesToSent = fClient.getMyTCPListener().getConnectionDataConverter().Convert(super.fPacketsData);
                }
                
                try
                {
                    fClient.fOutput.write(bytesToSent);
                    fClient.fBytesOut += bytesToSent.length;
                    fClient.fMyListener.fBytesOut += bytesToSent.length;
                    fClient.fMyExtasysServer.fTotalBytesOut += bytesToSent.length;
                }
                catch (IOException ioException)
                {
                    fCancel = true;
                    fDone.Set();
                    fClient.DisconnectMe();
                    return;
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
