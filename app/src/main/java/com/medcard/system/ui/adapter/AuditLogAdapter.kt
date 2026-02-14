package com.medcard.system.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medcard.system.R
import com.medcard.system.model.AuditAction
import com.medcard.system.model.AuditLog
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения записей журнала аудита
 */
class AuditLogAdapter : ListAdapter<AuditLog, AuditLogAdapter.AuditLogViewHolder>(AuditLogDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audit_log, parent, false)
        return AuditLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuditLogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AuditLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAction: TextView = itemView.findViewById(R.id.tvAction)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        private val tvUser: TextView = itemView.findViewById(R.id.tvUser)

        fun bind(log: AuditLog) {
            tvAction.text = getActionDisplayName(log.action)
            tvAction.setBackgroundColor(getActionColor(log.action))
            tvTimestamp.text = dateFormat.format(log.timestamp)
            tvDetails.text = log.details
            tvUser.text = "Пользователь: ${log.userName}"
        }

        private fun getActionDisplayName(action: AuditAction): String {
            return when (action) {
                AuditAction.VIEW -> "ПРОСМОТР"
                AuditAction.CREATE -> "СОЗДАНИЕ"
                AuditAction.UPDATE -> "ИЗМЕНЕНИЕ"
                AuditAction.DELETE -> "УДАЛЕНИЕ"
                AuditAction.LOGIN -> "ВХОД"
                AuditAction.LOGOUT -> "ВЫХОД"
                AuditAction.EXPORT -> "ЭКСПОРТ"
                AuditAction.PRINT -> "ПЕЧАТЬ"
                AuditAction.SHARE -> "ОБЩИЙ ДОСТУП"
                AuditAction.SEARCH -> "ПОИСК"
            }
        }

        private fun getActionColor(action: AuditAction): Int {
            return when (action) {
                AuditAction.CREATE -> Color.parseColor("#4CAF50")
                AuditAction.UPDATE -> Color.parseColor("#FF9800")
                AuditAction.DELETE -> Color.parseColor("#F44336")
                AuditAction.LOGIN -> Color.parseColor("#2196F3")
                AuditAction.LOGOUT -> Color.parseColor("#9E9E9E")
                AuditAction.VIEW -> Color.parseColor("#1976D2")
                AuditAction.SEARCH -> Color.parseColor("#607D8B")
                AuditAction.EXPORT -> Color.parseColor("#9C27B0")
                AuditAction.PRINT -> Color.parseColor("#795548")
                AuditAction.SHARE -> Color.parseColor("#00BCD4")
            }
        }
    }

    class AuditLogDiffCallback : DiffUtil.ItemCallback<AuditLog>() {
        override fun areItemsTheSame(oldItem: AuditLog, newItem: AuditLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AuditLog, newItem: AuditLog): Boolean {
            return oldItem == newItem
        }
    }
}
