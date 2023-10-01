package com.quan.mcapi;

public abstract class UnmanagedRunnable extends RunnableBase implements AutoCloseable
{
    protected UnmanagedRunnable()
    {
        isClosed =false;
    }

    protected boolean isClosed;

    public boolean getIsClosed()
    {
        return isClosed;
    }

    @Override
    public boolean start()
    {
        if (isClosed)
            return false;

        return super.start();
    }

    @Override
    public void stop()
    {
        close();
        super.stop();
    }

    protected abstract void closeUnmanaged();

    @Override
    public synchronized void close()
    {
        if (isClosed)
            return;

        closeUnmanaged();
        isClosed =true;
    }
}
