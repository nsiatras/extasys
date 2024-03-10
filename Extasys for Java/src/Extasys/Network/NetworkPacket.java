/*Copyright (c) 2024 Nikos Siatras

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
package Extasys.Network;

import Extasys.ManualResetEvent;

/**
 *
 * @author Nikos Siatras
 */
public class NetworkPacket
{

    public final ManualResetEvent fDone = new ManualResetEvent(false);
    public final byte[] fPacketsData;
    public NetworkPacket fPreviousPacket;
    public volatile boolean fCancel = false;

    public NetworkPacket(byte[] data, NetworkPacket previousPacket)
    {
        fPacketsData = data;
        fPreviousPacket = previousPacket;
    }

    /**
     * This method waits until this packet is processed by the Thread Pool and
     * checks if it was canceled.
     */
    public void WaitForPreviousPacketToBeProcessedAndCheckIfItWasCanceled()
    {
        if (fPreviousPacket != null)
        {
            fPreviousPacket.fDone.WaitOne();

            // Previous packet was canceled.
            // That means this packet must be canceled too!
            if (fPreviousPacket.fCancel)
            {
                fCancel = true;
            }
        }
    }

    /**
     * Cancel this network packet.
     *
     * Calling this method marks this and its previous NetworkPacket as
     * Canceled. Call this method for the last incoming packet of a connection
     * when the connection drops.
     */
    public void Cancel()
    {
        fCancel = true;

        if (fPreviousPacket != null)
        {
            fPreviousPacket.Cancel();
        }
    }

}
