package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrbState
import com.example.data.model.PersonalityMode
import com.example.ui.components.AnimatedOrb
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun MainAssistantScreen(
    viewModel: AssistantViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToModes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val isActive by viewModel.assistantActive.collectAsState()
    val currentMode by viewModel.personalityMode.collectAsState()
    val transcript by viewModel.currentTranscript.collectAsState()
    val latestResponse by viewModel.latestResponse.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()
    val apiConfig by viewModel.apiConfig.collectAsState()

    val quickCommands = listOf(
        "Who created you?",
        "Volume full",
        "Turn flashlight on",
        "Open YouTube",
        "Open WhatsApp",
        "Tumhara naam kya hai?",
        "Open Settings"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09071A),
                        Color(0xFF120E2C),
                        Color(0xFF0A071E)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("menu_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer Menu",
                    tint = Color(0xFFC7D2FE)
                )
            }

            // Central Branding & Mode Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigateToModes() }
            ) {
                Text(
                    text = "ANIKA AI",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )
                Surface(
                    color = when (currentMode) {
                        PersonalityMode.GIRLFRIEND -> Color(0xFFEC4899).copy(alpha = 0.25f)
                        PersonalityMode.FUNNY -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                        PersonalityMode.ROAST -> Color(0xFFEF4444).copy(alpha = 0.25f)
                        else -> Color(0xFF6366F1).copy(alpha = 0.25f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = currentMode.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (currentMode) {
                                PersonalityMode.GIRLFRIEND -> Color(0xFFF472B6)
                                PersonalityMode.FUNNY -> Color(0xFFFBBF24)
                                PersonalityMode.ROAST -> Color(0xFFF87171)
                                else -> Color(0xFFA5B4FC)
                            },
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Full Volume Toggle / Booster Button
                IconButton(
                    onClick = { viewModel.setDeviceVolumeFull() },
                    modifier = Modifier.testTag("volume_full_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Set Volume Full",
                        tint = Color(0xFF38BDF8)
                    )
                }

                IconButton(
                    onClick = onShowAbout,
                    modifier = Modifier.testTag("about_creator_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About Creator Saad Babu",
                        tint = Color(0xFFC7D2FE)
                    )
                }

                // API Key Settings with green/orange status indicator dot
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Settings",
                            tint = if (apiConfig.isConfigured) Color(0xFF34D399) else Color(0xFFFBBF24)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (apiConfig.isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Animated Orb and Status Display ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            AnimatedOrb(
                orbState = orbState,
                isActive = isActive,
                modifier = Modifier
                    .size(240.dp)
                    .clickable {
                        if (orbState == OrbState.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // State Indicator Label
            val stateText = when {
                !isActive -> "ASSISTANT INACTIVE"
                orbState == OrbState.LISTENING -> "LISTENING..."
                orbState == OrbState.THINKING -> "THINKING..."
                orbState == OrbState.SPEAKING -> "SPEAKING..."
                orbState == OrbState.PROCESSING -> "EXECUTING COMMAND..."
                orbState == OrbState.ERROR -> "SYSTEM ALERT"
                else -> "READY & LISTENING"
            }

            // State Indicator Pill Badge
            Surface(
                color = when {
                    !isActive -> Color(0xFF1F2937).copy(alpha = 0.6f)
                    orbState == OrbState.LISTENING -> Color(0xFF0284C7).copy(alpha = 0.25f)
                    orbState == OrbState.SPEAKING -> Color(0xFF9333EA).copy(alpha = 0.25f)
                    orbState == OrbState.PROCESSING -> Color(0xFF059669).copy(alpha = 0.25f)
                    orbState == OrbState.ERROR -> Color(0xFFDC2626).copy(alpha = 0.25f)
                    else -> Color(0xFF312E81).copy(alpha = 0.35f)
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    when {
                        !isActive -> Color(0xFF4B5563)
                        orbState == OrbState.LISTENING -> Color(0xFF00F0FF)
                        orbState == OrbState.SPEAKING -> Color(0xFFD946EF)
                        orbState == OrbState.PROCESSING -> Color(0xFF34D399)
                        orbState == OrbState.ERROR -> Color(0xFFEF4444)
                        else -> Color(0xFF6366F1)
                    }
                )
            ) {
                Text(
                    text = stateText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = when {
                            !isActive -> Color.Gray
                            orbState == OrbState.LISTENING -> Color(0xFF00F0FF)
                            orbState == OrbState.SPEAKING -> Color(0xFFF0ABFC)
                            orbState == OrbState.PROCESSING -> Color(0xFF6EE7B7)
                            orbState == OrbState.ERROR -> Color(0xFFFCA5A5)
                            else -> Color(0xFFA5B4FC)
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Latest Spoken Content / Action Result Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161233).copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color(0xFF3B2D75).copy(alpha = 0.7f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            !actionFeedback.isNullOrBlank() -> actionFeedback!!
                            transcript.isNotBlank() && orbState == OrbState.LISTENING -> "“$transcript”"
                            latestResponse.isNotBlank() -> latestResponse
                            else -> "Tap the microphone below to talk to Anika"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF1F5F9),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Quick Commands Carousel ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickCommands) { cmd ->
                Surface(
                    color = Color(0xFF281C5C).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C1D95)),
                    modifier = Modifier.clickable { viewModel.processUserInput(cmd) }
                ) {
                    Text(
                        text = cmd,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFDDD6FE)),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- Circular Bottom Controls ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power ON/OFF Toggle Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Brush.linearGradient(
                                listOf(Color(0xFF10B981), Color(0xFF059669))
                            ) else Brush.linearGradient(
                                listOf(Color(0xFF374151), Color(0xFF1F2937))
                            )
                        )
                        .clickable { viewModel.toggleAssistantActive(!isActive) }
                        .testTag("power_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Assistant Power",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isActive) "ACTIVE" else "OFF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isActive) Color(0xFF34D399) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Big Central Microphone Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(86.dp)
                        .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color(0xFF00F0FF))
                        .clip(CircleShape)
                        .background(
                            if (orbState == OrbState.LISTENING) {
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F0FF), Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4))
                                )
                            }
                        )
                        .clickable {
                            if (orbState == OrbState.LISTENING) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        }
                        .testTag("microphone_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone Button",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (orbState == OrbState.LISTENING) "LISTENING" else "TAP TO TALK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Chat Mode Screen Shortcut
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4C1D95), Color(0xFF2E1065))
                            )
                        )
                        .clickable { onNavigateToChat() }
                        .testTag("chat_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Open Chat Mode",
                        tint = Color(0xFFE9D5FF),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CHAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFC084FC),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
