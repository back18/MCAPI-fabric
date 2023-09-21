package com.quan.mcapi;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class MinecraftApiCommandOutput implements CommandOutput
{
    public static final MinecraftApiCommandOutput DEFAULT_COMMAND_OUTPUT = new MinecraftApiCommandOutput();
    private static final String NAME = "MCAPI";
    private static final Text NAME_TEXT = Text.literal("MCAPI");
    private final StringBuffer buffer = new StringBuffer();

    public void clear()
    {
        buffer.setLength(0);
    }

    public String asString()
    {
        return buffer.toString();
    }

    public ServerCommandSource createServerCommandSource(MinecraftServer server)
    {
        ServerWorld serverWorld = server.getOverworld();
        return new ServerCommandSource(this, Vec3d.of(serverWorld.getSpawnPos()), Vec2f.ZERO, serverWorld, 4, NAME, NAME_TEXT, server, null);
    }

    public void sendMessage(Text message)
    {
        buffer.append(message.getString());
    }

    public boolean shouldReceiveFeedback()
    {
        return true;
    }

    public boolean shouldTrackOutput()
    {
        return true;
    }

    public boolean shouldBroadcastConsoleToOps()
    {
        return false;
    }
}
