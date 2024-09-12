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
import Extasys.DataConvertion.Base64Converter;
import Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException;
import Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException;
import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import Extasys.Network.TCP.Server.Listener.TCPListener;
import java.net.InetAddress;
import java.util.HashMap;

/**
 *
 * @author Nikos Siatras
 */
public class TCPChatServer extends Extasys.Network.TCP.Server.ExtasysTCPServer
{

    private final HashMap<String, TCPChatUser> fConnectedClients;

    private Thread fPingThread;
    private boolean fServerIsActive;
    private final frmTCPChatServer fMainForm;

    public TCPChatServer(InetAddress listenerIP, int port, frmTCPChatServer frmMain)
    {
        super("TCP Chat Server", "This is a simple chat server", 10, 20);

        TCPListener listener = super.AddListener("Main Listener", listenerIP, port, 9999, 8192, 10000, 100, ((char) 3));
        listener.setAutoApplyMessageSplitterState(true); // Auto apply message splitter to outgoing messages
        listener.setConnectionDataConverter(new Base64Converter()); // Base 64 Encoding

        fConnectedClients = new HashMap<>();
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
                            // Send "Ping" command to all Client
                            MessageToken token = new MessageToken("Ping", "");
                            ReplyToAll(token.toJSON());
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
            // This isa the client's incoming message
            //String[] splittedMessage = new String(data.getBytes()).split(fSPT);
            MessageToken token = MessageToken.fromJSON(new String(data.getBytes()));
            final String tokenHeader = token.getHeader();
            final String tokenData = token.getData();

            switch (tokenHeader)
            {
                case "Login":
                    // Message: "Login","Username"
                    // Client wants to login.
                    // Server checks if username is unique.
                    // If the username is taken server replys -> 'Change_Username,""'
                    // If the username is not taken then server replys -> 'Welcome,""' to the new client
                    // and sends "New_User ((char)2) NewUsername" to all other clients.
                    String tmpUsername = tokenData;
                    if (IsUsernameTaken(tmpUsername))
                    {
                        MessageToken tokenToSend = new MessageToken("Change_Username", tmpUsername);
                        sender.SendData(tokenToSend.toJSON());
                    }
                    else
                    {
                        TCPChatUser user = new TCPChatUser(tmpUsername, sender);
                        fConnectedClients.put(sender.getIPAddress(), user);

                        // Inform all users that a new user is now connected to the chat.
                        MessageToken tokenToSend = new MessageToken("New_User", tmpUsername);
                        super.ReplyToAll(tokenToSend.toJSON());

                        // Send the welcome message to the user just connected.
                        tokenToSend = new MessageToken("Welcome", "");
                        sender.SendData(tokenToSend.toJSON());
                    }
                    break;

                case "Message":
                    // Message: Message ((char)2) some_text
                    // Client sends a chat message to the server.
                    // Server checks if the client is registered to the server.
                    // If the client is registered to the server
                    // the server sends this message to all the other clients "Message ((char)2) Sender's username : some_text" else
                    // it disconnects the client.
                    if (fConnectedClients.containsKey(sender.getIPAddress()))
                    {
                        final String clientName = GetClientName(sender);
                        MessageToken tokenToSend = new MessageToken("Message", (clientName + ": " + tokenData));

                        super.ReplyToAll(tokenToSend.toJSON());
                    }
                    else
                    {
                        sender.DisconnectMe();
                    }
                    break;

                case "Get_List":
                    // Message: "Get_List",""
                    // Client requets a list with other connected clients.
                    // If the client is registered to the server the server replys the list
                    // else it disconnects the client.
                    if (fConnectedClients.containsKey(sender.getIPAddress()))
                    {
                        sender.SendData(GetConnectedClientsList());
                    }
                    else
                    {
                        sender.DisconnectMe();
                    }
                    break;

                case "Pong":
                    // Message: Pong ((char)2)
                    // A Client responded to Ping. 
                    // Do nothing....
                    System.out.println(GetClientName(sender) + " PONG!");
                    break;

                default:
                    System.out.println(sender.getIPAddress() + " sends wrong message");
                    break;
            }
        }
        catch (ClientIsDisconnectedException | OutgoingPacketFailedException ex)
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
        for (TCPChatUser user : fConnectedClients.values())
        {
            if (user.getUsername().equals(username))
            {
                return true;
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

    private String GetConnectedClientsList()
    {
        String list = "";
        for (TCPChatUser user : fConnectedClients.values())
        {
            list = list + user.getUsername() + String.valueOf(((char) 1));
        }

        MessageToken token = new MessageToken("User_List", list);
        return token.toJSON();
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
            final String username = GetClientName(client);

            MessageToken token = new MessageToken("Remove_User", username);
            super.ReplyToAll(token.toJSON());

            fConnectedClients.remove(client.getIPAddress());
        }
        fMainForm.UpdateClientsCount();
    }
}
