package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class McapiBase extends UnmanagedRunnable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final MinecraftServer server;
    protected final String password;

    protected McapiBase(MinecraftServer server, String password)
    {
        this.server = server;
        this.password = password;
    }
}
