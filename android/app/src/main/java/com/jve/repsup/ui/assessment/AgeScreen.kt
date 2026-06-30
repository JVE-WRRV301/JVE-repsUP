package com.jve.repsup.ui.assessment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AgeScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var text by remember { mutableStateOf(viewModel.assessment.age?.toString() ?: "") }
    val ageValue = text.toIntOrNull()

    AssessmentScaffold(
        sectionTitle = "About your body.",
        currentStep = 2,
        totalSteps = 4,
        onBack = onBack,
        onNext = {
            ageValue?.let { viewModel.updateAge(it) }
            onNext()
        },
        nextEnabled = ageValue != null && ageValue in 13..100
    ) {
        Text("How old are you?", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it.filter { ch -> ch.isDigit() } },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}