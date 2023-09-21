package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import com.quan.mcapi.packet.IRequestPacketHandler;
import com.quan.mcapi.packet.RequestPacket;
import com.quan.mcapi.packet.ResponsePacket;
import net.minecraft.server.MinecraftServer;
import org.bson.BsonDocument;
import org.slf4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MinecraftApiClient extends MinecraftApiBase
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Socket socket;
    private final DataPacketManager dataPacketManager;
    private boolean authenticated;

    public MinecraftApiClient(MinecraftServer server, String password, Socket socket)
    {
        super(server, password, "MCAPI Client " + socket.getInetAddress());

        this.socket = socket;
        dataPacketManager = new DataPacketManager();
    }

    @Override
    public void run()
    {
        try
        {
            InputStream stream = socket.getInputStream();
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream cache = new ByteArrayOutputStream();
            int total = buffer.length;
            int current = 0;
            boolean initial = true;
            //socket.setSoTimeout(0);

            while (running)
            {
                int length = stream.read(buffer, 0, Math.min(total - current, buffer.length));

                //LOGGER.info("expected length: {}, read length: {},", Math.min(total - current, buffer.length), length);

                if (length < 0)
                {
                    running = false;
                    break;
                }

                current += length;
                if (initial)
                {
                    //socket.setSoTimeout(30 * 1000);

                    if (current < 4)
                        continue;

                    total = ByteBuffer.wrap(new byte[] { buffer[3], buffer[2], buffer[1], buffer[0] }).getInt();

                    if (total < 4)
                        throw new IOException("Error reading packet: Packet length identifier cannot be less than 4");

                    initial = false;
                }

                cache.write(buffer, 0, length);

                if (current < total)
                    continue;

                handleDataPacket(cache.toByteArray());

                cache.reset();
                total = buffer.length;
                current = 0;
                initial = true;
                //socket.setSoTimeout(0);
            }
        }
        catch (IOException ioException)
        {
            running = false;
            LOGGER.error("MCAPI Client io exception", ioException);
        }
    }

    @Override
    public synchronized void stop()
    {
        running = false;
        close();
        super.stop();
    }

    private void close()
    {
        try
        {
            socket.close();
        }
        catch (IOException ioException)
        {
            LOGGER.warn("Failed to close socket", ioException);
        }
    }

    public void sendResponsePacket(ResponsePacket responsePacket) throws IOException
    {
        byte[] bytes = responsePacket.serialize();
        OutputStream stream = socket.getOutputStream();
        stream.write(bytes);
        stream.flush();
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
            LOGGER.error("MCAPI unable to parse request", ex);
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
