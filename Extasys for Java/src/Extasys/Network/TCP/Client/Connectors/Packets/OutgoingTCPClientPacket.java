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
public final class OutgoingTCPClientPacket extends NetworkPacket
{
    
    private final TCPConnector fConnector;

    /**
     * Constructs a new outgoing packet for an existing TCP Connector.
     *
     * Use this class to send data from a TCP Connector. This is an outgoing
     * message that will remain in the server's thread pool as a job for the
     * thread pool workers.
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
        super(data, previousPacket);
        fConnector = connector;
        
        try
        {
            connector.getMyExtasysTCPClient().getMyThreadPool().EnqueNetworkPacket(this);
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
            
            if (!fCancel)
            {
                final byte[] bytesToSent;

                // Check if the MessageSplitter must be applied to the end of 
                // the 'Converted Data' byte array to sent.
                if (fConnector.isUsingMessageCollector() && fConnector.isAutoApplyMessageSplitterOn())
                {
                    // Convert Data using the DataConverter data
                    final byte[] convertedData = fConnector.getConnectionDataConverter().Convert(super.fPacketsData);

                    // Add ETX to the end of Converted Data
                    bytesToSent = super.Combine2ByteArrays(convertedData, fConnector.getMessageETX().getBytes());
                }
                else
                {
                    // Convert Data using the connector's DataConverter
                    bytesToSent = fConnector.getConnectionDataConverter().Convert(fPacketsData);
                }
                
                try
                {
                    fConnector.fOutput.write(bytesToSent);
                    fConnector.fBytesOut += bytesToSent.length;
                    fConnector.getMyExtasysTCPClient().fTotalBytesOut += bytesToSent.length;
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

        // Mark previous Packet as null.
        // GC will take it out later...
        fPreviousPacket = null;
        
        fDone.Set();
    }
}
