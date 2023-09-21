package com.quan.mcapi.utility;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftUtil
{
    private static final BlockStateArgumentType PARSER = new BlockStateArgumentType(CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup()));
    private static final Map<String, BlockState> BLOCK_STATE_MAP = new HashMap<>();

    public static BlockState getBlockState(String blockID) throws CommandSyntaxException
    {
        BlockState state = BLOCK_STATE_MAP.get(blockID);
        if (state == null)
        {
            state = PARSER.parse(new StringReader(blockID)).getBlockState();
            BLOCK_STATE_MAP.put(blockID, state);
        }
        return state;
    }

    public static int batchSetBlock(ServerWorld world, List<SetBlockArgument> arguments)
    {
        int count = 0;
        for (SetBlockArgument argument : arguments)
        {
            BlockState state;
            try
            {
                state = getBlockState(argument.blockID());
            }
            catch (CommandSyntaxException commandSyntaxException)
            {
                continue;
            }
            WorldChunk chunk = world.getWorldChunk(argument.blockPos());
            BlockState newState = chunk.setBlockState(argument.blockPos(), state, false);
            if (newState != null && newState != state && chunk.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING))
                world.updateListeners(argument.blockPos(), state, newState, 0);
            count++;
        }
        return count;
    }
}
