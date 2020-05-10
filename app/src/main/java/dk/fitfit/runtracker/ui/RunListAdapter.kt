package dk.fitfit.runtracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import dk.fitfit.runtracker.R
import dk.fitfit.runtracker.data.db.RunEntity
import dk.fitfit.runtracker.utils.toHHMMSS
import kotlinx.android.synthetic.main.run_item.view.*
import java.time.Duration

class RunListAdapter(private val onItemClickListener: (RunEntity) -> Unit) : ListAdapter<RunEntity, RunListAdapter.RunHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.run_item, parent, false)
        return RunHolder(itemView)
    }

    override fun onBindViewHolder(holder: RunHolder, position: Int) {
        val run = getItem(position)

        val description = if (run.distance != null && run.endDataTime != null) {
            val distance = "%.2f km".format(run.distance?.div(1_000))
            val durationString = Duration.between(run.startDateTime, run.endDataTime).toHHMMSS()

            "$distance run done in $durationString"
        } else {
            ""
        }

        holder.runName.text = run.startDateTime.toString()
        holder.runDescription.text = description
    }

    inner class RunHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val runName: TextView = itemView.runName
        val runDescription: TextView = itemView.runDescription

        init {
            itemView.setOnClickListener {
                if (adapterPosition != NO_POSITION) {
                    onItemClickListener(getItem(adapterPosition))
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RunEntity>() {
            override fun areItemsTheSame(oldItem: RunEntity, newItem: RunEntity) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RunEntity, newItem: RunEntity) = oldItem.startDateTime == newItem.startDateTime
                    && oldItem.endDataTime == newItem.endDataTime
        }
    }
}
