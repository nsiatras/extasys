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
import Extasys.ManualResetEvent;
import java.net.DatagramPacket;

/**
 *
 * @author Nikos Siatras
 */
public class OutgoingUDPClientPacket implements Runnable
{

    public ManualResetEvent fDone = new ManualResetEvent(false);
    private UDPConnector fConnector;
    private DatagramPacket fData;
    private OutgoingUDPClientPacket fPreviousPacket;
    public boolean fCancel = false;

    /**
     * Constructs a new outgoing packet for an existing UDP Connector.
     * Use this class to send data from the UDP Connector to a server.
     * This is an outgoing message that will remain in the clients thread pool 
     * as a job for the thread pool workers.
     * @param connector is the UDPConnector where this message belongs to.
     * @param data is the outgoing DatagramPacket.
     * @param previousPacket is the previous outgoing packet of the UDPConnector.
     */
    public OutgoingUDPClientPacket(UDPConnector connector, DatagramPacket data, OutgoingUDPClientPacket previousPacket)
    {
        fConnector = connector;
        fData = data;
        fPreviousPacket = previousPacket;

        connector.getMyExtasysUDPClient().getMyThreadPool().execute(this);
    }

    /**
     * Cancel this outgoing packet.
     * By calling this method this and all the previous outgoing packets that 
     * are stored in the thread pool will be canceled.
     * Call this method for the last outgoing packet of the UDPConnector
     * when the UDPConnector disconnects.
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
                fConnector.fSocket.send(fData);
                fConnector.fBytesOut += fData.getLength();
            }
            else
            {
                fPreviousPacket.fDone.WaitOne();
                if (!fCancel && !fPreviousPacket.fCancel)
                {
                    fConnector.fSocket.send(fData);
                    fConnector.fBytesOut += fData.getLength();
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
     * Returns the DatagramPacket of this outgoing UDP packet.
     * @return the DatagramPacket of this outgoing UDP packet.
     */
    public DatagramPacket getData()
    {
        return fData;
    }

    /**
     * Returns the previus outgoing packet of the client.
     * If the packet is null means that the packet has been send to server.
     * @return the previus outgoing packet of the client.
     */
    public OutgoingUDPClientPacket getPreviusPacket()
    {
        return fPreviousPacket;
    }
}


