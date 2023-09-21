package com.quan.mcapi.event;

import com.quan.mcapi.MCAPI;
import com.quan.mcapi.MinecraftApiListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerStartedHandler implements ServerLifecycleEvents.ServerStarted
{
    @Override
    public void onServerStarted(MinecraftServer server)
    {
        MCAPI.listener = MinecraftApiListener.create(server, 25595, "123456");
        MCAPI.listener.start();
    }
}
