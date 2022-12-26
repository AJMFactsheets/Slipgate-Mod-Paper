package me.ajmfactsheets.slipgate.listeners

import me.ajmfactsheets.slipgate.util.SlipgateConstants
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent

class TeleportListener: Listener {

    @EventHandler
    fun useNetherPortalPlayer(event: PlayerPortalEvent) {
        val currentWorldName = event.player.world.name

        if (event.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            event.isCancelled = true
            if (currentWorldName == SlipgateConstants.OVERWORLD_WORLD_NAME) {
                // TODO: Use teleporter to search for or generate portals
                event.player.teleport(Location(Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME), event.from.x / 8, 128.0, event.from.z / 8))
                // Fix stupid bug where this doesn't play
                Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)?.playSound(event.player, Sound.BLOCK_PORTAL_TRAVEL, 1f, 1f)
            } else if (currentWorldName == SlipgateConstants.NETHER_WORLD_NAME) {
                // Make sure we are in a slipgate and not a nether portal
                var tpLocation = event.from.clone()
                var heightMoved = 1
                while (heightMoved < SlipgateConstants.MAX_PORTAL_SIZE && tpLocation.block.type != SlipgateConstants.PORTAL_FRAME_MATERIAL && tpLocation.block.type != Material.OBSIDIAN) {
                    tpLocation.add(0.0, -1.0, 0.0)
                    heightMoved++
                }

                if (tpLocation.block.type == SlipgateConstants.PORTAL_FRAME_MATERIAL) {
                    // TODO: Use teleporter to search for or generate portals
                    event.player.teleport(Location(Bukkit.getWorld(SlipgateConstants.SLIP_WORLD_NAME), event.from.x / 8, 128.0, event.from.z / 8))
                } else {
                    // TODO: Use teleporter to search for or generate portals
                    event.player.teleport(Location(Bukkit.getWorld(SlipgateConstants.OVERWORLD_WORLD_NAME), event.from.x * 8, 128.0, event.from.z * 8))
                }

            } else if (currentWorldName == SlipgateConstants.SLIP_WORLD_NAME) {
                // TODO: Use teleporter to search for or generate portals
                event.player.teleport(Location(Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME), event.from.x * 8, 128.0, event.from.z * 8))
            }
        }

    }
}