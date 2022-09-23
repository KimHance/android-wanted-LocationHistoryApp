package com.preonboarding.locationhistory.feature.history.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.preonboarding.locationhistory.R
import com.preonboarding.locationhistory.data.entity.History
import com.preonboarding.locationhistory.databinding.ItemHistoryBinding

class HistoryListAdapter(
    private val itemClickListener: (History) -> Unit
) : ListAdapter<History, HistoryListAdapter.HistoryViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_history,
                parent,
                false
            ),
            itemClickListener
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
        private val itemClickListener: (History) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                itemView.setOnClickListener {
                    history?.let {
                        itemClickListener(it)
                    }
                }
            }
        }

        fun bind(item: History) {
            binding.apply {
                history = item
                executePendingBindings()
            }
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<History>() {
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean =
                oldItem.time == newItem.time

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean =
                oldItem == newItem
        }
    }
}