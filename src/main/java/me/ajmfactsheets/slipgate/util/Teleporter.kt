package me.ajmfactsheets.slipgate.util

import me.ajmfactsheets.slipgate.constants.SlipgateConstants
import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.data.Orientable
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import kotlin.random.Random

object Teleporter {

    private data class PortalDestination (
        val x: Int,
        val y: Int,
        val z: Int,
        val axis: Axis
    )

    private const val xzOffset = 0.5
    private const val xzOffsetGen = 1 + xzOffset
    private const val yOffset = 0.5

    // TODO Reverse min & max Y so you don't spawn as much underground? Would probably make performance a bit worse so do some testing.
    fun teleport(entity: Entity, location: Location, world: World, frameMaterial: Material, searchRadius: Int, createRadius: Int) {
        // Check portal cache
        val cachedLocation = PortalCache.getCacheEntry(entity.world.name, world.name, location, searchRadius)
        if (cachedLocation != null) {
            entity.teleport(cachedLocation)
            if (entity is Player) playRandomPortalSound(cachedLocation)
            return
        }

        // Search for valid portal block & frame block
        val x = location.x.toInt()
        var minX = x - searchRadius
        var maxX = x + searchRadius
        val minXCreate = x - createRadius
        val maxXCreate = x + createRadius
        var minY = world.minHeight + SlipgateConstants.MIN_HEIGHT_MODIFIER
        var maxY = world.logicalHeight - SlipgateConstants.MAX_HEIGHT_MODIFIER // Hopefully prevent ILLEGAL bedrock breaking! (Frowned upon by Hause)
        val z = location.z.toInt()
        var minZ = z - searchRadius
        var maxZ = z + searchRadius
        val minZCreate = z - createRadius
        val maxZCreate = z + createRadius

        var groundGapLocation: PortalDestination? = null
        var airGapLocation: PortalDestination? = null

        while (minX < maxX) {
            minY = world.minHeight
            while (minY < maxY) {
                minZ = z - searchRadius
                while (minZ < maxZ) {
                    if (world.getBlockAt(minX, minY, minZ).type == Material.NETHER_PORTAL && world.getBlockAt(minX, minY - 1, minZ).type == frameMaterial) {
                        val axis = (world.getBlockAt(minX, minY, minZ).blockData as Orientable).axis
                        val teleportDestination: Location = if (axis != Axis.Y) {
                            Location(world, minX.toDouble() + xzOffset, minY.toDouble() + yOffset, minZ.toDouble() + xzOffset)
                        } else {
                            Location(world, minX.toDouble(), minY.toDouble() + yOffset, minZ.toDouble())
                        }
                        PortalCache.addCacheEntry(entity.world.name, world.name, teleportDestination)
                        entity.teleport(teleportDestination)
                        if (entity is Player) playRandomPortalSound(teleportDestination)
                        return
                    } else if (x in minXCreate .. maxXCreate && z in minZCreate .. maxZCreate && (groundGapLocation == null || airGapLocation == null)) { // Check possible ground / air location to generate portal
                        if (groundGapLocation == null) {
                            if (isGroundGapX(world, minX, minY, minZ)) {
                                groundGapLocation = PortalDestination(minX, minY, minZ, Axis.X)
                            } else if (isGroundGapZ(world, minX, minY, minZ)) {
                                groundGapLocation = PortalDestination(minX, minY, minZ, Axis.Z)
                            }
                        } else if (airGapLocation == null) {
                            if (isAirGapX(world, minX, minY, minZ)) {
                                airGapLocation = PortalDestination(minX, minY, minZ, Axis.X)
                            } else if (isAirGapZ(world, minX, minY, minZ)) {
                                airGapLocation = PortalDestination(minX, minY, minZ, Axis.Z)
                            }
                        }
                    }
                    minZ++
                }
                minY++
            }
            minX++
        }

        // place portal in valid 4x4 air gap adjacent to ground
        if (groundGapLocation != null) {
            if (groundGapLocation.axis == Axis.X) {
                createPortalX(world, groundGapLocation.x, groundGapLocation.y, groundGapLocation.z, frameMaterial)
                val teleportDestination = Location(world, groundGapLocation.x.toDouble() + xzOffsetGen, groundGapLocation.y.toDouble() + yOffset, groundGapLocation.z.toDouble() + xzOffset)
                PortalCache.addCacheEntry(entity.world.name, world.name, teleportDestination)
                entity.teleport(teleportDestination)
                if (entity is Player) playRandomPortalSound(teleportDestination)
                return
            } else if (groundGapLocation.axis == Axis.Z) {
                createPortalZ(world, groundGapLocation.x, groundGapLocation.y, groundGapLocation.z, frameMaterial)
                val teleportDestination = Location(world, groundGapLocation.x.toDouble() + xzOffset, groundGapLocation.y.toDouble() + yOffset, groundGapLocation.z.toDouble() + xzOffsetGen)
                PortalCache.addCacheEntry(entity.world.name, world.name, teleportDestination)
                entity.teleport(teleportDestination)
                if (entity is Player) playRandomPortalSound(teleportDestination)
                return
            }
            // Place portal in valid 4x4 air gap not adjacent to ground
        } else if (airGapLocation != null) {
            if (airGapLocation.axis == Axis.X) {
                createFloatingPortalX(world, airGapLocation.x, airGapLocation.y, airGapLocation.z, frameMaterial)
                val teleportDestination = Location(world, airGapLocation.x.toDouble() + xzOffsetGen, airGapLocation.y.toDouble() + yOffset, airGapLocation.z.toDouble() + xzOffset)
                PortalCache.addCacheEntry(entity.world.name, world.name, teleportDestination)
                entity.teleport(teleportDestination)
                if (entity is Player) playRandomPortalSound(teleportDestination)
                return
            } else if (airGapLocation.axis == Axis.Z) {
                createFloatingPortalZ(world, airGapLocation.x, airGapLocation.y, airGapLocation.z, frameMaterial)
                val teleportDestination = Location(world, airGapLocation.x.toDouble() + xzOffset, airGapLocation.y.toDouble() + yOffset, airGapLocation.z.toDouble() + xzOffsetGen)
                PortalCache.addCacheEntry(entity.world.name, world.name, teleportDestination)
                entity.teleport(teleportDestination)
                if (entity is Player) playRandomPortalSound(teleportDestination)
                return
            }
        }

        // Else just destroy blocks at location and force a portal
        val yClamped = this.clampLogicalDimensionY(location.y.toInt(), world.minHeight + SlipgateConstants.MIN_HEIGHT_MODIFIER , world.logicalHeight - SlipgateConstants.MAX_HEIGHT_MODIFIER)
        val teleportDestination: Location = if (Random.nextBoolean()) {
            createFloatingPortalX(world, x, yClamped, z, frameMaterial)
            Location(world, x.toDouble() + xzOffsetGen, yClamped + yOffset, z.toDouble() + xzOffset)
        } else {
            createFloatingPortalZ(world, x, yClamped, z, frameMaterial)
            Location(world, x.toDouble() + xzOffset, yClamped + yOffset, z.toDouble() + xzOffsetGen)
        }
        PortalCache.addCacheEntry(entity.world.name, world.name, teleportDestination)
        entity.teleport(teleportDestination)
        if (entity is Player) playRandomPortalSound(teleportDestination)
    }

