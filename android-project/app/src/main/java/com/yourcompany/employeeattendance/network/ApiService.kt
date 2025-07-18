package com.yourcompany.employeeattendance.network

import com.yourcompany.employeeattendance.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("attendance/punch")
    suspend fun punchAttendance(@Body request: PunchRequest): Response<PunchResponse>
    
    @GET("attendance/today")
    suspend fun getTodayAttendance(): Response<AttendanceHistoryResponse>
    
    @GET("attendance/user/{userId}")
    suspend fun getUserAttendance(@Path("userId") userId: Int): Response<AttendanceHistoryResponse>
    
    @GET("attendance/history/{userId}")
    suspend fun getAttendanceHistory(
        @Path("userId") userId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<AttendanceHistoryResponse>
    
    @GET("attendance/daily-report")
    suspend fun getDailyReport(@Query("date") date: String): Response<DailyReportData>
    
    @POST("attendance/send-report")
    suspend fun sendDailyReport(@Body reportData: DailyReportData): Response<Map<String, Any>>
}
