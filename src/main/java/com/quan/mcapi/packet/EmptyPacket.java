package com.quan.mcapi.packet;

import com.quan.mcapi.McapiClient;
import com.quan.mcapi.PacketType;
import com.quan.mcapi.StatusCode;
import net.minecraft.server.MinecraftServer;

public class EmptyPacket implements IRequestPacketHandler
{
    public static ResponsePacket createResponsePacket(int id)
    {
        return new ResponsePacket(StatusCode.OK, PacketType.Empty, new byte[0], id);
    }

    @Override
    public ResponsePacket HandleRequestPacket(MinecraftServer server, McapiClient mcapiClient, RequestPacket requestPacket)
    {
        return createResponsePacket(requestPacket.getID());
    }
}
