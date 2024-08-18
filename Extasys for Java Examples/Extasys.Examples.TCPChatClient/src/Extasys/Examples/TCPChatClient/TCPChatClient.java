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

    private InetAddress fServerIP;
    private int fPort;
    private final String fUsername;
    private final frmTCPChatClient fMainForm;
    private String fSPT = String.valueOf(((char) 2)); // Message splitter character.

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
        String[] splittedMessage = new String(data.getBytes()).split(fSPT);

        switch (splittedMessage[0])
        {
            case "Change_Username":
                // Message: Change_Username ((char)2)
                // This user must change username because this one is allready in use by an other user.
                fMainForm.MarkAsDisconnected();
                fMainForm.DisplayMessage("Please change your username. This one is allready in use by an other user.");
                break;

            case "Welcome":
                /* Message: Welcome ((char)2)
                Server welcomes you.
                 */
                fMainForm.MarkAsConnected();
                fMainForm.DisplayMessage("You are now connected !!!");
                SendDataToServer("Get_List" + fSPT);
                break;

            case "User_List":
                // Message: User_List ((char)2) list...
                // Server sends a list with connected clients to the client.
                if (!splittedMessage[1].equals(""))
                {
                    String[] connectedUsers = splittedMessage[1].split(String.valueOf(((char) 1)));
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
                // Message: New_User ((char)2) username
                // A user connected to the server.
                fMainForm.AddUserInList(splittedMessage[1]);
                fMainForm.DisplayMessage("User " + splittedMessage[1] + " connected");
                break;

            case "Remove_User":
                // Message: Remove_User ((char)2) username
                // A user has been disconnected from server.
                fMainForm.RemoveUser(splittedMessage[1]);
                fMainForm.DisplayMessage("User " + splittedMessage[1] + " disconnected");
                break;

            case "Message":
                // Message: Message ((char)2) some_text
                // Server sends a message
                fMainForm.DisplayMessage(splittedMessage[1]);
                break;

            case "Ping":
                // Message: Ping ((char)2)
                // Server pings you.
                SendDataToServer("Pong" + fSPT);
                break;
            default:
                break;
        }
    }

    @Override
    public void OnConnect(TCPConnector connector)
    {
        SendDataToServer("Login" + fSPT + fUsername);
    }

    @Override
    public void OnDisconnect(TCPConnector connector)
    {
        fMainForm.MarkAsDisconnected();
        fMainForm.DisplayMessage("You are disconnected...");
    }

    public void SendDataToServer(String data)
    {
        try
        {
            super.SendData(data);
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
