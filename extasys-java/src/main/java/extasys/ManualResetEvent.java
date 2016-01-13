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

/**
 *
 * @author Nikos Siatras
 */
public class ManualResetEvent
{

    private boolean fInitialState;

    public ManualResetEvent(boolean initialState)
    {
        fInitialState = initialState;
    }

    public synchronized void Reset()
    {
        fInitialState = false;
    }

    public synchronized void WaitOne()
    {
        while (!fInitialState)
        {
            try
            {
                wait();
            }
            catch (InterruptedException ex)
            {
            }
        }
    }

    public synchronized void WaitOne(long milliseconds)
    {
        try
        {
            if (!fInitialState)
            {
                wait(milliseconds);
            }
        }
        catch (InterruptedException ex)
        {
        }
    }

    public synchronized void Set()
    {
        fInitialState = true;
        notify();
    }
}
