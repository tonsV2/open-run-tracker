package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dk.fitfit.runtracker.BuildConfig
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.data.db.LocationEntity
import dk.fitfit.runtracker.viewmodels.RunListViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import org.koin.android.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {
    private val runListViewModel: RunListViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map.onCreate(savedInstanceState)
        activity?.title = getString(R.string.map_fragment_label)

        map.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        arguments?.let { bundle ->
            val runId = bundle.getLong(EXTRA_ID)

            runListViewModel.getLocations(runId).observe(viewLifecycleOwner) { locations ->
                if (BuildConfig.DEBUG) {
                    drawDebugRoute(map, locations)
                }

                val latLngs = locations.map { LatLng(it.latitude, it.longitude) }
                drawRoute(map, latLngs)

                val bounds = getBounds(latLngs)
                updateCamera(map, bounds)
            }
        }
    }

    private fun drawDebugRoute(map: GoogleMap, locations: List<LocationEntity>) {
        locations.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title("Id(${it.id}), accuracy(${it.accuracy}), verticalAccuracy(${it.verticalAccuracy})")
                    .icon(defaultMarker(HUE_ORANGE))
            )
        }
    }

    private fun drawRoute(map: GoogleMap, latLngs: List<LatLng>) {
        map.addMarker(
            MarkerOptions()
                .position(latLngs.first())
                .title("Start")
                .icon(defaultMarker(HUE_GREEN))
        )

        map.addMarker(
            MarkerOptions()
                .position(latLngs.last())
                .title("End")
                .icon(defaultMarker(HUE_RED))
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

    companion object {
        const val EXTRA_ID = "run-id"
    }
}
