package uz.petchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.petchat.domain.model.Message
import uz.petchat.domain.repository.MessageRepository

class SendMessageUseCaseTest {

    private val repository = RecordingMessageRepository()
    private val sendMessage = SendMessageUseCase(repository)

    @Test
    fun `text is trimmed before it is queued`() = runTest {
        val result = sendMessage("chat-1", "  hello  ")

        assertEquals(SendMessageResult.Queued, result)
        assertEquals(listOf("hello"), repository.sent)
    }

    @Test
    fun `blank text is rejected`() = runTest {
        val result = sendMessage("chat-1", "   \n ")

        assertEquals(SendMessageResult.Empty, result)
        assertTrue(repository.sent.isEmpty())
    }

    @Test
    fun `text over the limit is rejected`() = runTest {
        val result = sendMessage("chat-1", "a".repeat(SendMessageUseCase.MAX_LENGTH + 1))

        assertEquals(SendMessageResult.TooLong, result)
        assertTrue(repository.sent.isEmpty())
    }

    private class RecordingMessageRepository : MessageRepository {
        val sent = mutableListOf<String>()

        override fun observeMessages(chatId: String): Flow<List<Message>> = emptyFlow()
        override suspend fun loadOlder(chatId: String): Boolean = false
        override suspend fun sendMessage(chatId: String, text: String) {
            sent += text
        }
        override suspend fun retryMessage(localId: String) = Unit
    }
}