    private fun isGroundGapX(world: World, x: Int, y: Int, z: Int): Boolean {
        for (curx in x .. x + 3) {
            for (cury in y - 1 until y) {
                if (!world.getBlockAt(curx, cury, z).isSolid) {
                    return false
                }
            }
        }
        return isAirGapX(world, x, y, z)
    }

    private fun isAirGapX(world: World, x: Int, y: Int, z: Int): Boolean {
        for (curx in x .. x + 3) {
            for (cury in y .. y + 3) {
                if (!world.getBlockAt(curx, cury, z).isEmpty) {
                    return false
                }
            }
        }
        return true
    }

    private fun createFloatingPortalX(world: World, x: Int, y: Int, z: Int, frame: Material) {
        for (curx in x + 1 .. x + 2) {
            for (cury in y - 1 .. y + 2) {
                for (curz in z - 1 .. z + 1) {
                    if (curz == z) {
                        continue
                    } else if (cury == y - 1 && curx != x && curx != x + 3) {
                        world.getBlockAt(curx, cury, curz).type = frame
                    } else {
                        world.getBlockAt(curx, cury, curz).type = Material.AIR
                    }
                }
            }
        }
        this.createPortalX(world, x, y, z, frame)
    }

    private fun createPortalX(world: World, x: Int, y: Int, z: Int, frame: Material) {
        val portalBlockList = mutableListOf<Location>()
        for (curx in x .. x + 3) {
            for (cury in y - 1 .. y + 3) {
                if (curx == x || curx == x + 3 || cury == y - 1 || cury == y + 3) {
                    world.getBlockAt(curx, cury, z).type = frame
                } else {
                    portalBlockList.add(Location(world, curx.toDouble(), cury.toDouble(), z.toDouble()))
                }
            }
        }
        // Need to create frame, then fill in portal or else it doesn't work.
        portalBlockList.forEach {
            world.getBlockAt(it).type = Material.NETHER_PORTAL
        }
    }

