package com.yourcompany.employeeattendance.models

data class AttendanceRecord(
    val id: Int? = null,
    val userId: Int,
    val username: String,
    val name: String,
    val punchInTime: String,
    val punchOutTime: String? = null,
    val date: String,
    val cluster: String,
    val workingHours: String? = null
)

data class PunchRequest(
    val userId: Int,
    val username: String,
    val name: String,
    val cluster: String,
    val type: String, // "IN" or "OUT"
    val timestamp: String
)

data class PunchResponse(
    val success: Boolean,
    val message: String,
    val attendanceRecord: AttendanceRecord?
)

data class AttendanceHistoryResponse(
    val success: Boolean,
    val message: String,
    val records: List<AttendanceRecord>
)

data class DailyReportData(
    val date: String,
    val totalEmployees: Int,
    val presentEmployees: Int,
    val absentEmployees: Int,
    val records: List<AttendanceRecord>
)
