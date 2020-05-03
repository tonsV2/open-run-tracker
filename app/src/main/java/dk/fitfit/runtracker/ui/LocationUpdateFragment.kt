package dk.fitfit.runtracker.ui

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
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

        locationUpdateViewModel.receivingLocationUpdates.observe(viewLifecycleOwner) {
            button_start.isEnabled = !it
        }

        button_start.setOnClickListener {
            locationUpdateViewModel.startLocationUpdates()
        }

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }
}
