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
package Extasys.Examples.TCPChatClient;

import Extasys.DataFrame;
import Extasys.DataConvertion.Base64Converter;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.TCP.Client.Exceptions.*;
import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras
 */
public class TCPChatClient extends Extasys.Network.TCP.Client.ExtasysTCPClient
{

    private final InetAddress fServerIP;
    private final int fPort;
    private final String fUsername;
    private final frmTCPChatClient fMainForm;

    public TCPChatClient(InetAddress serverIP, int port, String username, frmTCPChatClient frmMain)
    {
        super("TCP Chat Client", "", 1, 2);
        fServerIP = serverIP;
        fPort = port;
        fUsername = username;
        fMainForm = frmMain;

        TCPConnector connector = super.AddConnector("Main Connector", serverIP, port, 8192, ((char) 3));
        connector.setAutoApplyMessageSplitterState(true); // Auto apply message splitter to outgoing messages
        connector.setConnectionDataConverter(new Base64Converter()); // Base 64 Encoding
    }

    public void Connect()
    {
        try
        {
            super.Start();
        }
        catch (Exception ex)
        {
            fMainForm.MarkAsDisconnected();
        }
    }

    @Override
    public void OnDataReceive(TCPConnector connector, DataFrame data)
    {
        MessageToken token = MessageToken.fromJSON(new String(data.getBytes()));
        final String tokenHeader = token.getHeader();
        final String tokenData = token.getData();

        switch (tokenHeader)
        {
            case "Change_Username":
                // Message: "Change_Username",""
                // This user must change username because this one is allready in use by an other user.
                fMainForm.MarkAsDisconnected();
                fMainForm.DisplayMessage("Please change your username. This one is allready in use by an other user.");
                break;

            case "Welcome":
                // Message: "Welcome",""
                // Server welcomes you.
                fMainForm.MarkAsConnected();
                fMainForm.DisplayMessage("You are now connected !!!");
                MessageToken tokenToSend = new MessageToken("Get_List", "");
                SendDataToServer(tokenToSend);
                break;

            case "User_List":
                // Message: "User_List","A list of Usernames separated with (char)1"
                // Server sends a list with connected clients to the client.
                if (!tokenData.equals(""))
                {
                    String[] connectedUsers = token.getData().split(String.valueOf(((char) 1)));
                    for (int i = 0; i < connectedUsers.length; i++)
                    {
                        if (!connectedUsers[i].equals(fUsername))
                        {
                            fMainForm.AddUserInList(connectedUsers[i]);
                        }
                    }
                }
                break;

            case "New_User":
                // Message: "New_User","username"
                // A user connected to the server.
                fMainForm.AddUserInList(tokenData);
                fMainForm.DisplayMessage("User " + tokenData + " connected");
                break;

            case "Remove_User":
                // Message: "Remove_User","username"
                // A user has been disconnected from server.
                fMainForm.RemoveUser(tokenData);
                fMainForm.DisplayMessage("User " + tokenData + " disconnected");
                break;

            case "Message":
                // Message: "Message","Message text"
                // Server sends a message
                fMainForm.DisplayMessage(tokenData);
                break;

            case "Ping":
                // Message: "Ping",""
                // Server pings you, reply with Pong
                MessageToken pongToken = new MessageToken("Pong", "");
                SendDataToServer(pongToken);
                break;

            default:
                break;
        }
    }

    @Override
    public void OnConnect(TCPConnector connector)
    {
        MessageToken tokenToSend = new MessageToken("Login", fUsername);
        SendDataToServer(tokenToSend);
    }

    @Override
    public void OnDisconnect(TCPConnector connector)
    {
        fMainForm.MarkAsDisconnected();
        fMainForm.DisplayMessage("You are disconnected...");
    }

    public void SendDataToServer(MessageToken token)
    {
        try
        {
            super.SendData(token.toJSON());
        }
        catch (ConnectorDisconnectedException ex)
        {
            System.err.println(ex.getMessage());
        }
        catch (ConnectorCannotSendPacketException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
}
