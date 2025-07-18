package com.yourcompany.employeeattendance.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourcompany.employeeattendance.databinding.ItemAttendanceBinding
import com.yourcompany.employeeattendance.models.AttendanceRecord
import java.text.SimpleDateFormat
import java.util.*

class AttendanceAdapter(private val attendanceList: List<AttendanceRecord>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendanceList[position])
    }

    override fun getItemCount(): Int = attendanceList.size

    class AttendanceViewHolder(private val binding: ItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: AttendanceRecord) {
            binding.apply {
                tvDate.text = formatDate(record.date)
                tvPunchIn.text = formatTime(record.punchInTime)
                tvPunchOut.text = if (record.punchOutTime != null) {
                    formatTime(record.punchOutTime)
                } else {
                    "Not punched out"
                }
                
                // Calculate working hours if both punch in and out are available
                if (record.punchOutTime != null) {
                    val workingHours = calculateWorkingHours(record.punchInTime, record.punchOutTime)
                    tvWorkingHours.text = workingHours
                } else {
                    tvWorkingHours.text = "In progress"
                }
                
                tvCluster.text = record.cluster
            }
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

        private fun calculateWorkingHours(punchIn: String, punchOut: String): String {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val inTime = format.parse(punchIn)
                val outTime = format.parse(punchOut)
                
                if (inTime != null && outTime != null) {
                    val diffInMillis = outTime.time - inTime.time
                    val hours = diffInMillis / (1000 * 60 * 60)
                    val minutes = (diffInMillis % (1000 * 60 * 60)) / (1000 * 60)
                    "${hours}h ${minutes}m"
                } else {
                    "N/A"
                }
            } catch (e: Exception) {
                "N/A"
            }
        }
    }
}
