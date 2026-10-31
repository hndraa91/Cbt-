package com.example.data.model

enum class UserRole {
    ADMIN,
    STUDENT
}

data class StudentUser(
    val id: String,
    val username: String,
    val password: String,
    val fullName: String,
    val className: String,
    val nisn: String,
    val createdAt: Long = System.currentTimeMillis()
)
