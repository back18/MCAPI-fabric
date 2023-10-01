package com.quan.mcapi;

import org.jetbrains.annotations.Nullable;

public interface IRunnable
{
    @Nullable
    public Thread getThread();

    public boolean getIsRunning();

    public boolean start();

    public void stop();

    public void waitForStop();
}
