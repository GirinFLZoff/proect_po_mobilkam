package com.medcard.system.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medcard.system.R
import com.medcard.system.model.Patient
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения списка пациентов
 */
class PatientAdapter(
    private val onItemClick: (Patient) -> Unit,
    private val onEditClick: (Patient) -> Unit,
    private val onDeleteClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = getItem(position)
        holder.bind(patient)
    }
    
    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.patientName)
        private val infoTextView: TextView = itemView.findViewById(R.id.patientInfo)
        private val dateTextView: TextView = itemView.findViewById(R.id.patientDate)
        private val editButton: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
        
        fun bind(patient: Patient) {
            nameTextView.text = patient.getFullName()
            
            val info = buildString {
                append("Возраст: ${patient.getAge()} лет")
                if (patient.phone.isNotEmpty()) {
                    append(" • Тел: ${patient.phone}")
                }
            }
            infoTextView.text = info
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateTextView.text = "Создано: ${dateFormat.format(patient.createdAt)}"
            
            itemView.setOnClickListener {
                onItemClick(patient)
            }
            
            editButton.setOnClickListener {
                onEditClick(patient)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(patient)
            }
        }
    }
    
    class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}
