package com.quan.mcapi.packet;

import com.quan.mcapi.MinecraftApiClient;
import com.quan.mcapi.MinecraftApiCommandOutput;
import com.quan.mcapi.PacketType;
import com.quan.mcapi.StatusCode;
import net.minecraft.server.MinecraftServer;

import java.nio.charset.StandardCharsets;

public class CommandPacket implements IRequestPacketHandler
{
    public static ResponsePacket createResponsePacket(String result, int id)
    {
        byte[] data = result.getBytes(StandardCharsets.UTF_8);
        return new ResponsePacket(StatusCode.OK, PacketType.String, data, id);
    }

    public static String ParseRequestPacket(RequestPacket requestPacket)
    {
        byte[] data = requestPacket.getData();
        return new String(data, 0, data.length, StandardCharsets.UTF_8);
    }

    @Override
    public ResponsePacket HandleRequestPacket(MinecraftServer server, MinecraftApiClient apiClient, RequestPacket requestPacket)
    {
        String command = ParseRequestPacket(requestPacket);
        MinecraftApiCommandOutput commandOutput = MinecraftApiCommandOutput.DEFAULT_COMMAND_OUTPUT;
        commandOutput.clear();
        server.submitAndJoin(() ->
        {
            server.getCommandManager().executeWithPrefix(commandOutput.createServerCommandSource(server), command);
        });
        return createResponsePacket(commandOutput.asString(), requestPacket.getID());
    }
}
