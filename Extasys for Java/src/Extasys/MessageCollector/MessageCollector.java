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
package Extasys.MessageCollector;

import Extasys.ByteArrayBuilder;
import Extasys.Encryption.ConnectionEncryptor;

/**
 *
 * @author Nikos Siatras
 */
public abstract class MessageCollector
{

    private final ByteArrayBuilder fIncomingDataBuffer = new ByteArrayBuilder();
    private final MessageETX fMessageETX;
    private int fIndexOf = -1;

    public MessageCollector(MessageETX messageETX)
    {
        fMessageETX = messageETX;
    }

    public synchronized void AppendDataWithDecryption(final byte[] bytes, ConnectionEncryptor encryptor)
    {
        try
        {
            fIncomingDataBuffer.Append(bytes);
            fIndexOf = fIncomingDataBuffer.IndexOf(fMessageETX.getBytes());

            while (fIndexOf > -1)
            {
                MessageCollected(encryptor.Decrypt(fIncomingDataBuffer.SubList(0, fIndexOf)));
                fIncomingDataBuffer.Delete(0, fIndexOf + fMessageETX.getLength());
                fIndexOf = fIncomingDataBuffer.IndexOf(fMessageETX.getBytes());
            }
        }
        catch (Exception ex)
        {
            //System.err.println("Extasys.Network.TCP.Client.Connectors.Tools.TCPClientMessageCollector Error: " + ex.getMessage());
        }
    }

    public synchronized void AppendData(final byte[] bytes)
    {
        try
        {
            fIncomingDataBuffer.Append(bytes);
            fIndexOf = fIncomingDataBuffer.IndexOf(fMessageETX.getBytes());

            while (fIndexOf > -1)
            {
                MessageCollected(fIncomingDataBuffer.SubList(0, fIndexOf));
                fIncomingDataBuffer.Delete(0, fIndexOf + fMessageETX.getLength());
                fIndexOf = fIncomingDataBuffer.IndexOf(fMessageETX.getBytes());
            }
        }
        catch (Exception ex)
        {
            //System.err.println("Extasys.Network.TCP.Client.Connectors.Tools.TCPClientMessageCollector Error: " + ex.getMessage());
        }
    }

    /**
     * This method is called every time the message collector collects a message
     *
     * @param bytes
     */
    public abstract void MessageCollected(final byte[] bytes);

    public void Dispose()
    {
        // Do nothing..
    }

}
