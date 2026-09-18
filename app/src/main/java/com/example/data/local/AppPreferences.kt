package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.ApiConfig
import com.example.data.model.PersonalityMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("anika_preferences", Context.MODE_PRIVATE)

    private val _assistantActive = MutableStateFlow(prefs.getBoolean(KEY_ASSISTANT_ACTIVE, true))
    val assistantActive: StateFlow<Boolean> = _assistantActive.asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(prefs.getBoolean(KEY_WAKE_WORD, false))
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    private val _speechTtsEnabled = MutableStateFlow(prefs.getBoolean(KEY_TTS_ENABLED, true))
    val speechTtsEnabled: StateFlow<Boolean> = _speechTtsEnabled.asStateFlow()

    private val _personalityMode = MutableStateFlow(
        PersonalityMode.valueOf(prefs.getString(KEY_PERSONALITY_MODE, PersonalityMode.NORMAL.name) ?: PersonalityMode.NORMAL.name)
    )
    val personalityMode: StateFlow<PersonalityMode> = _personalityMode.asStateFlow()

    private val _apiConfig = MutableStateFlow(loadApiConfig())
    val apiConfig: StateFlow<ApiConfig> = _apiConfig.asStateFlow()

    private fun loadApiConfig(): ApiConfig {
        val provider = prefs.getString(KEY_API_PROVIDER, "GEMINI") ?: "GEMINI"
        val customKey = prefs.getString(KEY_API_KEY, "") ?: ""
        // Fallback to BuildConfig if present and customKey is empty
        val envKey = runCatching {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val v = field.get(null) as? String
            if (v == "MY_GEMINI_API_KEY" || v.isNullOrBlank()) "" else v
        }.getOrDefault("")

        val effectiveKey = if (customKey.isNotBlank()) customKey else envKey
        val model = prefs.getString(KEY_API_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash"
        val baseUrl = prefs.getString(
            KEY_API_BASE_URL,
            "https://generativelanguage.googleapis.com/"
        ) ?: "https://generativelanguage.googleapis.com/"

        return ApiConfig(
            provider = provider,
            apiKey = effectiveKey,
            model = model,
            baseUrl = baseUrl,
            isConfigured = effectiveKey.isNotBlank()
        )
    }

    fun setAssistantActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_ASSISTANT_ACTIVE, active).apply()
        _assistantActive.value = active
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    fun setSpeechTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
        _speechTtsEnabled.value = enabled
    }

    fun setPersonalityMode(mode: PersonalityMode) {
        prefs.edit().putString(KEY_PERSONALITY_MODE, mode.name).apply()
        _personalityMode.value = mode
    }

    fun saveApiConfig(config: ApiConfig) {
        prefs.edit()
            .putString(KEY_API_PROVIDER, config.provider)
            .putString(KEY_API_KEY, config.apiKey)
            .putString(KEY_API_MODEL, config.model)
            .putString(KEY_API_BASE_URL, config.baseUrl)
            .apply()
        _apiConfig.value = config.copy(isConfigured = config.apiKey.isNotBlank())
    }

    fun removeApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
        val updated = _apiConfig.value.copy(apiKey = "", isConfigured = false)
        _apiConfig.value = updated
    }

    companion object {
        private const val KEY_ASSISTANT_ACTIVE = "key_assistant_active"
        private const val KEY_WAKE_WORD = "key_wake_word"
        private const val KEY_TTS_ENABLED = "key_tts_enabled"
        private const val KEY_PERSONALITY_MODE = "key_personality_mode"
        private const val KEY_API_PROVIDER = "key_api_provider"
        private const val KEY_API_KEY = "key_api_key"
        private const val KEY_API_MODEL = "key_api_model"
        private const val KEY_API_BASE_URL = "key_api_base_url"
    }
}
