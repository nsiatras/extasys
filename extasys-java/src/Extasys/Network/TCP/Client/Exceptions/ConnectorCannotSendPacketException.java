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
package Extasys.Network.TCP.Client.Exceptions;

import Extasys.Network.TCP.Client.Connectors.Packets.OutgoingTCPClientPacket;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;

/**
 *
 * @author Nikos Siatras
 */
public final class ConnectorCannotSendPacketException extends Exception
{

    private final TCPConnector fConnector;
    private final OutgoingTCPClientPacket fPacket;

    public ConnectorCannotSendPacketException(TCPConnector connector, OutgoingTCPClientPacket packet)
    {
        super("Connector is disconnected");

        fConnector = connector;
        fPacket = packet;
    }

    /**
     * Returns the connector was unable to send the packet.
     *
     * @return the connector was unable to send the packet.
     */
    public TCPConnector getConnectorInstance()
    {
        return fConnector;
    }

    /**
     * Returns the unsent packet.
     *
     * @return the unsent packet.
     */
    public OutgoingTCPClientPacket getOutgoingPacket()
    {
        return fPacket;
    }

}
