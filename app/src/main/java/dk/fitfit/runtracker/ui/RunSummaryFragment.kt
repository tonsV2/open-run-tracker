package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.data.db.duration
import dk.fitfit.runtracker.ui.exception.RunIdNotDefinedException
import dk.fitfit.runtracker.utils.toHHMMSS
import dk.fitfit.runtracker.viewmodels.RunSummaryViewModel
import kotlinx.android.synthetic.main.fragment_run_summary.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RunSummaryFragment : Fragment(R.layout.fragment_run_summary), OnMapReadyCallback {
    private val runSummaryViewModel by viewModel<RunSummaryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        summaryMap.onCreate(savedInstanceState)
        activity?.title = getString(R.string.run_summary_fragment_label)

        summaryMap.getMapAsync(this)

        // TODO: Will this only throw an exception if there's no arguments at all?
        val runId = arguments?.getLong(EXTRA_ID) ?: throw RunIdNotDefinedException()

        runSummaryViewModel.getRun(runId).observe(viewLifecycleOwner) {
            timeOfDay.text = "${dayTimeString(it.startDateTime.hour)} run"
            started.text = DateTimeFormatter.RFC_1123_DATE_TIME.format(it.startDateTime.atZone(ZoneId.systemDefault()))
            duration.text = it.duration().toHHMMSS()
            distance.text = "%.2f km".format(it.distance?.div(1_000))
        }

        fullscreenButton.setOnClickListener {
            val bundle = bundleOf(EXTRA_ID to runId)
            findNavController().navigate(R.id.action_RunSummaryFragment_to_MapFragment, bundle)
        }
    }

    override fun onResume() {
        super.onResume()
        summaryMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        summaryMap.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        summaryMap.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        arguments?.let { bundle ->
            val runId = bundle.getLong(EXTRA_ID)
            if (runId == 0L) throw RunIdNotDefinedException()

            runSummaryViewModel.getLocations(runId).observe(viewLifecycleOwner) { locations ->
                val latLngs = locations.map { LatLng(it.latitude, it.longitude) }
                drawRoute(map, latLngs)

                val bounds = getBounds(latLngs)
                updateCamera(map, bounds)
            }
        }
    }

    private fun drawRoute(map: GoogleMap, latLngs: List<LatLng>) {
        map.addMarker(
            MarkerOptions()
                .position(latLngs.first())
                .title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        map.addMarker(
            MarkerOptions()
                .position(latLngs.last())
                .title("End")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        map.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(latLngs)
        )
    }

    private fun updateCamera(map: GoogleMap, bounds: LatLngBounds) {
        val dip = 32f
        val padding =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.displayMetrics)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding.toInt())
        map.animateCamera(cameraUpdate)
    }

    private fun getBounds(latLngs: List<LatLng>): LatLngBounds {
        val boundsBuilder = LatLngBounds.Builder()
        latLngs.forEach {
            boundsBuilder.include(it)
        }
        return boundsBuilder.build()
    }

    private fun dayTimeString(hour: Int) = when (hour) {
        in 0..3 -> "Night"
        in 4..11 -> "Morning"
        in 12..15 -> "Afternoon"
        in 16..20 -> "Evening"
        in 21..23 -> "Night"
        else -> "Hello" // TODO: Throw exception... maybe
    }

    companion object {
        const val EXTRA_ID = "run-id"
    }
}
