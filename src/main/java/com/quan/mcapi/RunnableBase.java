package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.Semaphore;

public abstract class RunnableBase implements IRunnable
{
    protected RunnableBase()
    {
        stopSemaphore = new Semaphore(0);
        threadName = getClass().getTypeName();
        isRunning = false;
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    protected Semaphore stopSemaphore;
    @Nullable
    protected Thread thread;
    protected String threadName;
    protected boolean isRunning;

    @Override
    @Nullable
    public Thread getThread()
    {
        return thread;
    }

    public String getThreadName()
    {
        return threadName;
    }

    @Override
    public boolean getIsRunning()
    {
        return isRunning;
    }

    protected abstract void run();

    public boolean start(String threadName)
    {
        this.threadName = threadName;
        return start();
    }

    @Override
    public synchronized boolean start()
    {
        if (isRunning)
            return false;

        isRunning = true;
        thread = new Thread(this::threadStart, threadName);
        thread.start();
        return true;
    }

    @Override
    public synchronized void stop()
    {
        if (isRunning)
        {
            isRunning = false;
            int i = 0;
            try
            {
                while (thread != null)
                {
                    thread.join(1000);
                    if (!thread.isAlive())
                        break;
                    i++;
                    LOGGER.warn("Waiting for thread ({}) to stop, waiting for {} second", thread.getName(), i);
                    if (i > 5)
                    {
                        LOGGER.warn("About to forcibly stop thread ({})", thread.getName());
                        while (stopSemaphore.getQueueLength() > 0)
                            stopSemaphore.release();
                        thread.interrupt();
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                if (thread != null && thread.isAlive())
                    LOGGER.error("Unable to stop thread ({})", thread.getName());
            }
        }
    }

    @Override
    public void waitForStop()
    {
        try
        {
            stopSemaphore.acquire();
        }
        catch (InterruptedException ignored)
        {

        }
    }

    protected void threadStart()
    {
        try
        {
            LOGGER.info("Thread ({}) started", thread == null ? "null" : thread.getName());
            run();
        }
        catch (Exception ex)
        {
            LOGGER.error("Thread ({}) threw an exception", thread == null ? "null" : thread.getName(), ex);
        }
        finally
        {
            isRunning = false;
            while (stopSemaphore.getQueueLength() > 0)
                stopSemaphore.release();
            LOGGER.info("Thread ({}) stopped", thread == null ? "null" : thread.getName());
        }
    }
}
