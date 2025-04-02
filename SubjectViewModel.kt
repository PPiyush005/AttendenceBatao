package com.Piyush.attendencebatao.viewmodel



import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Piyush.attendencebatao.model.Subject

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SubjectViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("AttendanceData", Context.MODE_PRIVATE)
    private val gson = Gson()

    var subjects = mutableStateListOf<Subject>()
        private set

    init {
        loadSubjects()
//        updateSubjectsFromAPI()
    }

    fun addSubject(name: String, attendance: Int, classDays: List<String>) {
        viewModelScope.launch {
            val response = fetchAttendanceData(attendance, classDays, 0)
            subjects.add(Subject(name, attendance, classDays, response.possibleLeaves, response.remSesClass, response.updatedAttendance))
            saveSubjects()
        }
    }

    data class AttendanceResponse(val updatedAttendance: Double, val possibleLeaves: Int, val remSesClass: Int)

    private suspend fun fetchAttendanceData(attendance: Int, classDays: List<String>, percentage: Int): AttendanceResponse {

        val fullDayNames = mapDaysToFullNames(classDays)

        val requestBody = JSONObject().apply {
            put("startDate", "2024-12-10")
            put("endDate", "2025-4-05")
            put("classDays", JSONArray(fullDayNames))
            put("attendedClasses", attendance)
            put("modPercentage", percentage)
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://vercel-att-tracker.vercel.app/calculate-attendance")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.write(requestBody.toString().toByteArray())
                }

                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)

                AttendanceResponse(
                    updatedAttendance = json.getDouble("Percentage"),
                    possibleLeaves = json.getInt("remainingClasses"),
                    remSesClass = json.getInt("remainingSessionClasses")
                )
            } catch (e: Exception) {
                Log.e("API_ERROR", "Failed to fetch data: ${e.message}")
                AttendanceResponse(0.0, 0,0) // Return default values on failure
            }
        }
    }

    private fun mapDaysToFullNames(shortDays: List<String>): List<String> {
        val dayMapping = mapOf(
            "Mon" to "Monday",
            "Tue" to "Tuesday",
            "Wed" to "Wednesday",
            "Thu" to "Thursday",
            "Fri" to "Friday",
            "Sat" to "Saturday",
            "Sun" to "Sunday"
        )
        return shortDays.mapNotNull { dayMapping[it] }
    }



    private fun updateSubjectWithAPI(subject: Subject) {
        viewModelScope.launch {
            val response = fetchAttendanceData(subject.attendance, subject.classDays, 0)
            val updatedSubject = subject.copy(
                attendance = subject.attendance,
                possibleLeaves = response.possibleLeaves,
                remSesClass = response.remSesClass,
                attendPercentage = response.updatedAttendance
            )
            val index = subjects.indexOf(subject)
            if (index != -1) subjects[index] = updatedSubject
            saveSubjects()
        }
    }

    private fun updateSubjectsFromAPI() {
        viewModelScope.launch {
            subjects.forEach { subject ->
                updateSubjectWithAPI(subject)
            }
        }
    }
    fun updateAttendance(subjectName: String, newAttendance: Int, newPercentage: Int) {
        val index = subjects.indexOfFirst { it.name == subjectName }
        if (index != -1) {
            viewModelScope.launch {
                val response = fetchAttendanceData(newAttendance, subjects[index].classDays, newPercentage)
                subjects[index] = subjects[index].copy(attendance = newAttendance)
                subjects[index] = subjects[index].copy(possibleLeaves = response.possibleLeaves)
                subjects[index] = subjects[index].copy(remSesClass = response.remSesClass)
                subjects[index] =
                    subjects[index].copy(attendPercentage = response.updatedAttendance)
                saveSubjects()
            }
        }
    }

    fun deleteSubject(subject: Subject) {
        subjects.remove(subject)
        saveSubjects()
    }

    private fun saveSubjects() {
        val json = gson.toJson(subjects)
        sharedPreferences.edit().putString("subjects", json).apply()
    }

    private fun loadSubjects() {
        val json = sharedPreferences.getString("subjects", null)
        if (json != null) {
            val type = object : TypeToken<List<Subject>>() {}.type
            subjects.addAll(gson.fromJson(json, type))
        }
    }
}