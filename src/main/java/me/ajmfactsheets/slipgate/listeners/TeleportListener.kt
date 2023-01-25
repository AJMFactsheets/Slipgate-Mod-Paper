package me.ajmfactsheets.slipgate.listeners

import me.ajmfactsheets.slipgate.constants.SlipgateConstants
import me.ajmfactsheets.slipgate.util.Teleporter
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.PortalType
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.random.Random

class TeleportListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun useNetherPortalEntity(event: EntityPortalEvent) {
        val currentWorldName = event.entity.world.name

        if (event.portalType == PortalType.NETHER) {
            event.isCancelled = true
            // Overworld to nether
            if (currentWorldName == SlipgateConstants.OVERWORLD_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    var location = event.from
                    location.x = location.x / 8
                    location.z = location.z / 8
                    location = clampToWorldBorder(location, event.searchRadius / 8)
                    Teleporter.teleport(event.entity, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius / 8, event.searchRadius / 8)
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
                        Bukkit.getLogger().log(Level.SEVERE,"Could not locate portal block. Please report bug to AJMFactsheets. $tpLocationCopy")
                        Bukkit.getWorld(currentWorldName)?.playSound(tpLocationCopy, Sound.BLOCK_ANVIL_DESTROY, 1f, Random.nextFloat() * 0.4f + 0.8f)
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
                        var location = event.from
                        location.x = location.x / 8
                        location.z = location.z / 8
                        location = clampToWorldBorder(location, event.searchRadius / 8)
                        Teleporter.teleport(event.entity, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius / 8, event.searchRadius / 8)
                    }
                // Nether to overworld
                } else if (tpLocation.block.type == SlipgateConstants.NETHER_PORTAL_MATERIAL) {
                    val world = Bukkit.getWorld(SlipgateConstants.OVERWORLD_WORLD_NAME)
                    if (world != null) {
                        var location = event.from
                        location.x = location.x * 8
                        location.z = location.z * 8
                        location = clampToWorldBorder(location, event.searchRadius)
                        Teleporter.teleport(event.entity, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius, event.searchRadius)
                    }
                }
            // Slip to nether
            } else if (currentWorldName == SlipgateConstants.SLIP_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    var location = event.from
                    location.x = location.x * 8
                    location.z = location.z * 8
                    location = clampToWorldBorder(location, event.searchRadius)
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
                    var location = event.from
                    location.x = location.x / 8
                    location.z = location.z / 8
                    location = clampToWorldBorder(location, event.searchRadius / 8)
                    Teleporter.teleport(event.player, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius / 8, event.creationRadius / 8)
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
                        Bukkit.getLogger().log(Level.SEVERE,"Could not locate portal block. Please report bug to AJMFactsheets. $tpLocationCopy")
                        event.player.sendMessage("Could not figure out destination dimension. Please report bug to AJMFactsheets. $tpLocationCopy")
                        Bukkit.getWorld(currentWorldName)?.playSound(tpLocationCopy, Sound.BLOCK_ANVIL_DESTROY, 1f, Random.nextFloat() * 0.4f + 0.8f)
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
                        var location = event.from
                        location.x = location.x / 8
                        location.z = location.z / 8
                        location = clampToWorldBorder(location, event.searchRadius / 8)
                        Teleporter.teleport(event.player, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius / 8, event.creationRadius / 8)
                    }
                // Nether to overworld
                } else if (tpLocation.block.type == SlipgateConstants.NETHER_PORTAL_MATERIAL) {
                   val world = Bukkit.getWorld(SlipgateConstants.OVERWORLD_WORLD_NAME)
                    if (world != null) {
                        var location = event.from
                        location.x = location.x * 8
                        location.z = location.z * 8
                        location = clampToWorldBorder(location, event.searchRadius)
                        Teleporter.teleport(event.player, location, world, SlipgateConstants.NETHER_PORTAL_MATERIAL, event.searchRadius, event.creationRadius)
                    }
                }
            // Slip to nether
            } else if (currentWorldName == SlipgateConstants.SLIP_WORLD_NAME) {
                val world = Bukkit.getWorld(SlipgateConstants.NETHER_WORLD_NAME)
                if (world != null) {
                    var location = event.from
                    location.x = location.x * 8
                    location.z = location.z * 8
                    location = clampToWorldBorder(location, event.searchRadius)
                    Teleporter.teleport(event.player, location, world, SlipgateConstants.SLIPGATE_MATERIAL, event.searchRadius, event.creationRadius)
                }
            }
        }
    }

    private fun clampToWorldBorder(location: Location, safetyMargin: Int): Location {
        val center = location.world.worldBorder.center
        val radius = location.world.worldBorder.size / 2
        val minX = center.x - radius + safetyMargin
        val maxX = center.x + radius - safetyMargin
        val minZ = center.z - radius + safetyMargin
        val maxZ = center.z + radius - safetyMargin

        return if (location.x in minX .. maxX && location.z in minZ .. maxZ) {
            location
        } else {
            Location(location.world, clampLocation(location.x, minX, maxX), location.y, clampLocation(location.z, minZ, maxZ))
        }
    }

    private fun clampLocation(value: Double, min: Double, max: Double): Double {
        return if (value < min) {
            min
        } else if (value > max) {
            max
        } else {
            value
        }
    }
}