package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import com.quan.mcapi.event.ServerStartedHandler;
import com.quan.mcapi.event.ServerStoppedHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MCAPI implements ModInitializer
{
    public static MinecraftApiListener listener;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize()
    {
        LogUtils.getLogger().info("服务端初始化");
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerStartedHandler());
        ServerLifecycleEvents.SERVER_STOPPED.register(new ServerStoppedHandler());
    }
}
