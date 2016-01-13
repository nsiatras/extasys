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
package extasys.network.tcp.server.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 *
 * @author Nikos Siatras
 */
public class TCPListenerThread implements Runnable
{

    private final ServerSocket fSocket;
    private final TCPListener fMyListener;

    public TCPListenerThread(ServerSocket socket, TCPListener myListener)
    {
        fSocket = socket;
        fMyListener = myListener;
    }

    @Override
    public void run()
    {
        while (fMyListener.isActive())
        {
            try
            {
                TCPClientConnection client = new TCPClientConnection(fSocket.accept(), fMyListener, fMyListener.isMessageCollectorInUse(), fMyListener.getMessageSplitter());
                if (fMyListener.getConnectedClients().size() >= fMyListener.getMaxConnections())
                {
                    client.DisconnectMe();
                }
            }
            catch (SocketTimeoutException ex)
            {
            }
            catch (SocketException ex)
            {
            }
            catch (SecurityException | IOException ex)
            {
            }
        }

        //System.out.println("Listener thread interrupted.");
    }
}
