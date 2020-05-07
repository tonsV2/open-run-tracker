package dk.fitfit.runtracker.utils

import android.location.Location
import dk.fitfit.runtracker.data.db.LocationEntity
import java.util.stream.IntStream

class RouteUtils {
    fun calculateDistance(locations: List<LocationEntity>): Double {
        val results = floatArrayOf(0f)
        return IntStream
            .range(0, locations.size - 1)
            .mapToDouble { i ->
                Location.distanceBetween(
                    locations[i].latitude,
                    locations[i].longitude,
                    locations[i + 1].latitude,
                    locations[i + 1].longitude,
                    results
                )
                results[0].toDouble()
            }
            .sum()
    }
}
