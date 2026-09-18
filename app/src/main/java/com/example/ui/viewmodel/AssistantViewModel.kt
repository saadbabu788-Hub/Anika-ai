package com.example.ui.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.action.DeviceActionExecutor
import com.example.api.AnikaApiClient
import com.example.data.local.AnikaDatabase
import com.example.data.local.AppPreferences
import com.example.data.model.ApiConfig
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.OrbState
import com.example.data.model.PersonalityMode
import com.example.data.repository.AssistantRepository
import com.example.voice.AnikaVoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AnikaDatabase.getDatabase(application)
    private val preferences = AppPreferences(application)
    private val repository = AssistantRepository(database.conversationDao(), preferences)
    private val actionExecutor = DeviceActionExecutor(application)

    // Current Navigation Destination
    private val _currentScreen = MutableStateFlow("voice") // "voice", "chat", "modes", "api_settings"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Orb & Speech Status
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _currentTranscript = MutableStateFlow("")
    val currentTranscript: StateFlow<String> = _currentTranscript.asStateFlow()

    private val _latestResponse = MutableStateFlow("Namaste! Main Anika hoon. Aapki kya madad kar sakti hoon?")
    val latestResponse: StateFlow<String> = _latestResponse.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    // Config & Preferences
    val assistantActive: StateFlow<Boolean> = repository.assistantActive
    val wakeWordEnabled: StateFlow<Boolean> = repository.wakeWordEnabled
    val speechTtsEnabled: StateFlow<Boolean> = repository.speechTtsEnabled
    val personalityMode: StateFlow<PersonalityMode> = repository.personalityMode
    val apiConfig: StateFlow<ApiConfig> = repository.apiConfig

    // Conversations
    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _selectedConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice Engine
    private val voiceEngine = AnikaVoiceEngine(
        context = application,
        onStateChanged = { newState -> _orbState.value = newState },
        onSpeechRecognized = { spoken ->
            _currentTranscript.value = spoken
            processUserInput(spoken, isVoice = true)
        },
        onWakeWordDetected = {
            _latestResponse.value = "Haan, main sun rahi hoon!"
        }
    )

    init {
        // Ensure at least one default conversation exists
        viewModelScope.launch {
            repository.conversations.collect { list ->
                if (list.isEmpty() && _selectedConversationId.value == null) {
                    val defaultConv = repository.createConversation("Anika Daily Assistant")
                    _selectedConversationId.value = defaultConv.id
                } else if (_selectedConversationId.value == null && list.isNotEmpty()) {
                    _selectedConversationId.value = list.first().id
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setOrbState(state: OrbState) {
        _orbState.value = state
    }

    fun toggleAssistantActive(active: Boolean) {
        repository.setAssistantActive(active)
        if (!active) {
            voiceEngine.stopListening()
            voiceEngine.stopSpeaking()
            _orbState.value = OrbState.IDLE
            _latestResponse.value = "Anika Assistant inactive hai. Start karne ke liye ON karein."
        } else {
            _latestResponse.value = "Anika Assistant active ho chuki hai!"
        }
    }

    fun toggleWakeWord(enabled: Boolean) {
        repository.setWakeWordEnabled(enabled)
    }

    fun toggleSpeechTts(enabled: Boolean) {
        repository.setSpeechTtsEnabled(enabled)
        if (!enabled) voiceEngine.stopSpeaking()
    }

    fun setPersonalityMode(mode: PersonalityMode) {
        repository.setPersonalityMode(mode)
    }

    fun saveApiConfig(config: ApiConfig) {
        repository.saveApiConfig(config)
    }

    fun removeApiKey() {
        repository.removeApiKey()
    }

    fun startListening() {
        if (!assistantActive.value) {
            _latestResponse.value = "Anika off hai. Pehle power toggle ON karein."
            return
        }
        voiceEngine.stopSpeaking()
        voiceEngine.startListening()
    }

    fun stopListening() {
        voiceEngine.stopListening()
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
    }

    fun selectConversation(id: String) {
        _selectedConversationId.value = id
    }

    fun createNewConversation(title: String = "Conversation ${conversations.value.size + 1}") {
        viewModelScope.launch {
            val newConv = repository.createConversation(title)
            _selectedConversationId.value = newConv.id
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_selectedConversationId.value == id) {
                _selectedConversationId.value = conversations.value.firstOrNull { it.id != id }?.id
            }
        }
    }

    fun processUserInput(input: String, isVoice: Boolean = false, imageUri: Uri? = null) {
        if (input.isBlank() && imageUri == null) return
        if (!assistantActive.value) {
            _latestResponse.value = "Anika inactive hai. Kripya pehle ON switch enable karein."
            return
        }

        viewModelScope.launch {
            var convId = _selectedConversationId.value
            if (convId == null) {
                val newConv = repository.createConversation("Chat with Anika")
                convId = newConv.id
                _selectedConversationId.value = convId
            }

            _currentTranscript.value = input

            // Record user message
            repository.addMessage(
                conversationId = convId,
                role = "USER",
                content = input,
                imageUri = imageUri?.toString(),
                isVoice = isVoice,
                modeUsed = personalityMode.value.name
            )

            // Step 1: Check Device Action Execution
            _orbState.value = OrbState.PROCESSING
            val actionResult = actionExecutor.evaluateAction(input)

            if (actionResult != null) {
                // Action was executed
                val replyText = actionResult.message
                _latestResponse.value = replyText
                _actionFeedback.value = replyText

                repository.addMessage(
                    conversationId = convId,
                    role = "ACTION",
                    content = replyText,
                    isVoice = isVoice,
                    modeUsed = personalityMode.value.name
                )

                if (speechTtsEnabled.value) {
                    voiceEngine.speak(replyText, true)
                } else {
                    _orbState.value = OrbState.IDLE
                }
                return@launch
            }

            // Step 2: Conversational / AI Query
            _orbState.value = OrbState.THINKING
            val currentMode = personalityMode.value
            val config = apiConfig.value

            // Prepare base64 image if attached
            val base64Img = imageUri?.let { uri ->
                runCatching {
                    val stream = getApplication<Application>().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    bitmap?.let { AnikaApiClient.bitmapToBase64(it) }
                }.getOrNull()
            }

            // Conversation history tuples for multi-turn context
            val historyTuples = currentMessages.value.takeLast(6).map { it.role to it.content }

            val aiResponse = AnikaApiClient.generateResponse(
                prompt = input,
                history = historyTuples,
                imageBase64 = base64Img,
                mode = currentMode,
                config = config
            )

            _latestResponse.value = aiResponse

            // Save assistant response
            repository.addMessage(
                conversationId = convId,
                role = "ASSISTANT",
                content = aiResponse,
                isVoice = isVoice,
                modeUsed = currentMode.name
            )

            if (speechTtsEnabled.value) {
                voiceEngine.speak(aiResponse, true)
            } else {
                _orbState.value = OrbState.IDLE
            }
        }
    }

    fun testApiConnection(config: ApiConfig, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _orbState.value = OrbState.PROCESSING
            val result = AnikaApiClient.testConnection(config)
            _orbState.value = if (result.first) OrbState.IDLE else OrbState.ERROR
            onResult(result.first, result.second)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }
}
