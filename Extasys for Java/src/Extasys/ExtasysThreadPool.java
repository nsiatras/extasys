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

import Extasys.Network.NetworkPacket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Nikos Siatras
 *
 */
public class ExtasysThreadPool extends ThreadPoolExecutor
{

    private final LinkedBlockingQueue<Runnable> fPoolsQueue;

    public ExtasysThreadPool(int corePoolWorkers, int maximumPoolWorkers)
    {
        // LinkedBlockingQueue has better performance than ArrayBlockingQueue
        this(corePoolWorkers, maximumPoolWorkers, new LinkedBlockingQueue<>(50000));
    }

    private ExtasysThreadPool(int corePoolWorkers, int maximumPoolWorkers, LinkedBlockingQueue<Runnable> queue)
    {
        super(corePoolWorkers, maximumPoolWorkers, 10, TimeUnit.SECONDS, queue);
        fPoolsQueue = queue;
        this.prestartAllCoreThreads();
    }

    /**
     * Enqueue a NetworkPacket into the thread pool. If the queue is full, wait
     * up to 5 seconds for space to become available. If still full after
     * timeout, reject the packet by throwing RejectedExecutionException.
     *
     * @param packet the NetworkPacket to enqueue
     */
    public void EnqueNetworkPacket(NetworkPacket packet)
    {
        try
        {
            // offer() blocks until space is available or timeout is reached
            // This prevents RejectedExecutionException under burst traffic
            boolean accepted = fPoolsQueue.offer(packet, 5, TimeUnit.SECONDS);

            if (!accepted)
            {
                // Queue is still full after timeout - reject the packet
                throw new RejectedExecutionException("Thread pool queue is full");
            }
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

}
