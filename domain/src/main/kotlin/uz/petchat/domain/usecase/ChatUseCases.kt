package uz.petchat.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import uz.petchat.domain.model.Chat
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.repository.ChatRepository
import uz.petchat.domain.repository.ConnectionRepository

class ObserveChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(): Flow<List<Chat>> =
        chatRepository.observeChats().map { chats -> chats.sortedWith(CHAT_ORDER) }

    private companion object {
        val CHAT_ORDER: Comparator<Chat> = compareByDescending<Chat> { it.isPinned }
            .thenByDescending { it.lastMessage?.createdAt ?: Long.MIN_VALUE }
    }
}

class ObserveChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(chatId: String): Flow<Chat?> = chatRepository.observeChat(chatId)
}

class MarkChatAsReadUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(chatId: String) = chatRepository.markAsRead(chatId)
}

class ObserveConnectionUseCase @Inject constructor(
    private val connectionRepository: ConnectionRepository,
) {
    operator fun invoke(): StateFlow<ConnectionStatus> = connectionRepository.status
}
