package com.quan.mcapi.event;

import com.quan.mcapi.MCAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerStoppedHandler implements ServerLifecycleEvents.ServerStopped
{
    @Override
    public void onServerStopped(MinecraftServer server)
    {
        MCAPI.listener.stop();
    }
}
