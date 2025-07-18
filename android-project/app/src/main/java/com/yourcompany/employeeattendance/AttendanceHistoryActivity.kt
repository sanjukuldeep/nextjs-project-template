package com.yourcompany.employeeattendance

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.employeeattendance.adapters.AttendanceAdapter
import com.yourcompany.employeeattendance.databinding.ActivityAttendanceHistoryBinding
import com.yourcompany.employeeattendance.models.AttendanceRecord
import com.yourcompany.employeeattendance.network.RetrofitClient
import com.yourcompany.employeeattendance.utils.PreferenceManager
import kotlinx.coroutines.launch

class AttendanceHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceHistoryBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var attendanceAdapter: AttendanceAdapter
    private val attendanceList = mutableListOf<AttendanceRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        setupViews()
        loadAttendanceHistory()
    }

    private fun setupViews() {
        // Set toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Attendance History"

        // Setup RecyclerView
        attendanceAdapter = AttendanceAdapter(attendanceList)
        binding.recyclerViewAttendance.apply {
            layoutManager = LinearLayoutManager(this@AttendanceHistoryActivity)
            adapter = attendanceAdapter
        }

        // Refresh button
        binding.btnRefresh.setOnClickListener {
            loadAttendanceHistory()
        }
    }

    private fun loadAttendanceHistory() {
        val userId = preferenceManager.getUserId()
        if (userId == 0) return

        // Show loading
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnRefresh.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserAttendance(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val records = response.body()?.records ?: emptyList()
                    attendanceList.clear()
                    attendanceList.addAll(records.sortedByDescending { it.date })
                    attendanceAdapter.notifyDataSetChanged()
                    
                    if (attendanceList.isEmpty()) {
                        binding.tvNoData.visibility = android.view.View.VISIBLE
                        binding.recyclerViewAttendance.visibility = android.view.View.GONE
                    } else {
                        binding.tvNoData.visibility = android.view.View.GONE
                        binding.recyclerViewAttendance.visibility = android.view.View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@AttendanceHistoryActivity, "Failed to load attendance history", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AttendanceHistoryActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnRefresh.isEnabled = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
