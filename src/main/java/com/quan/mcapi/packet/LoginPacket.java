package com.quan.mcapi.packet;

import com.quan.mcapi.*;
import net.minecraft.server.MinecraftServer;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.nio.charset.StandardCharsets;

public class LoginPacket implements IRequestPacketHandler
{
    public static ResponsePacket createResponsePacket(boolean isSuccessful, String message, int id)
    {
        byte[] data = new ResponseData(isSuccessful, message).serialize();
        return new ResponsePacket(StatusCode.OK, PacketType.String, data, id);
    }

    public static String ParseRequestPacket(RequestPacket requestPacket)
    {
        byte[] data = requestPacket.getData();
        return new String(data, 0, data.length, StandardCharsets.UTF_8);
    }

    @Override
    public ResponsePacket HandleRequestPacket(MinecraftServer server, McapiClient mcapiClient, RequestPacket requestPacket)
    {
        String password = ParseRequestPacket(requestPacket);
        if (mcapiClient.validatePassword(password))
        {
            return createResponsePacket(true, "Login is successful", requestPacket.getID());
        }
        else
        {
            return createResponsePacket(false, "Login failed, password error", requestPacket.getID());
        }
    }

    public static class ResponseData implements ISerializable
    {
        public final boolean isSuccessful;
        public final String message;

        public ResponseData(boolean isSuccessful, String message)
        {
            this.isSuccessful = isSuccessful;
            this.message = message;
        }

        @Override
        public byte[] serialize()
        {
            BsonDocument bsonDocument = new BsonDocument();
            bsonDocument.put("IsSuccessful", new BsonBoolean(isSuccessful));
            bsonDocument.put("Message", new BsonString(message));
            return BsonDocumentUtil.serialize(bsonDocument);
        }
    }
}
