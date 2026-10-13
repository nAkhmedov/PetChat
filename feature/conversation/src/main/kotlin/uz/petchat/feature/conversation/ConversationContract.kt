package uz.petchat.feature.conversation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uz.petchat.core.ui.time.RelativeDay
import uz.petchat.domain.model.ConnectionStatus
import uz.petchat.domain.model.MessageStatus

@Immutable
data class ConversationState(
    val chatId: String,
    val header: ChatHeaderUi? = null,
    val items: ImmutableList<ConversationItem> = persistentListOf(),
    val isLoadingOlder: Boolean = false,
    val endReached: Boolean = false,
    val pageError: Boolean = false,
    val connection: ConnectionStatus = ConnectionStatus.CONNECTING,
) {
    val isInitialLoading: Boolean
        get() = items.isEmpty() && isLoadingOlder
}

@Immutable
data class ChatHeaderUi(
    val title: String,
    val initials: String,
    val avatarColorIndex: Int,
    val isGroup: Boolean,
    val isOnline: Boolean,
)

@Immutable
sealed interface ConversationItem {
    val key: String
    val contentType: Int
}

@Immutable
data class MessageUi(
    override val key: String,
    val text: String,
    val time: String,
    val isOutgoing: Boolean,
    val status: MessageStatus,
    val senderName: String?,
    val showTail: Boolean,
) : ConversationItem {
    override val contentType: Int
        get() = if (isOutgoing) CONTENT_TYPE_OUTGOING else CONTENT_TYPE_INCOMING
}

@Immutable
data class DateSeparatorUi(
    override val key: String,
    val label: String,
    val relativeDay: RelativeDay,
) : ConversationItem {
    override val contentType: Int
        get() = CONTENT_TYPE_DATE
}

const val CONTENT_TYPE_INCOMING = 0
const val CONTENT_TYPE_OUTGOING = 1
const val CONTENT_TYPE_DATE = 2

sealed interface ConversationAction {
    data object Init : ConversationAction
    data object LoadOlder : ConversationAction
    data class Send(val text: String) : ConversationAction
    data class Retry(val localId: String) : ConversationAction

    data class HeaderUpdated(val header: ChatHeaderUi) : ConversationAction
    data class MessagesUpdated(val items: ImmutableList<ConversationItem>) : ConversationAction {
        override fun toString() = "MessagesUpdated(count=${items.size})"
    }
    data class OlderLoaded(val endReached: Boolean) : ConversationAction
    data object OlderLoadFailed : ConversationAction
    data class ConnectionChanged(val status: ConnectionStatus) : ConversationAction
}

sealed interface ConversationEffect {
    data object ScrollToBottom : ConversationEffect
    data class ShowMessage(@StringRes val messageRes: Int) : ConversationEffect
}
