package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.components.ConversationDrawer
import com.example.ui.screens.AboutSaadBabuDialog
import com.example.ui.screens.ApiSettingsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.PersonalityModesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AnikaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AnikaApp(viewModel: AssistantViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val selectedConvId by viewModel.selectedConversationId.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

    // Permissions launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (recordAudioGranted) {
            Toast.makeText(context, "Microphone permission granted.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Handle back press
    BackHandler(enabled = drawerState.isOpen || currentScreen != "voice") {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen != "voice") {
            viewModel.navigateTo("voice")
        }
    }

    if (showAboutDialog) {
        AboutSaadBabuDialog(onDismiss = { showAboutDialog = false })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationDrawer(
                conversations = conversations,
                selectedConversationId = selectedConvId,
                currentDestination = currentScreen,
                onSelectConversation = { id ->
                    viewModel.selectConversation(id)
                    viewModel.navigateTo("chat")
                    scope.launch { drawerState.close() }
                },
                onNewConversation = {
                    viewModel.createNewConversation()
                    viewModel.navigateTo("chat")
                    scope.launch { drawerState.close() }
                },
                onDeleteConversation = { id ->
                    viewModel.deleteConversation(id)
                },
                onNavigate = { destination ->
                    viewModel.navigateTo(destination)
                    scope.launch { drawerState.close() }
                },
                onShowAbout = {
                    showAboutDialog = true
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "voice" -> {
                        MainAssistantScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToChat = { viewModel.navigateTo("chat") },
                            onNavigateToModes = { viewModel.navigateTo("modes") },
                            onNavigateToSettings = { viewModel.navigateTo("api_settings") },
                            onShowAbout = { showAboutDialog = true }
                        )
                    }

                    "chat" -> {
                        ChatScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("voice") }
                        )
                    }

                    "modes" -> {
                        PersonalityModesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("voice") }
                        )
                    }

                    "api_settings" -> {
                        ApiSettingsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("voice") }
                        )
                    }
                }
            }
        }
    }
}
