package com.quan.mcapi;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class McapiListener extends McapiBase
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private final ServerSocket listener;
    private final List<McapiClient> clients;

    private McapiListener(MinecraftServer server, String password, ServerSocket listener)
    {
        super(server, password);
        this.listener = listener;
        clients = new ArrayList<>();
    }

    @Override
    protected void run()
    {
        while (isRunning)
        {
            try
            {
                Socket socket = this.listener.accept();
                McapiClient client = new McapiClient(server, password, socket);
                client.start("McapiClient Thread #" + THREAD_COUNTER.incrementAndGet());
                clients.add(client);
                removeStoppedClients();
            }
            catch (SocketTimeoutException socketTimeoutException)
            {
                removeStoppedClients();
            }
            catch (IOException ioException)
            {
                if (this.isRunning)
                {
                    LOGGER.error("McapiListener io exception", ioException);
                }
            }
        }
    }

    @Override
    protected void closeUnmanaged()
    {
        for (McapiClient client : clients)
            client.stop();

        LOGGER.debug("Close socket: {}", listener);
        try
        {
            listener.close();
        }
        catch (IOException ex)
        {
            LOGGER.warn("Failed to close socket", ex);
        }
    }

    private void removeStoppedClients()
    {
        for (McapiClient client : clients)
        {
            if (!client.getIsRunning())
                client.stop();
        }

        clients.removeIf((client) -> !client.getIsRunning());
    }

    @Nullable
    public static McapiListener create(MinecraftServer server, int port, String password)
    {
        if (port < 0 || port > 65535)
        {
            LOGGER.warn("Invalid MCAPI port {}, MCAPI disabled!", port);
            return null;
        }
        if (password.isEmpty())
        {
            LOGGER.warn("No MCAPI password, MCAPI disabled!");
            return null;
        }

        String address = "0.0.0.0";
        try
        {
            ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName(address));
            serverSocket.setSoTimeout(500);
            return new McapiListener(server, password, serverSocket);
        }
        catch (IOException ex)
        {
            LOGGER.warn("Unable to initialise MCAPI on {}:{}", address, port, ex);
            return null;
        }
    }
}
