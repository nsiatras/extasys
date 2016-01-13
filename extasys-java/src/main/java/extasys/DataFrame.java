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
package extasys;

import java.util.Arrays;

/**
 *
 * @author Nikos Siatras
 */
public final class DataFrame
{

    private final byte[] fBytes;
    private final int fLength;

    public DataFrame(byte[] bytes)
    {
        fBytes = bytes;
        fLength = bytes.length;
    }

    public DataFrame(byte[] bytes, int offset, int length)
    {
        fBytes = Arrays.copyOfRange(bytes, offset, length);
        fLength = fBytes.length;
    }

    public final byte[] getBytes()
    {
        return fBytes;
    }

    public final int getLength()
    {
        return fLength;
    }
}
