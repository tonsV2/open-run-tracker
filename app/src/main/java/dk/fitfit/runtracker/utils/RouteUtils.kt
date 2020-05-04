package dk.fitfit.runtracker.utils

import android.location.Location
import dk.fitfit.runtracker.data.db.LocationEntity
import java.util.stream.IntStream

class RouteUtils {
    fun calculateDistance(it: List<LocationEntity>): Double {
        val results = floatArrayOf(0f)
        return IntStream
            .range(0, it.size - 1)
            .mapToDouble { i ->
                Location.distanceBetween(
                    it[i].latitude,
                    it[i].longitude,
                    it[i + 1].latitude,
                    it[i + 1].longitude,
                    results
                )
                results[0].toDouble()
            }
            .sum()
    }
}
