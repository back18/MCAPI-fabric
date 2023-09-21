package com.quan.mcapi.packet;

import com.quan.mcapi.*;
import com.quan.mcapi.utility.MinecraftUtil;
import com.quan.mcapi.utility.SetBlockArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;

import java.util.ArrayList;
import java.util.List;

public class BatchSetBlockPacket implements IRequestPacketHandler
{
    public static ResponsePacket createResponsePacket(int totalCount, int completedCount, int id)
    {
        byte[] data = new ResponseData(totalCount, completedCount).serialize();
        return new ResponsePacket(StatusCode.OK, PacketType.BSON, data, id);
    }

    public static List<SetBlockArgument> parseRequestPacket(RequestPacket requestPacket)
    {
        byte[] data = requestPacket.getData();
        return new RequestData(data).toSetBlockArguments();
    }

    @Override
    public ResponsePacket HandleRequestPacket(MinecraftServer server, MinecraftApiClient apiClient, RequestPacket requestPacket)
    {
        List<SetBlockArgument> arguments = parseRequestPacket(requestPacket);
        ServerWorld world = server.getOverworld();
        int count = MinecraftUtil.batchSetBlock(world, arguments);
        return createResponsePacket(arguments.size(), count, requestPacket.getID());
    }

    public static class RequestData
    {
        public final String[] palette;
        public final int[] data;

        public RequestData(byte[] data)
        {
            BsonDocument bsonDocument = BsonDocumentUtil.deserialize(data);
            BsonArray paletteArray = bsonDocument.getArray("Palette");
            BsonArray dataArray = bsonDocument.getArray("Data");
            palette = new String[paletteArray.size()];
            this.data = new int[dataArray.size()];
            for (int i = 0; i < paletteArray.size(); i++)
                palette[i] = paletteArray.get(i).asString().getValue();
            for (int i = 0; i < dataArray.size(); i++)
                this.data[i] = dataArray.get(i).asInt32().getValue();
        }

        public List<SetBlockArgument> toSetBlockArguments()
        {
            List<SetBlockArgument> result = new ArrayList<>();
            for (int i = 0; i + 3 < data.length; i += 4)
                result.add(new SetBlockArgument(new BlockPos(data[i], data[i + 1], data[i + 2]), palette[data[i + 3]]));
            return result;
        }
    }

    public static class ResponseData implements ISerializable
    {
        public final int totalCount;
        public final int completedCount;

        public ResponseData(int totalCount, int completedCount)
        {
            this.totalCount = totalCount;
            this.completedCount = completedCount;
        }

        @Override
        public byte[] serialize()
        {
            BsonDocument bsonDocument = new BsonDocument();
            bsonDocument.put("TotalCount", new BsonInt32(totalCount));
            bsonDocument.put("CompletedCount", new BsonInt32(completedCount));
            return BsonDocumentUtil.serialize(bsonDocument);
        }
    }
}