    private fun isGroundGapZ(world: World, x: Int, y: Int, z: Int): Boolean {
        for (curz in z .. z + 3) {
            for (cury in y - 1 until y) {
                if (!world.getBlockAt(x, cury, curz).isSolid) {
                    return false
                }
            }
        }
        return isAirGapZ(world, x, y, z)
    }

    private fun isAirGapZ(world: World, x: Int, y: Int, z: Int): Boolean {
        for (curz in z .. z + 3) {
            for (cury in y .. y + 3) {
                if (!world.getBlockAt(x, cury, curz).isEmpty) {
                    return false
                }
            }
        }
        return true
    }

    private fun createFloatingPortalZ(world: World, x: Int, y: Int, z: Int, frame: Material) {
        for (curz in z + 1 .. z + 2) {
            for (cury in y - 1 .. y + 2) {
                for (curx in x - 1 .. x + 1) {
                    if (curx == x) {
                        continue
                    } else if (cury == y - 1 && curz != z && curz != z + 3) {
                        world.getBlockAt(curx, cury, curz).type = frame
                    } else {
                        world.getBlockAt(curx, cury, curz).type = Material.AIR
                    }
                }
            }
        }
        this.createPortalZ(world, x, y, z, frame)
    }

    private fun createPortalZ(world: World, x: Int, y: Int, z: Int, frame: Material) {
        val portalBlockList = mutableListOf<Location>()
        for (curz in z .. z + 3) {
            for (cury in y - 1 .. y + 3) {
                if (curz == z || curz == z + 3 || cury == y - 1 || cury == y + 3) {
                    world.getBlockAt(x, cury, curz).type = frame
                } else {
                    portalBlockList.add(Location(world, x.toDouble(), cury.toDouble(), curz.toDouble()))
                }
            }
        }
        // Need to create frame, then fill in portal or else it doesn't work. Probably block updates.
        portalBlockList.forEach {
            val block = world.getBlockAt(it)
            block.type = Material.NETHER_PORTAL
            val blockdata = block.blockData as Orientable
            blockdata.axis = Axis.Z
            block.blockData = blockdata
        }
    }

    private fun clampLogicalDimensionY(y: Int, yMin: Int, yMax: Int): Int {
        return if (y < yMin) {
            yMin
        } else if (y > yMax) {
            yMax
        } else {
            y
        }
    }

    /**
     * Random portal sound pitch from 0.8 - 1.2 which sounds very similar to the vanilla range, but I didn't check exactly
     */
    private fun playRandomPortalSound(location: Location) {
        Bukkit.getWorld(location.world.name)?.playSound(location, Sound.BLOCK_PORTAL_TRAVEL, 1f, Random.nextFloat() * 0.4f + 0.8f)
    }

}