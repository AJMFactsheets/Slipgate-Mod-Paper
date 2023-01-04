package me.ajmfactsheets.slipgate.listeners

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener: Listener {

    // Prevent chat reports from being valid
    // Temporarily disabled due to an exploit
//    @EventHandler
//    fun onChat(e: AsyncPlayerChatEvent) {
//        e.isCancelled = true
//        val message = String.format(e.format, e.player.name, e.message)
//        Bukkit.getConsoleSender().sendMessage(message)
//
//        for(player: Player in e.recipients) {
//            player.sendMessage(e.player.uniqueId, message)
//        }
//
//    }
}