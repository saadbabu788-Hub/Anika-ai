package com.example.data.repository

import com.example.data.local.AppPreferences
import com.example.data.local.ConversationDao
import com.example.data.model.ApiConfig
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.PersonalityMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class AssistantRepository(
    private val conversationDao: ConversationDao,
    private val preferences: AppPreferences
) {
    val conversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    val assistantActive: StateFlow<Boolean> = preferences.assistantActive
    val wakeWordEnabled: StateFlow<Boolean> = preferences.wakeWordEnabled
    val speechTtsEnabled: StateFlow<Boolean> = preferences.speechTtsEnabled
    val personalityMode: StateFlow<PersonalityMode> = preferences.personalityMode
    val apiConfig: StateFlow<ApiConfig> = preferences.apiConfig

    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>> =
        conversationDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(title: String): ConversationEntity {
        val conv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastMessage = ""
        )
        conversationDao.insertConversation(conv)
        return conv
    }

    suspend fun addMessage(
        conversationId: String,
        role: String,
        content: String,
        imageUri: String? = null,
        isVoice: Boolean = false,
        modeUsed: String = "NORMAL"
    ): ChatMessageEntity {
        val msg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            content = content,
            timestamp = System.currentTimeMillis(),
            imageUri = imageUri,
            isVoice = isVoice,
            modeUsed = modeUsed
        )
        conversationDao.insertMessage(msg)

        // Update conversation summary
        val existing = conversationDao.getConversationById(conversationId)
        if (existing != null) {
            val preview = if (content.length > 50) content.take(47) + "..." else content
            conversationDao.insertConversation(
                existing.copy(
                    updatedAt = System.currentTimeMillis(),
                    lastMessage = preview
                )
            )
        }
        return msg
    }

    suspend fun deleteConversation(id: String) {
        conversationDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversationById(id)
    }

    fun setAssistantActive(active: Boolean) = preferences.setAssistantActive(active)
    fun setWakeWordEnabled(enabled: Boolean) = preferences.setWakeWordEnabled(enabled)
    fun setSpeechTtsEnabled(enabled: Boolean) = preferences.setSpeechTtsEnabled(enabled)
    fun setPersonalityMode(mode: PersonalityMode) = preferences.setPersonalityMode(mode)
    fun saveApiConfig(config: ApiConfig) = preferences.saveApiConfig(config)
    fun removeApiKey() = preferences.removeApiKey()
}
