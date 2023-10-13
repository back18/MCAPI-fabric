package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import com.quan.mcapi.packet.IRequestPacketHandler;
import com.quan.mcapi.packet.RequestPacket;
import com.quan.mcapi.packet.ResponsePacket;
import net.minecraft.server.MinecraftServer;
import org.bson.BsonDocument;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class McapiClient extends McapiBase
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Socket socket;
    private final DataPacketManager dataPacketManager;
    private boolean authenticated;

    public McapiClient(MinecraftServer server, String password, Socket socket)
    {
        super(server, password);
        this.socket = socket;
        dataPacketManager = new DataPacketManager();
    }

    @Override
    protected void run()
    {
        ByteArrayOutputStream cache = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int total = 0;
        int current = 0;

        try
        {
            InputStream stream = socket.getInputStream();
            socket.setSoTimeout(0);

            while (isRunning)
            {
                int length = total == 0 ? 4 : Math.min(total - current, buffer.length);
                int readLength = stream.read(buffer, 0, length);
                if (readLength < 0)
                {
                    isRunning = false;
                    break;
                }

                current += readLength;
                if (total == 0)
                {
                    socket.setSoTimeout(30 * 1000);

                    if (current < 4)
                        continue;

                    total = ByteBuffer.wrap(new byte[] { buffer[3], buffer[2], buffer[1], buffer[0] }).getInt();

                    if (total < 4)
                        throw new IOException(String.format("Packet length %s is less than the minimum length of 4", total));
                }

                cache.write(buffer, 0, readLength);

                //LOGGER.info("Total length {}, read length {}", total, current);

                if (current < total)
                    continue;

                handleDataPacket(cache.toByteArray());

                cache.reset();
                total = 0;
                current = 0;
                socket.setSoTimeout(0);
            }
        }
        catch (SocketTimeoutException socketTimeoutException)
        {
            isRunning = false;
            LOGGER.error("McapiClient request timeout, expected to read {} bytes, actual read {} bytes", total, current, socketTimeoutException);
        }
        catch (IOException ioException)
        {
            isRunning = false;
            LOGGER.error("McapiClient io exception", ioException);
        }
    }

    @Override
    protected void closeUnmanaged()
    {
        LOGGER.debug("Close socket: {}", socket);
        try
        {
            socket.close();
        }
        catch (IOException ex)
        {
            LOGGER.warn("Failed to close socket", ex);
        }
    }

    public void sendResponsePacket(ResponsePacket response) throws IOException
    {
        byte[] bytes = response.serialize();
        OutputStream stream = socket.getOutputStream();
        stream.write(bytes);
    }

    public void handleDataPacket(byte[] bytes) throws IOException
    {
        RequestPacket request;
        try
        {
            BsonDocument bsonDocument = BsonDocumentUtil.deserialize(bytes);
            request = new RequestPacket(bsonDocument);
        }
        catch (Exception ex)
        {
            LOGGER.error("McapiClient unable to parse request", ex);
            return;
        }

        ResponsePacket response;
        try
        {
            IRequestPacketHandler handler = dataPacketManager.getRequestPacketHandler(request.getKey());
            if (handler == null)
            {
                response = ResponsePacket.fromNotFoundError("", "", request.getID());
            }
            else if ((!authenticated && !request.getKey().equals(PacketKey.Login)) || (authenticated && request.getKey().equals(PacketKey.Login)))
            {
                response = ResponsePacket.fromForbiddenError("", "", request.getID());
            }
            else
            {
                response = handler.HandleRequestPacket(server, this, request);
            }
        }
        catch (Exception ex)
        {
            response = ResponsePacket.formInternalServerError(ex.getClass().getTypeName(), ex.getMessage(), request.getID());
        }

        if (request.getNeedResponse())
            sendResponsePacket(response);
    }

    public boolean validatePassword(String password)
    {
        if (this.password.equals(password))
        {
            authenticated = true;
            return true;
        }
        return false;
    }
}
