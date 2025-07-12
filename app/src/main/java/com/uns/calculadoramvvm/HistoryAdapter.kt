package com.uns.calculadoramvvm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uns.calculadoramvvm.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onItemClick: (CalculationHistory) -> Unit
) : ListAdapter<CalculationHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(item: CalculationHistory) {
            binding.tvExpression.text = item.expression
            binding.tvResult.text = formatResult(item.result)
            binding.tvTime.text = dateFormat.format(Date(item.timestamp))

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun formatResult(result: Double): String {
            return when {
                result.isNaN() -> "Error"
                result.isInfinite() -> "∞"
                result == result.toLong().toDouble() -> result.toLong().toString()
                else -> String.format("%.10g", result)
            }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<CalculationHistory>() {
    override fun areItemsTheSame(oldItem: CalculationHistory, newItem: CalculationHistory): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: CalculationHistory, newItem: CalculationHistory): Boolean {
        return oldItem == newItem
    }
}
