package uz.petchat.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import uz.petchat.domain.model.Message
import uz.petchat.domain.repository.MessageRepository

class ObserveMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(chatId: String): Flow<List<Message>> = messageRepository.observeMessages(chatId)
}

class LoadOlderMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(chatId: String): Boolean = messageRepository.loadOlder(chatId)
}

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(chatId: String, rawText: String): SendMessageResult {
        val text = rawText.trim()
        return when {
            text.isEmpty() -> SendMessageResult.Empty
            text.length > MAX_LENGTH -> SendMessageResult.TooLong
            else -> {
                messageRepository.sendMessage(chatId, text)
                SendMessageResult.Queued
            }
        }
    }

    companion object {
        const val MAX_LENGTH = 4096
    }
}

sealed interface SendMessageResult {
    data object Queued : SendMessageResult
    data object Empty : SendMessageResult
    data object TooLong : SendMessageResult
}

class RetryMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(localId: String) = messageRepository.retryMessage(localId)
}
