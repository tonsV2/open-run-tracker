package dk.fitfit.runtracker.utils

import dk.fitfit.runtracker.data.db.LocationEntity
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Track
import io.jenetics.jpx.TrackSegment
import java.io.File
import java.time.ZoneOffset

class GpxWriter {
    fun toFile(outputDir: File, filename: String, locations: List<LocationEntity>): File {
        val gpx = GPX.builder()
            .creator("Open Run Tracker")
            .addTrack { track: Track.Builder ->
                track.addSegment { segment: TrackSegment.Builder ->
                    locations.forEach { location ->
                        segment.addPoint { it
                            .lat(location.latitude)
                            .lon(location.longitude)
                            .ele(location.altitude)
                            .time(location.dateTime.toInstant(ZoneOffset.UTC)) }
                    }
                }
            }
            .build()

        val outputFile = File(outputDir, filename)

        GPX.write(gpx, outputFile.outputStream())

        return outputFile
    }
}
