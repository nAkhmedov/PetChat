package uz.petchat.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import uz.petchat.domain.model.Chat
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.model.Message

interface ChatRepository {
    fun observeChats(): Flow<List<Chat>>

    fun observeChat(chatId: String): Flow<Chat?>

    suspend fun markAsRead(chatId: String)
}

interface MessageRepository {
    fun observeMessages(chatId: String): Flow<List<Message>>

    suspend fun loadOlder(chatId: String): Boolean

    suspend fun sendMessage(chatId: String, text: String)

    suspend fun retryMessage(localId: String)
}

interface ConnectionRepository {
    val status: StateFlow<ConnectionStatus>
}
