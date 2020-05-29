package dk.fitfit.runtracker.utils

import android.location.Location
import dk.fitfit.runtracker.data.db.LocationEntity
import java.util.stream.IntStream
import kotlin.math.absoluteValue

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

    fun calculateascent(locations: List<LocationEntity>): Double = IntStream
        .range(0, locations.size - 1)
        .mapToDouble { i ->
            val altitudeDelta = locations[i].altitude - locations[i + 1].altitude
            if (altitudeDelta > 0) { altitudeDelta } else { 0.0 }
        }
        .sum()

    fun calculateDescent(locations: List<LocationEntity>): Double = IntStream
        .range(0, locations.size - 1)
        .mapToDouble { i ->
            val altitudeDelta = locations[i].altitude - locations[i + 1].altitude
            if (altitudeDelta < 0) { altitudeDelta } else { 0.0 }
        }
        .sum()
        .absoluteValue
}
