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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Nikos Siatras
 *
 */
public class ExtasysThreadPool extends ThreadPoolExecutor
{

    private final BlockingQueue fPoolsQueue;

    public ExtasysThreadPool(int corePoolWorkers, int maximumPoolWorkers)
    {
        //super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue(250000, true));

        // It appears that LinkedBlockingQueue has better performance than the ArrayBlockingQueue
        super(corePoolWorkers, maximumPoolWorkers, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(25000));
        fPoolsQueue = this.getQueue();
        this.prestartAllCoreThreads();
    }

    public void EnqueNetworkPacket(NetworkPacket packet)
    {
        try
        {
            synchronized (fPoolsQueue)
            {
                while (fPoolsQueue.remainingCapacity() < 500)
                {
                    fPoolsQueue.wait(); // Wait until space is freed up in the queue
                }

                super.execute(packet);
            }
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

    }

}
