package me.ajmfactsheets.slipgate

import me.ajmfactsheets.slipgate.constants.SlipgateConstants
import me.ajmfactsheets.slipgate.listeners.BlockListener
import me.ajmfactsheets.slipgate.listeners.ChatListener
import me.ajmfactsheets.slipgate.listeners.TeleportListener
import me.ajmfactsheets.slipgate.util.PortalCache
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Slipgate : JavaPlugin() {

    private val config = this.getConfig()

    override fun onEnable() {
        this.handleConfigDefaults()
        SlipgateConstants.SLIP_WORLD_NAME = config.getString("slip-world-name") ?: SlipgateConstants.DEFAULT_SLIP_WORLD_NAME
        SlipgateConstants.PORTALS_SEARCH_TOP_TO_BOTTOM = config.getBoolean("portals-search-from-top-to-bottom-of-world")
        SlipgateConstants.MIN_HEIGHT_MODIFIER = config.getInt("min-teleport-height-modifier")
        SlipgateConstants.MAX_HEIGHT_MODIFIER = config.getInt("max-teleport-height-modifier")
        SlipgateConstants.CACHE_TTL = config.getInt("portal-cache-entry-expires-minutes")

        server.pluginManager.registerEvents(BlockListener(), this)
        server.pluginManager.registerEvents(TeleportListener(this), this)

        if (config.getBoolean("disable-chat-reporting")) {
            server.pluginManager.registerEvents(ChatListener(), this)
        }

        // Remove expired locations from portal cache
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            Bukkit.getScheduler().runTask(this, Runnable {
                   PortalCache.tickCacheEntries()
             })
        }, 0L, 1_200L) // Run every minute
    }

    private fun handleConfigDefaults() {
        config.addDefault("slip-world-name", "world_slipgate_the_slip")
        config.addDefault("portals-search-from-top-to-bottom-of-world", false)
        config.addDefault("min-teleport-height-modifier", 2)
        config.addDefault("max-teleport-height-modifier", 5)
        config.addDefault("portal-cache-entry-expires-minutes", 5)
        config.addDefault("disable-chat-reporting", false)
        config.options().copyDefaults(true)
        this.saveConfig()
    }
}