package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.logging.UncaughtExceptionHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class MinecraftApiBase implements Runnable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    protected volatile boolean running;
    @Nullable
    protected Thread thread;
    protected MinecraftServer server;
    protected final String password;
    protected final String description;

    protected MinecraftApiBase(MinecraftServer server, String password, String description)
    {
        this.server = server;
        this.password = password;
        this.description = description;
    }

    public synchronized void start()
    {
        if (this.running)
            return;

        this.running = true;
        this.thread = new Thread(this, this.description + " #" + THREAD_COUNTER.incrementAndGet());
        this.thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOGGER));
        this.thread.start();
        LOGGER.info("Thread {} started", this.description);
    }

    public synchronized void stop()
    {
        this.running = false;
        if (this.thread != null)
        {
            int i = 0;
            while(this.thread.isAlive())
            {
                try
                {
                    this.thread.join(1000L);
                    i++;
                    if (i >= 5)
                    {
                        LOGGER.warn("Waited {} seconds attempting force stop!", i);
                    } else if (this.thread.isAlive())
                    {
                        LOGGER.warn("Thread {} ({}) failed to exit after {} second(s)", this, this.thread.getState(), i, new Exception("Stack:"));
                        this.thread.interrupt();
                    }
                } catch (InterruptedException ignored)
                {
                }
            }

            LOGGER.info("Thread {} stopped", this.description);
            this.thread = null;
        }
    }

    public boolean isRunning()
    {
        return this.running;
    }
}
