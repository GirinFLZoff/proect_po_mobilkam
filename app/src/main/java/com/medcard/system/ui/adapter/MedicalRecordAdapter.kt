package com.medcard.system.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medcard.system.R
import com.medcard.system.model.MedicalRecord
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения записей истории болезни
 */
class MedicalRecordAdapter :
    ListAdapter<MedicalRecord, MedicalRecordAdapter.RecordViewHolder>(RecordDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvRecordDate)
        private val tvDoctor: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvDiagnosis: TextView = itemView.findViewById(R.id.tvDiagnosis)
        private val tvComplaints: TextView = itemView.findViewById(R.id.tvComplaints)
        private val tvTreatment: TextView = itemView.findViewById(R.id.tvTreatment)
        private val tvIcd10: TextView = itemView.findViewById(R.id.tvIcd10)

        fun bind(record: MedicalRecord) {
            tvDate.text = dateFormat.format(record.date)
            tvDoctor.text = if (record.doctorName.isNotBlank()) "Врач: ${record.doctorName}" else ""
            tvDiagnosis.text = "Диагноз: ${record.diagnosis}"

            if (record.complaints.isNotBlank()) {
                tvComplaints.text = "Жалобы: ${record.complaints}"
                tvComplaints.visibility = View.VISIBLE
            } else {
                tvComplaints.visibility = View.GONE
            }

            if (record.treatment.isNotBlank()) {
                tvTreatment.text = "Лечение: ${record.treatment}"
                tvTreatment.visibility = View.VISIBLE
            } else {
                tvTreatment.visibility = View.GONE
            }

            if (record.icd10Code.isNotBlank()) {
                tvIcd10.text = "МКБ-10: ${record.icd10Code}"
                tvIcd10.visibility = View.VISIBLE
            } else {
                tvIcd10.visibility = View.GONE
            }
        }
    }

    class RecordDiffCallback : DiffUtil.ItemCallback<MedicalRecord>() {
        override fun areItemsTheSame(oldItem: MedicalRecord, newItem: MedicalRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MedicalRecord, newItem: MedicalRecord): Boolean {
            return oldItem == newItem
        }
    }
}
