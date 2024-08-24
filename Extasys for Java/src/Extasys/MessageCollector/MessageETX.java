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

/**
 *
 * @author Nikos Siatras - https://github.com/nsiatras
 *
 * This class holds the properties of the message splitter for TCP Connections.
 * Early Extasys versions holds the ETX as a string. This class was implemented
 * to keep message splitter char(characters) as a byte array.
 */
public class MessageETX
{

    private final byte[] fBytes;

    public MessageETX(String messageETX)
    {
        fBytes = messageETX.getBytes();
    }

    public MessageETX(byte[] bytes)
    {
        fBytes = bytes;
    }

    public byte[] getBytes()
    {
        return fBytes;
    }

    public int getLength()
    {
        return fBytes.length;
    }
}
