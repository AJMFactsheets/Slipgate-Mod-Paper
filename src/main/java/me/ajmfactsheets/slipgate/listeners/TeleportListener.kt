package me.ajmfactsheets.slipgate.listeners

import me.ajmfactsheets.slipgate.util.SlipgateConstants
import me.ajmfactsheets.slipgate.util.Teleporter
import org.bukkit.Bukkit
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
            // Overworld to nether
            if (currentWorldName == SlipgateConstants.OVERWORLD_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    val location = event.from
                    location.x = location.x / 8
                    location.z = location.z / 8
                    Teleporter.teleport(event.player, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius, event.creationRadius)
                }
            } else if (currentWorldName == SlipgateConstants.NETHER_WORLD_NAME) {
                // Make sure we are in a slipgate and not a nether portal
                val tpLocation = event.from.clone()
                var heightMoved = 1
                while (heightMoved < SlipgateConstants.MAX_PORTAL_SIZE && tpLocation.block.type != SlipgateConstants.SLIPGATE_MATERIAL && tpLocation.block.type != Material.OBSIDIAN) {
                    tpLocation.add(0.0, -1.0, 0.0)
                    heightMoved++
                }

                // Nether to slip
                if (tpLocation.block.type == SlipgateConstants.SLIPGATE_MATERIAL) {
                    val world = Bukkit.getWorld(SlipgateConstants.SLIP_WORLD_NAME)
                    if (world != null) {
                        val location = event.from
                        location.x = location.x / 8
                        location.z = location.z / 8
                        Teleporter.teleport(event.player, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius, event.creationRadius)
                    }
                } else { // Nether to overworld
                   val world = Bukkit.getWorld(SlipgateConstants.OVERWORLD_WORLD_NAME)
                    if (world != null) {
                        val location = event.from
                        location.x = location.x * 8
                        location.z = location.z * 8
                        Teleporter.teleport(event.player, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius, event.creationRadius)
                    }
                }
                // Slip to nether
            } else if (currentWorldName == SlipgateConstants.SLIP_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    val location = event.from
                    location.x = location.x * 8
                    location.z = location.z * 8
                    Teleporter.teleport(event.player, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius, event.creationRadius)
                }
            }
        } else {
            Bukkit.getWorld(event.to.world.name)
                ?.playSound(event.player, Sound.BLOCK_PORTAL_TRAVEL, 1f, 1f)
        }

    }
}