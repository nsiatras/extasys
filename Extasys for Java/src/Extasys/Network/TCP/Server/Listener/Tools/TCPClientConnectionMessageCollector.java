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
package Extasys.Network.TCP.Server.Listener.Tools;

import Extasys.ByteArrayBuilder;
import Extasys.DataFrame;
import Extasys.Network.TCP.Server.Listener.TCPClientConnection;

/**
 *
 * @author Nikos Siatras
 */
public class TCPClientConnectionMessageCollector
{

    private TCPClientConnection fMyClient;
    private final byte[] fETXStr;
    private final ByteArrayBuilder fIncomingDataBuffer;
    private int fIndexOfETX;
    private final int fETXLength;

    public TCPClientConnectionMessageCollector(TCPClientConnection myClient, char splitter)
    {
        fMyClient = myClient;
        fETXStr = String.valueOf(splitter).getBytes(fMyClient.fMyListener.getCharset());
        fETXLength = fETXStr.length;
        fIncomingDataBuffer = new ByteArrayBuilder();
    }

    public TCPClientConnectionMessageCollector(TCPClientConnection myClient, String splitter)
    {
        fMyClient = myClient;
        fETXStr = splitter.getBytes(fMyClient.fMyListener.getCharset());
        fETXLength = fETXStr.length;
        fIncomingDataBuffer = new ByteArrayBuilder();
    }

    public void AppendData(byte[] bytes)
    {
        try
        {
            fIncomingDataBuffer.Append(bytes);
            fIndexOfETX = fIncomingDataBuffer.IndexOf(fETXStr);
            while (fIndexOfETX > -1)
            {
                fMyClient.getMyTCPListener().getMyExtasysTCPServer().OnDataReceive(fMyClient, new DataFrame(fIncomingDataBuffer.SubList(0, fIndexOfETX)));
                fIncomingDataBuffer.Delete(0, fIndexOfETX + fETXLength);
                fIndexOfETX = fIncomingDataBuffer.IndexOf(fETXStr);
            }
        }
        catch (Exception ex)
        {
            //System.err.println("<TCPCLientConnectionMessageCollector> Error:" + ex.getMessage());
        }
    }

    public void Dispose()
    {
        try
        {
            fMyClient = null;
        }
        catch (Exception ex)
        {
        }
    }
}
