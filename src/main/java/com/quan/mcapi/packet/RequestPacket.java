package com.quan.mcapi.packet;

import com.quan.mcapi.BsonDocumentUtil;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;

public class RequestPacket extends DataPacket
{
    private static final String KEY = "Key";
    private static final String NEED_RESPONSE = "NeedResponse";
    private final String key;
    private final boolean needResponse;

    public RequestPacket(String key, String type, byte[] data, int id, boolean needResponse)
    {
        super(type, data, id);

        this.key = key;
        this.needResponse = needResponse;
    }

    public RequestPacket(BsonDocument bsonDocument)
    {
        super(bsonDocument);

        this.key = bsonDocument.get(KEY).asString().getValue();
        this.needResponse = bsonDocument.getBoolean(NEED_RESPONSE).getValue();
    }

    public String getKey()
    {
        return key;
    }

    public boolean getNeedResponse()
    {
        return needResponse;
    }

    @Override
    public BsonDocument toBsonDocument()
    {
        BsonDocument bsonDocument = super.toBsonDocument();
        bsonDocument.put(KEY, new BsonString(key));
        bsonDocument.put(NEED_RESPONSE, new BsonBoolean(needResponse));
        return bsonDocument;
    }

    @Override
    public byte[] serialize()
    {
        return BsonDocumentUtil.serialize(toBsonDocument());
    }

    public static RequestPacket deserialize(byte[] bytes)
    {
        return new RequestPacket(BsonDocumentUtil.deserialize(bytes));
    }
}
