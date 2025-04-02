package com.Piyush.attendencebatao.model

data class Subject(
    val name: String,
    val attendance: Int,
    val classDays: List<String>,
    val possibleLeaves: Int = 0,
    val remSesClass: Int=0,
    val attendPercentage: Double =0.0
)