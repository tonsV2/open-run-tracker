package dk.fitfit.runtracker.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent.*
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.ui.MapFragment.Companion.EXTRA_ID
import dk.fitfit.runtracker.utils.GpxWriter
import dk.fitfit.runtracker.viewmodels.RunListViewModel
import kotlinx.android.synthetic.main.fragment_run_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class RunListFragment : Fragment(R.layout.fragment_run_list) {
    private val runListViewModel: RunListViewModel by viewModel()
    private val locationRepository: LocationRepository by inject()
    private val gpxWriter: GpxWriter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.run_list_fragment_label)

        runRecyclerView.layoutManager = LinearLayoutManager(context)
        runRecyclerView.setHasFixedSize(true)

        val adapter = RunListAdapter {
            val bundle = bundleOf(EXTRA_ID to it.id)
            findNavController().navigate(R.id.action_RunListFragment_to_RunSummaryFragment, bundle)
        }

        adapter.onItemLongClickListener = {
            itemLongClick(it)
        }

        runRecyclerView.adapter = adapter

        runListViewModel.runs.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                runRecyclerView.visibility = GONE
                emptyView.visibility = VISIBLE
            } else {
                runRecyclerView.visibility = VISIBLE
                emptyView.visibility = GONE
                adapter.submitList(it)
            }
        }
    }

    private fun itemLongClick(run: RunEntity) {
        val runId = run.id
        val items = arrayOf<CharSequence>("Delete", "Export as GPX file")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setItems(items) { _, item ->
            when(item) {
                0 -> deleteRun(runId)
                1 -> shareAsGpx(runId)
            }
        }
        builder.show()
    }

    private fun shareAsGpx(runId: Long) {
        CoroutineScope(IO).launch {
            val locations = locationRepository.getLocations(runId)
            val file = gpxWriter.toFile(requireContext().cacheDir, "exported_$runId.gpx", locations)
            val uri = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", file)

            val shareIntent = ShareCompat.IntentBuilder.from(activity as Activity).intent.apply {
                action = ACTION_SEND
                putExtra(EXTRA_STREAM, uri)
                type = "application/gpx+xml"
                addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(uri, type)
            }

            startActivity(createChooser(shareIntent, "resources.getText(R.string.send_to)"))
        }
    }

    private fun deleteRun(runId: Long) {
        AlertDialog.Builder(context)
            .setMessage("Confirm deletion?")
            .setPositiveButton(android.R.string.yes) { _, _ ->
                runListViewModel.delete(runId)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }
}
