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

public class MinecraftApiListener extends MinecraftApiBase
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerSocket listener;
    private final List<MinecraftApiClient> clients;

    private MinecraftApiListener(MinecraftServer server, String password, ServerSocket listener)
    {
        super(server, password, "MCAPI Listener");

        this.listener = listener;
        clients = new ArrayList<>();
    }

    @Override
    public void run()
    {
        try
        {
            while (running)
            {
                try
                {
                    Socket socket = this.listener.accept();
                    MinecraftApiClient client = new MinecraftApiClient(server, password, socket);
                    client.start();
                    clients.add(client);
                    removeStoppedClients();
                }
                catch (SocketTimeoutException socketTimeoutException)
                {
                    removeStoppedClients();
                }
                catch (IOException ioException)
                {
                    if (this.running)
                    {
                        LOGGER.info("IO exception: ", ioException);
                    }
                }
            }
        }
        finally
        {
            close();
        }
    }

    @Nullable
    public static MinecraftApiListener create(MinecraftServer server, int port, String password)
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
            MinecraftApiListener listener = new MinecraftApiListener(server, password, serverSocket);
            listener.start();
            return listener;
        }
        catch (IOException ex)
        {
            LOGGER.warn("Unable to initialise MCAPI on {}:{}", address, port, ex);
            return null;
        }
    }

    @Override
    public void stop()
    {
        running = false;
        close();
        super.stop();

        for (MinecraftApiClient client : clients)
        {
            if (client.isRunning())
            {
                client.stop();
            }
        }

        clients.clear();
    }

    private void close()
    {
        LOGGER.debug("closeSocket: {}", listener);
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
        this.clients.removeIf((client) -> !client.isRunning());
    }
}
