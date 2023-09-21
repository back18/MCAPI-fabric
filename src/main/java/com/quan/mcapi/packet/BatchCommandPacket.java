package com.quan.mcapi.packet;

import com.quan.mcapi.*;
import net.minecraft.server.MinecraftServer;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;

public class BatchCommandPacket implements IRequestPacketHandler
{
    public static ResponsePacket createResponsePacket(String[] results, int id)
    {
        byte[] data = new ResponseData(results).serialize();
        return new ResponsePacket(StatusCode.OK, PacketType.BSON, data, id);
    }

    public static String[] ParseRequestPacket(RequestPacket requestPacket)
    {
        byte[] data = requestPacket.getData();
        return new RequestData(data).commands;
    }

    @Override
    public ResponsePacket HandleRequestPacket(MinecraftServer server, MinecraftApiClient apiClient, RequestPacket requestPacket)
    {
        MinecraftApiCommandOutput commandOutput = MinecraftApiCommandOutput.DEFAULT_COMMAND_OUTPUT;
        String[] commands = ParseRequestPacket(requestPacket);
        String[] results = new String[commands.length];
        server.submitAndJoin(() ->
        {
            for (int i = 0; i < commands.length; i++)
            {
                commandOutput.clear();
                server.getCommandManager().executeWithPrefix(commandOutput.createServerCommandSource(server), commands[i]);
                results[i] = commandOutput.asString();
            }
        });
        return createResponsePacket(results, requestPacket.getID());
    }

    public static class RequestData
    {
        public final String[] commands;

        public RequestData(byte[] data)
        {
            BsonDocument bsonDocument = BsonDocumentUtil.deserialize(data);
            BsonArray bsonArray = bsonDocument.getArray("Commands");
            commands = new String[bsonArray.size()];
            for (int i = 0; i < bsonArray.size(); i++)
                commands[i] = bsonArray.get(i).asString().getValue();
        }
    }

    public static class ResponseData implements ISerializable
    {
        public final String[] results;

        public ResponseData(String[] results)
        {
            this.results = results;
        }

        @Override
        public byte[] serialize()
        {
            BsonDocument bsonDocument = new BsonDocument();
            BsonArray bsonArray = new BsonArray();
            for (String result : results)
                bsonArray.add(new BsonString(result));

            bsonDocument.put("Results", bsonArray);
            return BsonDocumentUtil.serialize(bsonDocument);
        }
    }
}
