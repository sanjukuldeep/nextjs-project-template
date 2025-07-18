package com.yourcompany.employeeattendance.models

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val name: String,
    val cluster: String,
    val phoneNumber: String,
    val createdAt: String = ""
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val name: String,
    val cluster: String,
    val phoneNumber: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)
