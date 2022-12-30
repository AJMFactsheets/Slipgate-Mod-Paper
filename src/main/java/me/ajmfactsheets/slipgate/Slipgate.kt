package me.ajmfactsheets.slipgate

import me.ajmfactsheets.slipgate.listeners.BlockListener
import me.ajmfactsheets.slipgate.listeners.TeleportListener
import org.bukkit.plugin.java.JavaPlugin

class Slipgate : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Slipgate engaged!")
        server.pluginManager.registerEvents(BlockListener(), this)
        server.pluginManager.registerEvents(TeleportListener(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}