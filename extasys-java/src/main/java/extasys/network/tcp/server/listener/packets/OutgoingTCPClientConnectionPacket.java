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
package extasys.network.tcp.server.listener.packets;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

import extasys.ManualResetEvent;
import extasys.network.tcp.server.listener.TCPClientConnection;
import extasys.network.tcp.server.listener.exceptions.OutgoingPacketFailedException;

/**
 *
 * @author Nikos Siatras
 */
public final class OutgoingTCPClientConnectionPacket implements Runnable
{

    public final ManualResetEvent fDone = new ManualResetEvent(false);
    private final TCPClientConnection fClient;
    private final byte[] fBytes;
    private final int fOffset;
    private final int fLength;
    private OutgoingTCPClientConnectionPacket fPreviousPacket;
    public boolean fCancel = false;

    /**
     * Constructs a new outgoing packet for an existing TCP client connection.
     *
     * Use this class to send data from the server to a connected client. This
     * is an outgoing message that will remain in the servers thread pool as a
     * job for the thread pool workers.
     *
     * @param client is the client where this message belongs to.
     * @param bytes is the byte array to be sent.
     * @param offset is the position in the data buffer at witch to begin
     * sending.
     * @param length is the number of the bytes to be send.
     * @param previousPacket is the previous outgoing packet of the client.
     * @throws
     * extasys.network.tcp.server.listener.exceptions.OutgoingPacketFailedException
     */
    public OutgoingTCPClientConnectionPacket(TCPClientConnection client, byte[] bytes, int offset, int length, OutgoingTCPClientConnectionPacket previousPacket) throws OutgoingPacketFailedException
    {
        fClient = client;
        fBytes = bytes;
        fOffset = offset;
        fLength = length;
        fPreviousPacket = previousPacket;

        SendToThreadPool();
    }

    protected void SendToThreadPool()
    {
        try
        {
            fClient.fMyExtasysServer.fMyThreadPool.execute(this);
        }
        catch (RejectedExecutionException ex)
        {
            fClient.ForceDisconnect();
        }
    }

    /**
     * Cancel this outgoing packet.
     *
     * By calling this method this and all the previous outgoing packets that
     * are stored in the thread pool will be canceled. Call this method for the
     * last outgoing packet of the client when the client disconnects.
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
                try
                {
                    fClient.fOutput.write(fBytes, fOffset, fLength);
                    fClient.fOutput.flush();
                    fClient.fBytesOut += fLength;
                    fClient.fMyListener.fBytesOut += fLength;
                }
                catch (IOException ioException)
                {
                    fCancel = true;
                    fDone.Set();
                    fClient.DisconnectMe();
                }
            }
            else
            {
                fPreviousPacket.fDone.WaitOne();

                if (!fCancel && !fPreviousPacket.fCancel)
                {
                    try
                    {
                        fClient.fOutput.write(fBytes, fOffset, fLength);
                        fClient.fOutput.flush();
                        fClient.fBytesOut += fLength;
                        fClient.fMyListener.fBytesOut += fLength;
                    }
                    catch (IOException ioException)
                    {
                        fCancel = true;
                        fDone.Set();
                        fClient.DisconnectMe();
                    }
                }
                else
                {
                    fCancel = true;
                }

                fPreviousPacket = null;
            }
        }
        catch (Exception ex)
        {
        }

        fDone.Set();

    }

    /**
     * Returns the byte array of this packet.
     *
     * @return the byte array of this packet.
     */
    public byte[] getBytes()
    {
        return fBytes;
    }

    /**
     * Returns the offset of this packet.
     *
     * @return the offset of this packet.
     */
    public int getOffset()
    {
        return fOffset;
    }

    /**
     * Returns the number of bytes to send from this packet.
     *
     * @return the number of bytes to send from this packet.
     */
    public int getLength()
    {
        return fLength;
    }
}
