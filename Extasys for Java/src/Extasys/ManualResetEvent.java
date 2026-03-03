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
package Extasys;

/**
 *
 * @author Nikos Siatras
 *
 * ManualResetEvent mimics the behavior of the .NET ManualResetEvent class. It
 * allows threads to communicate by signaling each other.
 *
 * - Call Set() to signal the event (open the gate) - all waiting threads are
 * released.
 *
 * - Call Reset() to reset the event (close the gate) - threads will block on
 * WaitOne().
 *
 * - Call WaitOne() to block the current thread until the event is set.
 *
 * - Call WaitOne(milliseconds) to block the current thread until the event is
 * set or the timeout expires.
 */
public class ManualResetEvent
{

    private final Object fLock = new Object();
    private volatile boolean fIsOpen;

    public ManualResetEvent(boolean initialState)
    {
        fIsOpen = initialState;
    }

    public void Reset()
    {
        fIsOpen = false;
    }

    /**
     * Blocks the current thread until the event is set (fIsOpen = true). Mimics
     * the behavior of .NET ManualResetEvent.WaitOne(). This method ignores
     * interrupts and keeps waiting until Set() is called.
     */
    public void WaitOne()
    {
        synchronized (fLock)
        {
            while (!fIsOpen)
            {
                try
                {
                    fLock.wait();
                }
                catch (InterruptedException ex)
                {
                    // Intentionally ignored - mimic .NET ManualResetEvent behavior.
                    // WaitOne() must keep waiting until Set() is called.
                }
            }
        }
    }

    /**
     * Blocks the current thread until the event is set or the timeout expires.
     * Mimics the behavior of .NET ManualResetEvent.WaitOne(milliseconds).
     * Handles spurious wakeups by tracking remaining time.
     *
     * @param milliseconds maximum time to wait in milliseconds
     * @return true if the event was set, false if the timeout expired
     * @throws java.lang.InterruptedException
     */
    public boolean WaitOne(long milliseconds) throws InterruptedException
    {
        synchronized (fLock)
        {
            if (fIsOpen)
            {
                return true;
            }

            // Track remaining time to handle spurious wakeups correctly
            long deadline = System.currentTimeMillis() + milliseconds;
            long remaining = milliseconds;

            while (!fIsOpen && remaining > 0)
            {
                fLock.wait(remaining);
                remaining = deadline - System.currentTimeMillis();
            }

            return fIsOpen;
        }
    }

    /**
     * Sets the event to the signaled state (open the gate). All threads waiting
     * on WaitOne() will be released.
     */
    public void Set()
    {
        synchronized (fLock)
        {
            fIsOpen = true;
            fLock.notifyAll();
        }
    }

    /**
     * Returns the current state of the event.
     *
     * @return true if the event is set (gate is open), false otherwise
     */
    public boolean getState()
    {
        return fIsOpen;
    }

}
