package uz.petchat.feature.conversation.middleware

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import uz.petchat.core.mvi.Middleware
import uz.petchat.core.mvi.MiddlewareContext
import uz.petchat.core.mvi.Next
import uz.petchat.domain.usecase.MarkChatAsReadUseCase
import uz.petchat.domain.usecase.ObserveChatUseCase
import uz.petchat.domain.usecase.ObserveConnectionUseCase
import uz.petchat.domain.usecase.ObserveMessagesUseCase
import uz.petchat.feature.conversation.ConversationAction
import uz.petchat.feature.conversation.ConversationEffect
import uz.petchat.feature.conversation.ConversationItemMapper
import uz.petchat.feature.conversation.ConversationState

class ConversationObserverMiddleware @Inject constructor(
    private val observeChat: ObserveChatUseCase,
    private val observeMessages: ObserveMessagesUseCase,
    private val observeConnection: ObserveConnectionUseCase,
    private val markChatAsRead: MarkChatAsReadUseCase,
    private val mapper: ConversationItemMapper,
) : Middleware<ConversationState, ConversationAction, ConversationEffect> {

    override fun process(
        context: MiddlewareContext<ConversationState, ConversationAction, ConversationEffect>,
        action: ConversationAction,
        next: Next<ConversationAction>,
    ) {
        next(action)
        if (action != ConversationAction.Init) return

        val chatId = context.state.chatId

        context.launch(key = KEY_HEADER) {
            observeChat(chatId).filterNotNull().collect { chat ->
                context.dispatch(ConversationAction.HeaderUpdated(mapper.mapHeader(chat)))
                if (chat.unreadCount > 0) markChatAsRead(chatId)
            }
        }

        context.launch(key = KEY_MESSAGES) {
            val isGroup = observeChat(chatId).map { it?.isGroup == true }.distinctUntilChanged()
            combine(isGroup, observeMessages(chatId)) { group, messages -> mapper.map(messages, group) }
                .flowOn(Dispatchers.Default)
                .collect { items -> context.dispatch(ConversationAction.MessagesUpdated(items)) }
        }

        context.launch(key = KEY_CONNECTION) {
            observeConnection().collect { context.dispatch(ConversationAction.ConnectionChanged(it)) }
        }
    }

    private companion object {
        const val KEY_HEADER = "header"
        const val KEY_MESSAGES = "messages"
        const val KEY_CONNECTION = "connection"
    }
}
