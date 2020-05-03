package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.fitfit.runtracker.R
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

    override fun onDestroy() {
        super.onDestroy()
        map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        val cph = LatLng(55.676098, 12.568337)

        val marker = MarkerOptions()
            .position(cph)
            .title("Hello Maps")
        map.addMarker(marker)

        val cameraPosition = CameraPosition.Builder()
            .target(cph).zoom(12f)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    companion object {
        const val EXTRA_ID = "run-id"
    }
}
