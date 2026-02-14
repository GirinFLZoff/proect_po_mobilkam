package com.medcard.system.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medcard.system.R
import com.medcard.system.repository.PatientRepository
import com.medcard.system.security.EncryptionManager
import com.medcard.system.ui.adapter.AuditLogAdapter
import kotlinx.coroutines.launch

/**
 * Активность для просмотра журнала аудита
 */
class AuditLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AuditLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audit_log)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Журнал аудита"

        recyclerView = findViewById(R.id.auditRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = AuditLogAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadAuditLogs()
    }

    private fun loadAuditLogs() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val repository = PatientRepository(EncryptionManager(this))

        lifecycleScope.launch {
            val result = repository.getAuditLogs(200)

            result.onSuccess { logs ->
                progressBar.visibility = View.GONE
                if (logs.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                    adapter.submitList(logs)
                }
            }.onFailure { exception ->
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Ошибка загрузки журнала"
                Toast.makeText(this@AuditLogActivity, "Ошибка: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
