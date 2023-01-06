package me.ajmfactsheets.slipgate.constants

import org.bukkit.Material

object SlipgateConstants {

    const val MIN_PORTAL_WIDTH = 2
    const val MIN_PORTAL_HEIGHT = 3
    const val MAX_PORTAL_SIZE = 23
    val SLIPGATE_MATERIAL = Material.CRYING_OBSIDIAN
    val NETHER_PORTAL_MATERIAL = Material.OBSIDIAN
    const val OVERWORLD_WORLD_NAME = "world"
    const val NETHER_WORLD_NAME = "world_nether"
    const val DEFAULT_SLIP_WORLD_NAME =  "world_slipgate_the_slip"
    // Not ideal that these can be modified in code
    var SLIP_WORLD_NAME = "world_slipgate_the_slip"
    var MIN_HEIGHT_MODIFIER = 2
    var MAX_HEIGHT_MODIFIER = 5
    var PORTALS_SEARCH_TOP_TO_BOTTOM = false

    var CACHE_TTL = 5 // In minutes
}