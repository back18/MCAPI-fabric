package com.quan.mcapi.packet;

import com.ibm.icu.impl.InvalidFormatException;
import com.quan.mcapi.BsonDocumentUtil;
import com.quan.mcapi.ISerializable;
import com.quan.mcapi.PacketType;
import com.quan.mcapi.StatusCode;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

import java.nio.ByteBuffer;

public class ResponsePacket extends DataPacket
{
    private static final String STATUS_CODE = "StatusCode";
    private final StatusCode statusCode;

    public ResponsePacket(StatusCode statusCode, String type, byte[] data, int id)
    {
        super(type, data, id);

        this.statusCode = statusCode;
    }

    public ResponsePacket(BsonDocument bsonDocument)
    {
        super(bsonDocument);

        statusCode = StatusCode.values()[bsonDocument.getInt32(STATUS_CODE).getValue()];
    }

    public StatusCode getStatusCode()
    {
        return statusCode;
    }

    public static ResponsePacket fromForbiddenError(String errorType, String errorMessage, int id)
    {
        return fromError(StatusCode.Forbidden, errorType, errorMessage, id);
    }

    public static ResponsePacket fromNotFoundError(String errorType, String errorMessage, int id)
    {
        return fromError(StatusCode.NotFound, errorType, errorMessage, id);
    }

    public static ResponsePacket formInternalServerError(String errorType, String errorMessage, int id)
    {
        return fromError(StatusCode.InternalServerError, errorType, errorMessage, id);
    }

    private static ResponsePacket fromError(StatusCode statusCode, String errorType, String errorMessage, int id)
    {
        byte[] data = new ResponsePacket.ErrorResponseData(errorType, errorMessage).serialize();
        return new ResponsePacket(statusCode, PacketType.BSON, data, id);
    }

    @Override
    public BsonDocument toBsonDocument()
    {
        BsonDocument bsonDocument = super.toBsonDocument();
        bsonDocument.put(STATUS_CODE, new BsonInt32(statusCode.getValue()));
        return bsonDocument;
    }

    @Override
    public byte[] serialize()
    {
        return BsonDocumentUtil.serialize(toBsonDocument());
    }

    public static ResponsePacket deserialize(byte[] bytes)
    {
        return new ResponsePacket(BsonDocumentUtil.deserialize(bytes));
    }

    public static class ErrorResponseData implements ISerializable
    {
        public final String errorType;
        public final String errorMessage;

        public ErrorResponseData(String errorType, String errorMessage)
        {
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }

        @Override
        public byte[] serialize()
        {
            BsonDocument bsonDocument = new BsonDocument();
            bsonDocument.put("ErrorType", new BsonString(errorType));
            bsonDocument.put("ErrorMessage", new BsonString(errorMessage));
            return BsonDocumentUtil.serialize(bsonDocument);
        }
    }
}
