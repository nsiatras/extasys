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
package extasys.network.udp.server.listener.packets;

import java.net.DatagramPacket;

import extasys.ManualResetEvent;
import extasys.network.udp.server.listener.UDPListener;

/**
 *
 * @author Nikos Siatras
 */
public class IncomingUDPServerPacket implements Runnable
{

    private ManualResetEvent fDone = new ManualResetEvent(false);
    private boolean fCancel = false;
    private UDPListener fMyListener;
    private DatagramPacket fData;
    private IncomingUDPServerPacket fPreviousPacket;

    /**
     * Constructs a new incoming UDP packet.
     * 
     * Use this class to receive data from a client.
     * This is an incoming message that will remain in the servers thread pool 
     * as a job for the thread pool workers.
     * 
     * @param listener is the listener caught the message.
     * @param packet is a Datagram packet.
     * @param previousPacket is the previous incoming message of the listener.
     */
    public IncomingUDPServerPacket(UDPListener listener, DatagramPacket packet, IncomingUDPServerPacket previousPacket)
    {
        fMyListener = listener;
        fData = packet;
        listener.getMyExtasysUDPServer().fMyThreadPool.execute(this);
    }

    /**
     * Cancel this incoming packet.
     * 
     * By calling this method this and all the previous incoming packets that 
     * are stored in the thread pool will be canceled.
     * Call this method for the last incoming packet of the server
     * when the server stops.
     * 
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
                fMyListener.getMyExtasysUDPServer().OnDataReceive(fMyListener, fData);
            }
            else
            {
                fPreviousPacket.fDone.WaitOne();
                if (!fCancel && !fPreviousPacket.fCancel)
                {
                    fMyListener.getMyExtasysUDPServer().OnDataReceive(fMyListener, fData);
                }
                else
                {
                    System.out.println("IncomingUDPServerPacket canceled!");
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
     * Returns the DatagramPacket of this incoming UDP packet.
     * 
     * @return the DatagramPacket of this incoming UDP packet.
     */
    public DatagramPacket getData()
    {
        return fData;
    }

    /**
     * Returns the previus incoming packet received by the server.
     * If the packet is null means that the packet has been received and parsed from the server.
     * 
     * @return the previus incoming packet received by the server.
     */
    public IncomingUDPServerPacket getPreviusPacket()
    {
        return fPreviousPacket;
    }
}
