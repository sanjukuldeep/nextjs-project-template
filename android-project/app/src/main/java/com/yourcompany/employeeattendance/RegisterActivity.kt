package com.yourcompany.employeeattendance

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yourcompany.employeeattendance.databinding.ActivityRegisterBinding
import com.yourcompany.employeeattendance.models.RegisterRequest
import com.yourcompany.employeeattendance.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnRegister.setOnClickListener {
            register()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }

        // Set toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register New User"
    }

    private fun register() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val cluster = binding.etCluster.text.toString().trim()
        val phoneNumber = binding.etPhone.text.toString().trim()

        // Validation
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || 
            cluster.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate username range (5001-6000)
        val userId = username.toIntOrNull()
        if (userId == null || userId !in 5001..6000) {
            Toast.makeText(this, "Username must be between 5001-6000", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate phone number
        if (phoneNumber.length < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(username, password, name, cluster, phoneNumber)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@RegisterActivity, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "Registration failed"
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Register"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
