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
package Extasys.Network.UDP.Server.Listener.Packets;

import Extasys.Network.UDP.Server.Listener.UDPListener;

import Extasys.Network.NetworkPacket;
import java.net.DatagramPacket;

/**
 *
 * @author Nikos Siatras
 */
public class OutgoingUDPServerPacket extends NetworkPacket implements Runnable
{

    private final UDPListener fMyListener;
    private final DatagramPacket fDataGram;

    public OutgoingUDPServerPacket(UDPListener listener, DatagramPacket packet, NetworkPacket previousPacket)
    {
        super(new byte[0], previousPacket);
        fMyListener = listener;
        fDataGram = packet;

        listener.getMyExtasysUDPServer().fMyThreadPool.execute(this);
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
                fMyListener.fSocket.send(fDataGram);
                fMyListener.fBytesOut += fDataGram.getLength();
            }

        }
        catch (Exception ex)
        {
        }

        fDone.Set();
    }

}
