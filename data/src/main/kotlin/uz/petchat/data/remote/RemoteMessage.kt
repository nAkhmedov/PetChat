package uz.petchat.data.remote

import uz.petchat.domain.model.Message
import uz.petchat.domain.model.MessageStatus

internal data class RemoteMessage(
    val id: String,
    val chatId: String,
    val senderName: String?,
    val text: String,
    val sentAt: Long,
    val isMine: Boolean,
    val status: String,
)

internal fun RemoteMessage.toDomain(): Message = Message(
    localId = id,
    serverId = id,
    chatId = chatId,
    senderName = senderName,
    text = text,
    createdAt = sentAt,
    isOutgoing = isMine,
    status = status.toMessageStatus(),
)

internal fun String.toMessageStatus(): MessageStatus = when (lowercase()) {
    "read" -> MessageStatus.READ
    "delivered" -> MessageStatus.DELIVERED
    else -> MessageStatus.SENT
}
