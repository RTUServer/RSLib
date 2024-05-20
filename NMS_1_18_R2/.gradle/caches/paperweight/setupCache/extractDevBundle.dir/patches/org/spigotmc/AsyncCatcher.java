package org.spigotmc;

import net.minecraft.server.MinecraftServer;

public class AsyncCatcher
{

    public static boolean enabled = true;
    public static boolean shuttingDown = false; // Paper

    public static void catchOp(String reason)
    {
        if ( (AsyncCatcher.enabled || io.papermc.paper.util.TickThread.STRICT_THREAD_CHECKS) && Thread.currentThread() != MinecraftServer.getServer().serverThread ) // Paper
        {
            MinecraftServer.LOGGER.error("Thread " + Thread.currentThread().getName() + " failed main thread check: " + reason, new Throwable()); // Paper
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
