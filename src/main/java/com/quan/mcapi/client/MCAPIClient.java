package com.quan.mcapi.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;

public class MCAPIClient implements ClientModInitializer
{
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient()
    {
        LogUtils.getLogger().info("客户端初始化");
    }
}
