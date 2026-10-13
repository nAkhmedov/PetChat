package uz.petchat.domain.model

data class Chat(
    val id: String,
    val title: String,
    val isGroup: Boolean,
    val isOnline: Boolean,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val unreadCount: Int,
    val lastMessage: Message?,
)

data class Message(
    val localId: String,
    val serverId: String?,
    val chatId: String,
    val senderName: String?,
    val text: String,
    val createdAt: Long,
    val isOutgoing: Boolean,
    val status: MessageStatus,
)

enum class MessageStatus { PENDING, SENT, DELIVERED, READ, FAILED }

enum class ConnectionStatus { WAITING_FOR_NETWORK, CONNECTING, CONNECTED }
