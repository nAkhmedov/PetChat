package uz.petchat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.petchat.domain.model.Chat
import uz.petchat.domain.model.Message
import uz.petchat.domain.model.MessageStatus
import uz.petchat.domain.repository.ChatRepository

class ObserveChatsUseCaseTest {

    @Test
    fun `pinned chats come first, then newest last message`() = runTest {
        val chats = listOf(
            chat(id = "old", lastMessageAt = 100),
            chat(id = "empty", lastMessageAt = null),
            chat(id = "pinned-old", lastMessageAt = 50, pinned = true),
            chat(id = "new", lastMessageAt = 300),
        )
        val useCase = ObserveChatsUseCase(FakeChatRepository(chats))

        val ids = useCase().first().map { it.id }

        assertEquals(listOf("pinned-old", "new", "old", "empty"), ids)
    }

    private fun chat(id: String, lastMessageAt: Long?, pinned: Boolean = false) = Chat(
        id = id,
        title = id,
        isGroup = false,
        isOnline = false,
        isPinned = pinned,
        isMuted = false,
        unreadCount = 0,
        lastMessage = lastMessageAt?.let {
            Message(
                localId = "m-$id",
                serverId = "m-$id",
                chatId = id,
                senderName = null,
                text = "hi",
                createdAt = it,
                isOutgoing = false,
                status = MessageStatus.READ,
            )
        },
    )

    private class FakeChatRepository(private val chats: List<Chat>) : ChatRepository {
        override fun observeChats(): Flow<List<Chat>> = flowOf(chats)
        override fun observeChat(chatId: String): Flow<Chat?> = flowOf(chats.find { it.id == chatId })
        override suspend fun markAsRead(chatId: String) = Unit
    }
}
