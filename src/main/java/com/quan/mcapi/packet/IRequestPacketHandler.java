package com.quan.mcapi.packet;

import com.quan.mcapi.MinecraftApiClient;
import net.minecraft.server.MinecraftServer;

public interface IRequestPacketHandler
{
    public ResponsePacket HandleRequestPacket(MinecraftServer server, MinecraftApiClient apiClient, RequestPacket requestPacket);
}
