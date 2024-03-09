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
package Extasys.Network.UDP.Client.Connectors.Packets;

import Extasys.Network.UDP.Client.Connectors.UDPConnector;
import Extasys.Network.NetworkPacket;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 *
 * @author Nikos Siatras
 */
public class OutgoingUDPClientPacket extends NetworkPacket implements Runnable
{

    private final UDPConnector fConnector;
    private final DatagramPacket fDataGram;

    /**
     * Constructs a new outgoing packet for an existing UDP Connector. Use this
     * class to send data from the UDP Connector to a server. This is an
     * outgoing message that will remain in the clients thread pool as a job for
     * the thread pool workers.
     *
     * @param connector is the UDPConnector where this message belongs to.
     * @param data is the outgoing DatagramPacket.
     * @param previousPacket is the previous outgoing packet of the
     * UDPConnector.
     */
    public OutgoingUDPClientPacket(UDPConnector connector, DatagramPacket data, NetworkPacket previousPacket)
    {
        super(new byte[0], previousPacket);
        fConnector = connector;
        fDataGram = data;

        connector.getMyExtasysUDPClient().getMyThreadPool().execute(this);
    }

    @Override
    public void run()
    {
        try
        {
            // Wait for previous Packet to be processed
            // by the thread pool.
            this.WaitForPreviousPacketToBeProcessedAndCheckIfItWasCanceled();

            // Encrypt outgoing data
            byte[] encryptedData = fConnector.getConnectionEncyptor().Encrypt(fDataGram.getData());
            fDataGram.setData(encryptedData, 0, encryptedData.length);

            if (!fCancel)
            {
                fConnector.fSocket.send(fDataGram);
                fConnector.fBytesOut += fDataGram.getLength();
            }
        }
        catch (IOException ex)
        {
        }

        fDone.Set();
    }

}
