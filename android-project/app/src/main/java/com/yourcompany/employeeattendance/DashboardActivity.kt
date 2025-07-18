package com.yourcompany.employeeattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yourcompany.employeeattendance.databinding.ActivityDashboardBinding
import com.yourcompany.employeeattendance.models.PunchRequest
import com.yourcompany.employeeattendance.network.RetrofitClient
import com.yourcompany.employeeattendance.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        
        if (!preferenceManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupViews()
        updateUI()
    }

    private fun setupViews() {
        binding.btnPunchIn.setOnClickListener {
            showPunchConfirmation("IN")
        }

        binding.btnPunchOut.setOnClickListener {
            showPunchConfirmation("OUT")
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, AttendanceHistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Set toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Employee Dashboard"
    }

    private fun updateUI() {
        val user = preferenceManager.getUser()
        user?.let {
            binding.tvWelcome.text = "Welcome, ${it.name}"
            binding.tvUsername.text = "ID: ${it.username}"
            binding.tvCluster.text = "Cluster: ${it.cluster}"
            
            // Update current time
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            binding.tvCurrentTime.text = currentTime
            binding.tvCurrentDate.text = currentDate
        }
    }

    private fun showPunchConfirmation(type: String) {
        val message = if (type == "IN") {
            "Are you sure you want to Punch In?"
        } else {
            "Are you sure you want to Punch Out?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                punchAttendance(type)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun punchAttendance(type: String) {
        val user = preferenceManager.getUser() ?: return
        
        // Show loading
        val button = if (type == "IN") binding.btnPunchIn else binding.btnPunchOut
        button.isEnabled = false
        button.text = if (type == "IN") "Punching In..." else "Punching Out..."

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.punchAttendance(
                    PunchRequest(
                        userId = user.id,
                        username = user.username,
                        name = user.name,
                        cluster = user.cluster,
                        type = type,
                        timestamp = timestamp
                    )
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val message = if (type == "IN") {
                        "Punch In successful at $timestamp"
                    } else {
                        "Punch Out successful at $timestamp"
                    }
                    Toast.makeText(this@DashboardActivity, message, Toast.LENGTH_LONG).show()
                    
                    // Update button states
                    if (type == "IN") {
                        binding.btnPunchIn.isEnabled = false
                        binding.btnPunchOut.isEnabled = true
                    } else {
                        binding.btnPunchIn.isEnabled = true
                        binding.btnPunchOut.isEnabled = false
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Punch failed"
                    Toast.makeText(this@DashboardActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                button.isEnabled = true
                button.text = if (type == "IN") "Punch In" else "Punch Out"
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        preferenceManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onBackPressed() {
        // Prevent going back to login screen
        moveTaskToBack(true)
    }
}
