package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.model.ApiConfig
import com.example.data.model.PersonalityMode
import com.example.engine.AnikaPersona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object AnikaApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        prompt: String,
        history: List<Pair<String, String>>,
        imageBase64: String? = null,
        mode: PersonalityMode,
        config: ApiConfig
    ): String = withContext(Dispatchers.IO) {
        // First check local persona rules (name, creator Saad Babu, API query, etc.)
        val localMatch = AnikaPersona.getLocalResponse(prompt, mode)
        if (localMatch != null) {
            return@withContext localMatch
        }

        // Check if API key is configured
        if (!config.isConfigured || config.apiKey.isBlank()) {
            return@withContext AnikaPersona.getLocalResponse(prompt, mode)
                ?: getOfflineFriendlyResponse(prompt, mode)
        }

        try {
            when (config.provider.uppercase()) {
                "OPENAI" -> callOpenAiCompatible(prompt, history, imageBase64, mode, config)
                "CUSTOM" -> callCustomEndpoint(prompt, history, imageBase64, mode, config)
                else -> callGeminiRest(prompt, history, imageBase64, mode, config)
            }
        } catch (e: Exception) {
            // Fallback gracefully without crashing
            val fallback = AnikaPersona.getLocalResponse(prompt, mode)
            if (fallback != null) {
                fallback
            } else {
                "API call mein issue aaya (${e.localizedMessage ?: "Network error"}). Main Anika aapki offline sahayata kar sakti hoon!"
            }
        }
    }

    private fun callGeminiRest(
        prompt: String,
        history: List<Pair<String, String>>,
        imageBase64: String?,
        mode: PersonalityMode,
        config: ApiConfig
    ): String {
        val model = if (config.model.isNotBlank()) config.model.trim() else "gemini-2.5-flash"
        val cleanBaseUrl = if (config.baseUrl.endsWith("/")) config.baseUrl else "${config.baseUrl}/"
        val endpoint = "${cleanBaseUrl}v1beta/models/$model:generateContent?key=${config.apiKey.trim()}"

        val root = JSONObject()

        // System Instruction with master rules
        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", AnikaPersona.buildSystemInstruction(mode)))
        systemInstruction.put("parts", sysParts)
        root.put("systemInstruction", systemInstruction)

        // Contents array
        val contentsArray = JSONArray()

        // Prior turns
        val recentHistory = history.takeLast(6)
        for ((role, text) in recentHistory) {
            val turn = JSONObject()
            val mappedRole = if (role.equals("user", ignoreCase = true)) "user" else "model"
            turn.put("role", mappedRole)
            val parts = JSONArray()
            parts.put(JSONObject().put("text", text))
            turn.put("parts", parts)
            contentsArray.put(turn)
        }

        // Current user prompt
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()
        currentParts.put(JSONObject().put("text", prompt))

        if (!imageBase64.isNullOrBlank()) {
            val inlineData = JSONObject()
            inlineData.put("mimeType", "image/jpeg")
            inlineData.put("data", imageBase64)
            currentParts.put(JSONObject().put("inlineData", inlineData))
        }

        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)
        root.put("contents", contentsArray)

        // Generation config
        val genConfig = JSONObject()
        genConfig.put("temperature", if (mode == PersonalityMode.FUNNY || mode == PersonalityMode.ROAST) 0.9 else 0.7)
        genConfig.put("maxOutputTokens", 1000)
        root.put("generationConfig", genConfig)

        val requestBody = root.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = runCatching {
                JSONObject(bodyStr).optJSONObject("error")?.optString("message")
            }.getOrNull() ?: "HTTP ${response.code}"
            return "API Error: $errorMsg"
        }

        val json = JSONObject(bodyStr)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val first = candidates.getJSONObject(0)
            val parts = first.optJSONObject("content")?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val sb = StringBuilder()
                for (i in 0 until parts.length()) {
                    sb.append(parts.getJSONObject(i).optString("text", ""))
                }
                val result = sb.toString().trim()
                if (result.isNotBlank()) return result
            }
        }
        return "Anika ko response process karne mein dikkat aayi."
    }

    private fun callOpenAiCompatible(
        prompt: String,
        history: List<Pair<String, String>>,
        imageBase64: String?,
        mode: PersonalityMode,
        config: ApiConfig
    ): String {
        val cleanBaseUrl = if (config.baseUrl.endsWith("/")) config.baseUrl else "${config.baseUrl}/"
        val endpoint = if (cleanBaseUrl.contains("chat/completions")) cleanBaseUrl else "${cleanBaseUrl}v1/chat/completions"

        val root = JSONObject()
        root.put("model", if (config.model.isNotBlank()) config.model else "gpt-4o-mini")

        val messages = JSONArray()
        messages.put(
            JSONObject()
                .put("role", "system")
                .put("content", AnikaPersona.buildSystemInstruction(mode))
        )

        for ((role, text) in history.takeLast(6)) {
            val mappedRole = if (role.equals("user", ignoreCase = true)) "user" else "assistant"
            messages.put(JSONObject().put("role", mappedRole).put("content", text))
        }

        messages.put(JSONObject().put("role", "user").put("content", prompt))
        root.put("messages", messages)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
            .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            return "API Error (${response.code})"
        }
        val json = JSONObject(body)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            return choices.getJSONObject(0).optJSONObject("message")?.optString("content", "") ?: ""
        }
        return "Khali response mila."
    }

    private fun callCustomEndpoint(
        prompt: String,
        history: List<Pair<String, String>>,
        imageBase64: String?,
        mode: PersonalityMode,
        config: ApiConfig
    ): String {
        val root = JSONObject()
        root.put("prompt", prompt)
        root.put("mode", mode.name)
        root.put("system", AnikaPersona.buildSystemInstruction(mode))

        val req = Request.Builder()
            .url(config.baseUrl)
            .addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
            .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val resp = client.newCall(req).execute()
        val str = resp.body?.string() ?: ""
        if (!resp.isSuccessful) return "Custom API Error: ${resp.code}"
        return runCatching {
            val json = JSONObject(str)
            json.optString("response", json.optString("text", str))
        }.getOrDefault(str)
    }

    suspend fun testConnection(config: ApiConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (config.apiKey.isBlank()) {
            return@withContext Pair(false, "API Key missing hai! Kripya pehle valid key dalein.")
        }
        val startTime = System.currentTimeMillis()
        try {
            val model = if (config.model.isNotBlank()) config.model.trim() else "gemini-2.5-flash"
            val cleanBaseUrl = if (config.baseUrl.endsWith("/")) config.baseUrl else "${config.baseUrl}/"
            val endpoint = "${cleanBaseUrl}v1beta/models/$model:generateContent?key=${config.apiKey.trim()}"

            val root = JSONObject()
            val contents = JSONArray()
            val turn = JSONObject().put("role", "user")
            val parts = JSONArray().put(JSONObject().put("text", "Ping test. Reply with 'OK'"))
            turn.put("parts", parts)
            contents.put(turn)
            root.put("contents", contents)

            val request = Request.Builder()
                .url(endpoint)
                .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Pair(true, "Connected successfully! Latency: ${latency}ms")
            } else {
                val errorMsg = runCatching {
                    JSONObject(body).optJSONObject("error")?.optString("message")
                }.getOrNull() ?: "HTTP ${response.code}"
                Pair(false, "Failed ($errorMsg)")
            }
        } catch (e: Exception) {
            Pair(false, "Connection error: ${e.localizedMessage ?: "Unknown"}")
        }
    }

    private fun getOfflineFriendlyResponse(prompt: String, mode: PersonalityMode): String {
        return when (mode) {
            PersonalityMode.NORMAL -> "Aapka command note ho gaya! Main Anika hoon. External API key configure karne ke baad main aur gehraai se uttar de sakti hoon."
            PersonalityMode.GIRLFRIEND -> "Haan Babu ❤️ Main sun rahi hoon! Tum bolo, main hamesha tumhare saath hoon."
            PersonalityMode.FUNNY -> "Kamaal ka sawaal hai Babu! 😂 API key dalte hi iska aisa jawab doongi ki has-has ke pagal ho jaoge!"
            PersonalityMode.ROAST -> "Arre hero, offline mein bhi itna load le raha hai? 😂 Settings mein jaake API key laga pehle!"
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
