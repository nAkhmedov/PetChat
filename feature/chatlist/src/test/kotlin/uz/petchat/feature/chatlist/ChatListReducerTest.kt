package uz.petchat.feature.chatlist

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.petchat.domain.model.ConnectionStatus

class ChatListReducerTest {

    @Test
    fun `chats loaded replaces the list and marks the state as loaded`() {
        val chats = persistentListOf(chat("1"), chat("2"))

        val state = ChatListReducer.reduce(ChatListState(), ChatListAction.ChatsLoaded(chats))

        assertEquals(chats, state.chats)
        assertTrue(state.isLoaded)
    }

    @Test
    fun `connection change only touches the connection field`() {
        val initial = ChatListState(chats = persistentListOf(chat("1")), isLoaded = true)

        val state = ChatListReducer.reduce(initial, ChatListAction.ConnectionChanged(ConnectionStatus.CONNECTED))

        assertEquals(ConnectionStatus.CONNECTED, state.connection)
        assertSame(initial.chats, state.chats)
    }

    @Test
    fun `side-effect-only actions leave the state untouched`() {
        val initial = ChatListState()

        assertSame(initial, ChatListReducer.reduce(initial, ChatListAction.ChatClicked("1")))
        assertSame(initial, ChatListReducer.reduce(initial, ChatListAction.Init))
    }

    private fun chat(id: String) = ChatItemUi(
        id = id,
        title = "Chat $id",
        initials = "C",
        avatarColorIndex = 0,
        isOnline = false,
        isPinned = false,
        isMuted = false,
        time = "12:00",
        preview = "Hello",
        previewAuthor = null,
        isLastMessageOutgoing = false,
        lastMessageStatus = null,
        unreadBadge = null,
    )
}
