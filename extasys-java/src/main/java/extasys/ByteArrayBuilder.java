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
public class ByteArrayBuilder
{

    private byte[] fBytes;
    private final Object fLock;

    public ByteArrayBuilder()
    {
        fLock = new Object();
        fBytes = new byte[0];
    }

    /**
     * Append data to ByteArrayBuilder
     *
     * @param data is the byte[] array to append
     */
    public void Append(byte[] data)
    {
        synchronized (fLock)
        {
            final byte[] newArray = new byte[fBytes.length + data.length];
            System.arraycopy(fBytes, 0, newArray, 0, fBytes.length);
            System.arraycopy(data, 0, newArray, fBytes.length, data.length);
            fBytes = newArray;
        }
    }

    public int IndexOf(byte[] subArray)
    {
        synchronized (fLock)
        {
            int i = 0;
            int subArrayLength = subArray.length;

            // Find subArray in fBytes
            for (i = 0; i < fBytes.length; i++)
            {
                byte[] arrayToCompare = Arrays.copyOfRange(fBytes, i, i + subArrayLength);

                if (Arrays.equals(arrayToCompare, subArray))
                {
                    return i;
                }
            }

            return -1;
        }
    }

    public void Delete(int indexFrom, int indexTo)
    {
        synchronized (fLock)
        {
            final byte[] firstPart = Arrays.copyOfRange(fBytes, 0, indexFrom);
            final byte[] secondPart = Arrays.copyOfRange(fBytes, indexTo, fBytes.length);

            final byte[] C = new byte[firstPart.length + secondPart.length];
            System.arraycopy(firstPart, 0, C, 0, firstPart.length);
            System.arraycopy(secondPart, 0, C, firstPart.length, secondPart.length);

            fBytes = C;
        }
    }

    public byte[] SubList(int fromIndex, int toIndex)
    {
        synchronized (fLock)
        {
            return Arrays.copyOfRange(fBytes, fromIndex, toIndex);
        }
    }

    public void Dispose()
    {
        synchronized (fLock)
        {
            this.fBytes = new byte[0];
        }
    }
}
