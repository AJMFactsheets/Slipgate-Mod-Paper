package me.ajmfactsheets.slipgate

import me.ajmfactsheets.slipgate.listeners.BlockListener
import me.ajmfactsheets.slipgate.listeners.TeleportListener
import me.ajmfactsheets.slipgate.util.PortalCache
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Slipgate : JavaPlugin() {

    override fun onEnable() {
        server.pluginManager.registerEvents(BlockListener(), this)
        server.pluginManager.registerEvents(TeleportListener(), this)

        // Remove expired locations from portal cache
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            Bukkit.getScheduler().runTask(this, Runnable {
                   PortalCache.tickCacheEntries()
             })
        }, 0L, 1_200L) // Run every minute
    }

}