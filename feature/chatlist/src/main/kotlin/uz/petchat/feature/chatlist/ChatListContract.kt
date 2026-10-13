package uz.petchat.feature.chatlist

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.model.MessageStatus

@Immutable
data class ChatListState(
    val chats: ImmutableList<ChatItemUi> = persistentListOf(),
    val isLoaded: Boolean = false,
    val connection: ConnectionStatus = ConnectionStatus.CONNECTING,
)

@Immutable
data class ChatItemUi(
    val id: String,
    val title: String,
    val initials: String,
    val avatarColorIndex: Int,
    val isOnline: Boolean,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val time: String,
    val preview: String,
    val previewAuthor: String?,
    val isLastMessageOutgoing: Boolean,
    val lastMessageStatus: MessageStatus?,
    val unreadBadge: String?,
)

sealed interface ChatListAction {
    data object Init : ChatListAction
    data class ChatClicked(val chatId: String) : ChatListAction

    data class ChatsLoaded(val chats: ImmutableList<ChatItemUi>) : ChatListAction {
        override fun toString() = "ChatsLoaded(count=${chats.size})"
    }
    data class ConnectionChanged(val status: ConnectionStatus) : ChatListAction
}

sealed interface ChatListEffect {
    data class OpenConversation(val chatId: String) : ChatListEffect
}
