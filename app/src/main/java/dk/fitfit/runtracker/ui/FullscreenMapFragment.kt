package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.view.View
import dk.fitfit.runtracker.R

class FullscreenMapFragment : MapFragment(R.layout.fragment_map) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.map_fragment_label)
    }
}
