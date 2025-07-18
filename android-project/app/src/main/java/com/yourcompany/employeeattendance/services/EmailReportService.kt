package com.yourcompany.employeeattendance.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import com.yourcompany.employeeattendance.models.DailyReportData
import com.yourcompany.employeeattendance.network.RetrofitClient
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailReportService : Service() {

    companion object {
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "587"
        private const val SENDER_EMAIL = "your-company-email@gmail.com" // Replace with your email
        private const val SENDER_PASSWORD = "your-app-password" // Replace with your app password
        private const val RECIPIENT_EMAIL = "your-company-email@company.com" // Replace with recipient email
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendDailyReport()
        return START_NOT_STICKY
    }

    private fun sendDailyReport() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val response = RetrofitClient.apiService.getDailyReport(today)
                
                if (response.isSuccessful) {
                    val reportData = response.body()
                    reportData?.let {
                        sendEmailReport(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun sendEmailReport(reportData: DailyReportData) {
        withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT_EMAIL))
                    subject = "Daily Attendance Report - ${reportData.date}"
                    setText(buildEmailContent(reportData))
                }

                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun buildEmailContent(reportData: DailyReportData): String {
        val builder = StringBuilder()
        builder.append("DAILY ATTENDANCE REPORT\n")
        builder.append("========================\n\n")
        builder.append("Date: ${formatDate(reportData.date)}\n")
        builder.append("Total Employees: ${reportData.totalEmployees}\n")
        builder.append("Present: ${reportData.presentEmployees}\n")
        builder.append("Absent: ${reportData.absentEmployees}\n\n")
        
        builder.append("ATTENDANCE DETAILS:\n")
        builder.append("-------------------\n")
        builder.append(String.format("%-20s %-10s %-10s %-10s %-15s\n", 
            "Employee", "ID", "Punch In", "Punch Out", "Cluster"))
        builder.append("${"-".repeat(70)}\n")
        
        reportData.records.forEach { record ->
            val punchIn = formatTime(record.punchInTime)
            val punchOut = if (record.punchOutTime != null) formatTime(record.punchOutTime) else "N/A"
            
            builder.append(String.format("%-20s %-10s %-10s %-10s %-15s\n",
                record.name.take(20),
                record.username,
                punchIn,
                punchOut,
                record.cluster
            ))
        }
        
        builder.append("\n\nReport generated at: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}")
        builder.append("\n\nThis is an automated report from Employee Attendance System.")
        
        return builder.toString()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatTime(timeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = inputFormat.parse(timeString)
            outputFormat.format(time ?: Date())
        } catch (e: Exception) {
            timeString
        }
    }
}
