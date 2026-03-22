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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
 *
 * - Call WaitOneWithoutException(milliseconds) to block the current thread
 * until the event is set or the timeout expires, without throwing an exception.
 */
public class ManualResetEvent
{

    private final ReentrantLock fLock = new ReentrantLock();
    private final Condition fCondition = fLock.newCondition();
    private volatile boolean fIsOpen;

    public ManualResetEvent(boolean initialState)
    {
        fIsOpen = initialState;
    }

    /**
     * Resets the event to the non-signaled state (close the gate). Threads
     * calling WaitOne() will block until Set() is called.
     */
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
        // Fast path: if already open, no need to acquire the lock
        if (fIsOpen)
        {
            return;
        }

        fLock.lock();
        try
        {
            while (!fIsOpen)
            {
                try
                {
                    fCondition.await();
                }
                catch (InterruptedException ex)
                {
                    // Intentionally ignored - mimic .NET ManualResetEvent behavior.
                    // WaitOne() must keep waiting until Set() is called.
                }
            }
        }
        finally
        {
            fLock.unlock();
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
        // Fast path: if already open, no need to acquire the lock
        if (fIsOpen)
        {
            return true;
        }

        fLock.lock();
        try
        {
            // Track remaining time to handle spurious wakeups correctly
            long remaining = milliseconds;
            long deadline = System.currentTimeMillis() + milliseconds;

            while (!fIsOpen && remaining > 0)
            {
                fCondition.await(remaining, TimeUnit.MILLISECONDS);
                remaining = deadline - System.currentTimeMillis();
            }

            return fIsOpen;
        }
        finally
        {
            fLock.unlock();
        }
    }

    /**
     * Blocks the current thread until the event is set or the timeout expires.
     * Same behavior as WaitOne(milliseconds) but does not throw
     * InterruptedException. If the thread is interrupted, the interrupt flag is
     * restored and the method returns false.
     *
     * @param milliseconds maximum time to wait in milliseconds
     * @return true if the event was set, false if the timeout expired or the
     * thread was interrupted
     */
    public boolean WaitOneWithoutException(long milliseconds)
    {
        try
        {
            return WaitOne(milliseconds);
        }
        catch (InterruptedException ex)
        {
            // Restore the interrupt flag so the caller can detect the interruption if needed
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Sets the event to the signaled state (open the gate). All threads waiting
     * on WaitOne() will be released.
     */
    public void Set()
    {
        fLock.lock();
        try
        {
            fIsOpen = true;
            fCondition.signalAll();
        }
        finally
        {
            fLock.unlock();
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
