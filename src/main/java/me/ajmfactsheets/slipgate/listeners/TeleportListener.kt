package me.ajmfactsheets.slipgate.listeners

import me.ajmfactsheets.slipgate.constants.SlipgateConstants
import me.ajmfactsheets.slipgate.util.Teleporter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.PortalType
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.random.Random

class TeleportListener(private var plugin: JavaPlugin) : Listener {

    @EventHandler
    fun useNetherPortalEntity(event: EntityPortalEvent) {
        val currentWorldName = event.entity.world.name

        if (event.portalType == PortalType.NETHER) {
            event.isCancelled = true
            // Overworld to nether
            if (currentWorldName == SlipgateConstants.OVERWORLD_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    val location = event.from
                    location.x = location.x / 8
                    location.z = location.z / 8
                    Teleporter.teleport(event.entity, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius, event.searchRadius)
                }
            } else if (currentWorldName == SlipgateConstants.NETHER_WORLD_NAME) {
                // Make sure we are in a slipgate and not a nether portal
                var tpLocation = event.from.clone()
                if (tpLocation.block.type != Material.NETHER_PORTAL) {
                    val tpLocationCopy = tpLocation.clone()
                    if (tpLocationCopy.add(1.0, 0.0, 0.0).block.type == Material.NETHER_PORTAL) { // +x
                        tpLocation = tpLocationCopy
                    } else if (tpLocationCopy.add(-2.0, 0.0, 0.0).block.type == Material.NETHER_PORTAL) { // -x (undo above)
                        tpLocation = tpLocationCopy
                    } else if (tpLocationCopy.add(1.0, 0.0, 1.0).block.type == Material.NETHER_PORTAL) { // +z
                        tpLocation = tpLocationCopy
                    } else if (tpLocationCopy.add(0.0, 0.0, -2.0).block.type == Material.NETHER_PORTAL) { // -z (undo above)
                        tpLocation = tpLocationCopy
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE,"Could not locate portal block. Please report bug to AJMFactsheets")
                    }
                }

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
                        Teleporter.teleport(event.entity, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius, event.searchRadius)
                    }
                } else if (tpLocation.block.type == SlipgateConstants.NETHER_PORTAL_MATERIAL) { // Nether to overworld
                    val world = Bukkit.getWorld(SlipgateConstants.OVERWORLD_WORLD_NAME)
                    if (world != null) {
                        val location = event.from
                        location.x = location.x * 8
                        location.z = location.z * 8
                        Teleporter.teleport(event.entity, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius, event.searchRadius)
                    }
                }
                // Slip to nether
            } else if (currentWorldName == SlipgateConstants.SLIP_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    val location = event.from
                    location.x = location.x * 8
                    location.z = location.z * 8
                    Teleporter.teleport(event.entity, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius, event.searchRadius)
                }
            }
        }
    }

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
                var tpLocation = event.from.clone()
                if (tpLocation.block.type != Material.NETHER_PORTAL) {
                    val tpLocationCopy = tpLocation.clone()
                    if (tpLocationCopy.add(1.0, 0.0, 0.0).block.type == Material.NETHER_PORTAL) { // +x
                        tpLocation = tpLocationCopy
                    } else if (tpLocationCopy.add(-2.0, 0.0, 0.0).block.type == Material.NETHER_PORTAL) { // -x (undo above)
                        tpLocation = tpLocationCopy
                    } else if (tpLocationCopy.add(1.0, 0.0, 1.0).block.type == Material.NETHER_PORTAL) { // +z
                        tpLocation = tpLocationCopy
                    } else if (tpLocationCopy.add(0.0, 0.0, -2.0).block.type == Material.NETHER_PORTAL) { // -z (undo above)
                        tpLocation = tpLocationCopy
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE,"Could not locate portal block. Please report bug to AJMFactsheets")
                    }
                }

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
                } else if (tpLocation.block.type == SlipgateConstants.NETHER_PORTAL_MATERIAL) { // Nether to overworld
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
        }
        else {
            // Fixes travel.ogg in the End dimension
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                Bukkit.getWorld(event.to.world.name)?.playSound(event.to, Sound.BLOCK_PORTAL_TRAVEL, 1f, Random.nextFloat() * 0.4f + 0.8f)
            }, 1)
        }
    }
}