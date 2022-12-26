package me.ajmfactsheets.slipgate.listeners

import me.ajmfactsheets.slipgate.util.SlipgateConstants
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.Orientable
//import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.PortalCreateEvent

//import org.bukkit.event.player.AsyncPlayerChatEvent

class BlockListener: Listener {

    data class PortalDirectionData (var width: Int = 1, var isValid: Boolean = true)

    @EventHandler
    fun cancelNetherPortalsInSlip(event: PortalCreateEvent) {
        if (event.world.name == SlipgateConstants.SLIP_WORLD_NAME) {
            event.isCancelled = false
        }
    }

    @EventHandler
    fun useFlintNSteel(event: BlockPlaceEvent) {
        if (event.block.type == Material.FIRE && event.blockAgainst.type == SlipgateConstants.PORTAL_FRAME_MATERIAL && (event.block.world.name == SlipgateConstants.NETHER_WORLD_NAME || event.block.world.name == SlipgateConstants.SLIP_WORLD_NAME)) {
            val world = event.block.world
            val location = event.block.location.clone()

            val axis = isPortalFrameValid(location)

            if (axis != null) {
                makePortal(world, location, axis)
            }
        }
    }

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

    private fun isPortalFrameValid(location: Location): Axis? {
        val currentLocation = location.clone()
        var height = 0

        // handle center
        if (currentLocation.add(0.0, -1.0, 0.0).block.type == SlipgateConstants.PORTAL_FRAME_MATERIAL) {
            currentLocation.add(0.0, 1.0, 0.0)
            while (height <= SlipgateConstants.MAX_PORTAL_SIZE && (currentLocation.block.isEmpty || currentLocation.block.type == Material.FIRE)) {
                currentLocation.add(0.0, 1.0, 0.0)
                height++
            }
            if (height < SlipgateConstants.MIN_PORTAL_HEIGHT || currentLocation.block.type != SlipgateConstants.PORTAL_FRAME_MATERIAL) {
                return null
            }
        } else {
            return null
        }

        var isXValid: Boolean
        val negXWidth: Int
        val posXWidth: Int

        // handle -X
        val portalDirectionDataNegX = PortalDirectionData()
        isPortalDirectionValid(location, portalDirectionDataNegX, height, -1.0, 0.0)
        isXValid = portalDirectionDataNegX.isValid
        negXWidth = portalDirectionDataNegX.width

        // handle +X
        if (isXValid) {
            val portalDirectionDataPosX = PortalDirectionData()
            isPortalDirectionValid(location, portalDirectionDataPosX, height, 1.0, 0.0)
            isXValid = portalDirectionDataPosX.isValid
            posXWidth = portalDirectionDataPosX.width

            // negative + positive + center column
            val totalXWidth = negXWidth + posXWidth + 1
            if (totalXWidth > SlipgateConstants.MAX_PORTAL_SIZE || totalXWidth < SlipgateConstants.MIN_PORTAL_WIDTH) {
                isXValid = false
            }
        }

        var isZValid: Boolean
        val negZWidth: Int
        val posZWidth: Int

        // handle -Z
        val portalDirectionDataNegZ = PortalDirectionData()
        isPortalDirectionValid(location, portalDirectionDataNegZ, height, 0.0, -1.0)
        isZValid = portalDirectionDataNegZ.isValid
        negZWidth = portalDirectionDataNegZ.width

        // handle +Z
        if (isZValid) {
            val portalDirectionDataPosZ = PortalDirectionData()
            isPortalDirectionValid(location, portalDirectionDataPosZ, height, 0.0, 1.0)
            isZValid = portalDirectionDataPosZ.isValid
            posZWidth = portalDirectionDataPosZ.width

            // negative + positive + center column
            val totalZWidth = negZWidth + posZWidth + 1
            if (totalZWidth > SlipgateConstants.MAX_PORTAL_SIZE || totalZWidth < SlipgateConstants.MIN_PORTAL_WIDTH) {
                isZValid = false
            }
        }

        return if (isXValid) {
            Axis.X
        } else if (isZValid) {
            Axis.Z
        } else {
            null
        }
    }

    private fun isPortalDirectionValid(location: Location, portalDirectionData: PortalDirectionData, height: Int, xVal: Double, zVal: Double) {
        var currentLocation = location.clone()
        currentLocation.add(xVal, 0.0, zVal)
        while (portalDirectionData.width <= SlipgateConstants.MAX_PORTAL_SIZE && currentLocation.block.type.isEmpty && currentLocation.add(0.0, -1.0, 0.0).block.type == SlipgateConstants.PORTAL_FRAME_MATERIAL) {
            var currentHeight = 0
            currentLocation.add(0.0, 1.0, 0.0)
            val originalLocation = currentLocation.clone()
            while (currentHeight <= SlipgateConstants.MAX_PORTAL_SIZE && (currentLocation.block.isEmpty || currentLocation.block.type == Material.FIRE)) {
                currentLocation.add(0.0, 1.0, 0.0)
                currentHeight++
            }
            if (currentHeight != height || currentLocation.block.type != SlipgateConstants.PORTAL_FRAME_MATERIAL) {
                portalDirectionData.isValid = false
            }
            currentLocation = originalLocation
            portalDirectionData.width++
            currentLocation.add(xVal, 0.0, zVal)
        }
        // Check for valid portal frame wall
        if (portalDirectionData.isValid && currentLocation.block.type == SlipgateConstants.PORTAL_FRAME_MATERIAL) {
            var currentHeightLeft = height - 1
            while (portalDirectionData.isValid && currentHeightLeft > 0) {
                currentLocation.add(0.0, 1.0, 0.0)
                if (currentLocation.block.type == SlipgateConstants.PORTAL_FRAME_MATERIAL) {
                    currentHeightLeft--
                } else {
                    portalDirectionData.isValid = false
                }
            }
        } else {
            portalDirectionData.isValid = false
        }
    }

    private fun makePortal(world: World, location: Location, axis: Axis) {
        // Remove placed fire block
        world.getBlockAt(location).type = Material.AIR
        val centerLocation = location.clone()
        if (Axis.X == axis) {
            this.fillPortal(world, location, axis, centerLocation, 1.0, 0.0)

        } else if (Axis.Z == axis) {
            this.fillPortal(world, location, axis, centerLocation, 0.0, 1.0)
        }
    }

    private fun fillPortal(world: World, location: Location, axis: Axis, centerLocation: Location, x: Double, z: Double) {
        // fill center column
        this.fillPortalColumn(world, location, axis)

        // go -
        location.set(centerLocation.x, centerLocation.y, centerLocation.z)
        this.fillHorizontal(world, location, axis, -x, z)

        // go +
        location.set(centerLocation.x, centerLocation.y, centerLocation.z)
        this.fillHorizontal(world, location, axis, x, -z)
    }

    private fun fillHorizontal(world: World, location: Location, axis: Axis, x: Double, z: Double) {
        location.add(x, 0.0, z)
        while (location.block.isEmpty) {
            fillPortalColumn(world, location.clone(), axis)
            location.add(x, 0.0, z)
        }
    }

    private fun fillPortalColumn(world: World, location: Location, axis: Axis) {
        while (location.block.isEmpty) {
            val block =  world.getBlockAt(location)
            block.type = Material.NETHER_PORTAL
            val blockdata = block.blockData as Orientable
            blockdata.axis = axis
            block.blockData = blockdata
            location.add(0.0, 1.0, 0.0)
        }
    }
}