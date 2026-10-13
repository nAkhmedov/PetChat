package uz.petchat.feature.conversation

import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uz.petchat.core.ui.time.ChatTimeFormatter
import uz.petchat.core.ui.component.AVATAR_COLOR_COUNT
import uz.petchat.domain.model.Chat
import uz.petchat.domain.model.Message

class ConversationItemMapper @Inject constructor(
    private val timeFormatter: ChatTimeFormatter,
) {

    fun map(messages: List<Message>, isGroup: Boolean): ImmutableList<ConversationItem> {
        val days = messages.map { timeFormatter.localDate(it.createdAt) }
        val items = ArrayList<ConversationItem>(messages.size + 8)

        messages.forEachIndexed { index, message ->
            val newer = messages.getOrNull(index - 1)
            val older = messages.getOrNull(index + 1)
            val day = days[index]
            val olderDay = days.getOrNull(index + 1)

            items += MessageUi(
                key = message.localId,
                text = message.text,
                time = timeFormatter.time(message.createdAt),
                isOutgoing = message.isOutgoing,
                status = message.status,
                senderName = message.senderName.takeIf {
                    isGroup && !message.isOutgoing &&
                        (older == null || !sameSender(older, message) || olderDay != day)
                },
                showTail = newer == null || !sameSender(newer, message) || days[index - 1] != day,
            )

            if (olderDay != day) {
                items += DateSeparatorUi(
                    key = "day_$day",
                    label = timeFormatter.dayHeader(day),
                    relativeDay = timeFormatter.relativeDay(day),
                )
            }
        }
        return items.toImmutableList()
    }

    fun mapHeader(chat: Chat): ChatHeaderUi = ChatHeaderUi(
        title = chat.title,
        initials = chat.title.split(' ').filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() },
        avatarColorIndex = chat.id.hashCode().mod(AVATAR_COLOR_COUNT),
        isGroup = chat.isGroup,
        isOnline = chat.isOnline,
    )

    private fun sameSender(a: Message, b: Message): Boolean =
        a.isOutgoing == b.isOutgoing && a.senderName == b.senderName
}
