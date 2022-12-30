package me.ajmfactsheets.slipgate.util

import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.data.Orientable
import org.bukkit.entity.Entity
import kotlin.random.Random

class Teleporter {

    companion object {
        private const val xzOffset = 0.5
        private const val xzOffsetGen = 1 + xzOffset
        private const val yOffset = 0.5

        // TODO Travel.ogg not heard in End
        // TODO Make this work for entities as well not just players, see TeleportListener.kt for player implementation
        // TODO Code is very very very laggy when traveling from nether to overworld due to dimension expansion, possible rewrite or limit search radius
        // TODO Potentially add a hashmap cache of portal locations or use vanilla POIs to help with this
        // TODO Combine the 3 similar while loops and cache the first result for a possible portal generation location for a speedup.
        // TODO Reverse min & max Y so you don't spawn as much underground? Would probably make performance a bit worse so do some testing.
        fun teleport(entity: Entity, location: Location, world: World, frameMaterial: Material, searchRadius: Int, createRadius: Int) {
            // Search for valid portal block & frame block
            val x = location.x.toInt()
            var minX = x - searchRadius
            var maxX = x + searchRadius
            var minY = world.minHeight + SlipgateConstants.MIN_HEIGHT_MODIFIER
            var maxY = world.logicalHeight - SlipgateConstants.MAX_HEIGHT_MODIFIER // Hopefully prevent ILLEGAL bedrock breaking! (Frowned upon by Hause)
            val z = location.z.toInt()
            var minZ = z - searchRadius
            var maxZ = z + searchRadius

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
                            entity.teleport(teleportDestination)
                            playRandomPortalSound(teleportDestination)
                            return
                        }
                        minZ++
                    }
                    minY++
                }
                minX++
            }

            // Else, search for valid 4x4 air gap adjacent to ground to place portal
            minX = x - createRadius
            maxX = x + createRadius
            minY = world.minHeight
            maxY = world.logicalHeight
            minZ = z - createRadius
            maxZ = z + createRadius

            while (minX < maxX) {
                minY = world.minHeight
                while (minY < maxY) {
                    minZ = z - searchRadius
                    while (minZ < maxZ) {
                        if (isGroundGapX(world, minX, minY, minZ)) {
                            createPortalX(world, minX, minY, minZ, frameMaterial)
                            val teleportDestination = Location(world, minX.toDouble() + xzOffsetGen, minY.toDouble() + yOffset, minZ.toDouble() + xzOffset)
                            entity.teleport(teleportDestination)
                            playRandomPortalSound(teleportDestination)
                            return
                        } else if (isGroundGapZ(world, minX, minY, minZ)) {
                            createPortalZ(world, minX, minY, minZ, frameMaterial)
                            val teleportDestination = Location(world, minX.toDouble() + xzOffset, minY.toDouble() + yOffset, minZ.toDouble() + xzOffsetGen)
                            entity.teleport(teleportDestination)
                            playRandomPortalSound(teleportDestination)
                            return
                        }
                        minZ++
                    }
                    minY++
                }
                minX++
            }

            // Else, search for valid 4x4 air gap not adjacent to ground
            minX = x - createRadius
            maxX = x + createRadius
            minY = world.minHeight
            maxY = world.logicalHeight
            minZ = z - createRadius
            maxZ = z + createRadius

            while (minX < maxX) {
                while (minY < maxY) {
                    while (minZ < maxZ) {
                        if (isAirGapX(world, minX, minY, minZ)) {
                            createFloatingPortalX(world, minX, minY, minZ, frameMaterial)
                            val teleportDestination = Location(world, minX.toDouble() + xzOffsetGen, minY.toDouble() + yOffset, minZ.toDouble() + xzOffset)
                            entity.teleport(teleportDestination)
                            playRandomPortalSound(teleportDestination)
                            return
                        } else if (isAirGapZ(world, minX, minY, minZ)) {
                            createFloatingPortalZ(world, minX, minY, minZ, frameMaterial)
                            val teleportDestination = Location(world, minX.toDouble() + xzOffset, minY.toDouble() + yOffset, minZ.toDouble() + xzOffsetGen)
                            entity.teleport(teleportDestination)
                            playRandomPortalSound(teleportDestination)
                            return
                        }
                        minZ++
                    }
                    minY++
                }
                minX++
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
            entity.teleport(teleportDestination)
            playRandomPortalSound(teleportDestination)
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
}