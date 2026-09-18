package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiConfig
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.apiConfig.collectAsState()

    var selectedProvider by remember(currentConfig.provider) { mutableStateOf(currentConfig.provider) }
    var apiKeyText by remember(currentConfig.apiKey) { mutableStateOf(currentConfig.apiKey) }
    var modelText by remember(currentConfig.model) { mutableStateOf(currentConfig.model) }
    var baseUrlText by remember(currentConfig.baseUrl) { mutableStateOf(currentConfig.baseUrl) }

    var isKeyVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var saveFeedback by remember { mutableStateOf<String?>(null) }

    val providers = listOf(
        Triple("GEMINI", "Google Gemini", "Recommended (Gemini 2.5 Flash / Pro)"),
        Triple("OPENAI", "OpenAI Compatible", "GPT-4o / GPT-4o-mini / Local LLM"),
        Triple("CUSTOM", "Custom REST Endpoint", "Self-hosted webhook / custom server")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B1C))
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "API Configuration",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("api_settings_back_button")) {
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
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1638)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E265C))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentConfig.isConfigured) Color(0xFF065F46) else Color(0xFF3F3B66)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentConfig.isConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Status",
                            tint = if (currentConfig.isConfigured) Color(0xFF34D399) else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (currentConfig.isConfigured) "API Connected & Configured" else "Offline Persona Mode (No Key Set)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (currentConfig.isConfigured) "Provider: ${currentConfig.provider} • Model: ${currentConfig.model}"
                            else "Add your custom API key below to unlock limitless cloud reasoning.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }
            }

            // Provider Selector
            Text(
                text = "1. SELECT API PROVIDER",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                providers.forEach { (code, name, desc) ->
                    val isSelected = selectedProvider == code
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF8B5CF6) else Color(0xFF252044),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedProvider = code
                                when (code) {
                                    "GEMINI" -> {
                                        modelText = "gemini-2.5-flash"
                                        baseUrlText = "https://generativelanguage.googleapis.com/"
                                    }
                                    "OPENAI" -> {
                                        modelText = "gpt-4o-mini"
                                        baseUrlText = "https://api.openai.com/"
                                    }
                                    "CUSTOM" -> {
                                        modelText = "custom-model"
                                    }
                                }
                            }
                            .testTag("provider_option_$code"),
                        color = if (isSelected) Color(0xFF261E4C) else Color(0xFF14102A)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                                )
                            }
                        }
                    }
                }
            }

            // API Key input
            Text(
                text = "2. API KEY",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            OutlinedTextField(
                value = apiKeyText,
                onValueChange = {
                    apiKeyText = it
                    saveFeedback = null
                    testResult = null
                },
                placeholder = { Text("Paste your API Key here...", color = Color(0xFF64748B)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input_field"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Key Icon",
                        tint = Color(0xFF8B5CF6)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                        Icon(
                            imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle key visibility",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF2E265C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // Model Name Input
            Text(
                text = "3. MODEL IDENTIFIER",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            OutlinedTextField(
                value = modelText,
                onValueChange = { modelText = it },
                placeholder = { Text("e.g. gemini-2.5-flash", color = Color(0xFF64748B)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_model_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF2E265C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // Base URL Endpoint
            Text(
                text = "4. BASE URL / ENDPOINT",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            OutlinedTextField(
                value = baseUrlText,
                onValueChange = { baseUrlText = it },
                placeholder = { Text("https://generativelanguage.googleapis.com/", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Endpoint URL",
                        tint = Color(0xFF06B6D4)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_url_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF06B6D4),
                    unfocusedBorderColor = Color(0xFF2E265C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // Test Result Banner
            if (testResult != null) {
                val (success, msg) = testResult!!
                Surface(
                    color = if (success) Color(0xFF064E3B) else Color(0xFF450A0A),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Result",
                            tint = if (success) Color(0xFF34D399) else Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (success) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            if (saveFeedback != null) {
                Text(
                    text = saveFeedback!!,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Action Buttons: Test Connection & Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testResult = null
                        val tempConfig = ApiConfig(
                            provider = selectedProvider,
                            apiKey = apiKeyText,
                            model = modelText,
                            baseUrl = baseUrlText,
                            isConfigured = apiKeyText.isNotBlank()
                        )
                        viewModel.testApiConnection(tempConfig) { success, msg ->
                            isTesting = false
                            testResult = Pair(success, msg)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_api_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF38BDF8)
                    )
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF38BDF8),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = "Test")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Connection")
                    }
                }

                Button(
                    onClick = {
                        val newConfig = ApiConfig(
                            provider = selectedProvider,
                            apiKey = apiKeyText.trim(),
                            model = modelText.trim(),
                            baseUrl = baseUrlText.trim(),
                            isConfigured = apiKeyText.isNotBlank()
                        )
                        viewModel.saveApiConfig(newConfig)
                        saveFeedback = "Settings saved successfully!"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_api_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6)
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Settings")
                }
            }

            // Remove Key Button
            if (currentConfig.apiKey.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        apiKeyText = ""
                        viewModel.removeApiKey()
                        saveFeedback = "API Key removed. Switched to offline persona mode."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("remove_api_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Remove Stored Key")
                }
            }

            // Security Notice Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B20)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E173D))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Security note: API keys are stored solely in your device's private app storage. They are never sent to third parties and are strictly passed to your configured endpoint.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
