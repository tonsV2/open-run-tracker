package dk.fitfit.runtracker.ui

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.hasPermission
import dk.fitfit.runtracker.viewmodels.LocationUpdateViewModel
import kotlinx.android.synthetic.main.fragment_first.*
import org.koin.android.viewmodel.ext.android.viewModel

class LocationUpdateFragment : Fragment(R.layout.fragment_first) {
    private val locationUpdateViewModel: LocationUpdateViewModel by viewModel()
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (!context.hasPermission(ACCESS_FINE_LOCATION) || !context.hasPermission(ACCESS_BACKGROUND_LOCATION)) {
            findNavController().navigate(R.id.action_FirstFragment_to_PermissionRequestFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.duration.observe(viewLifecycleOwner) {
            duration.text = it
        }

        locationUpdateViewModel.distance.observe(viewLifecycleOwner) {
            distance.text = it.toString()
        }

        locationUpdateViewModel.speed.observe(viewLifecycleOwner) {
            speed.text = it
        }

/*
        locationUpdateViewModel.locations.observe(viewLifecycleOwner) {
            Log.d(TAG, "Size: ${it.size}")
            it.forEach {
                Log.d(TAG, "${it.runId}:${it.longitude} - ${it.latitude}")
            }
        }
*/
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

        button_run_list.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_RunListFragment)
        }
    }
}
