package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dk.fitfit.runtracker.R
import kotlinx.android.synthetic.main.fragment_run_summary.*

private const val TAG = "RunSummaryFragment"

class RunSummaryFragment : Fragment(R.layout.fragment_run_summary) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var runId = 0L

        arguments?.let { bundle ->
            runId = bundle.getLong(EXTRA_ID)
        }

        name.text = "RunId: $runId"

        map.setOnClickListener {
            val bundle = bundleOf(EXTRA_ID to runId)
            findNavController().navigate(R.id.action_RunSummaryFragment_to_MapFragment, bundle)
        }
    }

    companion object {
        const val EXTRA_ID = "run-id"
    }
}
