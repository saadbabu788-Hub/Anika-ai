package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PersonalityMode(val displayName: String, val description: String, val tag: String) {
    NORMAL("Normal Assistant", "Helpful, fast, and polite personal assistant", "Default"),
    GIRLFRIEND("AI Girlfriend", "Affectionate, playful, and caring (Babu, Sona ❤️)", "Sweet ❤️"),
    FUNNY("Funny Mode", "Humorous, sarcastic, and playful comebacks", "Witty 😂"),
    ROAST("Roast / Gaali Mode", "Mild fictional roasting and witty counter-attacks", "Spicy 🔥")
}

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    PROCESSING,
    ERROR
}

enum class MessageRole {
    USER,
    ASSISTANT,
    ACTION,
    SYSTEM
}

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessage: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "USER", "ASSISTANT", "ACTION", "SYSTEM"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isVoice: Boolean = false,
    val modeUsed: String = "NORMAL"
)

data class ApiConfig(
    val provider: String = "GEMINI", // "GEMINI", "OPENAI", "CUSTOM"
    val apiKey: String = "",
    val model: String = "gemini-2.5-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com/",
    val isConfigured: Boolean = false
)

data class ActionResult(
    val success: Boolean,
    val message: String,
    val permissionNeeded: String? = null,
    val intentHandled: Boolean = false
)
