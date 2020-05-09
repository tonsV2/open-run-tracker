package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.data.db.duration
import dk.fitfit.runtracker.ui.exception.RunIdNotDefinedException
import dk.fitfit.runtracker.utils.toHHMMSS
import kotlinx.android.synthetic.main.fragment_run_summary.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RunSummaryFragment : MapFragment(R.layout.fragment_run_summary) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.run_summary_fragment_label)

        val runId = arguments?.getLong(EXTRA_ID)
        if (runId == null || runId == 0L) throw RunIdNotDefinedException()

        runSummaryViewModel.getRun(runId).observe(viewLifecycleOwner) {
            timeOfDay.text = "${dayTimeString(it.startDateTime.hour)} run"
            started.text = DateTimeFormatter.RFC_1123_DATE_TIME.format(it.startDateTime.atZone(ZoneId.systemDefault()))
            durationValue.text = it.duration().toHHMMSS()
            distanceValue.text = "%.2f km".format(it.distance?.div(1_000))
        }

        fullscreenButton.setOnClickListener {
            val bundle = bundleOf(EXTRA_ID to runId)
            findNavController().navigate(R.id.action_RunSummaryFragment_to_FullscreenMapFragment, bundle)
        }
    }

    private fun dayTimeString(hour: Int) = when (hour) {
        in 0..3 -> "Night"
        in 4..11 -> "Morning"
        in 12..15 -> "Afternoon"
        in 16..20 -> "Evening"
        in 21..23 -> "Night"
        else -> "Hello" // TODO: Throw exception... maybe
    }
}
