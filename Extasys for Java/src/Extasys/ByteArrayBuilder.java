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
package Extasys;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;

/**
 *
 * @author Nikos Siatras
 */
public class ByteArrayBuilder
{

    // Internal buffer with extra capacity to avoid reallocations on every Append
    private final int fDefaultCapacity;
    private byte[] fBytes;
    private int fLength;

    private final Object fLock;

    public ByteArrayBuilder(int defaultCapacity)
    {
        fDefaultCapacity = defaultCapacity;
        fLock = new Object();
        fBytes = new byte[fDefaultCapacity];
        fLength = 0;
    }

    /**
     * Append data to ByteArrayBuilder. Uses a capacity-doubling strategy to
     * minimize array reallocations.
     *
     * @param data is the byte[] array to append
     */
    public void Append(byte[] data)
    {
        synchronized (fLock)
        {
            int requiredLength = fLength + data.length;

            // Grow the internal buffer if needed (doubling strategy)
            if (requiredLength > fBytes.length)
            {
                int newCapacity = Math.max(fBytes.length * 2, requiredLength);
                fBytes = Arrays.copyOf(fBytes, newCapacity);
            }

            System.arraycopy(data, 0, fBytes, fLength, data.length);
            fLength += data.length;
        }
    }

    /**
     * Find the first occurrence of subArray in the internal buffer. Uses
     * MemorySegment for zero-copy, SIMD-accelerated comparison.
     *
     * @param subArray is the byte[] array to search for
     * @return the index of the first occurrence, or -1 if not found
     */
    public int IndexOf(byte[] subArray)
    {
        synchronized (fLock)
        {
            int subArrayLength = subArray.length;

            // If subArray is empty or larger than the data, no match is possible
            if (subArrayLength == 0 || subArrayLength > fLength)
            {
                return -1;
            }

            // Wrap both arrays in MemorySegments (zero-copy, no heap allocation)
            // MemorySegment.ofArray gives access to JVM SIMD intrinsics via mismatch()
            MemorySegment haystack = MemorySegment.ofArray(fBytes).asSlice(0, fLength);
            MemorySegment needle = MemorySegment.ofArray(subArray);

            int limit = fLength - subArrayLength;

            for (int i = 0; i <= limit; i++)
            {
                // Quick check on first byte before full comparison
                if (haystack.get(ValueLayout.JAVA_BYTE, i) != subArray[0])
                {
                    continue;
                }

                // Compare a slice of haystack against the needle (no array allocation)
                // mismatch() returns -1 if the two segments are equal
                if (haystack.asSlice(i, subArrayLength).mismatch(needle) == -1)
                {
                    return i;
                }
            }

            return -1;
        }
    }

    /**
     * Delete bytes from the internal buffer in the range [indexFrom, indexTo).
     *
     * @param indexFrom start index (inclusive)
     * @param indexTo end index (exclusive)
     */
    public void Delete(int indexFrom, int indexTo)
    {
        synchronized (fLock)
        {
            int deleteCount = indexTo - indexFrom;

            // Shift remaining bytes left, avoiding intermediate array allocation
            System.arraycopy(fBytes, indexTo, fBytes, indexFrom, fLength - indexTo);
            fLength -= deleteCount;
        }
    }

    /**
     * Returns a copy of the bytes in the range [fromIndex, toIndex).
     *
     * @param fromIndex start index (inclusive)
     * @param toIndex end index (exclusive)
     * @return a new byte[] with the requested bytes
     */
    public byte[] SubList(int fromIndex, int toIndex)
    {
        synchronized (fLock)
        {
            return Arrays.copyOfRange(fBytes, fromIndex, toIndex);
        }
    }

    /**
     * Dispose the internal buffer and reset the builder.
     */
    public void Dispose()
    {
        synchronized (fLock)
        {
            fBytes = new byte[fDefaultCapacity];
            fLength = 0;
        }
    }

    /**
     * Returns the number of bytes currently stored.
     *
     * @return the number of bytes currently stored
     */
    public int getLength()
    {
        synchronized (fLock)
        {
            return fLength;
        }
    }
}
