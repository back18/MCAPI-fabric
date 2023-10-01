package com.quan.mcapi.event;

import com.quan.mcapi.MCAPI;
import com.quan.mcapi.McapiListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerStartedHandler implements ServerLifecycleEvents.ServerStarted
{
    @Override
    public void onServerStarted(MinecraftServer server)
    {
        MCAPI.listener = McapiListener.create(server, 25585, "123456");
        if (MCAPI.listener != null)
            MCAPI.listener.start("McapiListener Thread");
    }
}
