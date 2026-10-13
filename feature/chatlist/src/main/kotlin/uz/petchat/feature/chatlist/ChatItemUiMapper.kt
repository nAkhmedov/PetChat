package uz.petchat.feature.chatlist

import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uz.petchat.core.ui.time.ChatTimeFormatter
import uz.petchat.core.ui.component.AVATAR_COLOR_COUNT
import uz.petchat.domain.model.Chat

class ChatItemUiMapper @Inject constructor(
    private val timeFormatter: ChatTimeFormatter,
) {
    fun map(chats: List<Chat>): ImmutableList<ChatItemUi> = chats.map { toUi(it) }.toImmutableList()

    private fun toUi(chat: Chat): ChatItemUi {
        val last = chat.lastMessage
        return ChatItemUi(
            id = chat.id,
            title = chat.title,
            initials = initials(chat.title),
            avatarColorIndex = chat.id.hashCode().mod(AVATAR_COLOR_COUNT),
            isOnline = chat.isOnline,
            isPinned = chat.isPinned,
            isMuted = chat.isMuted,
            time = last?.let { timeFormatter.chatListTimestamp(it.createdAt) }.orEmpty(),
            preview = last?.text?.take(PREVIEW_MAX_CHARS)?.replace('\n', ' ').orEmpty(),
            previewAuthor = last?.takeIf { chat.isGroup && !it.isOutgoing }?.senderName,
            isLastMessageOutgoing = last?.isOutgoing == true,
            lastMessageStatus = last?.takeIf { it.isOutgoing }?.status,
            unreadBadge = when {
                chat.unreadCount <= 0 -> null
                chat.unreadCount > 99 -> "99+"
                else -> chat.unreadCount.toString()
            },
        )
    }

    private fun initials(title: String): String =
        title.split(' ')
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

    private companion object {
        const val PREVIEW_MAX_CHARS = 120
    }
}
