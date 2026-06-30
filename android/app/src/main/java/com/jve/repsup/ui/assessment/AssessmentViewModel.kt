package com.jve.repsup.ui.assessment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jve.repsup.data.FitnessAssessment

class AssessmentViewModel : ViewModel() {

    var assessment by mutableStateOf(FitnessAssessment())
        private set

    fun updateGender(value: String) {
        assessment = assessment.copy(gender = value)
    }

    fun updateAge(value: Int) {
        assessment = assessment.copy(age = value)
    }

    fun updateWeight(value: Float) {
        assessment = assessment.copy(weight = value)
    }

    fun updateHeight(value: Float) {
        assessment = assessment.copy(height = value)
    }

    fun updateBodyType(value: String) {
        assessment = assessment.copy(bodyType = value)
    }

    fun updateWorkoutLevel(value: String) {
        assessment = assessment.copy(workoutLevel = value)
    }

    fun updateWorkoutsPerWeek(value: Int) {
        assessment = assessment.copy(workoutsPerWeek = value)
    }

    fun updateFitnessGoal(value: String) {
        assessment = assessment.copy(fitnessGoal = value)
    }
}