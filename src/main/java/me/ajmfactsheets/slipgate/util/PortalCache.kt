package me.ajmfactsheets.slipgate.util

import me.ajmfactsheets.slipgate.constants.SlipgateConstants
import org.bukkit.Location

object PortalCache {

    private data class CacheEntry(
        val location: Location,
        var ttl: Int
    )

    private var overworld2NetherPortalCache = mutableListOf<CacheEntry>()
    private var nether2OverworldPortalCache = mutableListOf<CacheEntry>()
    private var nether2SlipPortalCache = mutableListOf<CacheEntry>()
    private var slip2NetherPortalCache = mutableListOf<CacheEntry>()

    fun getCacheEntry(fromWorld: String, toWorld: String, searchLocation: Location, radius: Int): Location? {
        val minX = searchLocation.x - radius
        val maxX = searchLocation.x + radius
        val minZ = searchLocation.z - radius
        val maxZ = searchLocation.z + radius

        val cache = findCache(fromWorld, toWorld)

        // Fuzzy equality based on min and max X/Z values
        cache?.forEach {
            if (it.location.x in minX .. maxX && it.location.z in minZ .. maxZ) {
                return it.location
            }
        }
        return null
    }

    fun addCacheEntry(fromWorld: String, toWorld: String, location: Location) {
        val cache = findCache(fromWorld, toWorld)

        // If value is already in cache, do not add again
        cache?.forEach {
            if (it.location.x == location.x && it.location.z == location.z) {
                it.ttl = SlipgateConstants.CACHE_TTL // Reset TTL since location is still in active use
                return
            }
        }

        cache?.add(CacheEntry(location, SlipgateConstants.CACHE_TTL))
    }

    fun tickCacheEntries() {
        updateCache(overworld2NetherPortalCache)
        updateCache(nether2OverworldPortalCache)
        updateCache(nether2SlipPortalCache)
        updateCache(slip2NetherPortalCache)
    }

    private fun updateCache(cache: MutableList<CacheEntry>) {
        cache.forEach {
            --it.ttl
        }
        cache.removeAll {
            it.ttl < 1
        }
    }

    private fun findCache(fromWorld: String, toWorld: String): MutableList<CacheEntry>? {
        var cache: MutableList<CacheEntry>? = null

        if (fromWorld == SlipgateConstants.OVERWORLD_WORLD_NAME) {
            cache = overworld2NetherPortalCache
        } else if (fromWorld == SlipgateConstants.NETHER_WORLD_NAME) {
            if (toWorld == SlipgateConstants.OVERWORLD_WORLD_NAME) {
                cache = nether2OverworldPortalCache
            } else if (toWorld == SlipgateConstants.SLIP_WORLD_NAME) {
                cache = nether2SlipPortalCache
            }
        } else if (fromWorld == SlipgateConstants.SLIP_WORLD_NAME){
            cache = slip2NetherPortalCache
        }
        return cache
    }
}