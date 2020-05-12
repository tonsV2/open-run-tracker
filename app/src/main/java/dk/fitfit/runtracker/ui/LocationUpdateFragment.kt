package dk.fitfit.runtracker.ui

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.utils.hasPermission
import dk.fitfit.runtracker.viewmodels.LocationUpdateViewModel
import dk.fitfit.runtracker.viewmodels.LocationUpdateViewModel.EndOfRunStatus
import kotlinx.android.synthetic.main.fragment_location_update.*
import org.koin.android.viewmodel.ext.android.viewModel

class LocationUpdateFragment : Fragment(R.layout.fragment_location_update) {
    private val locationUpdateViewModel: LocationUpdateViewModel by viewModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (!context.hasPermission(ACCESS_FINE_LOCATION) || !context.hasPermission(ACCESS_BACKGROUND_LOCATION)) {
            findNavController().navigate(R.id.action_LocationUpdateFragment_to_PermissionRequestFragment)
        }

        createNotificationChannel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.app_name)

        fab.setOnClickListener {
            val runId = locationUpdateViewModel.runId
            val bundle = bundleOf(MapFragment.EXTRA_ID to runId)
            findNavController().navigate(R.id.action_LocationUpdateFragment_to_FullscreenMapFragment, bundle)
        }

        val chartColors = getChartColors()
        initializeChart(chartColors)

        locationUpdateViewModel.lapProgress.observe(viewLifecycleOwner) {
            updateChart(it, chartColors)
        }

        locationUpdateViewModel.durationString.observe(viewLifecycleOwner) {
            duration.text = it
        }

        locationUpdateViewModel.distance.observe(viewLifecycleOwner) {
            distance.text = it
        }

        locationUpdateViewModel.speedString.observe(viewLifecycleOwner) {
            speed.text = it
        }

        locationUpdateViewModel.receivingLocationUpdates.observe(viewLifecycleOwner) {
            button_start.isEnabled = !it
            button_stop.isEnabled = it

            button_run_list_enabled.visibility = when (it) {
                false -> VISIBLE
                true -> GONE
            }
            button_run_list_disabled.visibility = when (it) {
                false -> GONE
                true -> VISIBLE
            }

            fab.visibility = when (it) {
                false -> GONE
                true -> VISIBLE
            }

            if (it) {
                showNotification()
            } else {

            }
        }

        button_start.setOnClickListener {
            locationUpdateViewModel.startLocationUpdates()
        }

        button_stop.setOnClickListener {
            Toast.makeText(context, "Long press to end run!", Toast.LENGTH_SHORT).show()
        }

        button_stop.setOnLongClickListener {
            locationUpdateViewModel.stopLocationUpdates()
            true
        }

        locationUpdateViewModel.endOfRunStatus.observe(viewLifecycleOwner) {
            when (it) {
                is EndOfRunStatus.Success -> {
                    val bundle = bundleOf(MapFragment.EXTRA_ID to it.runId)
                    findNavController().navigate(R.id.action_LocationUpdateFragment_to_RunSummaryFragment, bundle)
                }
                is EndOfRunStatus.Exceptional -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
            locationUpdateViewModel.endOfRunStatus.postValue(null)
        }

        button_run_list_enabled.setOnClickListener {
            findNavController().navigate(R.id.action_LocationUpdateFragment_to_RunListFragment)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "getString(R.string.channel_name)"
            val descriptionText = "getString(R.string.channel_description)"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
//                description = descriptionText
            }
            val c = context
            if (c != null) {
                val notificationManager = NotificationManagerCompat.from(c)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun showNotification() {
        val notificationId = 0
        val c = context
        if (c != null)  {
            val builder = NotificationCompat.Builder(c, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("textTitle")
                .setContentText("textContent")
                .setPriority(PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(c)) {
                notify(notificationId, builder.build())
            }
        }
    }

    private fun initializeChart(chartColors: List<Int>) {
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)

        pieChart.setDrawEntryLabels(false)
        pieChart.legend.isEnabled = false
        pieChart.description = null

        updateChart(0, chartColors)
    }

    private fun updateChart(it: Int, chartColors: List<Int>) {
        val entries = listOf(
            PieEntry(it.toFloat()),
            PieEntry((1_000 - it).toFloat())
        )

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.colors = chartColors

        val data = PieData(pieDataSet)
        data.setDrawValues(false)
        pieChart.data = data

        pieChart.invalidate()
    }

    private fun getChartColors(): List<Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            listOf(
                resources.getColor(R.color.button_background_disabled, null),
                resources.getColor(R.color.primary_text, null)
            )
        } else {
            listOf(
                resources.getColor(R.color.button_background_disabled),
                resources.getColor(R.color.primary_text)
            )
        }
    }
}
