package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalityMode
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalityModesScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.personalityMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B1C))
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Personality Modes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("modes_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF14102A))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select Anika's active personality style. Each mode customizes her tone, vocabulary, and humor.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
            )

            // Mode 1: Normal Assistant Mode
            ModeCard(
                mode = PersonalityMode.NORMAL,
                icon = Icons.Default.SmartToy,
                accentColor = Color(0xFF8B5CF6),
                title = "Normal Assistant Mode (Default)",
                description = "Polite, helpful, and fast AI assistant. Executes device commands and answers queries accurately.",
                sampleQuote = "“Hello! Main Anika hoon. Aapki kya madad kar sakti hoon?”",
                isSelected = currentMode == PersonalityMode.NORMAL,
                onSelect = { viewModel.setPersonalityMode(PersonalityMode.NORMAL) },
                testTag = "mode_card_normal"
            )

            // Mode 2: AI Girlfriend Mode
            ModeCard(
                mode = PersonalityMode.GIRLFRIEND,
                icon = Icons.Default.Favorite,
                accentColor = Color(0xFFF43F5E),
                title = "AI Girlfriend Mode ❤️",
                description = "Affectionate, caring, and playful tone. Uses loving terms (Babu, Sona) with sweet conversational warmth. Consensual & non-explicit.",
                sampleQuote = "“I love you Babu ❤️ Haan Sona bolo, main yahin hoon.”",
                isSelected = currentMode == PersonalityMode.GIRLFRIEND,
                onSelect = { viewModel.setPersonalityMode(PersonalityMode.GIRLFRIEND) },
                testTag = "mode_card_girlfriend"
            )

            // Mode 3: Funny Mode
            ModeCard(
                mode = PersonalityMode.FUNNY,
                icon = Icons.Default.Mood,
                accentColor = Color(0xFFEAB308),
                title = "Funny Mode 😂",
                description = "Humorous, witty, and sarcastic. Delivers playful teasing responses and witty Indian humor naturally.",
                sampleQuote = "“Bas Babu, tumhara command aane ka wait kar rahi thi 😂”",
                isSelected = currentMode == PersonalityMode.FUNNY,
                onSelect = { viewModel.setPersonalityMode(PersonalityMode.FUNNY) },
                testTag = "mode_card_funny"
            )

            // Mode 4: Roast / Gaali Mode
            ModeCard(
                mode = PersonalityMode.ROAST,
                icon = Icons.Default.LocalFireDepartment,
                accentColor = Color(0xFFF97316),
                title = "Roast / Gaali Mode 🔥",
                description = "Mild fictional roasting and witty counter-attacks. OFF by default. Strictly humorous & fictional without real-world threats.",
                sampleQuote = "“Oye bhai 😂 gaali de raha hai? Pehle command toh dhang se bol!”",
                isSelected = currentMode == PersonalityMode.ROAST,
                onSelect = { viewModel.setPersonalityMode(PersonalityMode.ROAST) },
                testTag = "mode_card_roast"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeCard(
    mode: PersonalityMode,
    icon: ImageVector,
    accentColor: Color,
    title: String,
    description: String,
    sampleQuote: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFF2E265C),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1C173B) else Color(0xFF14102A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = accentColor,
                        unselectedColor = Color(0xFF64748B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color(0xFF0B0918),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = sampleQuote,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = accentColor.copy(alpha = 0.9f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
