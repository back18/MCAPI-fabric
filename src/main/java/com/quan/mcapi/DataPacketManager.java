package com.quan.mcapi;

import com.quan.mcapi.packet.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DataPacketManager
{
    private final Map<String, IRequestPacketHandler> handlerMap;

    public DataPacketManager()
    {
        handlerMap = new HashMap<>();
        handlerMap.put(PacketKey.Empty, new EmptyPacket());
        handlerMap.put(PacketKey.Login, new LoginPacket());
        handlerMap.put(PacketKey.Command, new CommandPacket());
        handlerMap.put(PacketKey.BatchCommand, new BatchCommandPacket());
        handlerMap.put(PacketKey.BatchSetBlock, new BatchSetBlockPacket());
    }

    @Nullable
    public IRequestPacketHandler getRequestPacketHandler(String key)
    {
        return handlerMap.get(key);
    }
}
