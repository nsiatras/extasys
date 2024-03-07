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
package Extasys.Examples.TCPChatServer;

import Extasys.DataFrame;
import Extasys.Network.TCP.Server.Listener.Exceptions.*;

import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Nikos Siatras
 */
public class TCPChatServer extends Extasys.Network.TCP.Server.ExtasysTCPServer
{

    private final Hashtable fConnectedClients;
    private String fSPT = String.valueOf(((char) 2)); // Message splitter character.
    private String fMCChar = String.valueOf(((char) 3)); // Message collector character.
    private Thread fPingThread;
    private boolean fServerIsActive;
    private frmTCPChatServer fMainForm;

    public TCPChatServer(InetAddress listenerIP, int port, frmTCPChatServer frmMain)
    {
        super("TCP Chat Server", "", 10, 100);
        super.AddListener("Main Listener", listenerIP, port, 99999, 20480, 10000, 100, Charset.forName("UTF-8"), ((char) 3));
        fConnectedClients = new Hashtable();
        fMainForm = frmMain;
    }

    @Override
    public void Start()
    {
        try
        {
            super.Start();

            fPingThread = new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    while (fServerIsActive)
                    {
                        try
                        {
                            SendToAllClients("Ping" + fSPT);
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException ex)
                        {
                        }

                    }
                }
            });

            fServerIsActive = true;
            fPingThread.start();
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            Stop();
        }
    }

    @Override
    public void Stop()
    {
        fServerIsActive = false;

        if (fPingThread != null)
        {
            fServerIsActive = false;
            fPingThread.interrupt();
        }

        super.Stop();
    }

    @Override
    public void OnDataReceive(TCPClientConnection sender, DataFrame data)
    {
        try
        {
            System.out.println(new String(data.getBytes()));
            String[] splittedMessage = new String(data.getBytes()).split(fSPT);

            if (splittedMessage[0].equals("Login"))
            {
                /*  Message: Login ((char)2) Username
                Client wants to login.
                Server checks if username is unique.
                If the username is taken server replys -> "Change_Username ((char)2)"
                If the username is not taken then server replys -> "Welcome ((char)2)" to the new client
                and sends "New_User ((char)2) NewUsername" to all other clients.
                 */
                String tmpUsername = splittedMessage[1];
                if (IsUsernameTaken(tmpUsername))
                {
                    SendToClient("Change_Username" + fSPT, sender);
                }
                else
                {
                    TCPChatUser user = new TCPChatUser(tmpUsername, sender);
                    fConnectedClients.put(sender.getIPAddress(), user);

                    SendToAllClients("New_User" + fSPT + tmpUsername);
                    SendToClient("Welcome" + fSPT, sender);
                }
            }
            else if (splittedMessage[0].equals("Message"))
            {
                /*  Message: Message ((char)2) some_text
                Client sends a chat message to the server.
                Server checks if the client is registered to the server.
                If the client is registered to the server 
                the server sends this message to all the other clients "Message ((char)2) Sender's username : some_text" else
                it disconnects the client.  
                 */
                if (fConnectedClients.containsKey(sender.getIPAddress()))
                {
                    SendToAllClients("Message" + fSPT + GetClientName(sender) + ":" + splittedMessage[1]);
                }
                else
                {
                    sender.DisconnectMe();
                }
            }
            else if (splittedMessage[0].equals("Get_List"))
            {
                /*  Message: Get_List ((char)2)
                Client requets a list with other connected clients.
                If the client is registered to the server the server replys the list
                else it disconnects the client.
                 */
                if (fConnectedClients.containsKey(sender.getIPAddress()))
                {
                    SendToClient(GetConnectedClientsList(), sender);
                }
                else
                {
                    sender.DisconnectMe();
                }
            }
            else if (splittedMessage[0].equals("Pong"))
            {
                /* Message: Pong ((char)2)
                Client response to Ping.
                 */
                System.out.println(GetClientName(sender) + " PONG!");
            }
            else
            {
                System.out.println(sender.getIPAddress().toString() + " sends wrong message");
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Returns true if the username is taken.
     *
     * @param username is the username to check
     * @return true or false.
     */
    private boolean IsUsernameTaken(String username)
    {
        for (Enumeration e = fConnectedClients.keys(); e.hasMoreElements();)
        {
            try
            {
                if (((TCPChatUser) fConnectedClients.get(e.nextElement())).getUsername().equals(username))
                {
                    return true;
                }
            }
            catch (Exception ex)
            {
            }
        }
        return false;
    }

    private String GetClientName(TCPClientConnection sender)
    {
        if (fConnectedClients.containsKey(sender.getIPAddress()))
        {
            return ((TCPChatUser) fConnectedClients.get(sender.getIPAddress())).getUsername();
        }
        return "";
    }

    private void SendToAllClients(String message)
    {
        message = message + fMCChar;
        for (Enumeration e = fConnectedClients.keys(); e.hasMoreElements();)
        {
            try
            {
                ((TCPChatUser) fConnectedClients.get(e.nextElement())).getConnection().SendData(message);
            }
            catch (ClientIsDisconnectedException ex)
            {
                System.err.println(ex.getMessage());
            }
            catch (OutgoingPacketFailedException ex)
            {
                System.err.println(ex.getMessage());
            }
            catch (Exception ex)
            {
                System.err.println(ex.getMessage());
            }
        }
    }

    private void SendToClient(String data, TCPClientConnection sender)
    {
        try
        {
            sender.SendData(data + fMCChar);
        }
        catch (ClientIsDisconnectedException ex)
        {
            // Client disconnected.
            System.err.println(ex.getMessage());
        }
        catch (OutgoingPacketFailedException ex)
        {
            // Failed to send packet.
            System.err.println(ex.getMessage());
        }
        catch (Exception ex)
        {
        }
    }

    private String GetConnectedClientsList()
    {
        String list = "";
        for (Enumeration e = fConnectedClients.keys(); e.hasMoreElements();)
        {
            list = list + ((TCPChatUser) fConnectedClients.get(e.nextElement())).getUsername() + String.valueOf(((char) 1));
        }
        return "User_List" + fSPT + list;
    }

    @Override
    public void OnClientConnect(TCPClientConnection client)
    {
        System.out.println("New client starting connection");
        fMainForm.UpdateClientsCount();
    }

    @Override
    public void OnClientDisconnect(TCPClientConnection client)
    {
        if (fConnectedClients.containsKey(client.getIPAddress()))
        {
            SendToAllClients("Remove_User" + fSPT + ((TCPChatUser) fConnectedClients.get(client.getIPAddress())).getUsername());
            fConnectedClients.remove(client.getIPAddress());
        }
        fMainForm.UpdateClientsCount();
    }
}
