package com.example.data.model

data class ExamQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val category: String,
    val explanation: String
)
