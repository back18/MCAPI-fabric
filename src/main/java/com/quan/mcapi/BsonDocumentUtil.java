package com.quan.mcapi;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import java.nio.ByteBuffer;

public class BsonDocumentUtil
{
    private static final BsonDocumentCodec BSON_DOCUMENT_CODEC = new BsonDocumentCodec();

    public static byte[] serialize(BsonDocument bsonDocument)
    {
        try (BasicOutputBuffer buffer = new BasicOutputBuffer())
        {
            try (BsonBinaryWriter writer = new BsonBinaryWriter(buffer))
            {
                BSON_DOCUMENT_CODEC.encode(writer, bsonDocument, EncoderContext.builder().build());
            }
            return buffer.toByteArray();
        }
    }

    public static BsonDocument deserialize(byte[] bytes)
    {
        return deserialize(ByteBuffer.wrap(bytes));
    }

    public static BsonDocument deserialize(ByteBuffer byteBuffer)
    {
        try (BsonBinaryReader reader = new BsonBinaryReader(byteBuffer))
        {
            return BSON_DOCUMENT_CODEC.decode(reader, DecoderContext.builder().build());
        }
    }
}
