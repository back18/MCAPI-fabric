package com.quan.mcapi.packet;

import com.quan.mcapi.ISerializable;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.codecs.BsonDocumentCodec;

public abstract class DataPacket implements ISerializable
{
    private static final BsonDocumentCodec BSON_DOCUMENT_CODEC = new BsonDocumentCodec();
    private static final String TYPE = "Type";
    private static final String DATA = "Data";
    private static final String ID = "ID";
    private final String type;
    private final byte[] data;
    private final int id;

    protected DataPacket(String type, byte[] data, int id)
    {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    protected DataPacket(BsonDocument bsonDocument)
    {
        type = bsonDocument.getString(TYPE).getValue();
        data = bsonDocument.getBinary(DATA).getData();
        id = bsonDocument.getInt32(ID).getValue();
    }

    public String getType()
    {
        return type;
    }

    public byte[] getData()
    {
        return data;
    }

    public  int getID()
    {
        return id;
    }

    public BsonDocument toBsonDocument()
    {
        BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.put(TYPE, new BsonString(type));
        bsonDocument.put(DATA, new BsonBinary(data));
        bsonDocument.put(ID, new BsonInt32(id));
        return bsonDocument;
    }

    public abstract byte[] serialize();
}
