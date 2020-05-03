package dk.fitfit.runtracker.ui

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.hasPermission
import dk.fitfit.runtracker.viewmodels.LocationUpdateViewModel
import kotlinx.android.synthetic.main.fragment_first.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class LocationUpdateFragment : Fragment(R.layout.fragment_first) {
    private val locationUpdateViewModel: LocationUpdateViewModel by viewModel()
    private val TAG = "LocationUpdateFragment"
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (!context.hasPermission(ACCESS_FINE_LOCATION) || !context.hasPermission(ACCESS_BACKGROUND_LOCATION)) {
            findNavController().navigate(R.id.action_FirstFragment_to_PermissionRequestFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.currentRun.observe(viewLifecycleOwner) {
                duration.text = it?.startDateTime?.toString()
        }

        locationUpdateViewModel.locations.observe(viewLifecycleOwner) {
            Log.d(TAG, "Size: ${it.size}")
            it.forEach {
                Log.d(TAG, "${it.runId}:${it.longitude} - ${it.latitude}")
            }
        }

        locationUpdateViewModel.receivingLocationUpdates.observe(viewLifecycleOwner) {
            button_start.isEnabled = !it
        }

        locationUpdateViewModel.receivingLocationUpdates.observe(viewLifecycleOwner) {
            button_stop.isEnabled = it
        }

        button_start.setOnClickListener {
            locationUpdateViewModel.startLocationUpdates()
        }

        button_stop.setOnClickListener {
            locationUpdateViewModel.stopLocationUpdates()
        }
    }

    fun mduration(dateTime: LocalDateTime): String {
        val fromDateTime = dateTime
        val toDateTime = LocalDateTime.now()

        var tempDateTime: LocalDateTime = fromDateTime

        val years: Long = tempDateTime.until(toDateTime, ChronoUnit.YEARS)
        tempDateTime = tempDateTime.plusYears(years)

        val months: Long = tempDateTime.until(toDateTime, ChronoUnit.MONTHS)
        tempDateTime = tempDateTime.plusMonths(months)

        val days: Long = tempDateTime.until(toDateTime, ChronoUnit.DAYS)
        tempDateTime = tempDateTime.plusDays(days)


        val hours: Long = tempDateTime.until(toDateTime, ChronoUnit.HOURS)
        tempDateTime = tempDateTime.plusHours(hours)

        val minutes: Long = tempDateTime.until(toDateTime, ChronoUnit.MINUTES)
        tempDateTime = tempDateTime.plusMinutes(minutes)

        val seconds: Long = tempDateTime.until(toDateTime, ChronoUnit.SECONDS)

        return years.toString() + " years " +
                    months + " months " +
                    days + " days " +
                    hours + " hours " +
                    minutes + " minutes " +
                    seconds + " seconds."
    }
}
