package dk.fitfit.runtracker.utils

import java.time.Duration

fun Duration.toHHMMSS(): String {
    val seconds = this.toMillis() / 1_000
    return "%02d:%02d:%02d".format(
        seconds / 3600,
        (seconds % 3600) / 60,
        (seconds % 60)
    )
}
