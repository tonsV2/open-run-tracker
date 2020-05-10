package dk.fitfit.runtracker.ui

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.ui.MapFragment.Companion.EXTRA_ID
import dk.fitfit.runtracker.viewmodels.RunListViewModel
import kotlinx.android.synthetic.main.fragment_run_list.*
import org.koin.android.viewmodel.ext.android.viewModel

class RunListFragment : Fragment(R.layout.fragment_run_list) {
    private val runListViewModel: RunListViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.run_list_fragment_label)

        runRecyclerView.layoutManager = LinearLayoutManager(context)
        runRecyclerView.setHasFixedSize(true)

        val adapter = RunListAdapter {
            val bundle = bundleOf(EXTRA_ID to it.id)
            findNavController().navigate(R.id.action_RunListFragment_to_RunSummaryFragment, bundle)
        }

        runRecyclerView.adapter = adapter

        runListViewModel.runs.observe(viewLifecycleOwner) {
            if(it.isEmpty()) {
                runRecyclerView.visibility = GONE
                emptyView.visibility = VISIBLE
            } else {
                runRecyclerView.visibility = VISIBLE
                emptyView.visibility = GONE
                adapter.submitList(it)
            }
        }
    }
}
