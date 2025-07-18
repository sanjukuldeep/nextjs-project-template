package com.yourcompany.employeeattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yourcompany.employeeattendance.databinding.ActivityLoginBinding
import com.yourcompany.employeeattendance.models.LoginRequest
import com.yourcompany.employeeattendance.network.RetrofitClient
import com.yourcompany.employeeattendance.utils.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Set default values for testing
        binding.etUsername.setText("5001")
        binding.etPassword.setText("kuldeep singh")
    }

    private fun login() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate username range (5001-6000)
        val userId = username.toIntOrNull()
        if (userId == null || userId !in 5001..6000) {
            Toast.makeText(this, "Username must be between 5001-6000", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    user?.let {
                        preferenceManager.saveUser(it)
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Invalid credentials"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Close the app completely
    }
}
