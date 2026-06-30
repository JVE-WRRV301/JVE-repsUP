package com.jve.repsup.ui.assessment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun GenderScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val selected = viewModel.assessment.gender

    AssessmentScaffold(
        sectionTitle = "About your body.",
        currentStep = 1,
        totalSteps = 5,
        onBack = onBack,
        onNext = onNext,
        nextEnabled = selected != null
    ) {
        Text("What's your gender?", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        listOf("Male", "Female").forEach { option ->
            val isSelected = selected == option
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(CircleShape),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                onClick = { viewModel.updateGender(option) }
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}