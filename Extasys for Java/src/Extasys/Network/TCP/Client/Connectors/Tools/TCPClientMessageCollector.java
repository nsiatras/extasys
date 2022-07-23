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
package Extasys.Network.TCP.Client.Connectors.Tools;

import Extasys.ByteArrayBuilder;
import Extasys.DataFrame;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;

/**
 *
 * @author Nikos Siatras
 */
public class TCPClientMessageCollector
{

    private TCPConnector fMyConnector;
    private final byte[] fETXStr;
    private final ByteArrayBuilder fIncomingDataBuffer = new ByteArrayBuilder();
    private final int fETXLength;
    private int fIndexOfETX;

    public TCPClientMessageCollector(TCPConnector connector, char ETX)
    {
        fMyConnector = connector;
        fETXStr = String.valueOf(ETX).getBytes(connector.getCharset());
        fETXLength = fETXStr.length;
    }

    public TCPClientMessageCollector(TCPConnector connector, String splitter)
    {
        fMyConnector = connector;
        fETXStr = splitter.getBytes(connector.getCharset());
        fETXLength = fETXStr.length;
    }

    public void AppendData(byte[] bytes)
    {
        try
        {
            fIncomingDataBuffer.Append(bytes);
            fIndexOfETX = fIncomingDataBuffer.IndexOf(fETXStr);
            while (fIndexOfETX > -1)
            {
                fMyConnector.getMyExtasysTCPClient().OnDataReceive(fMyConnector, new DataFrame(fIncomingDataBuffer.SubList(0, fIndexOfETX)));
                fIncomingDataBuffer.Delete(0, fIndexOfETX + fETXLength);
                fIndexOfETX = fIncomingDataBuffer.IndexOf(fETXStr);
            }
        }
        catch (Exception ex)
        {
            //System.err.println("Extasys.Network.TCP.Client.Connectors.Tools.TCPClientMessageCollector Error: " + ex.getMessage());
        }
    }

    public void Dispose()
    {
        try
        {
            fMyConnector = null;
        }
        catch (Exception ex)
        {
        }
    }
}
