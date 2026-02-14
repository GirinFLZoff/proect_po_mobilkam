package com.medcard.system.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medcard.system.R
import com.medcard.system.model.LabResult
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения результатов анализов
 */
class LabResultAdapter :
    ListAdapter<LabResult, LabResultAdapter.LabResultViewHolder>(LabResultDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lab_result, parent, false)
        return LabResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LabResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTestName: TextView = itemView.findViewById(R.id.tvTestName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvLabDate)
        private val tvResult: TextView = itemView.findViewById(R.id.tvResult)
        private val tvReference: TextView = itemView.findViewById(R.id.tvReference)
        private val tvLaboratory: TextView = itemView.findViewById(R.id.tvLaboratory)

        fun bind(labResult: LabResult) {
            tvTestName.text = labResult.testName
            tvDate.text = dateFormat.format(labResult.date)

            val resultText = buildString {
                append("Результат: ${labResult.result}")
                if (labResult.unit.isNotBlank()) {
                    append(" ${labResult.unit}")
                }
            }
            tvResult.text = resultText

            if (labResult.referenceRange.isNotBlank()) {
                tvReference.text = "Норма: ${labResult.referenceRange}${if (labResult.unit.isNotBlank()) " ${labResult.unit}" else ""}"
                tvReference.visibility = View.VISIBLE
            } else {
                tvReference.visibility = View.GONE
            }

            if (labResult.laboratoryName.isNotBlank()) {
                tvLaboratory.text = "Лаборатория: ${labResult.laboratoryName}"
                tvLaboratory.visibility = View.VISIBLE
            } else {
                tvLaboratory.visibility = View.GONE
            }
        }
    }

    class LabResultDiffCallback : DiffUtil.ItemCallback<LabResult>() {
        override fun areItemsTheSame(oldItem: LabResult, newItem: LabResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LabResult, newItem: LabResult): Boolean {
            return oldItem == newItem
        }
    }
}
