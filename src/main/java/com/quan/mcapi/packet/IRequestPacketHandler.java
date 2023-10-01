package com.quan.mcapi.packet;

import com.quan.mcapi.McapiClient;
import net.minecraft.server.MinecraftServer;

public interface IRequestPacketHandler
{
    public ResponsePacket HandleRequestPacket(MinecraftServer server, McapiClient mcapiClient, RequestPacket requestPacket);
}
